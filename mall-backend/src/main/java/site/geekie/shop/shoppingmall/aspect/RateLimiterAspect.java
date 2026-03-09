package site.geekie.shop.shoppingmall.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.geekie.shop.shoppingmall.annotation.RateLimiter;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.security.SecurityUser;

import java.util.Collections;

/**
 * 接口限流 AOP 切面
 * 使用 Lua 脚本将 INCR + EXPIRE 合并为原子操作，实现固定窗口计数器
 * Redis 不可用时降级放行
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 限流 Lua 脚本：原子性地 INCR 计数器并在首次请求时设置 EXPIRE
     * 返回当前计数值
     */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setScriptText(
                "local count = redis.call('incr', KEYS[1])\n" +
                "if count == 1 then\n" +
                "    redis.call('expire', KEYS[1], ARGV[1])\n" +
                "end\n" +
                "return count"
        );
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        try {
            String key = buildKey(joinPoint, rateLimiter);
            Long count = stringRedisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(rateLimiter.period())
            );
            if (count != null && count > rateLimiter.count()) {
                throw new BusinessException(ResultCode.RATE_LIMIT_EXCEEDED);
            }
        } catch (BusinessException e) {
            throw e; // 限流异常直接抛出
        } catch (Exception e) {
            log.warn("限流检查时 Redis 异常，降级放行", e);
        }
        return joinPoint.proceed();
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        String methodName = joinPoint.getSignature().getName();
        String keySuffix = rateLimiter.key().isEmpty() ? methodName : rateLimiter.key();

        // 优先使用已认证用户 ID，未认证时降级为客户端 IP
        String identity = getClientIp();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof SecurityUser securityUser) {
                identity = "user:" + securityUser.getUser().getId();
            } else {
                identity = "ip:" + identity;
            }
        } catch (Exception e) {
            identity = "ip:" + identity;
            log.debug("获取用户信息异常，使用客户端 IP 限流", e);
        }

        return "ratelimit:" + identity + ":" + keySuffix;
    }

    /**
     * 获取客户端真实 IP（支持反向代理）
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return "unknownIP";
            }
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多个 IP，取第一个（客户端真实 IP）
                return ip.split(",")[0].trim();
            }
            ip = request.getHeader("X-Real-IP");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknownIP";
        }
    }
}

package site.geekie.shop.shoppingmall.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.geekie.shop.shoppingmall.annotation.Audit;
import site.geekie.shop.shoppingmall.entity.AuditLogDO;
import site.geekie.shop.shoppingmall.mapper.AuditLogMapper;
import site.geekie.shop.shoppingmall.security.SecurityUser;

import java.lang.reflect.Method;

/**
 * 审计日志切面
 * 自动记录标记了 @Audit 注解的方法的执行日志
 *
 * @author backend-dev
 * @since 2026-02-06
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 环绕通知：拦截所有标记了 @Audit 注解的方法
     */
    @Around("@annotation(site.geekie.shop.shoppingmall.annotation.Audit)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audit audit = method.getAnnotation(Audit.class);

        // 获取当前用户信息
        Long userId = null;
        String username = "匿名用户";
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
                SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
                userId = securityUser.getUser().getId();
                username = securityUser.getUser().getUsername();
            }
        } catch (Exception e) {
            // 获取用户信息失败时使用默认值
        }

        // 获取请求信息
        String ip = null;
        String userAgent = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ip = getClientIp(request);
                userAgent = request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            // 非HTTP请求时跳过
        }

        // 构建审计日志对象
        AuditLogDO auditLog = new AuditLogDO();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setOperation(audit.value());
        auditLog.setType(audit.type().name());
        auditLog.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());
        auditLog.setIp(ip);
        auditLog.setUserAgent(userAgent);

        // 记录请求参数
        if (audit.logParams()) {
            try {
                String params = objectMapper.writeValueAsString(joinPoint.getArgs());
                // 限制参数长度，避免过大
                auditLog.setParams(params.length() > 2000 ? params.substring(0, 2000) + "..." : params);
            } catch (Exception e) {
                auditLog.setParams("参数序列化失败: " + e.getMessage());
            }
        }

        // 执行目标方法
        Object result = null;
        try {
            result = joinPoint.proceed();

            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;
            auditLog.setDuration((int) duration);
            auditLog.setResult("SUCCESS");

            // 记录日志
            log.info("【审计】用户: {}, 操作: {}, 类型: {}, 耗时: {}ms",
                     username, audit.value(), audit.type(), duration);

            return result;

        } catch (Exception e) {
            // 执行失败
            long duration = System.currentTimeMillis() - startTime;
            auditLog.setDuration((int) duration);
            auditLog.setResult("FAILURE");
            auditLog.setErrorMsg(e.getMessage());

            log.error("【审计失败】用户: {}, 操作: {}, 错误: {}",
                      username, audit.value(), e.getMessage());

            throw e;

        } finally {
            // 异步保存审计日志到数据库
            try {
                auditLogMapper.insert(auditLog);
            } catch (Exception e) {
                // 审计日志保存失败不应影响业务，仅记录错误
                log.error("保存审计日志失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

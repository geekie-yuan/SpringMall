package site.geekie.shop.shoppingmall.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.security.SecurityUser;
import site.geekie.shop.shoppingmall.util.IpUtils;
import site.geekie.shop.shoppingmall.util.SensitiveFieldSerializer;

@Aspect
@Component
@Order(1)
public class AccessLogAspect {

    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("ACCESS_LOG");

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) " +
              "&& !@annotation(site.geekie.shop.shoppingmall.annotation.IgnoreLog)")
    public void accessLogPointcut() {}

    @Around("accessLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!ACCESS_LOG.isInfoEnabled()) {
            return joinPoint.proceed();
        }

        long start = System.currentTimeMillis();
        String method = "";
        String uri = "";
        String ip = "unknownIP";
        String userId = "unknownID";

        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                method = request.getMethod();
                uri = request.getRequestURI();
                ip = IpUtils.getClientIp(request);
            }

            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof SecurityUser securityUser) {
                    userId = String.valueOf(securityUser.getUser().getId());
                }
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}

        String params = serializeArgs(joinPoint.getArgs());
        Object result;
        String status;
        try {
            result = joinPoint.proceed();
            if (result instanceof Result<?> r) {
                status = String.valueOf(r.getCode());
            } else {
                status = "200";
            }
        } catch (Throwable ex) {
            status = "ERROR";
            long elapsed = System.currentTimeMillis() - start;
            ACCESS_LOG.info("{} {} | ip={} | userId={} | params={} | status={} | {}ms",
                    method, uri, ip, userId, params, status, elapsed);
            throw ex;
        }

        long elapsed = System.currentTimeMillis() - start;
        ACCESS_LOG.info("{} {} | ip={} | userId={} | params={} | status={} | {}ms",
                method, uri, ip, userId, params, status, elapsed);
        return result;
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                continue;
            }
            if (!first) sb.append(", ");
            first = false;
            sb.append(SensitiveFieldSerializer.serialize(arg));
        }
        sb.append("}");
        return sb.toString();
    }
}

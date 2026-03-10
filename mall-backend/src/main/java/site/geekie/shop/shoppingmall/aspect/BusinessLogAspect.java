package site.geekie.shop.shoppingmall.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import site.geekie.shop.shoppingmall.annotation.LogOperation;
import site.geekie.shop.shoppingmall.util.SensitiveFieldSerializer;

@Aspect
@Component
@Order(2)
public class BusinessLogAspect {

    private static final Logger BUSINESS_LOG = LoggerFactory.getLogger("BUSINESS_LOG");

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        if (!BUSINESS_LOG.isInfoEnabled()) {
            return joinPoint.proceed();
        }

        long start = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String module = logOperation.module().isEmpty() ? "" : "[" + logOperation.module() + "] ";
        String description = logOperation.value();

        StringBuilder logMsg = new StringBuilder();
        logMsg.append(module).append(description)
              .append(" | ").append(className).append(".").append(methodName);

        if (logOperation.logParams()) {
            logMsg.append(" | params=").append(serializeArgs(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            if (logOperation.logResult()) {
                logMsg.append(" | result=").append(SensitiveFieldSerializer.serialize(result));
            }
            logMsg.append(" | ").append(elapsed).append("ms");
            BUSINESS_LOG.info(logMsg.toString());
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            logMsg.append(" | error=").append(ex.getClass().getSimpleName())
                  .append(": ").append(ex.getMessage())
                  .append(" | ").append(elapsed).append("ms");
            BUSINESS_LOG.error(logMsg.toString());
            throw ex;
        }
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Object arg : args) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(SensitiveFieldSerializer.serialize(arg));
        }
        sb.append("}");
        return sb.toString();
    }
}

package site.geekie.shop.shoppingmall.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 * 基于 Redis 固定窗口计数器实现，防止接口被频繁调用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    /**
     * 窗口内最大请求数
     */
    int count() default 5;

    /**
     * 窗口时间（秒）
     */
    int period() default 60;

    /**
     * 自定义 key 后缀（默认按 userId + 方法名）
     */
    String key() default "";
}

package site.geekie.shop.shoppingmall.annotation;

import java.lang.annotation.*;

/**
 * 日志记录注解，用于标注需要记录日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {
    String value() default "";
    String module() default "";
    boolean logParams() default true;
    boolean logResult() default false;
}

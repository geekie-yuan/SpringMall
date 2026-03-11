package site.geekie.shop.shoppingmall.annotation;

import java.lang.annotation.*;

/**
 * 忽略日志注解的注释
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreLog {
}

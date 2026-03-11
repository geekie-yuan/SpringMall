package site.geekie.shop.shoppingmall.annotation;

import java.lang.annotation.*;

/**
 * 敏感字段注解，用于标记需要脱敏处理的字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveField {
    SensitiveType value() default SensitiveType.DEFAULT;
}

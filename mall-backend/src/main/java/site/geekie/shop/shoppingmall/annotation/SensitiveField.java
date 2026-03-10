package site.geekie.shop.shoppingmall.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveField {
    SensitiveType value() default SensitiveType.DEFAULT;
}

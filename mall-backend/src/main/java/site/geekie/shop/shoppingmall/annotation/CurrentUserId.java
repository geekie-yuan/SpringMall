package site.geekie.shop.shoppingmall.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注此注解的 Controller 方法参数将自动注入当前登录用户 ID
 *
 * @author yuan
 * @since 1.0.0
 * @see site.geekie.shop.shoppingmall.config.CurrentUserIdResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}

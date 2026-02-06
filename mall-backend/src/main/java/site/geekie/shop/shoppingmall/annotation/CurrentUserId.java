package site.geekie.shop.shoppingmall.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当前用户 ID 注解
 * 用于自动注入当前登录用户的 ID 到方法参数中
 *
 * 使用示例：
 * <pre>
 * public OrderVO createOrder(@CurrentUserId Long userId, OrderRequest request) {
 *     // userId 会自动从 SecurityContext 中提取
 * }
 * </pre>
 *
 * @author backend-dev
 * @since 2026-02-06
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}

package site.geekie.shop.shoppingmall.annotation;

import site.geekie.shop.shoppingmall.enums.AuditType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 * 用于标记需要记录审计日志的方法
 *
 * 使用示例：
 * <pre>
 * {@code @Audit}(value = "创建订单", type = AuditType.CREATE)
 * public OrderVO createOrder(OrderRequest request) {
 *     // 方法执行前后会自动记录审计日志
 * }
 * </pre>
 *
 * @author backend-dev
 * @since 2026-02-06
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    /**
     * 操作描述（必填）
     * 例如："创建订单"、"修改商品价格"、"删除用户"
     */
    String value();

    /**
     * 操作类型（默认为 READ）
     */
    AuditType type() default AuditType.READ;

    /**
     * 是否记录请求参数（默认记录）
     */
    boolean logParams() default true;

    /**
     * 是否记录返回结果（默认不记录，避免敏感信息泄露）
     */
    boolean logResult() default false;
}

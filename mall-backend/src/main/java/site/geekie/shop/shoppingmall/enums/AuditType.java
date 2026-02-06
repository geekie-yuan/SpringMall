package site.geekie.shop.shoppingmall.enums;

import lombok.Getter;

/**
 * 审计操作类型枚举
 *
 * @author backend-dev
 * @since 2026-02-06
 */
@Getter
public enum AuditType {

    /**
     * 创建操作（新增数据）
     */
    CREATE("创建"),

    /**
     * 更新操作（修改数据）
     */
    UPDATE("更新"),

    /**
     * 删除操作（删除数据）
     */
    DELETE("删除"),

    /**
     * 查询操作（敏感数据查询）
     */
    READ("查询"),

    /**
     * 登录操作
     */
    LOGIN("登录"),

    /**
     * 支付操作
     */
    PAYMENT("支付");

    private final String description;

    AuditType(String description) {
        this.description = description;
    }
}

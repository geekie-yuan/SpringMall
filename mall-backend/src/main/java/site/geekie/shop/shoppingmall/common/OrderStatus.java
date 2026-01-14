package site.geekie.shop.shoppingmall.common;

import lombok.Getter;

/**
 * 订单状态枚举
 * 定义订单的所有可能状态
 */
@Getter
public enum OrderStatus {
    //待支付
    UNPAID("UNPAID", "待支付"),

    //已支付
    PAID("PAID", "已支付"),

    //已发货
    SHIPPED("SHIPPED", "已发货"),

    //已完成
    COMPLETED("COMPLETED", "已完成"),

    //已取消
    CANCELLED("CANCELLED", "已取消");

    // 状态码
    private final String code;

    // 状态描述
    private final String description;

    /**
     * 构造函数
     *
     * @param code 状态码
     * @param description 状态描述
     */
    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取订单状态枚举
     *
     * @param code 状态码
     * @return 订单状态枚举
     * @throws IllegalArgumentException 如果状态码不存在
     */
    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }
}

package site.geekie.shop.shoppingmall.common;

import lombok.Getter;
import site.geekie.shop.shoppingmall.exception.BusinessException;

import java.util.EnumSet;
import java.util.Set;

/**
 * 订单状态枚举
 * 定义订单的所有可能状态及合法的状态转换规则
 *
 * 状态转换规则：
 * UNPAID   -> PAID, CANCELLED
 * PAID     -> SHIPPED, CANCELLED
 * SHIPPED  -> COMPLETED
 * COMPLETED -> (终态)
 * CANCELLED -> (终态)
 */
@Getter
public enum OrderStatus {

    // 待支付
    UNPAID("UNPAID", "待支付") {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return EnumSet.of(PAID, CANCELLED);
        }
    },

    // 已支付
    PAID("PAID", "已支付") {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return EnumSet.of(SHIPPED, CANCELLED);
        }
    },

    // 已发货
    SHIPPED("SHIPPED", "已发货") {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return EnumSet.of(COMPLETED);
        }
    },

    // 已完成（终态）
    COMPLETED("COMPLETED", "已完成") {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return EnumSet.noneOf(OrderStatus.class);
        }
    },

    // 已取消（终态）
    CANCELLED("CANCELLED", "已取消") {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return EnumSet.noneOf(OrderStatus.class);
        }
    };

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
     * 返回当前状态允许转换到的目标状态集合
     */
    public abstract Set<OrderStatus> allowedTransitions();

    /**
     * 判断当前状态是否可以转换到目标状态
     *
     * @param target 目标状态
     * @return 是否允许转换
     */
    public boolean canTransitTo(OrderStatus target) {
        return allowedTransitions().contains(target);
    }

    /**
     * 执行状态转换，非法转换时抛出 BusinessException
     *
     * @param target 目标状态
     * @throws BusinessException 当转换不合法时
     */
    public void transitTo(OrderStatus target) {
        if (!canTransitTo(target)) {
            throw new BusinessException(ResultCode.INVALID_ORDER_STATUS);
        }
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

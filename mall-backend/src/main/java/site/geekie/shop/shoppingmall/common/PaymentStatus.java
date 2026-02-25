package site.geekie.shop.shoppingmall.common;

import site.geekie.shop.shoppingmall.exception.BusinessException;

import java.util.EnumSet;
import java.util.Set;

/**
 * 支付状态枚举
 * 定义支付的所有可能状态及合法的状态转换规则
 *
 * 状态转换规则：
 * PENDING  -> SUCCESS, FAILED, CLOSED
 * SUCCESS  -> REFUNDED
 * FAILED   -> (终态)
 * CLOSED   -> (终态)
 * REFUNDED -> (终态)
 */
public enum PaymentStatus {

    /**
     * 待支付
     */
    PENDING("待支付") {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return EnumSet.of(SUCCESS, FAILED, CLOSED);
        }
    },

    /**
     * 支付成功
     */
    SUCCESS("支付成功") {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return EnumSet.of(REFUNDED);
        }
    },

    /**
     * 支付失败（终态）
     */
    FAILED("支付失败") {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return EnumSet.noneOf(PaymentStatus.class);
        }
    },

    /**
     * 已关闭（终态）
     */
    CLOSED("已关闭") {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return EnumSet.noneOf(PaymentStatus.class);
        }
    },

    /**
     * 已退款（终态）
     */
    REFUNDED("已退款") {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return EnumSet.noneOf(PaymentStatus.class);
        }
    };

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 返回当前状态允许转换到的目标状态集合
     */
    public abstract Set<PaymentStatus> allowedTransitions();

    /**
     * 判断当前状态是否可以转换到目标状态
     *
     * @param target 目标状态
     * @return 是否允许转换
     */
    public boolean canTransitTo(PaymentStatus target) {
        return allowedTransitions().contains(target);
    }

    /**
     * 执行状态转换，非法转换时抛出 BusinessException
     *
     * @param target 目标状态
     * @throws BusinessException 当转换不合法时
     */
    public void transitTo(PaymentStatus target) {
        if (!canTransitTo(target)) {
            throw new BusinessException(ResultCode.INVALID_PAYMENT_STATUS);
        }
    }
}

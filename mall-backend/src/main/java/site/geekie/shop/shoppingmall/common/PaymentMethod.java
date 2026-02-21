package site.geekie.shop.shoppingmall.common;

/**
 * 支付方式枚举
 */
public enum PaymentMethod {

    /**
     * 支付宝
     */
    ALIPAY("支付宝"),

    /**
     * 微信支付
     */
    WECHAT("微信支付"),

    /**
     * Stripe支付
     */
    STRIPE("Stripe支付");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

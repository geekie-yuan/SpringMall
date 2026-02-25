package site.geekie.shop.shoppingmall.service;

/**
 * 支付服务接口
 * 提供支付的业务逻辑方法
 */
public interface PaymentService {

    /**
     * 处理订单退款
     *
     * @param orderNo 订单号
     * @param refundReason 退款原因
     */
    void refundOrder(String orderNo, String refundReason);

    /**
     * 对指定支付流水号发起退款（用于多重支付场景，精确退款指定支付记录）
     *
     * @param paymentNo 支付流水号
     * @param refundReason 退款原因
     */
    void refundByPaymentNo(String paymentNo, String refundReason);
}

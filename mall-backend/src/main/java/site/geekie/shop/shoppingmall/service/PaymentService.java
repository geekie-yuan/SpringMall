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
}

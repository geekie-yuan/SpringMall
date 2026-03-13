package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.vo.PaymentVO;

/**
 * 支付服务接口
 * 提供支付的业务逻辑方法
 */
public interface PaymentService {

    /**
     * 根据支付流水号查询支付状态（自动识别支付方式）
     *
     * @param paymentNo 支付流水号
     * @param userId 用户ID
     * @return 支付信息
     */
    PaymentVO getPaymentByNo(String paymentNo, Long userId);

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

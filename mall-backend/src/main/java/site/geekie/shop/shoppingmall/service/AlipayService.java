package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.vo.PaymentVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付服务接口
 */
public interface AlipayService {

    /**
     * 创建支付宝支付
     *
     * @param paymentNo 支付流水号
     * @param orderNo 订单号
     * @param subject 支付标题（商品名称等）
     * @param body 支付描述（商品明细等）
     * @param totalAmount 支付金额
     * @return 支付表单HTML
     */
    String createPayment(String paymentNo, String orderNo, String subject, String body, String totalAmount);

    /**
     * 处理支付宝异步通知
     *
     * @param params 通知参数
     * @return 处理结果（success/failure）
     */
    String handleNotify(Map<String, String> params);

    /**
     * 查询支付宝支付状态
     *
     * @param paymentNo 支付流水号
     * @return 支付状态
     */
    String queryPaymentStatus(String paymentNo);

    /**
     * 关闭支付宝支付
     *
     * @param paymentNo 支付流水号
     */
    void closePayment(String paymentNo);

    /**
     * 申请支付宝退款
     *
     * @param refundNo 退款流水号
     * @param tradeNo 支付宝交易号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款是否成功
     */
    boolean refund(String refundNo, String tradeNo, BigDecimal refundAmount, String refundReason);
}

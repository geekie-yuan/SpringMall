package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.vo.AlipayPaymentVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付服务接口
 */
public interface AlipayPaymentService {

    /**
     * 创建支付宝支付
     *
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 支付信息（包含支付表单HTML）
     */
    AlipayPaymentVO createPayment(String orderNo, Long userId);

    /**
     * 处理支付宝异步通知
     *
     * @param params 通知参数
     * @return 处理结果（success/failure）
     */
    String handleNotify(Map<String, String> params);

    /**
     * 查询支付状态
     *
     * @param paymentNo 支付流水号
     * @param userId 用户ID
     * @return 支付信息
     */
    AlipayPaymentVO queryPayment(String paymentNo, Long userId);

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

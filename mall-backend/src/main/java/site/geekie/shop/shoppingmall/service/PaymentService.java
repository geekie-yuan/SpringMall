package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.PaymentDTO;
import site.geekie.shop.shoppingmall.dto.PaymentNotifyDTO;
import site.geekie.shop.shoppingmall.vo.PaymentVO;

import java.util.Map;

/**
 * 支付服务接口
 * 提供支付的业务逻辑方法
 */
public interface PaymentService {

    /**
     * 发起支付（模拟）
     * 1. 验证订单存在且属于当前用户
     * 2. 验证订单状态为待支付
     * 3. 模拟支付处理（实际项目中会调用第三方支付接口）
     * 4. 返回支付结果
     *
     * @param request 支付请求
     * @param userId 当前登录用户ID
     * @return 支付响应
     */
    PaymentVO pay(PaymentDTO request, Long userId);

    /**
     * 处理支付回调（模拟）
     * 1. 验证回调参数
     * 2. 查询订单
     * 3. 更新订单状态为已支付
     * 4. 记录支付时间
     *
     * @param request 支付回调请求
     */
    void handlePaymentNotify(PaymentNotifyDTO request);

    /**
     * 创建支付宝支付
     *
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 支付信息（包含支付表单HTML）
     */
    PaymentVO createAlipayPayment(String orderNo, Long userId);

    /**
     * 处理支付宝异步通知
     *
     * @param params 通知参数
     * @return 处理结果（success/failure）
     */
    String handleAlipayNotify(Map<String, String> params);

    /**
     * 查询支付状态
     *
     * @param paymentNo 支付流水号
     * @return 支付信息
     */
    PaymentVO queryPayment(String paymentNo);

    /**
     * 处理订单退款
     *
     * @param orderNo 订单号
     * @param refundReason 退款原因
     */
    void refundOrder(String orderNo, String refundReason);
}

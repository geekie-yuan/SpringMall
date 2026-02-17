package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.request.CreateWxPaymentDTO;
import site.geekie.shop.shoppingmall.dto.request.WxRefundDTO;
import site.geekie.shop.shoppingmall.vo.WxPaymentVO;
import site.geekie.shop.shoppingmall.vo.WxRefundVO;

import java.util.Map;

/**
 * 微信支付服务接口
 * 提供微信支付相关的业务逻辑方法
 */
public interface WxPayService {

    /**
     * 创建微信Native支付订单
     * 调用微信支付API生成二维码链接
     *
     * @param request 创建支付请求
     * @param userId 当前登录用户ID
     * @return 支付记录（包含二维码链接）
     */
    WxPaymentVO createNativePayment(CreateWxPaymentDTO request, Long userId);

    /**
     * 查询支付状态
     * 从数据库查询支付记录
     *
     * @param paymentNo 支付流水号
     * @param userId 当前登录用户ID
     * @return 支付记录
     */
    WxPaymentVO queryPayment(String paymentNo, Long userId);

    /**
     * 处理微信支付回调通知
     * 验证签名、更新支付状态、更新订单状态
     *
     * @param requestBody 回调请求体（原始JSON字符串）
     * @param headers 回调请求头（用于验签）
     */
    void handlePaymentNotify(String requestBody, Map<String, String> headers);

    /**
     * 申请退款
     * 调用微信退款API
     *
     * @param request 退款请求
     * @param userId 当前登录用户ID（可能用于权限校验）
     * @return 退款记录
     */
    WxRefundVO createRefund(WxRefundDTO request, Long userId);

    /**
     * 处理微信退款回调通知
     * 验证签名、更新退款状态
     *
     * @param requestBody 回调请求体（原始JSON字符串）
     * @param headers 回调请求头（用于验签）
     */
    void handleRefundNotify(String requestBody, Map<String, String> headers);
}

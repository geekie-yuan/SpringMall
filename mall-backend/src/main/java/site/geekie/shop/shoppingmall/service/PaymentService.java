package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.request.PaymentNotifyRequest;
import site.geekie.shop.shoppingmall.dto.request.PaymentRequest;
import site.geekie.shop.shoppingmall.dto.response.PaymentResponse;

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
     * @return 支付响应
     */
    PaymentResponse pay(PaymentRequest request);

    /**
     * 处理支付回调（模拟）
     * 1. 验证回调参数
     * 2. 查询订单
     * 3. 更新订单状态为已支付
     * 4. 记录支付时间
     *
     * @param request 支付回调请求
     */
    void handlePaymentNotify(PaymentNotifyRequest request);
}

package site.geekie.shop.shoppingmall.service;

/**
 * 支付关闭服务接口
 * 用于在创建新支付前，关闭同一订单下所有 PENDING 状态的历史支付记录，
 * 实现支付方式互斥
 */
public interface PaymentCloseService {

    /**
     * 关闭指定订单的所有 PENDING 状态支付记录
     * 根据支付方式分别调用第三方关单 API 并更新本地状态为 CLOSED
     * 每个关闭操作独立容错，失败仅记录日志，不阻断新支付创建
     *
     * @param orderNo 订单号
     */
    void closeAllPendingPayments(String orderNo);
}

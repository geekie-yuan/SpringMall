package site.geekie.shop.shoppingmall.dto.request;

import lombok.Data;

/**
 * 支付宝异步通知参数DTO
 */
@Data
public class AlipayNotifyRequest {

    /**
     * 通知时间
     */
    private String notifyTime;

    /**
     * 通知类型
     */
    private String notifyType;

    /**
     * 通知校验ID
     */
    private String notifyId;

    /**
     * 支付宝交易号
     */
    private String tradeNo;

    /**
     * 商户订单号（支付流水号）
     */
    private String outTradeNo;

    /**
     * 买家支付宝账号
     */
    private String buyerLogonId;

    /**
     * 交易状态
     * WAIT_BUYER_PAY（交易创建）
     * TRADE_CLOSED（未付款交易超时关闭或支付完成后全额退款）
     * TRADE_SUCCESS（交易支付成功）
     * TRADE_FINISHED（交易结束，不可退款）
     */
    private String tradeStatus;

    /**
     * 订单金额
     */
    private String totalAmount;

    /**
     * 实收金额
     */
    private String receiptAmount;

    /**
     * 商品标题
     */
    private String subject;

    /**
     * 商品描述
     */
    private String body;

    /**
     * 支付时间
     */
    private String gmtPayment;
}

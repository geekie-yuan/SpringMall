package site.geekie.shop.shoppingmall.dto.request;

import lombok.Data;

/**
 * 支付回调请求DTO
 * 用于接收支付回调通知
 */
@Data
public class PaymentNotifyRequest {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付凭证号
     */
    private String transactionNo;

    /**
     * 支付状态
     * SUCCESS - 支付成功
     * FAILED - 支付失败
     */
    private String paymentStatus;

    /**
     * 支付时间戳
     */
    private Long timestamp;
}

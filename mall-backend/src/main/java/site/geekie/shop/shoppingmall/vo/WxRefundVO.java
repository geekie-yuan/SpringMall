package site.geekie.shop.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 微信退款记录响应VO
 */
@Data
public class WxRefundVO {

    /**
     * 退款ID
     */
    private Long id;

    /**
     * 退款流水号
     */
    private String refundNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款状态（PROCESSING/SUCCESS/FAILED）
     */
    private String refundStatus;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

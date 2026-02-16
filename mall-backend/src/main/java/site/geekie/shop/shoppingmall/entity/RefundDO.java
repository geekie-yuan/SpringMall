package site.geekie.shop.shoppingmall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 * 对应数据库表：mall_refund
 */
@Data
public class RefundDO {

    /**
     * 退款ID（主键）
     */
    private Long id;

    /**
     * 退款流水号（唯一）
     */
    private String refundNo;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 关联支付流水号
     */
    private String paymentNo;

    /**
     * 第三方交易号（支付宝交易号）
     */
    private String tradeNo;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款状态（PROCESSING-处理中/SUCCESS-成功/FAILED-失败）
     */
    private String refundStatus;

    /**
     * 退款成功时间
     */
    private LocalDateTime refundTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

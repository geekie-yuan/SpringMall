package site.geekie.shop.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 微信支付记录响应VO
 */
@Data
public class WxPaymentVO {

    /**
     * 支付ID
     */
    private Long id;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付状态（PENDING/SUCCESS/FAILED/CLOSED）
     */
    private String paymentStatus;

    /**
     * 二维码链接（Native支付）
     */
    private String codeUrl;

    /**
     * 第三方交易号
     */
    private String tradeNo;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 通知时间
     */
    private LocalDateTime notifyTime;
}

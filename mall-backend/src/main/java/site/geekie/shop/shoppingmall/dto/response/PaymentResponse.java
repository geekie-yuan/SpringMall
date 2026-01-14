package site.geekie.shop.shoppingmall.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付响应DTO
 * 返回给客户端的支付信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付状态
     * SUCCESS - 支付成功
     * FAILED - 支付失败
     * PENDING - 待支付
     */
    private String paymentStatus;

    /**
     * 支付消息
     */
    private String message;

    /**
     * 支付凭证号（模拟生成）
     */
    private String transactionNo;
}

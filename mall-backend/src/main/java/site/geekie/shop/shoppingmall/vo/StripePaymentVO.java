package site.geekie.shop.shoppingmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Stripe 支付响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stripe 支付响应")
public class StripePaymentVO {

    @Schema(description = "支付编号", example = "PAY20240101123456")
    private String paymentNo;

    @Schema(description = "Stripe Checkout Session URL", example = "https://checkout.stripe.com/c/pay/cs_xxx")
    private String sessionUrl;

    @Schema(description = "Stripe Checkout Session ID", example = "cs_xxx")
    private String sessionId;

    @Schema(description = "订单编号", example = "ORDER20240101123456")
    private String orderNo;

    @Schema(description = "支付金额（CNY）", example = "99.99")
    private BigDecimal amount;

    @Schema(description = "支付状态", example = "SUCCESS")
    private String status;
}

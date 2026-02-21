package site.geekie.shop.shoppingmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Stripe 退款响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stripe 退款响应")
public class StripeRefundVO {

    @Schema(description = "退款编号", example = "REFUND20240101123456")
    private String refundNo;

    @Schema(description = "Stripe Refund ID", example = "re_xxx")
    private String refundId;

    @Schema(description = "支付编号", example = "PAY20240101123456")
    private String paymentNo;

    @Schema(description = "退款金额（CNY）", example = "99.99")
    private BigDecimal refundAmount;

    @Schema(description = "退款状态", example = "PROCESSING")
    private String status;

    @Schema(description = "退款原因", example = "用户申请退款")
    private String reason;
}

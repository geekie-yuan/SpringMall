package site.geekie.shop.shoppingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Stripe 退款请求
 */
@Data
@Schema(description = "Stripe 退款 DTO")
public class StripeRefundDTO {

    @Schema(description = "支付编号", example = "PAY20240101123456")
    @NotBlank(message = "支付编号不能为空")
    private String paymentNo;

    @Schema(description = "退款金额（CNY）", example = "99.99")
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于 0")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因", example = "用户申请退款")
    private String reason;
}

package site.geekie.shop.shoppingmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付宝支付响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付宝支付响应")
public class AlipayPaymentVO {

    @Schema(description = "支付流水号", example = "PAY20240101123456")
    private String paymentNo;

    @Schema(description = "订单编号", example = "ORDER20240101123456")
    private String orderNo;

    @Schema(description = "支付金额", example = "99.99")
    private BigDecimal amount;

    @Schema(description = "支付状态", example = "PENDING")
    private String paymentStatus;

    @Schema(description = "支付表单 HTML")
    private String paymentUrl;

    @Schema(description = "支付宝交易号", example = "2024010122001234567890")
    private String tradeNo;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}

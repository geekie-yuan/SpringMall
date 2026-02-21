package site.geekie.shop.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 微信支付退款请求 DTO
 */
@Data
public class WxRefundDTO {

    /**
     * 支付流水号
     */
    @NotBlank(message = "支付流水号不能为空")
    private String paymentNo;

    /**
     * 退款金额（单位：元）
     */
    @NotNull(message = "退款金额不能为空")
    @Positive(message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    private String reason;
}

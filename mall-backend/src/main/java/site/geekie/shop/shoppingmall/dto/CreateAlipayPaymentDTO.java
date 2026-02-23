package site.geekie.shop.shoppingmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建支付宝支付请求 DTO
 */
@Data
@Schema(description = "创建支付宝支付 DTO")
public class CreateAlipayPaymentDTO {

    @Schema(description = "订单编号", example = "ORDER20240101123456")
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;
}

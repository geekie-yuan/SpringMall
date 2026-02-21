package site.geekie.shop.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建支付请求 DTO
 */
@Data
public class CreatePaymentDTO {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}

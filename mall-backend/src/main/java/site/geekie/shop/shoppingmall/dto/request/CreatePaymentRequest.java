package site.geekie.shop.shoppingmall.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建支付请求DTO
 */
@Data
public class CreatePaymentRequest {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}

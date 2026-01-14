package site.geekie.shop.shoppingmall.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付请求DTO
 * 用于发起支付
 */
@Data
public class PaymentRequest {

    /**
     * 订单号
     * 验证规则：不能为空
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 支付方式（可选，用于扩展）
     * 例如：ALIPAY、WECHAT、MOCK
     * 默认使用MOCK模拟支付
     */
    private String paymentMethod;
}

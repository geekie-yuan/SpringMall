package site.geekie.shop.shoppingmall.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单请求DTO
 * 用于从购物车创建订单
 */
@Data
public class OrderRequest {

    /**
     * 收货地址ID
     * 验证规则：不能为null
     */
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    /**
     * 订单备注（可选）
     */
    private String remark;
}

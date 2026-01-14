package site.geekie.shop.shoppingmall.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车响应DTO
 * 返回给客户端的购物车项信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    /**
     * 购物车项ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称（非数据库字段，业务层填充）
     */
    private String productName;

    /**
     * 商品副标题（非数据库字段，业务层填充）
     */
    private String productSubtitle;

    /**
     * 商品主图（非数据库字段，业务层填充）
     */
    private String productImage;

    /**
     * 商品单价（非数据库字段，业务层填充）
     */
    private BigDecimal productPrice;

    /**
     * 商品库存（非数据库字段，业务层填充）
     */
    private Integer productStock;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 是否选中（0-未选中，1-已选中）
     */
    private Integer checked;

    /**
     * 小计（单价 × 数量）
     * 非数据库字段，业务层计算
     */
    private BigDecimal subtotal;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 构造方法：从实体创建响应对象（不包含商品信息）
     */
    public CartItemResponse(Long id, Long userId, Long productId, Integer quantity,
                            Integer checked, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.checked = checked;
        this.createdAt = createdAt;
    }
}

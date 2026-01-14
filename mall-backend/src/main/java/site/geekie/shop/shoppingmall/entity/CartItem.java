package site.geekie.shop.shoppingmall.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车项实体类
 * 对应数据库表：cart_item
 */
@Data
public class CartItem {

    /**
     * 购物车项ID（主键）
     */
    private Long id;

    /**
     * 用户ID（外键）
     */
    private Long userId;

    /**
     * 商品ID（外键）
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 是否选中
     * 0-未选中，1-已选中
     */
    private Integer checked;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

package site.geekie.shop.shoppingmall.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品响应DTO
 * 返回给客户端的商品信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称（非数据库字段，业务层填充）
     */
    private String categoryName;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 副标题/卖点
     */
    private String subtitle;

    /**
     * 主图URL
     */
    private String mainImage;

    /**
     * 图片列表（JSON数组字符串）
     */
    private String images;

    /**
     * 商品详情（HTML）
     */
    private String detail;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 状态（0-下架，1-上架）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 构造方法：从实体创建响应对象（不包含categoryName）
     */
    public ProductResponse(Long id, Long categoryId, String name, String subtitle,
                           String mainImage, String images, String detail,
                           BigDecimal price, Integer stock, Integer status,
                           LocalDateTime createdAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.subtitle = subtitle;
        this.mainImage = mainImage;
        this.images = images;
        this.detail = detail;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.createdAt = createdAt;
    }
}

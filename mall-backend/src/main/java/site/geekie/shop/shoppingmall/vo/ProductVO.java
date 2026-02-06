package site.geekie.shop.shoppingmall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String mainImage;
    private String images;
    private String detail;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private LocalDateTime createdAt;

    public ProductVO(Long id, Long categoryId, String name, String subtitle,
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

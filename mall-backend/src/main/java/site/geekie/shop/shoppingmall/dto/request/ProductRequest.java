package site.geekie.shop.shoppingmall.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品请求DTO
 * 用于新增和修改商品
 */
@Data
public class ProductRequest {

    /**
     * 分类ID
     * 验证规则：不能为null，必须>0
     */
    @NotNull(message = "分类ID不能为空")
    @Min(value = 1, message = "分类ID必须大于0")
    private Long categoryId;

    /**
     * 商品名称
     * 验证规则：不能为空，长度2-200个字符
     */
    @NotBlank(message = "商品名称不能为空")
    @Size(min = 2, max = 200, message = "商品名称长度必须在2-200个字符之间")
    private String name;

    /**
     * 副标题/卖点
     * 验证规则：可选，最长500字符
     */
    @Size(max = 500, message = "副标题长度不能超过500个字符")
    private String subtitle;

    /**
     * 主图URL
     * 验证规则：可选，最长500字符
     */
    @Size(max = 500, message = "主图URL长度不能超过500个字符")
    private String mainImage;

    /**
     * 图片列表（JSON数组字符串）
     * 验证规则：可选
     */
    private String images;

    /**
     * 商品详情（HTML）
     * 验证规则：可选
     */
    private String detail;

    /**
     * 商品价格
     * 验证规则：不能为null，必须>=0.01
     */
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于等于0.01")
    private BigDecimal price;

    /**
     * 库存数量
     * 验证规则：不能为null，必须>=0
     */
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量必须大于等于0")
    private Integer stock;

    /**
     * 状态
     * 0-下架，1-上架
     * 可选，默认为1
     */
    private Integer status = 1;
}

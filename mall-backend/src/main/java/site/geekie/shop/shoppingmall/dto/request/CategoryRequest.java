package site.geekie.shop.shoppingmall.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类请求DTO
 * 用于新增和修改商品分类
 */
@Data
public class CategoryRequest {

    /**
     * 分类名称
     * 验证规则：不能为空，长度2-100个字符
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 100, message = "分类名称长度必须在2-100个字符之间")
    private String name;

    /**
     * 父分类ID
     * 0表示顶级分类
     * 验证规则：不能为null，必须>=0
     */
    @NotNull(message = "父分类ID不能为空")
    @Min(value = 0, message = "父分类ID必须大于等于0")
    private Long parentId;

    /**
     * 分类层级
     * 1-一级分类，2-二级分类，3-三级分类
     * 验证规则：不能为null
     */
    @NotNull(message = "分类层级不能为空")
    private Integer level;

    /**
     * 排序值
     * 数值越小越靠前
     * 可选，默认为0
     */
    private Integer sortOrder = 0;

    /**
     * 分类图标URL
     * 可选
     */
    @Size(max = 500, message = "图标URL长度不能超过500个字符")
    private String icon;

    /**
     * 状态
     * 0-禁用，1-正常
     * 可选，默认为1
     */
    private Integer status = 1;
}

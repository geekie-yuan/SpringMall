package site.geekie.shop.shoppingmall.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类响应DTO
 * 返回给客户端的分类信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类层级
     */
    private Integer level;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 分类图标URL
     */
    private String icon;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 子分类列表（用于树形结构）
     * 非数据库字段，业务层填充
     */
    private List<CategoryResponse> children;

    /**
     * 构造方法：从实体创建响应对象（不包含children）
     */
    public CategoryResponse(Long id, String name, Long parentId, Integer level,
                            Integer sortOrder, String icon, Integer status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.level = level;
        this.sortOrder = sortOrder;
        this.icon = icon;
        this.status = status;
        this.createdAt = createdAt;
    }
}

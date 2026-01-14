package site.geekie.shop.shoppingmall.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体类
 * 对应数据库表：category
 * 支持多级分类结构（最多3级）
 */
@Data
public class Category {

    /**
     * 分类ID（主键）
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     * 0表示顶级分类
     */
    private Long parentId;

    /**
     * 分类层级
     * 1-一级分类，2-二级分类，3-三级分类
     */
    private Integer level;

    /**
     * 排序值
     * 数值越小越靠前
     */
    private Integer sortOrder;

    /**
     * 分类图标URL
     */
    private String icon;

    /**
     * 状态
     * 0-禁用，1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

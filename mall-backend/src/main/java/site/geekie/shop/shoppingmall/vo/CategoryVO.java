package site.geekie.shop.shoppingmall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private String icon;
    private Integer status;
    private LocalDateTime createdAt;
    private List<CategoryVO> children;

    public CategoryVO(Long id, String name, Long parentId, Integer level,
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

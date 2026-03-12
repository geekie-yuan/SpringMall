package site.geekie.shop.shoppingmall.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import site.geekie.shop.shoppingmall.dto.CategoryDTO;
import site.geekie.shop.shoppingmall.entity.CategoryDO;
import site.geekie.shop.shoppingmall.vo.CategoryVO;

import java.util.List;

/**
 * 分类转换器
 * 负责 CategoryDO、CategoryDTO 与 CategoryVO 之间的转换
 */
@Mapper(componentModel = "spring")
public interface CategoryConverter {

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "productCount", ignore = true)
    CategoryVO toVO(CategoryDO category);

    List<CategoryVO> toVOList(List<CategoryDO> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CategoryDO toDO(CategoryDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateDOFromDTO(CategoryDTO dto, @MappingTarget CategoryDO target);
}

package site.geekie.shop.shoppingmall.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import site.geekie.shop.shoppingmall.entity.CategoryDO;
import site.geekie.shop.shoppingmall.vo.CategoryVO;

import java.util.List;

/**
 * 分类转换器
 * 负责 CategoryDO 与 CategoryVO 之间的转换
 */
@Mapper(componentModel = "spring")
public interface CategoryConverter {

    /**
     * 将 CategoryDO 转换为 CategoryVO
     *
     * @param category 分类实体
     * @return 分类VO
     */
    @Mapping(target = "children", ignore = true)
    CategoryVO toVO(CategoryDO category);

    /**
     * 批量将 CategoryDO 转换为 CategoryVO
     *
     * @param categories 分类实体列表
     * @return 分类VO列表
     */
    List<CategoryVO> toVOList(List<CategoryDO> categories);
}

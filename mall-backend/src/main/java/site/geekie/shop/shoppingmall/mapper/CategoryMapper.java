package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.CategoryDO;

import java.util.List;

/**
 * 商品分类Mapper接口
 * 提供商品分类的数据访问方法
 */
@Mapper
public interface CategoryMapper {

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类信息，不存在返回null
     */
    CategoryDO findById(@Param("id") Long id);

    /**
     * 查询所有分类
     * 按排序值升序、创建时间升序排列
     *
     * @return 所有分类列表
     */
    List<CategoryDO> findAll();

    /**
     * 根据父分类ID查询子分类列表
     * 按排序值升序排列
     *
     * @param parentId 父分类ID，0表示查询顶级分类
     * @return 子分类列表
     */
    List<CategoryDO> findByParentId(@Param("parentId") Long parentId);

    /**
     * 根据层级查询分类列表
     * 按排序值升序排列
     *
     * @param level 分类层级（1-一级，2-二级，3-三级）
     * @return 指定层级的分类列表
     */
    List<CategoryDO> findByLevel(@Param("level") Integer level);

    /**
     * 根据状态查询分类列表
     * 按排序值升序排列
     *
     * @param status 状态（0-禁用，1-正常）
     * @return 指定状态的分类列表
     */
    List<CategoryDO> findByStatus(@Param("status") Integer status);

    /**
     * 插入新分类
     *
     * @param category 分类信息
     * @return 影响行数
     */
    int insert(CategoryDO category);

    /**
     * 根据ID更新分类信息
     * 使用动态SQL，只更新非null字段
     *
     * @param category 分类信息
     * @return 影响行数
     */
    int updateById(CategoryDO category);

    /**
     * 根据ID删除分类
     *
     * @param id 分类ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID列表批量查询分类
     *
     * @param ids 分类ID列表
     * @return 分类列表
     */
    List<CategoryDO> findByIds(@Param("ids") List<Long> ids);

    /**
     * 统计指定父分类下的子分类数量
     *
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    int countByParentId(@Param("parentId") Long id);

    /**
     * 根据分类名称查询分类
     * 用于检查分类名称是否重复
     *
     * @param name 分类名称
     * @param parentId 父分类ID
     * @return 分类信息，不存在返回null
     */
    CategoryDO findByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);
}

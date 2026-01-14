package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.Product;

import java.util.List;

/**
 * 商品Mapper接口
 * 提供商品的数据访问方法
 */
@Mapper
public interface ProductMapper {

    /**
     * 根据ID查询商品
     *
     * @param id 商品ID
     * @return 商品信息，不存在返回null
     */
    Product findById(@Param("id") Long id);

    /**
     * 查询所有商品
     * 按创建时间倒序排列
     *
     * @return 所有商品列表
     */
    List<Product> findAll();

    /**
     * 根据分类ID查询商品列表
     * 按创建时间倒序排列
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据状态查询商品列表
     * 按创建时间倒序排列
     *
     * @param status 状态（0-下架，1-上架）
     * @return 商品列表
     */
    List<Product> findByStatus(@Param("status") Integer status);

    /**
     * 根据关键词搜索商品
     * 在商品名称和副标题中模糊查询
     * 按创建时间倒序排列
     *
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 插入新商品
     *
     * @param product 商品信息
     * @return 影响行数
     */
    int insert(Product product);

    /**
     * 根据ID更新商品信息
     * 使用动态SQL，只更新非null字段
     *
     * @param product 商品信息
     * @return 影响行数
     */
    int updateById(Product product);

    /**
     * 根据ID删除商品
     *
     * @param id 商品ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计指定分类下的商品数量
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 扣减库存
     * 使用乐观锁，确保库存不为负
     *
     * @param id 商品ID
     * @param quantity 扣减数量
     * @return 影响行数，0表示库存不足
     */
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 增加库存
     *
     * @param id 商品ID
     * @param quantity 增加数量
     * @return 影响行数
     */
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}

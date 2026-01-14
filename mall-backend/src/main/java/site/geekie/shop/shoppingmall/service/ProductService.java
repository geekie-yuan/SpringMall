package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.request.ProductRequest;
import site.geekie.shop.shoppingmall.dto.response.ProductResponse;

import java.util.List;

/**
 * 商品服务接口
 * 提供商品的CRUD功能和库存管理
 */
public interface ProductService {

    /**
     * 获取所有商品列表
     * 按创建时间倒序排列
     *
     * @return 所有商品列表
     */
    List<ProductResponse> getAllProducts();

    /**
     * 根据分类ID获取商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<ProductResponse> getProductsByCategoryId(Long categoryId);

    /**
     * 根据状态获取商品列表
     *
     * @param status 状态（0-下架，1-上架）
     * @return 商品列表
     */
    List<ProductResponse> getProductsByStatus(Integer status);

    /**
     * 搜索商品
     * 根据关键词在商品名称和副标题中模糊查询
     *
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    List<ProductResponse> searchProducts(String keyword);

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当商品不存在时抛出
     */
    ProductResponse getProductById(Long id);

    /**
     * 新增商品
     * 验证分类存在性
     *
     * @param request 商品请求
     * @return 新增的商品信息
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当分类不存在时抛出
     */
    ProductResponse addProduct(ProductRequest request);

    /**
     * 修改商品信息
     *
     * @param id 商品ID
     * @param request 商品请求
     * @return 修改后的商品信息
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当商品不存在或分类不存在时抛出
     */
    ProductResponse updateProduct(Long id, ProductRequest request);

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当商品不存在时抛出
     */
    void deleteProduct(Long id);

    /**
     * 扣减库存
     * 用于下单时减库存
     *
     * @param id 商品ID
     * @param quantity 扣减数量
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当商品不存在或库存不足时抛出
     */
    void decreaseStock(Long id, Integer quantity);

    /**
     * 增加库存
     * 用于取消订单时恢复库存
     *
     * @param id 商品ID
     * @param quantity 增加数量
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当商品不存在时抛出
     */
    void increaseStock(Long id, Integer quantity);

    // ===== 管理员方法 =====

    /**
     * 新增商品（管理员）
     * 与addProduct相同，为AdminController提供一致的命名
     *
     * @param request 商品请求
     * @return 新增的商品信息
     */
    ProductResponse createProduct(ProductRequest request);

    /**
     * 更新商品状态（管理员）
     *
     * @param id 商品ID
     * @param status 状态（0-下架，1-上架）
     */
    void updateProductStatus(Long id, Integer status);

    /**
     * 更新商品库存（管理员）
     *
     * @param id 商品ID
     * @param stock 新库存数量
     */
    void updateProductStock(Long id, Integer stock);
}

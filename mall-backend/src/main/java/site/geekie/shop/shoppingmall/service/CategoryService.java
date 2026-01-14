package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.request.CategoryRequest;
import site.geekie.shop.shoppingmall.dto.response.CategoryResponse;

import java.util.List;

/**
 * 分类服务接口
 * 提供商品分类的CRUD功能
 */
public interface CategoryService {

    /**
     * 获取所有分类列表
     * 按排序值和创建时间排序
     *
     * @return 所有分类列表
     */
    List<CategoryResponse> getAllCategories();

    /**
     * 获取分类树形结构
     * 返回所有分类的树形层级结构
     *
     * @return 分类树列表（仅包含一级分类，children包含子分类）
     */
    List<CategoryResponse> getCategoryTree();

    /**
     * 根据父分类ID获取子分类列表
     *
     * @param parentId 父分类ID，0表示获取一级分类
     * @return 子分类列表
     */
    List<CategoryResponse> getCategoriesByParentId(Long parentId);

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当分类不存在时抛出
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * 新增分类
     * 验证父分类存在性和层级合法性
     *
     * @param request 分类请求
     * @return 新增的分类信息
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当父分类不存在或层级不合法时抛出
     */
    CategoryResponse addCategory(CategoryRequest request);

    /**
     * 修改分类信息
     * 不允许修改parentId和level，防止破坏层级结构
     *
     * @param id 分类ID
     * @param request 分类请求
     * @return 修改后的分类信息
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当分类不存在时抛出
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    /**
     * 删除分类
     * 仅允许删除没有子分类且没有商品的分类
     *
     * @param id 分类ID
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当分类不存在、有子分类或有商品时抛出
     */
    void deleteCategory(Long id);
}

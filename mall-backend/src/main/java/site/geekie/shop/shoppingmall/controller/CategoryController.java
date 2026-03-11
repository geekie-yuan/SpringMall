package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.annotation.RateLimiter;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.vo.CategoryVO;
import site.geekie.shop.shoppingmall.service.CategoryService;

import java.util.List;

/**
 * 商品分类控制器
 * 处理商品分类管理相关接口
 *
 * 接口路径前缀：/api/v1/categories
 * 认证要求：管理接口需要ADMIN角色，查询接口公开
 */
@Tag(name = "Category", description = "商品分类接口")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类列表
     * 公开接口，无需认证
     *
     * @return 所有分类列表
     */
    @Operation(summary = "获取所有分类")
    @GetMapping
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 获取分类树形结构
     * 公开接口，无需认证
     * 返回树形层级结构，便于前端展示多级分类
     *
     * @return 分类树列表
     */
    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    @RateLimiter(count = 60, period = 60)
    public Result<List<CategoryVO>> getCategoryTree() {
        List<CategoryVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    /**
     * 根据父分类ID获取子分类列表
     * 公开接口，无需认证
     *
     * @param parentId 父分类ID，0表示获取一级分类
     * @return 子分类列表
     */
    @Operation(summary = "获取子分类列表")
    @GetMapping("/parent/{parentId}")
    @RateLimiter(count = 60, period = 60)
    public Result<List<CategoryVO>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<CategoryVO> categories = categoryService.getCategoriesByParentId(parentId);
        return Result.success(categories);
    }

    /**
     * 获取分类详情
     * 公开接口，无需认证
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    @RateLimiter(count = 60, period = 60)
    public Result<CategoryVO> getCategoryById(@PathVariable Long id) {
        CategoryVO category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

}

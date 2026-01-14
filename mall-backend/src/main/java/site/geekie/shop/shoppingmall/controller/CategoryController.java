package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.CategoryRequest;
import site.geekie.shop.shoppingmall.dto.response.CategoryResponse;
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
    public Result<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
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
    public Result<List<CategoryResponse>> getCategoryTree() {
        List<CategoryResponse> tree = categoryService.getCategoryTree();
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
    public Result<List<CategoryResponse>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<CategoryResponse> categories = categoryService.getCategoriesByParentId(parentId);
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
    public Result<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    /**
     * 新增分类
     * 需要ADMIN角色
     *
     * @param request 分类请求
     * @return 新增的分类信息
     */
    @Operation(summary = "新增分类")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public Result<CategoryResponse> addCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.addCategory(request);
        return Result.success("分类添加成功", category);
    }

    /**
     * 修改分类
     * 需要ADMIN角色
     *
     * @param id 分类ID
     * @param request 分类请求
     * @return 修改后的分类信息
     */
    @Operation(summary = "修改分类")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public Result<CategoryResponse> updateCategory(@PathVariable Long id,
                                                    @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return Result.success("分类修改成功", category);
    }

    /**
     * 删除分类
     * 需要ADMIN角色
     * 仅允许删除没有子分类且没有商品的分类
     *
     * @param id 分类ID
     * @return 统一响应对象
     */
    @Operation(summary = "删除分类")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功", null);
    }
}

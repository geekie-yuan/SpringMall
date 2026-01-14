package site.geekie.shop.shoppingmall.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.CategoryRequest;
import site.geekie.shop.shoppingmall.dto.response.CategoryResponse;
import site.geekie.shop.shoppingmall.service.CategoryService;

import java.util.List;

/**
 * 管理员-分类管理控制器
 * 提供分类管理的REST API（仅管理员可访问）
 *
 * 基础路径：/api/v1/admin/categories
 * 所有接口都需要ADMIN角色权限
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类（管理员）
     * GET /api/v1/admin/categories
     *
     * @return 分类列表
     */
    @GetMapping
    public Result<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 获取分类详情（管理员）
     * GET /api/v1/admin/categories/{id}
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    public Result<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    /**
     * 新增分类
     * POST /api/v1/admin/categories
     *
     * @param request 分类请求
     * @return 新增的分类信息
     */
    @PostMapping
    public Result<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.addCategory(request);
        return Result.success(category);
    }

    /**
     * 修改分类
     * PUT /api/v1/admin/categories/{id}
     *
     * @param id 分类ID
     * @param request 分类请求
     * @return 修改后的分类信息
     */
    @PutMapping("/{id}")
    public Result<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return Result.success(category);
    }

    /**
     * 删除分类
     * DELETE /api/v1/admin/categories/{id}
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}

package site.geekie.shop.shoppingmall.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.CategoryDTO;
import site.geekie.shop.shoppingmall.vo.CategoryVO;
import site.geekie.shop.shoppingmall.service.CategoryService;

import java.util.List;

/**
 * 管理员-分类管理控制器
 * 提供分类管理的REST API（仅管理员可访问）
 *
 * 基础路径：/api/v1/admin/categories
 * 所有接口都需要ADMIN角色权限
 */
@Tag(name = "AdminCategory", description = "管理员分类管理接口")
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
    @Operation(summary = "获取所有分类（管理员）")
    @GetMapping
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 获取分类详情（管理员）
     * GET /api/v1/admin/categories/{id}
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @Operation(summary = "获取分类详情（管理员）")
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategoryById(@PathVariable Long id) {
        CategoryVO category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    /**
     * 新增分类
     * POST /api/v1/admin/categories
     *
     * @param request 分类请求
     * @return 新增的分类信息
     */
    @Operation(summary = "新增分类")
    @PostMapping
    public Result<CategoryVO> createCategory(@Valid @RequestBody CategoryDTO request) {
        CategoryVO category = categoryService.addCategory(request);
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
    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<CategoryVO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO request) {
        CategoryVO category = categoryService.updateCategory(id, request);
        return Result.success(category);
    }

    /**
     * 删除分类
     * DELETE /api/v1/admin/categories/{id}
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}

package site.geekie.shop.shoppingmall.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.PageResult;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.ProductRequest;
import site.geekie.shop.shoppingmall.dto.response.ProductResponse;
import site.geekie.shop.shoppingmall.service.ProductService;

/**
 * 管理员-商品管理控制器
 * 提供商品管理的REST API（仅管理员可访问）
 *
 * 基础路径：/api/v1/admin/products
 * 所有接口都需要ADMIN角色权限
 */

@Tag(name = "AdminProduct", description = "管理员商品管理接口")
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminProductController {

    private final ProductService productService;

    /**
     * 获取所有商品（管理员）
     * GET /api/v1/admin/products
     *
     * @return 商品列表
     */
    @Operation(summary = "获取所有商品（管理员）")
    @GetMapping
    public Result<PageResult<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") @Max(100) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {
        Integer statusInt = null;
        if ("ON_SALE".equals(status)) statusInt = 1;
        else if ("OFF_SALE".equals(status)) statusInt = 0;
        return Result.success(productService.getAllProducts(page, size, keyword, categoryId, statusInt));
    }

    /**
     * 获取商品详情（管理员）
     * GET /api/v1/admin/products/{id}
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @Operation(summary = "获取商品详情（管理员）")
    @GetMapping("/{id}")
    public Result<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return Result.success(product);
    }

    /**
     * 新增商品
     * POST /api/v1/admin/products
     *
     * @param request 商品请求
     * @return 新增的商品信息
     */
    @Operation(summary = "新增商品")
    @PostMapping
    public Result<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return Result.success(product);
    }

    /**
     * 修改商品
     * PUT /api/v1/admin/products/{id}
     *
     * @param id 商品ID
     * @param request 商品请求
     * @return 修改后的商品信息
     */
    @Operation(summary = "修改商品")
    @PutMapping("/{id}")
    public Result<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return Result.success(product);
    }

    /**
     * 删除商品
     * DELETE /api/v1/admin/products/{id}
     *
     * @param id 商品ID
     * @return 操作结果
     */
    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    /**
     * 修改商品状态（上架/下架）
     * PUT /api/v1/admin/products/{id}/status
     *
     * @param id 商品ID
     * @param status 状态（0-下架，1-上架）
     * @return 操作结果
     */
    @Operation
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        productService.updateProductStatus(id, status);
        return Result.success();
    }

    /**
     * 修改商品库存
     * PUT /api/v1/admin/products/{id}/stock
     *
     * @param id 商品ID
     * @param stock 新库存
     * @return 操作结果
     */
    @Operation
    @PutMapping("/{id}/stock")
    public Result<Void> updateProductStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        productService.updateProductStock(id, stock);
        return Result.success();
    }
}

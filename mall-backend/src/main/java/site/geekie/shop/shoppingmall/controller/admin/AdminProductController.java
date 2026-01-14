package site.geekie.shop.shoppingmall.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.ProductRequest;
import site.geekie.shop.shoppingmall.dto.response.ProductResponse;
import site.geekie.shop.shoppingmall.service.ProductService;

import java.util.List;

/**
 * 管理员-商品管理控制器
 * 提供商品管理的REST API（仅管理员可访问）
 *
 * 基础路径：/api/v1/admin/products
 * 所有接口都需要ADMIN角色权限
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    /**
     * 获取所有商品（管理员）
     * GET /api/v1/admin/products
     *
     * @return 商品列表
     */
    @GetMapping
    public Result<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return Result.success(products);
    }

    /**
     * 获取商品详情（管理员）
     * GET /api/v1/admin/products/{id}
     *
     * @param id 商品ID
     * @return 商品详情
     */
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
    @PutMapping("/{id}/stock")
    public Result<Void> updateProductStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        productService.updateProductStock(id, stock);
        return Result.success();
    }
}

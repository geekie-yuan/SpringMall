package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.ProductRequest;
import site.geekie.shop.shoppingmall.dto.response.ProductResponse;
import site.geekie.shop.shoppingmall.service.ProductService;

import java.util.List;

/**
 * 商品控制器
 * 处理商品管理相关接口
 *
 * 接口路径前缀：/api/v1/products
 * 认证要求：管理接口需要ADMIN角色，查询接口公开
 */
@Tag(name = "Product", description = "商品接口")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 获取所有商品列表
     * 公开接口，无需认证
     *
     * @return 所有商品列表
     */
    @Operation(summary = "获取所有商品")
    @GetMapping
    public Result<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return Result.success(products);
    }

    /**
     * 根据分类ID获取商品列表
     * 公开接口，无需认证
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @Operation(summary = "根据分类获取商品")
    @GetMapping("/category/{categoryId}")
    public Result<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getProductsByCategoryId(categoryId);
        return Result.success(products);
    }

    /**
     * 搜索商品
     * 公开接口，无需认证
     *
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    @Operation(summary = "搜索商品")
    @GetMapping("/search")
    public Result<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        List<ProductResponse> products = productService.searchProducts(keyword);
        return Result.success(products);
    }

    /**
     * 获取商品详情
     * 公开接口，无需认证
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return Result.success(product);
    }

    /**
     * 新增商品
     * 需要ADMIN角色
     *
     * @param request 商品请求
     * @return 新增的商品信息
     */
    @Operation(summary = "新增商品")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public Result<ProductResponse> addProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.addProduct(request);
        return Result.success("商品添加成功", product);
    }

    /**
     * 修改商品
     * 需要ADMIN角色
     *
     * @param id 商品ID
     * @param request 商品请求
     * @return 修改后的商品信息
     */
    @Operation(summary = "修改商品")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public Result<ProductResponse> updateProduct(@PathVariable Long id,
                                                  @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return Result.success("商品修改成功", product);
    }

    /**
     * 删除商品
     * 需要ADMIN角色
     *
     * @param id 商品ID
     * @return 统一响应对象
     */
    @Operation(summary = "删除商品")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功", null);
    }
}

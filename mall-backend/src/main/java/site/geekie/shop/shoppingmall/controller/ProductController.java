package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.PageResult;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.vo.ProductVO;
import site.geekie.shop.shoppingmall.annotation.RateLimiter;
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
@Validated
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
    public Result<PageResult<ProductVO>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") @Max(100) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "sales") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        return Result.success(productService.getAllProducts(page, size, keyword, categoryId, 1, sortBy, sortDir));
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
    public Result<List<ProductVO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductVO> products = productService.getProductsByCategoryId(categoryId);
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
    @RateLimiter(count = 15, period = 60)
    @GetMapping("/search")
    public Result<PageResult<ProductVO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") @Max(100) int size) {
        return Result.success(productService.searchProducts(keyword, page, size));
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
    public Result<ProductVO> getProductById(@PathVariable Long id) {
        ProductVO product = productService.getProductById(id);
        return Result.success(product);
    }

}

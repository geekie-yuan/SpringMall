package site.geekie.shop.shoppingmall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.CartItemRequest;
import site.geekie.shop.shoppingmall.dto.response.CartItemResponse;
import site.geekie.shop.shoppingmall.service.CartService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车控制器
 * 提供购物车管理的REST API
 *
 * 基础路径：/api/v1/cart
 * 所有接口都需要USER角色权限
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CartController {

    private final CartService cartService;

    /**
     * 获取购物车列表
     * GET /api/v1/cart
     *
     * @return 购物车项列表
     */
    @GetMapping
    public Result<List<CartItemResponse>> getCartItems() {
        List<CartItemResponse> cartItems = cartService.getCartItems();
        return Result.success(cartItems);
    }

    /**
     * 添加商品到购物车
     * POST /api/v1/cart
     *
     * @param request 购物车请求（商品ID和数量）
     * @return 购物车项信息
     */
    @PostMapping
    public Result<CartItemResponse> addToCart(@Valid @RequestBody CartItemRequest request) {
        CartItemResponse cartItem = cartService.addToCart(request);
        return Result.success(cartItem);
    }

    /**
     * 更新购物车项数量
     * PUT /api/v1/cart/{id}/quantity
     *
     * @param id 购物车项ID
     * @param quantity 新数量
     * @return 更新后的购物车项
     */
    @PutMapping("/{id}/quantity")
    public Result<CartItemResponse> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        CartItemResponse cartItem = cartService.updateQuantity(id, quantity);
        return Result.success(cartItem);
    }

    /**
     * 更新购物车项选中状态
     * PUT /api/v1/cart/{id}/checked
     *
     * @param id 购物车项ID
     * @param checked 选中状态（0-未选中，1-已选中）
     * @return 操作结果
     */
    @PutMapping("/{id}/checked")
    public Result<Void> updateChecked(
            @PathVariable Long id,
            @RequestParam Integer checked) {
        cartService.updateChecked(id, checked);
        return Result.success();
    }

    /**
     * 批量更新购物车选中状态（全选/取消全选）
     * PUT /api/v1/cart/checked
     *
     * @param checked 选中状态（0-未选中，1-已选中）
     * @return 操作结果
     */
    @PutMapping("/checked")
    public Result<Void> updateAllChecked(@RequestParam Integer checked) {
        cartService.updateAllChecked(checked);
        return Result.success();
    }

    /**
     * 删除购物车项
     * DELETE /api/v1/cart/{id}
     *
     * @param id 购物车项ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCartItem(@PathVariable Long id) {
        cartService.deleteCartItem(id);
        return Result.success();
    }

    /**
     * 批量删除购物车项
     * DELETE /api/v1/cart/batch
     *
     * @param ids 购物车项ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteCartItems(@RequestParam List<Long> ids) {
        cartService.deleteCartItems(ids);
        return Result.success();
    }

    /**
     * 清空购物车
     * DELETE /api/v1/cart
     *
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Void> clearCart() {
        cartService.clearCart();
        return Result.success();
    }

    /**
     * 获取已选中商品的总价
     * GET /api/v1/cart/total
     *
     * @return 总价
     */
    @GetMapping("/total")
    public Result<BigDecimal> getCartTotal() {
        BigDecimal total = cartService.getCartTotal();
        return Result.success(total);
    }

    /**
     * 获取购物车商品种类数
     * GET /api/v1/cart/count
     *
     * @return 商品种类数
     */
    @GetMapping("/count")
    public Result<Integer> getCartCount() {
        int count = cartService.getCartCount();
        return Result.success(count);
    }
}

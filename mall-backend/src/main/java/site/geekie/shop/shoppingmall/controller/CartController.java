package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.annotation.CurrentUserId;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.CartItemDTO;
import site.geekie.shop.shoppingmall.vo.CartItemVO;
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
@Tag(name="Cart", description="购物车接口")
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
     * @param userId 当前登录用户ID（自动注入）
     * @return 购物车项列表
     */
    @Operation(summary = "获取购物车列表")
    @GetMapping
    public Result<List<CartItemVO>> getCartItems(@Parameter(hidden = true) @CurrentUserId Long userId) {
        List<CartItemVO> cartItems = cartService.getCartItems(userId);
        return Result.success(cartItems);
    }

    /**
     * 添加商品到购物车
     * POST /api/v1/cart
     *
     * @param request 购物车请求（商品ID和数量）
     * @param userId 当前登录用户ID（自动注入）
     * @return 购物车项信息
     */
    @Operation(summary = "添加商品到购物车")
    @PostMapping
    public Result<CartItemVO> addToCart(@Valid @RequestBody CartItemDTO request, @Parameter(hidden = true) @CurrentUserId Long userId) {
        CartItemVO cartItem = cartService.addToCart(request, userId);
        return Result.success(cartItem);
    }

    /**
     * 更新购物车项数量
     * PUT /api/v1/cart/{id}/quantity
     *
     * @param id 购物车项ID
     * @param quantity 新数量
     * @param userId 当前登录用户ID（自动注入）
     * @return 更新后的购物车项
     */
    @Operation(summary = "更新购物车项数量")
    @PutMapping("/{id}/quantity")
    public Result<CartItemVO> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @Parameter(hidden = true) @CurrentUserId Long userId) {
        CartItemVO cartItem = cartService.updateQuantity(id, quantity, userId);
        return Result.success(cartItem);
    }

    /**
     * 更新购物车项选中状态
     * PUT /api/v1/cart/{id}/checked
     *
     * @param id 购物车项ID
     * @param checked 选中状态（0-未选中，1-已选中）
     * @param userId 当前登录用户ID（自动注入）
     * @return 操作结果
     */
    @Operation(summary = "更新购物车项选中状态")
    @PutMapping("/{id}/checked")
    public Result<Void> updateChecked(
            @PathVariable Long id,
            @RequestParam Integer checked,
            @Parameter(hidden = true) @CurrentUserId Long userId) {
        cartService.updateChecked(id, checked, userId);
        return Result.success();
    }

    /**
     * 批量更新购物车选中状态（全选/取消全选）
     * PUT /api/v1/cart/checked
     *
     * @param checked 选中状态（0-未选中，1-已选中）
     * @param userId 当前登录用户ID（自动注入）
     * @return 操作结果
     */
    @Operation(summary = "批量更新购物车选中状态（全选/取消全选）")
    @PutMapping("/checked")
    public Result<Void> updateAllChecked(@RequestParam Integer checked, @Parameter(hidden = true) @CurrentUserId Long userId) {
        cartService.updateAllChecked(checked, userId);
        return Result.success();
    }

    /**
     * 删除购物车项
     * DELETE /api/v1/cart/{id}
     *
     * @param id 购物车项ID
     * @param userId 当前登录用户ID（自动注入）
     * @return 操作结果
     */
    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCartItem(@PathVariable Long id, @Parameter(hidden = true) @CurrentUserId Long userId) {
        cartService.deleteCartItem(id, userId);
        return Result.success();
    }

    /**
     * 批量删除购物车项
     * DELETE /api/v1/cart/batch
     *
     * @param ids 购物车项ID列表
     * @param userId 当前登录用户ID（自动注入）
     * @return 操作结果
     */
    @Operation(summary = "批量删除购物车项")
    @DeleteMapping("/batch")
    public Result<Void> deleteCartItems(@RequestParam List<Long> ids, @Parameter(hidden = true) @CurrentUserId Long userId) {
        cartService.deleteCartItems(ids, userId);
        return Result.success();
    }

    /**
     * 清空购物车
     * DELETE /api/v1/cart
     *
     * @param userId 当前登录用户ID（自动注入）
     * @return 操作结果
     */
    @Operation(summary = "清空购物车")
    @DeleteMapping
    public Result<Void> clearCart(@Parameter(hidden = true) @CurrentUserId Long userId) {
        cartService.clearCart(userId);
        return Result.success();
    }

    /**
     * 获取已选中商品的总价
     * GET /api/v1/cart/total
     *
     * @param userId 当前登录用户ID（自动注入）
     * @return 总价
     */
    @Operation(summary = "获取已选中商品的总价")
    @GetMapping("/total")
    public Result<BigDecimal> getCartTotal(@Parameter(hidden = true) @CurrentUserId Long userId) {
        BigDecimal total = cartService.getCartTotal(userId);
        return Result.success(total);
    }

    /**
     * 获取购物车商品种类数
     * GET /api/v1/cart/count
     *
     * @param userId 当前登录用户ID（自动注入）
     * @return 商品种类数
     */
    @Operation(summary = "获取购物车商品种类数")
    @GetMapping("/count")
    public Result<Integer> getCartCount(@Parameter(hidden = true) @CurrentUserId Long userId) {
        int count = cartService.getCartCount(userId);
        return Result.success(count);
    }
}

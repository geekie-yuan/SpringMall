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
import site.geekie.shop.shoppingmall.dto.OrderDTO;
import site.geekie.shop.shoppingmall.vo.OrderVO;
import site.geekie.shop.shoppingmall.service.OrderService;

import java.util.List;

/**
 * 订单控制器
 * 提供订单管理的REST API
 *
 * 基础路径：/api/v1/orders
 * 所有接口都需要USER角色权限
 */
@Tag(name = "Order", description = "订单控制器")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单（从购物车结算）
     * POST /api/v1/orders
     *
     * @param request 订单请求（收货地址ID和备注）
     * @param userId 当前登录用户ID（自动注入）
     * @return 订单信息
     */
    @Operation(summary = "创建订单（结算购物车）")
    @PostMapping
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderDTO request, @Parameter(hidden = true) @CurrentUserId Long userId) {
        OrderVO order = orderService.createOrder(request, userId);
        return Result.success(order);
    }

    /**
     * 获取用户的所有订单
     * GET /api/v1/orders
     *
     * @param userId 当前登录用户ID（自动注入）
     * @return 订单列表
     */
    @Operation(summary = "获取用户的所有订单")
    @GetMapping
    public Result<List<OrderVO>> getMyOrders(@Parameter(hidden = true) @CurrentUserId Long userId) {
        List<OrderVO> orders = orderService.getMyOrders(userId);
        return Result.success(orders);
    }

    /**
     * 根据状态获取用户订单
     * GET /api/v1/orders/status/{status}
     *
     * @param status 订单状态（UNPAID/PAID/SHIPPED/COMPLETED/CANCELLED）
     * @param userId 当前登录用户ID（自动注入）
     * @return 订单列表
     */
    @Operation(summary = "根据状态获取用户订单")
    @GetMapping("/status/{status}")
    public Result<List<OrderVO>> getMyOrdersByStatus(@PathVariable String status, @Parameter(hidden = true) @CurrentUserId Long userId) {
        List<OrderVO> orders = orderService.getMyOrdersByStatus(status, userId);
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     * GET /api/v1/orders/{orderNo}
     *
     * @param orderNo 订单号
     * @param userId 当前登录用户ID（自动注入）
     * @return 订单详情
     */
    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderNo, @Parameter(hidden = true) @CurrentUserId Long userId) {
        OrderVO order = orderService.getOrderDetail(orderNo, userId);
        return Result.success(order);
    }

    /**
     * 取消订单
     * PUT /api/v1/orders/{orderNo}/cancel
     *
     * @param orderNo 订单号
     * @param userId 当前登录用户ID（自动注入）
     * @return 操作结果
     */
    @Operation(summary = "取消订单")
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo, @Parameter(hidden = true) @CurrentUserId Long userId) {
        orderService.cancelOrder(orderNo, userId);
        return Result.success();
    }

    /**
     * 确认收货
     * PUT /api/v1/orders/{orderNo}/confirm
     *
     * @param orderNo 订单号
     * @param userId 当前登录用户ID（自动注入）
     * @return 操作结果
     */
    @Operation(summary = "确认收货")
    @PutMapping("/{orderNo}/confirm")
    public Result<Void> confirmReceipt(@PathVariable String orderNo, @Parameter(hidden = true) @CurrentUserId Long userId) {
        orderService.confirmReceipt(orderNo, userId);
        return Result.success();
    }
}

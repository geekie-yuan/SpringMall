package site.geekie.shop.shoppingmall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.OrderRequest;
import site.geekie.shop.shoppingmall.dto.response.OrderResponse;
import site.geekie.shop.shoppingmall.service.OrderService;

import java.util.List;

/**
 * 订单控制器
 * 提供订单管理的REST API
 *
 * 基础路径：/api/v1/orders
 * 所有接口都需要USER角色权限
 */
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
     * @return 订单信息
     */
    @PostMapping
    public Result<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return Result.success(order);
    }

    /**
     * 获取我的所有订单
     * GET /api/v1/orders
     *
     * @return 订单列表
     */
    @GetMapping
    public Result<List<OrderResponse>> getMyOrders() {
        List<OrderResponse> orders = orderService.getMyOrders();
        return Result.success(orders);
    }

    /**
     * 根据状态获取我的订单
     * GET /api/v1/orders/status/{status}
     *
     * @param status 订单状态（UNPAID/PAID/SHIPPED/COMPLETED/CANCELLED）
     * @return 订单列表
     */
    @GetMapping("/status/{status}")
    public Result<List<OrderResponse>> getMyOrdersByStatus(@PathVariable String status) {
        List<OrderResponse> orders = orderService.getMyOrdersByStatus(status);
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     * GET /api/v1/orders/{orderNo}
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<OrderResponse> getOrderDetail(@PathVariable String orderNo) {
        OrderResponse order = orderService.getOrderDetail(orderNo);
        return Result.success(order);
    }

    /**
     * 取消订单
     * PUT /api/v1/orders/{orderNo}/cancel
     *
     * @param orderNo 订单号
     * @return 操作结果
     */
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return Result.success();
    }

    /**
     * 确认收货
     * PUT /api/v1/orders/{orderNo}/confirm
     *
     * @param orderNo 订单号
     * @return 操作结果
     */
    @PutMapping("/{orderNo}/confirm")
    public Result<Void> confirmReceipt(@PathVariable String orderNo) {
        orderService.confirmReceipt(orderNo);
        return Result.success();
    }
}

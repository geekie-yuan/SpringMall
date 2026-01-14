package site.geekie.shop.shoppingmall.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.response.OrderResponse;
import site.geekie.shop.shoppingmall.service.OrderService;

import java.util.List;

/**
 * 管理员-订单管理控制器
 * 提供订单管理的REST API（仅管理员可访问）
 *
 * 基础路径：/api/v1/admin/orders
 * 所有接口都需要ADMIN角色权限
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 获取所有订单（管理员）
     * GET /api/v1/admin/orders
     *
     * @return 所有订单列表
     */
    @GetMapping
    public Result<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return Result.success(orders);
    }

    /**
     * 根据状态获取订单（管理员）
     * GET /api/v1/admin/orders/status/{status}
     *
     * @param status 订单状态
     * @return 订单列表
     */
    @GetMapping("/status/{status}")
    public Result<List<OrderResponse>> getOrdersByStatus(@PathVariable String status) {
        List<OrderResponse> orders = orderService.getAllOrdersByStatus(status);
        return Result.success(orders);
    }

    /**
     * 获取订单详情（管理员）
     * GET /api/v1/admin/orders/{orderNo}
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<OrderResponse> getOrderDetail(@PathVariable String orderNo) {
        OrderResponse order = orderService.getOrderDetailAdmin(orderNo);
        return Result.success(order);
    }

    /**
     * 发货（管理员）
     * PUT /api/v1/admin/orders/{orderNo}/ship
     *
     * @param orderNo 订单号
     * @return 操作结果
     */
    @PutMapping("/{orderNo}/ship")
    public Result<Void> shipOrder(@PathVariable String orderNo) {
        orderService.shipOrder(orderNo);
        return Result.success();
    }

    /**
     * 取消订单（管理员）
     * PUT /api/v1/admin/orders/{orderNo}/cancel
     *
     * @param orderNo 订单号
     * @return 操作结果
     */
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrderByAdmin(orderNo);
        return Result.success();
    }
}

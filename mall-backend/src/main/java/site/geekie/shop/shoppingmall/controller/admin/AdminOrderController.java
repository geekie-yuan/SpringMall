package site.geekie.shop.shoppingmall.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.PageResult;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.vo.OrderVO;
import site.geekie.shop.shoppingmall.service.OrderService;

/**
 * 管理员-订单管理控制器
 * 提供订单管理的REST API（仅管理员可访问）
 *
 * 基础路径：/api/v1/admin/orders
 * 所有接口都需要ADMIN角色权限
 */
@Tag(name = "AdminOrder", description = "管理员订单管理接口")
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 获取所有订单（管理员）
     * GET /api/v1/admin/orders
     *
     * @return 所有订单列表
     */
    @Operation(summary = "获取所有订单（管理员）")
    @GetMapping
    public Result<PageResult<OrderVO>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") @Max(100) int size) {
        return Result.success(orderService.getAllOrders(page, size));
    }

    /**
     * 根据状态获取订单（管理员）
     * GET /api/v1/admin/orders/status/{status}
     *
     * @param status 订单状态
     * @param page 页码
     * @param size 每页大小
     * @return 分页订单列表
     */
    @Operation(summary = "根据状态获取订单（管理员）")
    @GetMapping("/status/{status}")
    public Result<PageResult<OrderVO>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") @Max(100) int size) {
        return Result.success(orderService.getAllOrdersByStatus(status, page, size));
    }

    /**
     * 获取订单详情（管理员）
     * GET /api/v1/admin/orders/{orderNo}
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @Operation(summary = "获取订单详情（管理员）")
    @GetMapping("/{orderNo}")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderNo) {
        OrderVO order = orderService.getOrderDetailAdmin(orderNo);
        return Result.success(order);
    }

    /**
     * 发货（管理员）
     * PUT /api/v1/admin/orders/{orderNo}/ship
     *
     * @param orderNo 订单号
     * @return 操作结果
     */
    @Operation(summary = "发货（管理员）")
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
    @Operation(summary = "取消订单（管理员）")
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrderByAdmin(orderNo);
        return Result.success();
    }

    // TODO: 增加销售额聚合接口，供 Dashboard 总销售额使用
    // GET /admin/stats/sales
    // SELECT SUM(pay_amount) FROM order WHERE status != 'CANCELLED'
}

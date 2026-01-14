package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.request.OrderRequest;
import site.geekie.shop.shoppingmall.dto.response.OrderResponse;

import java.util.List;

/**
 * 订单服务接口
 * 提供订单的业务逻辑方法
 */
public interface OrderService {

    /**
     * 创建订单（从购物车结算）
     * 1. 验证购物车中有已选中的商品
     * 2. 验证库存充足
     * 3. 扣减库存
     * 4. 生成订单号
     * 5. 创建订单主表和明细表
     * 6. 清空已购买的购物车商品
     *
     * @param request 订单请求（包含收货地址ID和备注）
     * @return 订单响应
     */
    OrderResponse createOrder(OrderRequest request);

    /**
     * 获取当前用户的所有订单
     *
     * @return 订单列表
     */
    List<OrderResponse> getMyOrders();

    /**
     * 根据状态获取当前用户的订单
     *
     * @param status 订单状态
     * @return 订单列表
     */
    List<OrderResponse> getMyOrdersByStatus(String status);

    /**
     * 获取订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderResponse getOrderDetail(String orderNo);

    /**
     * 取消订单
     * 只有待支付状态的订单可以取消
     * 取消后恢复库存
     *
     * @param orderNo 订单号
     */
    void cancelOrder(String orderNo);

    /**
     * 确认收货
     * 只有已发货状态的订单可以确认收货
     *
     * @param orderNo 订单号
     */
    void confirmReceipt(String orderNo);

    // ===== 管理员方法 =====

    /**
     * 获取所有订单（管理员）
     *
     * @return 所有订单列表
     */
    List<OrderResponse> getAllOrders();

    /**
     * 根据状态获取所有订单（管理员）
     *
     * @param status 订单状态
     * @return 订单列表
     */
    List<OrderResponse> getAllOrdersByStatus(String status);

    /**
     * 获取订单详情（管理员）
     * 不验证订单所有权
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderResponse getOrderDetailAdmin(String orderNo);

    /**
     * 发货（管理员）
     * 只有已支付状态的订单可以发货
     *
     * @param orderNo 订单号
     */
    void shipOrder(String orderNo);

    /**
     * 取消订单（管理员）
     * 管理员可以取消待支付和待发货状态的订单
     * 取消后恢复库存
     *
     * @param orderNo 订单号
     */
    void cancelOrderByAdmin(String orderNo);
}

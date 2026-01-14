package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.OrderStatus;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.request.OrderRequest;
import site.geekie.shop.shoppingmall.dto.response.OrderItemResponse;
import site.geekie.shop.shoppingmall.dto.response.OrderResponse;
import site.geekie.shop.shoppingmall.entity.*;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.*;
import site.geekie.shop.shoppingmall.security.SecurityUser;
import site.geekie.shop.shoppingmall.service.OrderService;
import site.geekie.shop.shoppingmall.util.OrderNoGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return securityUser.getUser().getId();
    }

    /**
     * 构建订单响应对象
     */
    private OrderResponse buildOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setPayAmount(order.getPayAmount());
        response.setFreight(order.getFreight());
        response.setStatus(order.getStatus());

        // 设置状态描述
        try {
            OrderStatus orderStatus = OrderStatus.fromCode(order.getStatus());
            response.setStatusDesc(orderStatus.getDescription());
        } catch (IllegalArgumentException e) {
            response.setStatusDesc(order.getStatus());
        }

        response.setPaymentTime(order.getPaymentTime());
        response.setShipTime(order.getShipTime());
        response.setCompleteTime(order.getCompleteTime());
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setReceiverAddress(order.getReceiverAddress());
        response.setRemark(order.getRemark());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // 查询订单明细
        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductImage(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(OrderRequest request) {
        Long userId = getCurrentUserId();

        // 1. 查询购物车中已选中的商品
        List<CartItem> checkedItems = cartItemMapper.findCheckedByUserId(userId);
        if (checkedItems == null || checkedItems.isEmpty()) {
            throw new BusinessException(ResultCode.NO_CHECKED_CART_ITEMS);
        }

        // 2. 验证地址
        Address address = addressMapper.findById(request.getAddressId());
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 验证库存并计算总价
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : checkedItems) {
            Product product = productMapper.findById(cartItem.getProductId());
            if (product == null) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
            }
            if (product.getStatus() != 1) {
                throw new BusinessException(ResultCode.PRODUCT_UNAVAILABLE);
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
            }

            // 计算小计
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // 准备订单明细（稍后插入）
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);
        }

        // 4. 扣减库存（使用乐观锁）
        for (OrderItem orderItem : orderItems) {
            int result = productMapper.decreaseStock(orderItem.getProductId(), orderItem.getQuantity());
            if (result == 0) {
                // 库存不足或商品不存在
                throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
            }
        }

        // 5. 生成订单号
        String orderNo = OrderNoGenerator.generateOrderNo();

        // 6. 创建订单主表
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount); // 实付金额暂时等于总金额（未来可加优惠券等）
        order.setFreight(BigDecimal.ZERO); // 运费暂时为0
        order.setStatus(OrderStatus.UNPAID.getCode());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() +
                address.getDistrict() + address.getDetailAddress());
        order.setRemark(request.getRemark());

        orderMapper.insert(order);

        // 7. 创建订单明细
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemMapper.batchInsert(orderItems);

        // 8. 清空已购买的购物车商品
        List<Long> cartItemIds = checkedItems.stream()
                .map(CartItem::getId)
                .collect(Collectors.toList());
        cartItemMapper.deleteByIds(cartItemIds);

        // 9. 返回订单信息
        return buildOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        Long userId = getCurrentUserId();
        List<Order> orders = orderMapper.findByUserId(userId);

        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getMyOrdersByStatus(String status) {
        // 验证状态是否合法
        try {
            OrderStatus.fromCode(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.INVALID_ORDER_STATUS);
        }

        Long userId = getCurrentUserId();
        List<Order> orders = orderMapper.findByUserIdAndStatus(userId, status);

        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderDetail(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 验证订单所有权
        Long userId = getCurrentUserId();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return buildOrderResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 验证订单所有权
        Long userId = getCurrentUserId();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 用户可以取消待支付和待发货状态的订单（未发货订单）
        if (!OrderStatus.UNPAID.getCode().equals(order.getStatus())
            && !OrderStatus.PAID.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_BE_CANCELLED);
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        for (OrderItem item : items) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }

        // 更新订单状态为已取消
        orderMapper.updateStatus(orderNo, OrderStatus.CANCELLED.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 验证订单所有权
        Long userId = getCurrentUserId();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 只有已发货状态可以确认收货
        if (!OrderStatus.SHIPPED.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.INVALID_ORDER_STATUS);
        }

        // 更新订单状态为已完成
        orderMapper.updateStatus(orderNo, OrderStatus.COMPLETED.getCode());
        orderMapper.updateCompleteTime(orderNo);
    }

    // ===== 管理员方法实现 =====

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderMapper.findAll();
        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getAllOrdersByStatus(String status) {
        // 验证状态是否合法
        try {
            OrderStatus.fromCode(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.INVALID_ORDER_STATUS);
        }

        List<Order> orders = orderMapper.findAllByStatus(status);
        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderDetailAdmin(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 管理员查看订单详情不需要验证所有权
        return buildOrderResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 只有已支付状态可以发货
        if (!OrderStatus.PAID.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.INVALID_ORDER_STATUS);
        }

        // 更新订单状态为已发货
        orderMapper.updateStatus(orderNo, OrderStatus.SHIPPED.getCode());
        orderMapper.updateShipTime(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderByAdmin(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 管理员可以取消待支付和待发货状态的订单
        if (!OrderStatus.UNPAID.getCode().equals(order.getStatus())
            && !OrderStatus.PAID.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_BE_CANCELLED);
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        for (OrderItem item : items) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }

        // 更新订单状态为已取消
        orderMapper.updateStatus(orderNo, OrderStatus.CANCELLED.getCode());
    }
}

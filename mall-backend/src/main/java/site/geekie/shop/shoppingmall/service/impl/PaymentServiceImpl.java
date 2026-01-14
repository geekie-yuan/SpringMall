package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.OrderStatus;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.request.PaymentNotifyRequest;
import site.geekie.shop.shoppingmall.dto.request.PaymentRequest;
import site.geekie.shop.shoppingmall.dto.response.PaymentResponse;
import site.geekie.shop.shoppingmall.entity.Order;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.OrderMapper;
import site.geekie.shop.shoppingmall.security.SecurityUser;
import site.geekie.shop.shoppingmall.service.PaymentService;

import java.time.Instant;
import java.util.UUID;

/**
 * 支付服务实现类
 * 模拟支付功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderMapper orderMapper;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return securityUser.getUser().getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse pay(PaymentRequest request) {
        // 1. 查询订单
        Order order = orderMapper.findByOrderNo(request.getOrderNo());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 验证订单所有权
        Long userId = getCurrentUserId();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 验证订单状态
        if (!OrderStatus.UNPAID.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.INVALID_ORDER_STATUS);
        }

        // 4. 模拟支付处理
        // 实际项目中这里会调用第三方支付接口（支付宝、微信支付等）
        // 这里我们直接模拟支付成功
        String transactionNo = generateTransactionNo();
        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : "MOCK";

        log.info("模拟支付开始 - 订单号: {}, 金额: {}, 支付方式: {}",
                 order.getOrderNo(), order.getPayAmount(), paymentMethod);

        // 5. 更新订单状态为已支付
        orderMapper.updateStatus(order.getOrderNo(), OrderStatus.PAID.getCode());
        orderMapper.updatePaymentTime(order.getOrderNo());

        log.info("模拟支付成功 - 订单号: {}, 交易流水号: {}", order.getOrderNo(), transactionNo);

        // 6. 返回支付结果
        PaymentResponse response = new PaymentResponse();
        response.setOrderNo(order.getOrderNo());
        response.setPayAmount(order.getPayAmount());
        response.setPaymentMethod(paymentMethod);
        response.setPaymentStatus("SUCCESS");
        response.setMessage("支付成功");
        response.setTransactionNo(transactionNo);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentNotify(PaymentNotifyRequest request) {
        log.info("收到支付回调 - 订单号: {}, 交易流水号: {}, 状态: {}",
                 request.getOrderNo(), request.getTransactionNo(), request.getPaymentStatus());

        // 1. 查询订单
        Order order = orderMapper.findByOrderNo(request.getOrderNo());
        if (order == null) {
            log.error("支付回调失败 - 订单不存在: {}", request.getOrderNo());
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 验证订单状态（防止重复回调）
        if (OrderStatus.PAID.getCode().equals(order.getStatus())) {
            log.warn("订单已支付，忽略重复回调 - 订单号: {}", request.getOrderNo());
            return;
        }

        if (!OrderStatus.UNPAID.getCode().equals(order.getStatus())) {
            log.error("订单状态异常 - 订单号: {}, 当前状态: {}", request.getOrderNo(), order.getStatus());
            throw new BusinessException(ResultCode.INVALID_ORDER_STATUS);
        }

        // 3. 处理支付结果
        if ("SUCCESS".equals(request.getPaymentStatus())) {
            // 支付成功
            orderMapper.updateStatus(order.getOrderNo(), OrderStatus.PAID.getCode());
            orderMapper.updatePaymentTime(order.getOrderNo());
            log.info("支付回调处理成功 - 订单号: {}", request.getOrderNo());
        } else {
            // 支付失败
            log.warn("支付失败 - 订单号: {}, 状态: {}", request.getOrderNo(), request.getPaymentStatus());
            // 这里可以记录支付失败日志，但不改变订单状态，让用户可以重新支付
        }
    }

    /**
     * 生成交易流水号
     * 实际项目中由第三方支付平台生成
     *
     * @return 交易流水号
     */
    private String generateTransactionNo() {
        return "PAY" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

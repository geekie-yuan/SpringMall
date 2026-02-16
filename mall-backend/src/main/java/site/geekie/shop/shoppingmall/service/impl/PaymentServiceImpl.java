package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.OrderStatus;
import site.geekie.shop.shoppingmall.common.PaymentMethod;
import site.geekie.shop.shoppingmall.common.PaymentStatus;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.PaymentDTO;
import site.geekie.shop.shoppingmall.dto.PaymentNotifyDTO;
import site.geekie.shop.shoppingmall.entity.OrderDO;
import site.geekie.shop.shoppingmall.entity.OrderItemDO;
import site.geekie.shop.shoppingmall.entity.PaymentDO;
import site.geekie.shop.shoppingmall.entity.RefundDO;
import site.geekie.shop.shoppingmall.vo.PaymentVO;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.OrderItemMapper;
import site.geekie.shop.shoppingmall.mapper.OrderMapper;
import site.geekie.shop.shoppingmall.mapper.PaymentMapper;
import site.geekie.shop.shoppingmall.mapper.RefundMapper;
import site.geekie.shop.shoppingmall.security.SecurityUser;
import site.geekie.shop.shoppingmall.service.AlipayService;
import site.geekie.shop.shoppingmall.service.PaymentService;
import site.geekie.shop.shoppingmall.util.OrderNoGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final RefundMapper refundMapper;
    private final AlipayService alipayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO pay(PaymentDTO request, Long userId) {
        // 1. 查询订单
        OrderDO order = orderMapper.findByOrderNo(request.getOrderNo());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 验证订单所有权
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
        PaymentVO response = new PaymentVO();
        response.setOrderNo(order.getOrderNo());
        response.setAmount(order.getPayAmount());
        response.setPaymentMethod(paymentMethod);
        response.setPaymentStatus("SUCCESS");
        response.setTradeNo(transactionNo);
        response.setCreatedAt(LocalDateTime.now());

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentNotify(PaymentNotifyDTO request) {
        log.info("收到支付回调 - 订单号: {}, 交易流水号: {}, 状态: {}",
                 request.getOrderNo(), request.getTransactionNo(), request.getPaymentStatus());

        // 1. 查询订单
        OrderDO order = orderMapper.findByOrderNo(request.getOrderNo());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createAlipayPayment(String orderNo, Long userId) {
        // 1. 查询订单
        OrderDO order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 验证订单状态
        if (!OrderStatus.UNPAID.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.INVALID_ORDER_STATUS);
        }

        // 4. 查询订单商品明细
        List<OrderItemDO> orderItems = orderItemMapper.findByOrderId(order.getId());
        if (orderItems == null || orderItems.isEmpty()) {
            throw new BusinessException(ResultCode.ORDER_ITEM_NOT_FOUND);
        }

        // 5. 生成支付标题和描述
        String subject = buildPaymentSubject(orderItems, orderNo);
        String body = buildPaymentBody(orderItems, order.getPayAmount());

        // 6. 检查是否已有支付记录
        PaymentDO existingPayment = paymentMapper.findByOrderNo(orderNo);
        if (existingPayment != null && PaymentStatus.PENDING.name().equals(existingPayment.getPaymentStatus())) {
            // 已有待支付记录，返回已有支付信息
            log.info("订单已有待支付记录 - 订单号: {}, 支付流水号: {}", orderNo, existingPayment.getPaymentNo());

            PaymentVO vo = new PaymentVO();
            vo.setPaymentNo(existingPayment.getPaymentNo());
            vo.setOrderNo(existingPayment.getOrderNo());
            vo.setAmount(existingPayment.getAmount());
            vo.setPaymentMethod(existingPayment.getPaymentMethod());
            vo.setPaymentStatus(existingPayment.getPaymentStatus());
            vo.setCreatedAt(existingPayment.getCreatedAt());

            // 重新生成支付表单
            String paymentUrl = alipayService.createPayment(
                    existingPayment.getPaymentNo(),
                    orderNo,
                    subject,
                    body,
                    order.getPayAmount().toString()
            );
            vo.setPaymentUrl(paymentUrl);

            return vo;
        }

        // 7. 生成支付流水号
        String paymentNo = OrderNoGenerator.generateOrderNo();

        // 8. 创建支付记录
        PaymentDO payment = new PaymentDO();
        payment.setPaymentNo(paymentNo);
        payment.setOrderNo(orderNo);
        payment.setUserId(userId);
        payment.setAmount(order.getPayAmount());
        payment.setPaymentMethod(PaymentMethod.ALIPAY.name());
        payment.setPaymentStatus(PaymentStatus.PENDING.name());

        paymentMapper.insert(payment);

        log.info("创建支付记录 - 支付流水号: {}, 订单号: {}, 金额: {}, 商品: {}",
                 paymentNo, orderNo, order.getPayAmount(), subject);

        // 9. 调用支付宝API生成支付表单
        String paymentUrl = alipayService.createPayment(
                paymentNo,
                orderNo,
                subject,
                body,
                order.getPayAmount().toString()
        );

        // 10. 更新订单支付方式
        orderMapper.updatePaymentMethod(orderNo, PaymentMethod.ALIPAY.name());

        // 11. 返回支付信息
        PaymentVO vo = new PaymentVO();
        vo.setPaymentNo(paymentNo);
        vo.setOrderNo(orderNo);
        vo.setAmount(order.getPayAmount());
        vo.setPaymentMethod(PaymentMethod.ALIPAY.name());
        vo.setPaymentStatus(PaymentStatus.PENDING.name());
        vo.setPaymentUrl(paymentUrl);
        vo.setCreatedAt(LocalDateTime.now());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params) {
        // 1. 调用支付宝服务验证签名
        String result = alipayService.handleNotify(params);
        if (!"success".equals(result)) {
            return result;
        }

        // 2. 获取通知参数
        String outTradeNo = params.get("out_trade_no");  // 支付流水号
        String tradeNo = params.get("trade_no");  // 支付宝交易号
        String tradeStatus = params.get("trade_status");
        String totalAmount = params.get("total_amount");

        // 3. 查询支付记录
        PaymentDO payment = paymentMapper.findByPaymentNo(outTradeNo);
        if (payment == null) {
            log.error("支付记录不存在 - 支付流水号: {}", outTradeNo);
            return "failure";
        }

        // 4. 验证金额
        BigDecimal notifyAmount = new BigDecimal(totalAmount);
        if (payment.getAmount().compareTo(notifyAmount) != 0) {
            log.error("支付金额不匹配 - 支付流水号: {}, 订单金额: {}, 通知金额: {}",
                     outTradeNo, payment.getAmount(), notifyAmount);
            return "failure";
        }

        // 5. 幂等性检查
        if (PaymentStatus.SUCCESS.name().equals(payment.getPaymentStatus())) {
            log.warn("支付已完成，忽略重复通知 - 支付流水号: {}", outTradeNo);
            return "success";
        }

        // 6. 更新支付记录
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS.name());
            payment.setTradeNo(tradeNo);
            payment.setNotifyTime(LocalDateTime.now());
            paymentMapper.updateById(payment);

            // 7. 更新订单状态
            OrderDO order = orderMapper.findByOrderNo(payment.getOrderNo());
            if (order != null && OrderStatus.UNPAID.getCode().equals(order.getStatus())) {
                orderMapper.updateStatus(payment.getOrderNo(), OrderStatus.PAID.getCode());
                orderMapper.updatePaymentTime(payment.getOrderNo());
                log.info("订单支付成功 - 订单号: {}, 支付流水号: {}, 交易号: {}",
                         payment.getOrderNo(), outTradeNo, tradeNo);
            }

            return "success";
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            // 交易关闭
            payment.setPaymentStatus(PaymentStatus.CLOSED.name());
            payment.setTradeNo(tradeNo);
            payment.setNotifyTime(LocalDateTime.now());
            paymentMapper.updateById(payment);

            log.info("支付已关闭 - 支付流水号: {}", outTradeNo);
            return "success";
        } else {
            log.warn("未处理的交易状态 - 状态: {}", tradeStatus);
            return "failure";
        }
    }

    @Override
    public PaymentVO queryPayment(String paymentNo) {
        // 1. 查询支付记录
        PaymentDO payment = paymentMapper.findByPaymentNo(paymentNo);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }

        // 2. 如果是待支付状态，查询支付宝实时状态
        if (PaymentStatus.PENDING.name().equals(payment.getPaymentStatus())) {
            String tradeStatus = alipayService.queryPaymentStatus(paymentNo);
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功，更新记录
                payment.setPaymentStatus(PaymentStatus.SUCCESS.name());
                paymentMapper.updateById(payment);
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                // 交易关闭
                payment.setPaymentStatus(PaymentStatus.CLOSED.name());
                paymentMapper.updateById(payment);
            }
        }

        // 3. 返回支付信息
        PaymentVO vo = new PaymentVO();
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setAmount(payment.getAmount());
        vo.setPaymentMethod(payment.getPaymentMethod());
        vo.setPaymentStatus(payment.getPaymentStatus());
        vo.setTradeNo(payment.getTradeNo());
        vo.setCreatedAt(payment.getCreatedAt());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(String orderNo, String refundReason) {
        // 1. 查询订单的支付记录
        PaymentDO payment = paymentMapper.findByOrderNo(orderNo);
        if (payment == null) {
            log.error("退款失败 - 支付记录不存在，订单号: {}", orderNo);
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }

        // 2. 验证支付状态
        if (!PaymentStatus.SUCCESS.name().equals(payment.getPaymentStatus())) {
            log.error("退款失败 - 支付状态异常，订单号: {}, 支付状态: {}", orderNo, payment.getPaymentStatus());
            throw new BusinessException(ResultCode.INVALID_PARAMETER);
        }

        // 3. 检查是否已退款
        if (PaymentStatus.REFUNDED.name().equals(payment.getPaymentStatus())) {
            log.warn("退款失败 - 订单已退款，订单号: {}", orderNo);
            throw new BusinessException(ResultCode.PAYMENT_ALREADY_REFUNDED);
        }

        // 4. 检查是否已有退款记录
        RefundDO existingRefund = refundMapper.findByOrderNo(orderNo);
        if (existingRefund != null && "SUCCESS".equals(existingRefund.getRefundStatus())) {
            log.warn("退款失败 - 订单已退款，订单号: {}", orderNo);
            throw new BusinessException(ResultCode.PAYMENT_ALREADY_REFUNDED);
        }

        // 5. 生成退款流水号
        String refundNo = generateRefundNo();

        // 6. 根据支付方式调用对应的退款接口
        boolean refundSuccess = false;
        if (PaymentMethod.ALIPAY.name().equals(payment.getPaymentMethod())) {
            // 支付宝退款
            if (payment.getTradeNo() == null || payment.getTradeNo().isEmpty()) {
                log.error("退款失败 - 支付宝交易号为空，订单号: {}", orderNo);
                throw new BusinessException(ResultCode.REFUND_FAILED);
            }

            refundSuccess = alipayService.refund(
                    refundNo,
                    payment.getTradeNo(),
                    payment.getAmount(),
                    refundReason
            );
        } else {
            // 未来可以扩展其他支付方式
            log.error("退款失败 - 不支持的支付方式: {}", payment.getPaymentMethod());
            throw new BusinessException(ResultCode.REFUND_FAILED);
        }

        // 7. 创建退款记录
        RefundDO refund = new RefundDO();
        refund.setRefundNo(refundNo);
        refund.setOrderNo(orderNo);
        refund.setPaymentNo(payment.getPaymentNo());
        refund.setTradeNo(payment.getTradeNo());
        refund.setRefundAmount(payment.getAmount());
        refund.setRefundReason(refundReason);

        if (refundSuccess) {
            // 退款成功
            refund.setRefundStatus("SUCCESS");
            refund.setRefundTime(LocalDateTime.now());

            refundMapper.insert(refund);

            // 更新支付记录状态为已退款
            payment.setPaymentStatus(PaymentStatus.REFUNDED.name());
            paymentMapper.updateById(payment);

            log.info("退款成功 - 订单号: {}, 退款流水号: {}, 金额: {}",
                     orderNo, refundNo, payment.getAmount());
        } else {
            // 退款失败
            refund.setRefundStatus("FAILED");
            refundMapper.insert(refund);

            log.error("退款失败 - 订单号: {}, 退款流水号: {}", orderNo, refundNo);
            throw new BusinessException(ResultCode.REFUND_FAILED);
        }
    }

    /**
     * 生成退款流水号
     * 格式：RF + 时间戳 + 6位随机数
     *
     * @return 退款流水号
     */
    private String generateRefundNo() {
        return "RF" + Instant.now().toEpochMilli() +
               String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * 构建支付标题
     * 格式：
     * - 单商品："{商品名称} x{数量} - 订单{订单号}"
     * - 多商品："{首个商品名称}等{商品总数}件 - 订单{订单号}"
     *
     * @param orderItems 订单商品列表
     * @param orderNo 订单号
     * @return 支付标题（最长 256 字符）
     */
    private String buildPaymentSubject(List<OrderItemDO> orderItems, String orderNo) {
        StringBuilder subject = new StringBuilder();

        if (orderItems.size() == 1) {
            // 单商品订单
            OrderItemDO item = orderItems.get(0);
            String productName = item.getProductName() != null ? item.getProductName() : "商品";
            subject.append(productName)
                   .append(" x")
                   .append(item.getQuantity());
        } else {
            // 多商品订单
            OrderItemDO firstItem = orderItems.get(0);
            String productName = firstItem.getProductName() != null ? firstItem.getProductName() : "商品";
            subject.append(productName)
                   .append("等")
                   .append(orderItems.size())
                   .append("件商品");
        }

        subject.append(" - 订单").append(orderNo);

        // 支付宝 subject 限制：最长 256 字符
        String result = subject.toString();
        if (result.length() > 256) {
            result = result.substring(0, 253) + "...";
        }

        return result;
    }

    /**
     * 构建支付描述
     * 格式："{商品1名称} x{数量}、{商品2名称} x{数量}，共计¥{金额}"
     *
     * @param orderItems 订单商品列表
     * @param totalAmount 订单总金额
     * @return 支付描述（最长 128 字符）
     */
    private String buildPaymentBody(List<OrderItemDO> orderItems, BigDecimal totalAmount) {
        StringBuilder body = new StringBuilder();

        for (int i = 0; i < orderItems.size(); i++) {
            OrderItemDO item = orderItems.get(i);
            if (i > 0) {
                body.append("、");
            }
            String productName = item.getProductName() != null ? item.getProductName() : "商品";
            body.append(productName)
                .append(" x")
                .append(item.getQuantity());

            // 如果描述过长，提前截断
            if (body.length() > 100) {
                body.setLength(0);
                body.append(orderItems.get(0).getProductName() != null ? orderItems.get(0).getProductName() : "商品")
                    .append("等")
                    .append(orderItems.size())
                    .append("件商品");
                break;
            }
        }

        body.append("，共计¥").append(totalAmount);

        // 支付宝 body 限制：最长 128 字符
        String result = body.toString();
        if (result.length() > 128) {
            result = result.substring(0, 125) + "...";
        }

        return result;
    }
}

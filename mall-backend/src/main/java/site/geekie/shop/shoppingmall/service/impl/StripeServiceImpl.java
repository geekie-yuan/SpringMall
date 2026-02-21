package site.geekie.shop.shoppingmall.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.OrderStatus;
import site.geekie.shop.shoppingmall.common.PaymentMethod;
import site.geekie.shop.shoppingmall.common.PaymentStatus;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.CreateStripePaymentDTO;
import site.geekie.shop.shoppingmall.dto.StripeRefundDTO;
import site.geekie.shop.shoppingmall.config.StripeConfig;
import site.geekie.shop.shoppingmall.entity.OrderDO;
import site.geekie.shop.shoppingmall.entity.OrderItemDO;
import site.geekie.shop.shoppingmall.entity.PaymentDO;
import site.geekie.shop.shoppingmall.entity.RefundDO;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.OrderItemMapper;
import site.geekie.shop.shoppingmall.mapper.OrderMapper;
import site.geekie.shop.shoppingmall.mapper.PaymentMapper;
import site.geekie.shop.shoppingmall.mapper.RefundMapper;
import site.geekie.shop.shoppingmall.service.StripeService;
import site.geekie.shop.shoppingmall.util.OrderNoGenerator;
import site.geekie.shop.shoppingmall.vo.StripePaymentVO;
import site.geekie.shop.shoppingmall.vo.StripeRefundVO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Stripe 支付服务实现类 - Checkout + Adaptive Pricing 模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final RefundMapper refundMapper;
    private final OrderItemMapper orderItemMapper;
    private final StripeConfig stripeConfig;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.refund-webhook-secret}")
    private String refundWebhookSecret;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StripePaymentVO createPaymentIntent(CreateStripePaymentDTO request, Long userId) {
        Stripe.apiKey = stripeSecretKey;

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

        // 4. 检查是否已有支付记录
        PaymentDO existingPayment = paymentMapper.findByOrderNo(request.getOrderNo());
        if (existingPayment != null && PaymentStatus.PENDING.name().equals(existingPayment.getPaymentStatus())) {
            // 已有待支付记录,直接返回(如果 tradeNo 是 Session ID)
            if (existingPayment.getTradeNo() != null && existingPayment.getTradeNo().startsWith("cs_")) {
                log.info("订单已有 Checkout Session 记录 - 订单号: {}, Session ID: {}",
                    request.getOrderNo(), existingPayment.getTradeNo());
                return StripePaymentVO.builder()
                    .paymentNo(existingPayment.getPaymentNo())
                    .sessionUrl(existingPayment.getCodeUrl())
                    .sessionId(existingPayment.getTradeNo())
                    .orderNo(existingPayment.getOrderNo())
                    .amount(existingPayment.getAmount())
                    .build();
            }
        }

        // 5. 查询订单商品列表
        List<OrderItemDO> orderItems = orderItemMapper.findByOrderNo(request.getOrderNo());
        if (orderItems == null || orderItems.isEmpty()) {
            log.error("订单商品列表为空 - 订单号: {}", request.getOrderNo());
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 6. 生成支付流水号
        String paymentNo = OrderNoGenerator.generateOrderNo();

        // 7. 构建 Checkout Session 回调 URL
        String sessionSuccessUrl = successUrl + "?paymentNo=" + paymentNo;
        String sessionCancelUrl = cancelUrl.replace("{ORDER_NO}", request.getOrderNo());

        try {
            // 8. 为每个商品创建 line_item
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

            for (OrderItemDO item : orderItems) {
                // 处理商品图片 URL（拼接为完整公网 URL）
                String imageUrl = item.getProductImage();
                if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    imageUrl = stripeConfig.getProductImageBaseUrl() + imageUrl;
                }

                // 构建 product_data
                SessionCreateParams.LineItem.PriceData.ProductData.Builder productDataBuilder =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(item.getProductName())
                        .setDescription("商品编号: " + item.getProductId());

                // 如果有图片 URL，添加到 images
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    productDataBuilder.addImage(imageUrl);
                }

                // 创建 line_item
                lineItems.add(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity((long) item.getQuantity())
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("cny")
                                .setUnitAmount(item.getUnitPrice().multiply(new BigDecimal("100")).longValue())
                                .setProductData(productDataBuilder.build())
                                .build()
                        )
                        .build()
                );
            }

            // 9. 创建 Checkout Session (Adaptive Pricing 模式 + 自定义外观)
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(sessionSuccessUrl)
                .setCancelUrl(sessionCancelUrl)
                .addAllLineItem(lineItems)  // 批量添加所有商品

                // 元数据
                .putMetadata("order_no", request.getOrderNo())
                .putMetadata("payment_no", paymentNo)
                .putMetadata("user_id", userId.toString())

                // 会话过期时间（30 分钟）
                .setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES).getEpochSecond())

                // 语言本地化（简体中文）
                .setLocale(SessionCreateParams.Locale.ZH)

                // 启用账单地址收集
                .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)

                // 自定义提交按钮文字
                .setSubmitType(SessionCreateParams.SubmitType.PAY)

                // 自定义文本（条款说明等）
                .setCustomText(
                    SessionCreateParams.CustomText.builder()
                        .setSubmit(
                            SessionCreateParams.CustomText.Submit.builder()
                                .setMessage("完成支付后，我们将立即处理您的订单")
                                .build()
                        )
                        .build()
                )

                // 启用客户信息收集
                .setCustomerCreation(SessionCreateParams.CustomerCreation.IF_REQUIRED)

                // 启用电话号码收集
                .setPhoneNumberCollection(
                    SessionCreateParams.PhoneNumberCollection.builder()
                        .setEnabled(true)
                        .build()
                )

                // 启用支付方式（Adaptive Pricing 会自动根据地区显示更多合适的支付方式）
                // 注意：只添加基础的 CARD 类型，其他支付方式由 Stripe 根据用户地区自动启用
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)

                .build();

            Session session = Session.create(params);

            // 8. 创建支付记录
            PaymentDO payment = new PaymentDO();
            payment.setPaymentNo(paymentNo);
            payment.setOrderNo(request.getOrderNo());
            payment.setUserId(userId);
            payment.setAmount(order.getPayAmount());
            payment.setPaymentMethod(PaymentMethod.STRIPE.name());
            payment.setPaymentStatus(PaymentStatus.PENDING.name());
            payment.setTradeNo(session.getId());  // 存储 Session ID (cs_xxx)
            payment.setCodeUrl(session.getUrl());  // 存储 Checkout URL

            paymentMapper.insert(payment);

            log.info("创建 Stripe Checkout Session - 支付流水号: {}, 订单号: {}, 金额: {} CNY, Session ID: {}, 商品数量: {}",
                paymentNo, request.getOrderNo(), order.getPayAmount(), session.getId(), lineItems.size());

            // 9. 更新订单支付方式
            orderMapper.updatePaymentMethod(request.getOrderNo(), PaymentMethod.STRIPE.name());

            // 10. 返回支付信息
            return StripePaymentVO.builder()
                .paymentNo(paymentNo)
                .sessionUrl(session.getUrl())
                .sessionId(session.getId())
                .orderNo(request.getOrderNo())
                .amount(order.getPayAmount())
                .build();

        } catch (StripeException e) {
            log.error("创建 Stripe Checkout Session 失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.PAYMENT_FAILED);
        }
    }

    @Override
    public StripePaymentVO queryPayment(String paymentNo, Long userId) {
        // 1. 查询支付记录
        PaymentDO payment = paymentMapper.findByPaymentNo(paymentNo);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }

        // 2. 验证用户权限
        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 返回支付信息
        return StripePaymentVO.builder()
            .paymentNo(payment.getPaymentNo())
            .sessionUrl(payment.getCodeUrl())
            .sessionId(payment.getTradeNo())
            .orderNo(payment.getOrderNo())
            .amount(payment.getAmount())
            .status(payment.getPaymentStatus())
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWebhook(String payload, String signature) {
        Stripe.apiKey = stripeSecretKey;

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook 签名验证失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.PAYMENT_VERIFY_FAILED);
        }

        String eventType = event.getType();
        log.info("收到 Stripe Webhook 事件: {}", eventType);

        // 处理 Checkout Session 事件
        switch (eventType) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;

            case "checkout.session.expired":
                handleCheckoutSessionExpired(event);
                break;

            default:
                log.info("未处理的 Webhook 事件类型: {}", eventType);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StripeRefundVO createRefund(StripeRefundDTO request, Long userId) {
        Stripe.apiKey = stripeSecretKey;

        // 1. 查询支付记录
        PaymentDO payment = paymentMapper.findByPaymentNo(request.getPaymentNo());
        if (payment == null) {
            log.error("退款失败 - 支付记录不存在,支付流水号: {}", request.getPaymentNo());
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }

        // 2. 验证支付状态
        if (!PaymentStatus.SUCCESS.name().equals(payment.getPaymentStatus())) {
            log.error("退款失败 - 支付状态异常,支付流水号: {}, 支付状态: {}",
                request.getPaymentNo(), payment.getPaymentStatus());
            throw new BusinessException(ResultCode.INVALID_PARAMETER);
        }

        // 3. 验证退款金额
        if (request.getRefundAmount().compareTo(payment.getAmount()) > 0) {
            log.error("退款失败 - 退款金额超过支付金额,支付流水号: {}, 支付金额: {}, 退款金额: {}",
                request.getPaymentNo(), payment.getAmount(), request.getRefundAmount());
            throw new BusinessException(ResultCode.INVALID_PARAMETER);
        }

        // 4. 检查是否已退款（幂等性检查）
        RefundDO existingRefund = refundMapper.findByPaymentNo(request.getPaymentNo());
        if (existingRefund != null && "SUCCESS".equals(existingRefund.getRefundStatus())) {
            log.warn("支付已退款 - 返回已有退款记录，支付流水号: {}", request.getPaymentNo());
            // 幂等性：返回已有退款记录，而不是抛出异常
            return StripeRefundVO.builder()
                .refundNo(existingRefund.getRefundNo())
                .refundId(existingRefund.getTradeNo())
                .paymentNo(request.getPaymentNo())
                .refundAmount(existingRefund.getRefundAmount())
                .status("SUCCESS")
                .reason(existingRefund.getRefundReason())
                .build();
        }

        // 5. 检查支付记录是否已标记为已退款
        if (PaymentStatus.REFUNDED.name().equals(payment.getPaymentStatus())) {
            log.warn("支付记录已标记为已退款 - 支付流水号: {}", request.getPaymentNo());
            // 如果支付已退款但没有退款记录，返回现有信息
            if (existingRefund != null) {
                return StripeRefundVO.builder()
                    .refundNo(existingRefund.getRefundNo())
                    .refundId(existingRefund.getTradeNo())
                    .paymentNo(request.getPaymentNo())
                    .refundAmount(existingRefund.getRefundAmount())
                    .status(existingRefund.getRefundStatus())
                    .reason(existingRefund.getRefundReason())
                    .build();
            }
        }

        // 5. 生成退款流水号
        String refundNo = generateRefundNo();

        try {
            // 6. 从 Session 获取 PaymentIntent ID (关键改动)
            String sessionId = payment.getTradeNo();
            String paymentIntentId;

            if (sessionId != null && sessionId.startsWith("cs_")) {
                // Checkout Session 模式: 需要先查询 Session 获取 PaymentIntent ID
                Session session = Session.retrieve(sessionId);
                paymentIntentId = session.getPaymentIntent();
                if (paymentIntentId == null) {
                    log.error("无法从 Session 获取 PaymentIntent ID - Session ID: {}", sessionId);
                    throw new BusinessException(ResultCode.REFUND_FAILED);
                }
            } else {
                // 旧的 PaymentIntent 模式 (向后兼容)
                paymentIntentId = sessionId;
            }

            // 7. 调用 Stripe 退款 API
            long refundAmountStripe = request.getRefundAmount().multiply(new BigDecimal("100")).longValue();

            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(refundAmountStripe)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("refund_no", refundNo)
                .putMetadata("reason", request.getReason() != null ? request.getReason() : "用户申请退款")
                .build();

            Refund refund = Refund.create(params);

            // 8. 创建退款记录
            RefundDO refundDO = new RefundDO();
            refundDO.setRefundNo(refundNo);
            refundDO.setOrderNo(payment.getOrderNo());
            refundDO.setPaymentNo(request.getPaymentNo());
            refundDO.setTradeNo(refund.getId());
            refundDO.setRefundAmount(request.getRefundAmount());
            refundDO.setRefundReason(request.getReason() != null ? request.getReason() : "用户申请退款");
            refundDO.setRefundStatus("PROCESSING");

            refundMapper.insert(refundDO);

            log.info("创建 Stripe 退款 - 退款流水号: {}, 支付流水号: {}, 退款金额: {} CNY, Stripe Refund ID: {}",
                refundNo, request.getPaymentNo(), request.getRefundAmount(), refund.getId());

            // 9. 返回退款响应
            return StripeRefundVO.builder()
                .refundNo(refundNo)
                .refundId(refund.getId())
                .paymentNo(request.getPaymentNo())
                .refundAmount(request.getRefundAmount())
                .status("PROCESSING")
                .reason(request.getReason())
                .build();

        } catch (StripeException e) {
            // 处理 Stripe 特定错误
            if ("charge_already_refunded".equals(e.getCode())) {
                // 幂等性：Charge 已经退款，视为成功
                log.warn("Charge 已退款 - 更新退款记录为成功，支付流水号: {}, 错误: {}",
                        request.getPaymentNo(), e.getMessage());

                // 创建或更新退款记录为成功状态
                if (existingRefund != null) {
                    existingRefund.setRefundStatus("SUCCESS");
                    existingRefund.setRefundTime(LocalDateTime.now());
                    refundMapper.updateById(existingRefund);

                    return StripeRefundVO.builder()
                        .refundNo(existingRefund.getRefundNo())
                        .refundId(existingRefund.getTradeNo())
                        .paymentNo(request.getPaymentNo())
                        .refundAmount(existingRefund.getRefundAmount())
                        .status("SUCCESS")
                        .reason(existingRefund.getRefundReason())
                        .build();
                } else {
                    // 创建新的退款记录（SUCCESS 状态）
                    RefundDO refundDO = new RefundDO();
                    refundDO.setRefundNo(refundNo);
                    refundDO.setOrderNo(payment.getOrderNo());
                    refundDO.setPaymentNo(request.getPaymentNo());
                    refundDO.setTradeNo(payment.getTradeNo()); // 使用 Session ID
                    refundDO.setRefundAmount(request.getRefundAmount());
                    refundDO.setRefundReason(request.getReason() != null ? request.getReason() : "用户申请退款");
                    refundDO.setRefundStatus("SUCCESS");
                    refundDO.setRefundTime(LocalDateTime.now());

                    refundMapper.insert(refundDO);

                    // 更新支付状态为已退款
                    payment.setPaymentStatus(PaymentStatus.REFUNDED.name());
                    paymentMapper.updateById(payment);

                    return StripeRefundVO.builder()
                        .refundNo(refundNo)
                        .refundId(payment.getTradeNo())
                        .paymentNo(request.getPaymentNo())
                        .refundAmount(request.getRefundAmount())
                        .status("SUCCESS")
                        .reason(request.getReason())
                        .build();
                }
            }

            // 其他 Stripe 错误
            log.error("Stripe 退款失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.REFUND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundWebhook(String payload, String signature) {
        Stripe.apiKey = stripeSecretKey;

        Event event;
        try {
            // 验证 Webhook 签名
            event = Webhook.constructEvent(payload, signature, refundWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("退款 Webhook 签名验证失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.PAYMENT_VERIFY_FAILED);
        }

        // 处理退款事件
        String eventType = event.getType();
        log.info("收到 Stripe 退款 Webhook 事件: {}", eventType);

        if ("charge.refunded".equals(eventType)) {
            handleChargeRefunded(event);
        } else {
            log.info("未处理的退款 Webhook 事件类型: {}", eventType);
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 处理 Checkout Session 完成事件
     */
    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
            .getObject()
            .orElse(null);

        if (session == null) {
            log.error("无法解析 Checkout Session 对象");
            return;
        }

        String sessionId = session.getId();
        String orderNo = session.getMetadata().get("order_no");
        log.info("处理 Checkout Session 完成事件 - Session ID: {}, 订单号: {}", sessionId, orderNo);

        // 通过 orderNo 查询支付记录 (避免 tradeNo 查找失败)
        PaymentDO payment = paymentMapper.findByOrderNo(orderNo);
        if (payment == null) {
            log.error("支付记录不存在 - 订单号: {}", orderNo);
            return;
        }

        // 幂等性检查
        if (PaymentStatus.SUCCESS.name().equals(payment.getPaymentStatus())) {
            log.warn("支付已完成,忽略重复通知 - 支付流水号: {}", payment.getPaymentNo());
            return;
        }

        // 更新支付记录
        payment.setPaymentStatus(PaymentStatus.SUCCESS.name());
        payment.setNotifyTime(LocalDateTime.now());
        payment.setTradeNo(sessionId);  // 确保存储正确的 Session ID
        paymentMapper.updateById(payment);

        // 更新订单状态
        OrderDO order = orderMapper.findByOrderNo(orderNo);
        if (order != null && OrderStatus.UNPAID.getCode().equals(order.getStatus())) {
            orderMapper.updateStatus(orderNo, OrderStatus.PAID.getCode());
            orderMapper.updatePaymentTime(orderNo);
            log.info("订单支付成功 - 订单号: {}, 支付流水号: {}, Session ID: {}",
                orderNo, payment.getPaymentNo(), sessionId);
        }
    }

    /**
     * 处理 Checkout Session 过期事件
     */
    private void handleCheckoutSessionExpired(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
            .getObject()
            .orElse(null);

        if (session == null) {
            log.error("无法解析 Checkout Session 对象");
            return;
        }

        String sessionId = session.getId();
        String orderNo = session.getMetadata().get("order_no");
        log.info("处理 Checkout Session 过期事件 - Session ID: {}, 订单号: {}", sessionId, orderNo);

        // 查询支付记录
        PaymentDO payment = paymentMapper.findByOrderNo(orderNo);
        if (payment == null) {
            log.error("支付记录不存在 - 订单号: {}", orderNo);
            return;
        }

        // 仅当支付状态为 PENDING 时更新为 FAILED
        if (PaymentStatus.PENDING.name().equals(payment.getPaymentStatus())) {
            payment.setPaymentStatus(PaymentStatus.FAILED.name());
            payment.setNotifyTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
            log.info("支付 Session 已过期 - 订单号: {}, 支付流水号: {}", orderNo, payment.getPaymentNo());
        }
    }

    /**
     * 处理退款成功事件
     */
    private void handleChargeRefunded(Event event) {
        Charge charge = (Charge) event.getDataObjectDeserializer()
            .getObject()
            .orElse(null);

        if (charge == null) {
            log.error("无法解析 Charge 对象");
            return;
        }

        String chargeId = charge.getId();
        log.info("处理退款成功事件 - Charge ID: {}", chargeId);

        // 获取 PaymentIntent ID
        String paymentIntentId = charge.getPaymentIntent();
        if (paymentIntentId == null) {
            log.error("Charge 未关联 PaymentIntent - Charge ID: {}", chargeId);
            return;
        }

        // 查询支付记录
        PaymentDO payment = paymentMapper.findByTradeNo(paymentIntentId);
        if (payment == null) {
            log.error("支付记录不存在 - PaymentIntent ID: {}", paymentIntentId);
            return;
        }

        // 查询退款记录
        RefundDO refund = refundMapper.findByPaymentNo(payment.getPaymentNo());
        if (refund == null) {
            log.error("退款记录不存在 - 支付流水号: {}", payment.getPaymentNo());
            return;
        }

        // 更新退款记录
        refund.setRefundStatus("SUCCESS");
        refund.setRefundTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        // 更新支付记录
        payment.setPaymentStatus(PaymentStatus.REFUNDED.name());
        paymentMapper.updateById(payment);

        log.info("退款成功 - 订单号: {}, 退款流水号: {}, 支付流水号: {}",
            payment.getOrderNo(), refund.getRefundNo(), payment.getPaymentNo());
    }

    /**
     * 生成退款流水号
     */
    private String generateRefundNo() {
        return "RF" + System.currentTimeMillis() +
            String.format("%06d", (int) (Math.random() * 1000000));
    }
}

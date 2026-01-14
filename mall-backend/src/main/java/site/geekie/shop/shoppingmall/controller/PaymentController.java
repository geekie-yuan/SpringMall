package site.geekie.shop.shoppingmall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.PaymentNotifyRequest;
import site.geekie.shop.shoppingmall.dto.request.PaymentRequest;
import site.geekie.shop.shoppingmall.dto.response.PaymentResponse;
import site.geekie.shop.shoppingmall.service.PaymentService;

/**
 * 支付控制器
 * 提供支付相关的REST API
 *
 * 基础路径：/api/v1/payment
 */
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 发起支付（模拟）
     * POST /api/v1/payment/pay
     *
     * 需要USER角色权限
     *
     * @param request 支付请求（订单号）
     * @return 支付结果
     */
    @PostMapping("/pay")
    @PreAuthorize("hasRole('USER')")
    public Result<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.pay(request);
        return Result.success(response);
    }

    /**
     * 处理支付回调（模拟）
     * POST /api/v1/payment/notify
     *
     * 该接口为公开接口，模拟第三方支付平台的回调
     * 实际项目中需要验证签名等安全措施
     *
     * @param request 支付回调请求
     * @return 操作结果
     */
    @PostMapping("/notify")
    public Result<Void> handlePaymentNotify(@RequestBody PaymentNotifyRequest request) {
        paymentService.handlePaymentNotify(request);
        return Result.success();
    }
}

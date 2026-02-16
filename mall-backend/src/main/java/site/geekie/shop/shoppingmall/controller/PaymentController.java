package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.annotation.CurrentUserId;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.PaymentDTO;
import site.geekie.shop.shoppingmall.dto.PaymentNotifyDTO;
import site.geekie.shop.shoppingmall.dto.request.CreatePaymentRequest;
import site.geekie.shop.shoppingmall.vo.PaymentVO;
import site.geekie.shop.shoppingmall.service.PaymentService;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 * 提供支付相关的REST API
 *
 * 基础路径：/api/v1/payment
 */
@Tag(name = "Payment", description = "支付管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final site.geekie.shop.shoppingmall.config.AlipayConfig alipayConfig;

    /**
     * 发起支付（模拟）
     * POST /api/v1/payment/pay
     *
     * 需要USER角色权限
     *
     * @param request 支付请求（订单号）
     * @param requset 当前登录用户ID（自动注入）
     * @return 支付结果
     */
    @Operation(summary = "发起支付（模拟）")
    @PostMapping("/pay")
    @PreAuthorize("hasRole('USER')")
    public Result<PaymentVO> pay(@Valid @RequestBody PaymentDTO request,@Parameter(hidden = true) @CurrentUserId Long userId) {
        PaymentVO response = paymentService.pay(request, userId);
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
    @Operation(summary = "处理支付回调（模拟）")
    @PostMapping("/notify")
    public Result<Void> handlePaymentNotify(@RequestBody PaymentNotifyDTO request) {
        paymentService.handlePaymentNotify(request);
        return Result.success();
    }

    /**
     * 创建支付宝支付
     * POST /api/v1/payment/alipay/create
     *
     * 需要USER角色权限
     *
     * @param request 支付请求（订单号）
     * @param userId 当前登录用户ID（自动注入）
     * @return 支付信息（包含支付表单HTML）
     */
    @Operation(summary = "创建支付宝支付")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/alipay/create")
    @PreAuthorize("hasRole('USER')")
    public Result<PaymentVO> createAlipayPayment(@Valid @RequestBody CreatePaymentRequest request, @Parameter(hidden = true) @CurrentUserId Long userId) {
        PaymentVO payment = paymentService.createAlipayPayment(request.getOrderNo(), userId);
        return Result.success(payment);
    }

    /**
     * 支付宝异步通知
     * POST /api/v1/payment/alipay/notify
     *
     * 该接口为公开接口，接收支付宝的异步通知
     *
     * @param request HTTP请求对象
     * @return success/failure
     */
    @Operation(summary = "支付宝异步通知")
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        // 获取支付宝POST过来的所有参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        log.info("支付宝异步通知参数: {}", params);

        // 处理通知
        String result = paymentService.handleAlipayNotify(params);

        // 返回给支付宝的响应
        return result;
    }

    /**
     * 支付宝同步返回
     * GET /api/v1/payment/alipay/return
     *
     * 该接口为公开接口，支付宝支付完成后跳转回商户网站
     *
     * @param request HTTP请求对象
     * @return 重定向到前端结果页
     */
    @Operation(summary = "支付宝同步返回")
    @GetMapping("/alipay/return")
    public String alipayReturn(HttpServletRequest request) {
        // 获取支付宝GET过来的所有参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        String outTradeNo = params.get("out_trade_no");  // 支付流水号
        String tradeNo = params.get("trade_no");  // 支付宝交易号

        log.info("支付宝同步返回 - 支付流水号: {}, 交易号: {}", outTradeNo, tradeNo);

        // 重定向到前端结果页
        return "redirect:" + alipayConfig.getFrontendUrl() + "/payment/result?paymentNo=" + outTradeNo;
    }

    /**
     * 查询支付状态
     * GET /api/v1/payment/{paymentNo}
     *
     * 需要USER角色权限
     *
     * @param paymentNo 支付流水号
     * @return 支付信息
     */
    @Operation(summary = "查询支付状态")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{paymentNo}")
    @PreAuthorize("hasRole('USER')")
    public Result<PaymentVO> queryPayment(@PathVariable String paymentNo) {
        PaymentVO payment = paymentService.queryPayment(paymentNo);
        return Result.success(payment);
    }
}

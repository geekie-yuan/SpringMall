package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.annotation.CurrentUserId;
import site.geekie.shop.shoppingmall.annotation.RateLimiter;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.CreateAlipayPaymentDTO;
import site.geekie.shop.shoppingmall.service.AlipayPaymentService;
import site.geekie.shop.shoppingmall.vo.AlipayPaymentVO;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payment/alipay")
@RequiredArgsConstructor
@Tag(name = "支付宝支付", description = "支付宝支付相关接口")
public class AlipayPaymentController {

    private final AlipayPaymentService alipayPaymentService;

    @Value("${alipay.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/create")
    @Operation(summary = "创建支付宝支付", description = "创建支付宝支付，返回支付表单 HTML")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('USER')")
    @RateLimiter(count = 10, period = 60)
    public Result<AlipayPaymentVO> createPayment(
            @Valid @RequestBody CreateAlipayPaymentDTO request,
            @Parameter(hidden = true) @CurrentUserId Long userId) {
        AlipayPaymentVO payment = alipayPaymentService.createAlipay(request.getOrderNo(), userId);
        return Result.success("创建支付成功", payment);
    }

    @PostMapping("/notify")
    @Operation(summary = "支付宝异步通知", description = "处理支付宝异步通知回调")
    @RateLimiter(count = 10, period = 60, key = "alipay_notify")
    public String handleNotify(HttpServletRequest request) {
        // 必须在首次调用 getParameter() 之前设置编码，否则 body 等中文字段会被错误解析导致验签失败
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 是 JVM 强制支持的标准字符集，此分支实际上永远不会执行
            log.error("设置请求字符编码失败", e);
        }

        // 使用 getParameter() 逐个取值，正确处理 application/x-www-form-urlencoded 数据
        Map<String, String> params = new HashMap<>();
        for (String name : request.getParameterMap().keySet()) {
            params.put(name, request.getParameter(name));
        }

        return alipayPaymentService.handleNotify(params);
    }

    @GetMapping("/return")
    @Operation(summary = "支付宝同步返回", description = "支付宝支付完成后跳转回商户网站")
    public void handleReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String outTradeNo = request.getParameter("out_trade_no");  // 支付流水号（即本系统支付流水号）
        String tradeNo = request.getParameter("trade_no");  // 支付宝交易号

        log.info("支付宝同步返回 - 支付流水号: {}, 交易号: {}", outTradeNo, tradeNo);

        // 支付宝同步返回仅用于页面跳转，不在此处处理业务逻辑。
        // 若 out_trade_no 缺失（用户中途关闭页面），跳转到订单列表页。
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            log.warn("支付宝同步返回缺少 out_trade_no 参数，重定向到订单列表");
            response.sendRedirect(frontendUrl + "/orders");
            return;
        }

        // URL 编码参数值，防止特殊字符破坏查询字符串（Java 10+ 无需处理受检异常）
        String encodedPaymentNo = URLEncoder.encode(outTradeNo, StandardCharsets.UTF_8);
        response.sendRedirect(frontendUrl + "/payment/result?paymentNo=" + encodedPaymentNo);
    }

    @GetMapping("/{paymentNo}")
    @Operation(summary = "查询支付宝支付状态", description = "根据支付流水号查询支付状态")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('USER')")
    public Result<AlipayPaymentVO> queryPayment(
            @PathVariable String paymentNo,
            @Parameter(hidden = true) @CurrentUserId Long userId) {
        AlipayPaymentVO payment = alipayPaymentService.queryPayment(paymentNo, userId);
        return Result.success(payment);
    }
}

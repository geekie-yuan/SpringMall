package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.annotation.CurrentUserId;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.CreateWxPaymentDTO;
import site.geekie.shop.shoppingmall.dto.request.WxRefundDTO;
import site.geekie.shop.shoppingmall.service.WxPayService;
import site.geekie.shop.shoppingmall.vo.WxPaymentVO;
import site.geekie.shop.shoppingmall.vo.WxRefundVO;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付控制器
 * 提供微信Native支付和退款相关接口
 *
 * 注意：只有在 wxpay.enabled=true 时才会注册此 Controller
 */
@Slf4j
@Tag(name = "WeChat Pay", description = "微信支付相关接口")
@RestController
@RequestMapping("/api/v1/payment/wechat")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "wxpay", name = "enabled", havingValue = "true")
public class WxPayController {

    private final WxPayService wxPayService;

    /**
     * 创建微信Native支付订单
     * 生成二维码供用户扫码支付
     *
     * @param request 创建支付请求
     * @param userId 当前登录用户ID（自动注入）
     * @return 支付记录（包含二维码链接）
     */
    @Operation(summary = "创建微信Native支付订单", description = "为指定订单创建微信Native支付，返回二维码链接")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/native")
    public Result<WxPaymentVO> createNativePayment(
            @Valid @RequestBody CreateWxPaymentDTO request,
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        WxPaymentVO payment = wxPayService.createNativePayment(request, userId);
        return Result.success("支付订单创建成功", payment);
    }

    /**
     * 查询支付状态
     *
     * @param paymentNo 支付流水号
     * @param userId 当前登录用户ID（自动注入）
     * @return 支付记录
     */
    @Operation(summary = "查询支付状态", description = "根据支付流水号查询支付状态")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{paymentNo}")
    public Result<WxPaymentVO> queryPayment(
            @PathVariable String paymentNo,
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        WxPaymentVO payment = wxPayService.queryPayment(paymentNo, userId);
        return Result.success(payment);
    }

    /**
     * 微信支付回调通知接口
     * 由微信服务器调用，无需认证
     *
     * @param requestBody 原始请求体
     * @param serial 微信支付证书序列号
     * @param nonce 随机字符串
     * @param timestamp 时间戳
     * @param signature 签名
     * @return 处理结果
     */
    @Hidden
    @PostMapping("/notify")
    public Map<String, String> handlePaymentNotify(
            @RequestBody String requestBody,
            @RequestHeader("Wechatpay-Serial") String serial,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Signature") String signature) {
        log.info("收到微信支付回调通知");

        Map<String, String> headers = new HashMap<>();
        headers.put("Wechatpay-Serial", serial);
        headers.put("Wechatpay-Nonce", nonce);
        headers.put("Wechatpay-Timestamp", timestamp);
        headers.put("Wechatpay-Signature", signature);

        try {
            wxPayService.handlePaymentNotify(requestBody, headers);

            Map<String, String> response = new HashMap<>();
            response.put("code", "SUCCESS");
            response.put("message", "成功");
            return response;

        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);

            Map<String, String> response = new HashMap<>();
            response.put("code", "FAIL");
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 申请退款
     *
     * @param request 退款请求
     * @param userId 当前登录用户ID（自动注入）
     * @return 退款记录
     */
    @Operation(summary = "申请退款", description = "为已支付的订单申请退款")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/refund")
    public Result<WxRefundVO> createRefund(
            @Valid @RequestBody WxRefundDTO request,
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        WxRefundVO refund = wxPayService.createRefund(request, userId);
        return Result.success("退款申请已提交", refund);
    }

    /**
     * 微信退款回调通知接口
     * 由微信服务器调用，无需认证
     *
     * @param requestBody 原始请求体
     * @param serial 微信支付证书序列号
     * @param nonce 随机字符串
     * @param timestamp 时间戳
     * @param signature 签名
     * @return 处理结果
     */
    @Hidden
    @PostMapping("/refund/notify")
    public Map<String, String> handleRefundNotify(
            @RequestBody String requestBody,
            @RequestHeader("Wechatpay-Serial") String serial,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Signature") String signature) {
        log.info("收到微信退款回调通知");

        Map<String, String> headers = new HashMap<>();
        headers.put("Wechatpay-Serial", serial);
        headers.put("Wechatpay-Nonce", nonce);
        headers.put("Wechatpay-Timestamp", timestamp);
        headers.put("Wechatpay-Signature", signature);

        try {
            wxPayService.handleRefundNotify(requestBody, headers);

            Map<String, String> response = new HashMap<>();
            response.put("code", "SUCCESS");
            response.put("message", "成功");
            return response;

        } catch (Exception e) {
            log.error("处理微信退款回调失败", e);

            Map<String, String> response = new HashMap<>();
            response.put("code", "FAIL");
            response.put("message", e.getMessage());
            return response;
        }
    }
}

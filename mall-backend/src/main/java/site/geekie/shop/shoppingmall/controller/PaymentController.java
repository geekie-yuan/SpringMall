package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.annotation.CurrentUserId;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.service.PaymentService;
import site.geekie.shop.shoppingmall.vo.PaymentVO;

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

	@GetMapping("/{paymentNo}")
	@Operation(summary = "查询支付状态", description = "根据支付流水号查询支付状态（自动识别支付方式）")
	@SecurityRequirement(name = "Bearer Authentication")
	@PreAuthorize("hasRole('USER')")
	public Result<PaymentVO> queryPayment(
			@PathVariable String paymentNo,
			@Parameter(hidden = true) @CurrentUserId Long userId) {
		PaymentVO payment = paymentService.getPaymentByNo(paymentNo, userId);
		return Result.success(payment);
	}
}

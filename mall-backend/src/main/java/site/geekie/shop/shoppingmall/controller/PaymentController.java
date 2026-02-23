package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
}

package site.geekie.shop.shoppingmall.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe 支付配置
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "stripe")
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true", matchIfMissing = false)
public class StripeConfig {

    /**
     * 是否启用 Stripe 支付
     */
    private boolean enabled;

    /**
     * Stripe Secret Key (用于服务端 API 调用)
     */
    private String secretKey;

    /**
     * Webhook 签名密钥 (用于验证 Webhook 回调)
     */
    private String webhookSecret;

    /**
     * 退款 Webhook 签名密钥
     */
    private String refundWebhookSecret;

    /**
     * Checkout Session 成功返回地址
     */
    private String successUrl;

    /**
     * Checkout Session 取消返回地址（支持 {ORDER_NO} 占位符）
     */
    private String cancelUrl;

    /**
     * 是否为测试模式
     */
    private boolean testMode = true;

    /**
     * 商品图片 URL 基础路径（用于拼接相对路径为完整 URL）
     */
    private String productImageBaseUrl;

    /**
     * 初始化 Stripe SDK
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = this.secretKey;
        log.info("Stripe SDK 初始化成功 - 模式: {}", testMode ? "测试" : "生产");
    }
}

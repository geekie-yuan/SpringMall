package site.geekie.shop.shoppingmall.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 微信支付配置类
 * 使用官方 wechatpay-java SDK
 *
 * 注意：微信支付没有沙箱环境，必须使用真实商户信息
 * 通过 wxpay.enabled=true 启用此模块
 */
@Slf4j
@Data
@RequiredArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "wxpay")
@ConditionalOnProperty(prefix = "wxpay", name = "enabled", havingValue = "true")
public class WxPayConfig {

    private final ResourceLoader resourceLoader;

    /**
     * 应用ID（公众号或小程序的AppID）
     */
    private String appId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * APIv3 密钥
     */
    private String apiV3Key;

    /**
     * 商户证书序列号
     */
    private String serialNo;

    /**
     * 商户私钥字符串（PEM格式）
     */
    private String privateKeyString;

    /**
     * 商户私钥文件路径（可选，优先使用此配置）
     */
    private String privateKeyPath;

    /**
     * 支付回调通知URL
     */
    private String notifyUrl;

    /**
     * 退款回调通知URL
     */
    private String refundNotifyUrl;

    /**
     * 配置启动时检查
     */
    private void validateConfig() {
        if (appId == null || appId.isEmpty()) {
            throw new IllegalStateException("微信支付配置未正确设置：appId 不能为空");
        }
        if (mchId == null || mchId.isEmpty()) {
            throw new IllegalStateException("微信支付配置未正确设置：mchId 不能为空");
        }
        if (apiV3Key == null || apiV3Key.isEmpty()) {
            throw new IllegalStateException("微信支付配置未正确设置：apiV3Key 不能为空");
        }
        if (serialNo == null || serialNo.isEmpty()) {
            throw new IllegalStateException("微信支付配置未正确设置：serialNo 不能为空");
        }
        // 私钥路径和私钥字符串至少要有一个
        if ((privateKeyPath == null || privateKeyPath.isEmpty()) &&
            (privateKeyString == null || privateKeyString.isEmpty())) {
            throw new IllegalStateException("微信支付配置未正确设置：privateKeyPath 和 privateKeyString 至少要配置一个");
        }
    }

    /**
     * 微信支付配置（全局单例，自动更新证书）
     */
    @Bean
    public Config rsaAutoCertificateConfig() {
        validateConfig();

        RSAAutoCertificateConfig.Builder builder = new RSAAutoCertificateConfig.Builder()
            .merchantId(mchId)
            .merchantSerialNumber(serialNo)
            .apiV3Key(apiV3Key);

        // 优先使用私钥文件路径，其次使用私钥字符串
        if (privateKeyPath != null && !privateKeyPath.isEmpty()) {
            try {
                // 检查是否是 classpath 资源
                if (privateKeyPath.startsWith("classpath:")) {
                    log.info("从 classpath 加载私钥: {}", privateKeyPath);
                    Resource resource = resourceLoader.getResource(privateKeyPath);
                    String privateKeyContent = resource.getContentAsString(StandardCharsets.UTF_8);
                    builder.privateKey(privateKeyContent);
                } else {
                    log.info("从文件系统加载私钥: {}", privateKeyPath);
                    builder.privateKeyFromPath(privateKeyPath);
                }
            } catch (IOException e) {
                throw new IllegalStateException("无法读取私钥文件: " + privateKeyPath, e);
            }
        } else {
            log.info("使用私钥字符串配置");
            // 处理私钥字符串：将字符串字面量 "\n" 替换为实际的换行符
            String processedPrivateKey = privateKeyString.replace("\\n", "\n");
            builder.privateKey(processedPrivateKey);
        }

        return builder.build();
    }

    /**
     * Native支付服务
     */
    @Bean
    public NativePayService nativePayService(Config config) {
        return new NativePayService.Builder()
            .config(config)
            .build();
    }

    /**
     * 退款服务
     */
    @Bean
    public RefundService refundService(Config config) {
        return new RefundService.Builder()
            .config(config)
            .build();
    }

    /**
     * 回调通知解析器
     */
    @Bean
    public NotificationParser notificationParser(Config config) {
        return new NotificationParser((NotificationConfig) config);
    }
}

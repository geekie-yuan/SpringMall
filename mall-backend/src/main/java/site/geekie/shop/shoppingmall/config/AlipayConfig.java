package site.geekie.shop.shoppingmall.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 */
@Slf4j
@Getter
@Configuration
public class AlipayConfig {

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.private-key}")
    private String privateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.server-url}")
    private String serverUrl;

    @Value("${alipay.format:json}")
    private String format;

    @Value("${alipay.charset:UTF-8}")
    private String charset;

    @Value("${alipay.sign-type:RSA2}")
    private String signType;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Value("${alipay.frontend-url}")
    private String frontendUrl;

    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;

    /**
     * 创建支付宝客户端
     * <p>
     * 使用 AlipayConfig 构造器 + 自定义 TLSv1.2 HTTP 客户端，
     * 解决 Java 21 默认 TLSv1.3 与支付宝沙箱不兼容导致的 SSLHandshakeException。
     */
    @Bean
    public AlipayClient alipayClient() throws AlipayApiException {
        com.alipay.api.AlipayConfig config = new com.alipay.api.AlipayConfig();
        config.setServerUrl(serverUrl);
        config.setAppId(appId);
        config.setPrivateKey(privateKey);
        config.setFormat(format);
        config.setCharset(charset);
        config.setAlipayPublicKey(alipayPublicKey);
        config.setSignType(signType);
        config.setConnectTimeout(CONNECT_TIMEOUT_MS);
        config.setReadTimeout(READ_TIMEOUT_MS);
        config.setCustomizedHttpClient(new Tls12AlipayHttpClient(CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS));

        AlipayClient alipayClient = new DefaultAlipayClient(config);
        log.info("支付宝客户端初始化成功（TLSv1.2, connectTimeout={}ms, readTimeout={}ms）",
                CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
        return alipayClient;
    }
}

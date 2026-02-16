package site.geekie.shop.shoppingmall.config;

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

    /**
     * 创建支付宝客户端
     */
    @Bean
    public AlipayClient alipayClient() {
        AlipayClient alipayClient = new DefaultAlipayClient(
            serverUrl,
            appId,
            privateKey,
            format,
            charset,
            alipayPublicKey,
            signType
        );
        log.info("支付宝客户端初始化成功");
        return alipayClient;
    }
}

package site.geekie.shop.shoppingmall.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 配置类
 * 强制 Lettuce 使用 RESP2 协议，解决 Redis 8.x 下 HELLO 命令认证顺序问题
 */
@Configuration
public class RedisConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return builder -> builder.clientOptions(
                ClientOptions.builder()
                        .protocolVersion(ProtocolVersion.RESP2)
                        .build()
        );
    }
}

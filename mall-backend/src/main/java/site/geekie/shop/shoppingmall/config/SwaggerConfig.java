package site.geekie.shop.shoppingmall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI配置类
 * 配置API文档生成和展示
 *
 * 功能：
 *   - 配置API文档基本信息（标题、版本、描述）
 *   - 配置联系人和许可证信息
 *   - 配置JWT Bearer Token认证方式
 *   - 生成交互式API文档界面
 *
 * 访问地址：
 *   - Swagger UI: /swagger-ui.html
 *   - API Docs: /v3/api-docs
 *
 */
@Configuration
public class SwaggerConfig {

    /**
     * 自定义OpenAPI配置
     * 配置API文档的基本信息和安全认证方式
     *
     * 配置内容：
     *   - API标题：Shopping Mall API
     *   - API版本：1.0.0
     *   - 安全认证：Bearer Token (JWT)
     *   - 许可证：Apache 2.0
 *
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API基本信息
                .info(new Info()
                        .title("Shopping Mall API")
                        .version("1.0.0")
                        .description("Spring Boot Mall Backend API Documentation")
                        .contact(new Contact()
                                .name("Yuan")
                                .email("yuan.sn@outlook.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                // 全局安全要求：使用Bearer认证
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                // 安全方案定义：JWT Bearer Token
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }
}

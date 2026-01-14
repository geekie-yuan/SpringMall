package site.geekie.shop.shoppingmall.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import site.geekie.shop.shoppingmall.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security安全配置类
 * 配置应用的安全策略和认证授权机制
 *
 * 核心配置：
 *   - 无状态会话管理（JWT）
 *   - 禁用CSRF保护（REST API无需）
 *   - 配置URL访问权限规则
 *   - JWT认证过滤器集成
 *   - BCrypt密码加密
 *
 * 权限规则：
 *   - 公开访问：/api/v1/auth/**, /api/v1/health, Swagger相关路径
 *   - USER角色：/api/v1/user/**
 *   - ADMIN角色：/api/v1/admin/**
 *   - 其他路径：需要认证
 *
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT认证过滤器，用于验证每个请求的JWT令牌
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 用户详情服务，用于加载用户认证信息
    private final UserDetailsService userDetailsService;

    /**
     * 配置安全过滤器链
     * 定义HTTP安全策略，包括认证、授权和过滤器配置
     *
     * 安全配置：
     *   - 禁用CSRF：REST API使用无状态JWT，无需CSRF保护
     *   - 无状态会话：使用JWT，不创建HTTP Session
     *   - URL权限控制：配置哪些路径公开访问，哪些需要特定角色
     *   - JWT过滤器：在标准认证过滤器之前添加JWT验证
 *
     * 访问权限规则：
     *   - 完全公开：认证接口、健康检查、API文档
     *   - USER角色：用户管理接口
     *   - ADMIN角色：管理员接口
     *   - 默认：其他所有接口需要认证
 *
     * @param http HTTP安全配置对象
     * @return 配置完成的安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 启用CORS跨域支持
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 禁用CSRF（跨站请求伪造）保护
                .csrf(AbstractHttpConfigurer::disable)
                // 配置无状态会话管理（使用JWT，不创建Session）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 配置URL授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开访问路径（无需认证）
                        .requestMatchers(
                                "/api/v1/auth/**",      // 认证接口
                                "/api/v1/health",       // 健康检查
                                "/api/v1/categories/**",// 分类接口（查询公开，管理需ADMIN）
                                "/api/v1/products/**",  // 商品接口（查询公开，管理需ADMIN）
                                "/api/v1/payment/notify", // 支付回调接口（模拟第三方回调）
                                "/api-docs/**",         // API文档
                                "/swagger-ui/**",       // Swagger UI
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // USER角色访问路径
                        .requestMatchers("/api/v1/user/**").hasRole("USER")
                        // ADMIN角色访问路径
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // 设置认证提供者
                .authenticationProvider(authenticationProvider())
                // 在UsernamePasswordAuthenticationFilter之前添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置认证提供者
     * 使用DaoAuthenticationProvider进行用户认证
     *
     * 配置内容：
     *   - 用户详情服务：从数据库加载用户信息
     *   - 密码编码器：使用BCrypt验证密码
 *
     * @return 配置好的认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 配置认证管理器
     * Spring Security的核心认证接口
     *
     * 用途：
     *   - 在登录时调用authenticate()方法验证用户凭证
     *   - 整合所有配置的认证提供者
 *
     * @param config 认证配置
     * @return 认证管理器实例
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置密码编码器
     * 使用BCrypt算法加密和验证密码
     *
     * 特点：
     *   - 自动加盐
     *   - 单向加密
     *   - 计算成本可配置
     *   - 每次加密结果不同（彩虹表攻击无效）
 *
     * @return BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 CORS 跨域资源共享
     * 允许前端应用（localhost:3000）访问后端 API
     *
     * 配置内容：
     *   - 允许的源：http://localhost:3000（前端开发服务器）
     *   - 允许的方法：GET, POST, PUT, DELETE, OPTIONS
     *   - 允许的请求头：所有（包括 Authorization、Content-Type 等）
     *   - 允许携带凭证：true（允许发送 Cookie 和 Authorization header）
     *   - 预检请求缓存时间：3600秒（1小时）
     *
     * @return CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源（所有源）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的请求头（* 表示允许所有）
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 允许携带认证信息（如 Cookie、Authorization header）
        configuration.setAllowCredentials(true);

        // 预检请求的有效期（单位：秒）
        configuration.setMaxAge(3600L);

        // 注册 CORS 配置到所有 /api/** 路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}

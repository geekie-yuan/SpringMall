package site.geekie.shop.shoppingmall.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.Collections;
import java.util.Date;


import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // 1. 定义测试用的配置常量
    // 注意：HMAC-SHA 算法要求密钥长度至少 256位（32个字符），否则报错
    private final String TEST_SECRET = "TestSecretKeyForJwtTokenGenerationMustBeLongEnoughToWork!@#";
    private final Long TEST_EXPIRATION = 3600000L; // 1小时 (毫秒)

    @BeforeEach
    void setUp() {
        // 2. 手动实例化对象（不启动 Spring 容器，速度极快）
        jwtTokenProvider = new JwtTokenProvider();

        // 3. 使用反射工具注入 @Value 字段
        // 因为我们没有启动 Spring 上下文，所以必须手动把值塞进去
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("测试 generateToken 是否能调用 createToken 并生成有效 Token")
    void testGenerateToken_Success() {
        // --- 准备阶段 (Arrange) ---
        // 模拟一个 UserDetails 对象
        UserDetails userDetails = new User("adminTest", "test123456", Collections.emptyList());

        // --- 执行阶段 (Act) ---
        // 调用公有方法，它内部会去调用 private createToken
        String token = jwtTokenProvider.generateToken(userDetails);

        // --- 验证阶段 (Assert) ---

        // 1. 验证 Token 字符串不为空
        assertNotNull(token);
        assertFalse(token.isEmpty());
        System.out.println("Generated Token: " + token);

        // 2. 【核心验证】验证 createToken 的逻辑是否正确
        // 我们利用该类提供的解析方法，反向解密 Token，看里面的数据对不对

        // 验证用户名是否一致
        String actualUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("adminTest", actualUsername, "Token中的用户名应与输入一致");

        // 3. 验证过期时间是否被正确设置
        // 现在的逻辑是：过期时间 = 当前时间 + 1小时
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);
        Date now = new Date();

        // 过期时间应该在当前时间之后
        assertTrue(expirationDate.after(now), "Token 应该是未过期的");

        // 验证 validateToken 方法
        assertTrue(jwtTokenProvider.validateToken(token, userDetails), "Token 验证应当通过");
    }
}

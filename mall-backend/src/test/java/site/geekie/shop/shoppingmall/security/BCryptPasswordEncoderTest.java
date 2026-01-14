package site.geekie.shop.shoppingmall.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt 密码加密测试
 * 用于获取密码的 BCrypt 加密值
 */
@SpringBootTest
public class BCryptPasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取密码的 BCrypt 加密值
     * 直接修改 rawPassword 变量的值即可
     */
    @Test
    public void encodePassword() {
        // 在这里填充要加密的密码
        String rawPassword = "123456";

        // 使用与注册服务相同的 passwordEncoder 进行加密
        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密结果: " + encodedPassword);
    }
}

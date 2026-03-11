package site.geekie.shop.shoppingmall.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class JwtTokenTest {
    private final String SECRET = "dGhpc2lzYXJhbmRvbWtleWZvcmhtYWMyNTZhbGdvcml0aG0xMjM0NTY3ODkw";
    private final Long EXPIRATION = 31536000000L; // 1年

    @Test
    void generateTokens() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION);

        // —— 普通用户：修改以下信息匹配数据库 ——
        Map<String, Object> userClaims = new HashMap<>();
        userClaims.put("username", "user");
        userClaims.put("email", "yuan.sn@outllok.com");
        userClaims.put("createdAt", LocalDateTime.parse("2026-01-12T15:22:05")
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());

        String userToken = Jwts.builder()
                .claims(userClaims)
                .subject("3") // userId
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();

        // —— 管理员：修改以下信息匹配数据库 ——
        Map<String, Object> adminClaims = new HashMap<>();
        adminClaims.put("username", "admin");
        adminClaims.put("email", "admin@example.com");
        adminClaims.put("createdAt", LocalDateTime.of(2025, 5, 20, 14, 30, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        String adminToken = Jwts.builder()
                .claims(adminClaims)
                .subject("2") // userId
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();

        System.out.println("User: " + userToken);
        System.out.println("Admin: " + adminToken);
        System.out.println("过期时间: " + expiry);
    }
}

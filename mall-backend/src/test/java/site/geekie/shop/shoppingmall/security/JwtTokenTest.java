package site.geekie.shop.shoppingmall.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

class JwtTokenTest {
    private final String SECRET = "dGhpc2lzYXJhbmRvbWtleWZvcmhtYWMyNTZhbGdvcml0aG0xMjM0NTY3ODkw";
    private final Long EXPIRATION = 31536000000L; // 1年

    @Test
    void generateTokens() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION);

        String userToken = Jwts.builder()
                .subject("user")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();

        String adminToken = Jwts.builder()
                .subject("admin")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();

        System.out.println("User: " + userToken);
        System.out.println("Admin: " + adminToken);
        System.out.println("过期时间: " + expiry);
    }
}

package site.geekie.shop.shoppingmall.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 提供者
 * 负责JWT Token的生成、解析和验证
 *
 * 主要功能：
 *   - 生成JWT Token
 *   - 从Token中提取用户名
 *   - 从Token中提取过期时间
 *   - 验证Token的有效性
 *
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // JWT密钥（从配置文件读取）
    @Value("${jwt.secret}")
    private String secret;

    // Token过期时间（毫秒，从配置文件读取）
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取签名密钥
     *
     * @return HMAC SHA密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     *
     * @param userDetails 用户详情
     * @return JWT Token字符串
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 创建Token
     *
     * @param claims 自定义声明
     * @param subject 主题（用户名）
     * @return JWT Token字符串
     */

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中提取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从Token中提取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 从Token中提取所有声明
     *
     * @param token JWT Token
     * @return 声明对象
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @param userDetails 用户详情
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查Token是否已过期
     *
     * @param token JWT Token
     * @return true-已过期，false-未过期
     */
    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}

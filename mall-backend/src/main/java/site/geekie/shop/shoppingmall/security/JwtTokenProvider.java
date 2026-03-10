package site.geekie.shop.shoppingmall.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.geekie.shop.shoppingmall.entity.UserDO;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 提供者
 * 负责JWT Token的生成、解析和验证
 *
 * Token 载荷包含：
 *   - subject: 用户ID（唯一、不可变、数据库自增保证原子性）
 *   - username: 用户名（可变，仅作信息携带）
 *   - email: 邮箱（可变，仅作信息携带）
 *   - createdAt: 用户创建时间（不可变，用于辅助验证）
 *
 * 验证策略（性能优先）：
 *   - 仅校验 id + createdAt，不查询数据库中的 username/email
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     * claims 中写入 id + username + email + createdAt
     *
     * @param user 用户实体（必须包含 id、username、email、createdAt）
     * @return JWT Token字符串
     */
    public String generateToken(UserDO user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("createdAt", user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaimsFromToken(token).getSubject());
    }

    /**
     * 从Token中提取用户创建时间（毫秒时间戳）
     *
     * @param token JWT Token
     * @return 创建时间毫秒数
     */
    public Long getCreatedAtFromToken(String token) {
        return getClaimsFromToken(token).get("createdAt", Long.class);
    }

    /**
     * 从Token中提取用户名（仅用于信息展示，不用于验证）
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).get("username", String.class);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 从Token中提取签发时间（毫秒时间戳）
     *
     * @param token JWT Token
     * @return 签发时间毫秒数
     */
    public long getIssuedAtFromToken(String token) {
        return getClaimsFromToken(token).getIssuedAt().getTime();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证Token是否有效
     * 性能优先策略：仅校验 id + createdAt，不比对可变字段
     *
     * @param token JWT Token
     * @param user 数据库中查出的用户实体
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token, UserDO user) {
        try {
            Long tokenUserId = getUserIdFromToken(token);
            Long tokenCreatedAt = getCreatedAtFromToken(token);

            // 校验 id 一致
            if (!tokenUserId.equals(user.getId())) {
                return false;
            }

            // 校验 createdAt 一致（防止伪造 token 中的 id）
            long dbCreatedAt = user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (!tokenCreatedAt.equals(dbCreatedAt)) {
                return false;
            }

            // 校验未过期
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}

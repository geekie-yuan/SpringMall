package site.geekie.shop.shoppingmall.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务
 *
 * Redis Key 设计：
 * - 单 token 黑名单：auth:blacklist:{tokenHash}，TTL = token 剩余有效期
 * - 用户级强制登出：auth:force-logout:{userId}，TTL = 24h
 *
 * tokenHash 使用 SHA-256 前 16 位 hex（节省内存，碰撞率极低）。
 *
 * 所有方法不对外抛出异常，由调用方降级处理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String FORCE_LOGOUT_PREFIX = "auth:force-logout:";
    private static final long FORCE_LOGOUT_TTL_HOURS = 24;

    /**
     * 将单个 token 加入黑名单
     * TTL = token 剩余有效期（已过期则不写入）
     *
     * @param token     JWT token 字符串
     * @param expiresAt token 过期时间
     */
    public void blacklist(String token, Date expiresAt) {
        long remainingMs = expiresAt.getTime() - System.currentTimeMillis();
        if (remainingMs <= 0) {
            log.debug("token 已过期，无需加入黑名单");
            return;
        }
        String hash = hashToken(token);
        try {
            stringRedisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + hash,
                    "1",
                    remainingMs,
                    TimeUnit.MILLISECONDS
            );
            log.debug("token 已加入黑名单 - hash: {}", hash);
        } catch (Exception e) {
            log.warn("写入 token 黑名单异常 - hash: {}", hash, e);
        }
    }

    /**
     * 检查 token 是否在黑名单中
     *
     * @param token JWT token 字符串
     * @return true 表示已被列入黑名单（应拒绝访问）
     */
    public boolean isBlacklisted(String token) {
        String hash = hashToken(token);
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + hash));
        } catch (Exception e) {
            log.warn("查询 token 黑名单异常 - hash: {}，降级为放行", hash, e);
            return false;
        }
    }

    /**
     * 写入用户级强制登出标记（管理员禁用/改角色时调用）
     * 标记值为当前时间戳（毫秒），用于与 token.issuedAt 比对。
     * 所有在此时间之前签发的 token 将被视为无效。
     *
     * @param userId 用户ID
     */
    public void forceLogoutUser(Long userId) {
        String key = FORCE_LOGOUT_PREFIX + userId;
        try {
            stringRedisTemplate.opsForValue().set(
                    key,
                    String.valueOf(System.currentTimeMillis()),
                    FORCE_LOGOUT_TTL_HOURS,
                    TimeUnit.HOURS
            );
            log.info("写入用户强制登出标记 - userId: {}", userId);
        } catch (Exception e) {
            log.warn("写入用户强制登出标记异常 - userId: {}", userId, e);
        }
    }

    /**
     * 清除用户级强制登出标记（管理员启用用户时调用）
     *
     * @param userId 用户ID
     */
    public void clearForceLogout(Long userId) {
        try {
            stringRedisTemplate.delete(FORCE_LOGOUT_PREFIX + userId);
            log.debug("清除用户强制登出标记 - userId: {}", userId);
        } catch (Exception e) {
            log.warn("清除用户强制登出标记异常 - userId: {}", userId, e);
        }
    }

    /**
     * 检查用户是否被强制登出
     * 若 force-logout 标记存在，且 token 的签发时间早于标记时间，则认为 token 已失效。
     *
     * @param userId         用户ID
     * @param tokenIssuedAt  token 签发时间（毫秒时间戳）
     * @return true 表示该 token 已被强制失效（应拒绝访问）
     */
    public boolean isForceLoggedOut(Long userId, long tokenIssuedAt) {
        String key = FORCE_LOGOUT_PREFIX + userId;
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) {
                return false;
            }
            long forceLogoutAt = Long.parseLong(value);
            // token 签发时间早于强制登出时间，认为已失效
            return tokenIssuedAt < forceLogoutAt;
        } catch (Exception e) {
            log.warn("查询用户强制登出标记异常 - userId: {}，降级为放行", userId, e);
            return false;
        }
    }

    /**
     * 计算 token 的 SHA-256 哈希，取前 16 位 hex 字符串
     * 节省 Redis 存储空间，碰撞率极低（2^64 空间）。
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            // 取前 8 字节（16 位 hex）
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", hashBytes[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 Java 标准算法，不可能抛出此异常
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}

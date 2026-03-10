package site.geekie.shop.shoppingmall.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import site.geekie.shop.shoppingmall.dto.UserAuthCache;
import site.geekie.shop.shoppingmall.entity.UserDO;

import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * 用户认证缓存服务
 *
 * Key 格式：auth:user:{userId}
 * TTL：30 分钟
 *
 * 用途：在 JwtAuthenticationFilter 中替代每次请求查询数据库，
 * 提升认证性能。缓存内容不含 password，安全且轻量。
 *
 * 所有方法不对外抛出异常，由调用方 catch 后降级处理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "auth:user:";
    private static final long TTL_MINUTES = 30;

    /**
     * 从缓存获取用户认证信息
     * 缓存未命中或反序列化失败均返回 null，由调用方回源查 DB。
     *
     * @param userId 用户ID
     * @return 认证缓存对象；未命中返回 null
     */
    public UserAuthCache getUser(Long userId) {
        String key = KEY_PREFIX + userId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, UserAuthCache.class);
        } catch (Exception e) {
            log.warn("反序列化用户认证缓存异常 - userId: {}", userId, e);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    /**
     * 将用户信息写入认证缓存
     *
     * @param user 用户实体（不能为 null）
     */
    public void putUser(UserDO user) {
        UserAuthCache cache = buildCache(user);
        String key = KEY_PREFIX + user.getId();
        try {
            String json = objectMapper.writeValueAsString(cache);
            stringRedisTemplate.opsForValue().set(key, json, TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("写入用户认证缓存成功 - userId: {}", user.getId());
        } catch (Exception e) {
            log.warn("写入用户认证缓存异常 - userId: {}", user.getId(), e);
        }
    }

    /**
     * 清除用户认证缓存（用户信息变更时调用）
     *
     * @param userId 用户ID
     */
    public void evictUser(Long userId) {
        stringRedisTemplate.delete(KEY_PREFIX + userId);
        log.debug("清除用户认证缓存 - userId: {}", userId);
    }

    /**
     * 将 UserDO 转换为 UserAuthCache
     */
    private UserAuthCache buildCache(UserDO user) {
        UserAuthCache cache = new UserAuthCache();
        cache.setId(user.getId());
        cache.setUsername(user.getUsername());
        cache.setEmail(user.getEmail());
        cache.setRole(user.getRole());
        cache.setStatus(user.getStatus());
        cache.setCreatedAtMillis(
                user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
        return cache;
    }
}

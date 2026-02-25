package site.geekie.shop.shoppingmall.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的分布式锁
 * 使用 SETNX + 超时 + Lua 脚本原子释放的方式保证锁的安全性
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLock {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Lua 脚本：原子性地比较 value 并删除 key，防止 get + delete 之间的竞态条件
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "  return redis.call('del', KEYS[1]) " +
                "else " +
                "  return 0 " +
                "end"
        );
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    /**
     * 尝试获取分布式锁
     *
     * @param key     锁的 Key
     * @param timeout 超时时长
     * @param unit    时间单位
     * @return 成功返回锁标识值（UUID），失败返回 null
     */
    public String tryLock(String key, long timeout, TimeUnit unit) {
        String value = UUID.randomUUID().toString();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, value, timeout, unit);
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取分布式锁成功 - key: {}", key);
            return value;
        }
        log.debug("获取分布式锁失败 - key: {}", key);
        return null;
    }

    /**
     * 释放分布式锁
     * 使用 Lua 脚本原子性地校验 value 并删除 key，防止误删他人的锁
     *
     * @param key   锁的 Key
     * @param value 获取锁时返回的锁标识值
     */
    public void unlock(String key, String value) {
        if (value == null) {
            log.warn("锁标识值为 null，跳过释放 - key: {}", key);
            return;
        }
        try {
            Long result = stringRedisTemplate.execute(
                    UNLOCK_SCRIPT,
                    Collections.singletonList(key),
                    value
            );
            if (Long.valueOf(1L).equals(result)) {
                log.debug("释放分布式锁成功 - key: {}", key);
            } else {
                log.warn("锁 value 不匹配，锁可能已超时或被其他线程持有 - key: {}", key);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常 - key: {}", key, e);
        }
    }
}

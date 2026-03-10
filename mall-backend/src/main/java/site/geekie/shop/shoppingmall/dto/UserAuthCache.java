package site.geekie.shop.shoppingmall.dto;

import lombok.Data;

/**
 * 用户认证缓存对象
 *
 * 存储在 Redis 中，Key: auth:user:{userId}，TTL: 30 分钟。
 * 不含 password 字段，比 UserDO 更安全更轻量。
 * 用于 JwtAuthenticationFilter 替代每次请求都查询数据库。
 */
@Data
public class UserAuthCache {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 用户角色（USER/ADMIN） */
    private String role;

    /** 账户状态（1-正常，0-禁用） */
    private Integer status;

    /** 用户创建时间（毫秒时间戳），用于验证 token 中的 createdAt 字段 */
    private Long createdAtMillis;
}

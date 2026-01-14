package site.geekie.shop.shoppingmall.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：user
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    // 用户ID（主键）
    private Long id;

    // 用户名（唯一）
    private String username;

    // 密码（BCrypt加密）
    private String password;

    // 邮箱（唯一）
    private String email;

    // 手机号（唯一，可选）
    private String phone;

    // 头像URL
    private String avatar;

    // 用户角色（USER/ADMIN）
    private String role;

    // 账户状态（1-正常，0-禁用）
    private Integer status;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;
}

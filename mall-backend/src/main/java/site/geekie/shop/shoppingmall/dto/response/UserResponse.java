package site.geekie.shop.shoppingmall.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 * 返回给客户端的用户信息（不包含密码等敏感信息）
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    // 用户ID
    private Long id;

    // 用户名
    private String username;

    // 邮箱
    private String email;

    // 手机号
    private String phone;

    // 头像URL
    private String avatar;

    // 用户角色（USER/ADMIN）
    private String role;

    // 账户状态（1-正常，0-禁用）
    private Integer status;

    // 创建时间
    private LocalDateTime createdAt;
}

package site.geekie.shop.shoppingmall.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * 包含JWT Token和用户信息
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    // JWT访问令牌
    private String token;

    // Token类型（固定为Bearer）
    private String tokenType = "Bearer";

    // 用户信息
    private UserResponse user;

    /**
     * 构造登录响应（自动设置tokenType为Bearer）
     *
     * @param token JWT令牌
     * @param user 用户信息
     */
    public LoginResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }
}

package site.geekie.shop.shoppingmall.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 * 包含登录所需的用户名和密码
 *
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     * 验证规则：不能为空
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     * 验证规则：不能为空
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}

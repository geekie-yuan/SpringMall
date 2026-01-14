package site.geekie.shop.shoppingmall.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求DTO
 * 包含旧密码和新密码
 *
 */
@Data
public class UpdatePasswordRequest {

    /**
     * 旧密码
     * 验证规则：不能为空
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     * 验证规则：
     * - 不能为空
     * - 长度6-20个字符
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;
}

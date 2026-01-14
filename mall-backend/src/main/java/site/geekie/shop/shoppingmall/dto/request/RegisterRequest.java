package site.geekie.shop.shoppingmall.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 * 包含用户注册所需的所有字段和验证规则
 *
 */
@Data
public class RegisterRequest {

    /**
     * 用户名
     * 验证规则：
     * - 不能为空
     * - 长度3-20个字符
     * - 只能包含字母、数字和下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码
     * 验证规则：
     * - 不能为空
     * - 长度6-20个字符
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 邮箱
     * 验证规则：
     * - 不能为空
     * - 必须符合邮箱格式
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号（可选）
     * 验证规则：
     * - 如果提供，必须符合中国大陆手机号格式（1开头，11位数字）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

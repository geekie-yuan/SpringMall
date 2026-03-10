package site.geekie.shop.shoppingmall.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户资料更新 DTO
 * 仅包含允许用户自行修改的字段，防止越权修改 role/status 等敏感字段
 */
@Data
public class UpdateProfileDTO {

    @Size(min = 2, max = 30, message = "用户名长度为2-30个字符")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号不能超过20个字符")
    private String phone;

    @Size(max = 255, message = "头像URL不能超过255个字符")
    private String avatar;
}

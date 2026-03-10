package site.geekie.shop.shoppingmall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.geekie.shop.shoppingmall.annotation.SensitiveField;
import site.geekie.shop.shoppingmall.annotation.SensitiveType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    @SensitiveField(SensitiveType.TOKEN)
    private String token;
    private String tokenType = "Bearer";
    private UserVO user;

    public LoginVO(String token, UserVO user) {
        this.token = token;
        this.user = user;
    }
}

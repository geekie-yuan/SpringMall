package site.geekie.shop.shoppingmall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private String token;
    private String tokenType = "Bearer";
    private UserVO user;

    public LoginVO(String token, UserVO user) {
        this.token = token;
        this.user = user;
    }
}

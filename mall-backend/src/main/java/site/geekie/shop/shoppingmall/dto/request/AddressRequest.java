package site.geekie.shop.shoppingmall.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 地址请求DTO
 * 用于新增和修改收货地址
 */
@Data
public class AddressRequest {

    /**
     * 收货人姓名
     * 验证规则：不能为空，长度2-50个字符
     */
    @NotBlank(message = "收货人姓名不能为空")
    @Size(min = 2, max = 50, message = "收货人姓名长度必须在2-50个字符之间")
    private String receiverName;

    /**
     * 联系电话
     * 验证规则：不能为空，11位手机号格式
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 省份
     * 验证规则：不能为空
     */
    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份长度不能超过50个字符")
    private String province;

    /**
     * 城市
     * 验证规则：不能为空
     */
    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市长度不能超过50个字符")
    private String city;

    /**
     * 区县
     * 验证规则：不能为空
     */
    @NotBlank(message = "区县不能为空")
    @Size(max = 50, message = "区县长度不能超过50个字符")
    private String district;

    /**
     * 详细地址
     * 验证规则：不能为空，长度5-200个字符
     */
    @NotBlank(message = "详细地址不能为空")
    @Size(min = 5, max = 200, message = "详细地址长度必须在5-200个字符之间")
    private String detailAddress;

    /**
     * 是否为默认地址（0-否，1-是）
     * 可选，默认为0
     */
    private Integer isDefault = 0;
}

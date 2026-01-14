package site.geekie.shop.shoppingmall.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 地址响应DTO
 * 返回给客户端的收货地址信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    // 地址ID
    private Long id;

    // 用户ID
    private Long userId;

    // 收货人姓名
    private String receiverName;

    // 联系电话
    private String phone;

    // 省份
    private String province;

    // 城市
    private String city;

    // 区县
    private String district;

    // 详细地址
    private String detailAddress;

    // 完整地址（省+市+区+详细地址）
    private String fullAddress;

    // 是否为默认地址（0-否，1-是）
    private Integer isDefault;

    // 创建时间
    private LocalDateTime createdAt;

    /**
     * 构造方法：根据实体属性自动拼接完整地址
     */
    public AddressResponse(Long id, Long userId, String receiverName, String phone,
                            String province, String city, String district,
                            String detailAddress, Integer isDefault, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.receiverName = receiverName;
        this.phone = phone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
        // 自动拼接完整地址
        this.fullAddress = province + city + district + detailAddress;
    }
}

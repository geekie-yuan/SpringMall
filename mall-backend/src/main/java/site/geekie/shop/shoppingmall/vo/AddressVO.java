package site.geekie.shop.shoppingmall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressVO {
    private Long id;
    private Long userId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String fullAddress;
    private Integer isDefault;
    private LocalDateTime createdAt;

    public AddressVO(Long id, Long userId, String receiverName, String phone,
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
        this.fullAddress = province + city + district + detailAddress;
    }
}

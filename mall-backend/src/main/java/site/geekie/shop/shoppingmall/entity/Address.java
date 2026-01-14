package site.geekie.shop.shoppingmall.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址实体类
 * 对应数据库表：address
 */
@Data
public class Address {

    // 地址ID（主键）
    private Long id;

    // 用户ID（外键）
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

    // 是否为默认地址（0-否，1-是）
    private Integer isDefault;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;
}

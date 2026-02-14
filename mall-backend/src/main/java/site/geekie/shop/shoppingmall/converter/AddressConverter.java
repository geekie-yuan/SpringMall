package site.geekie.shop.shoppingmall.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import site.geekie.shop.shoppingmall.entity.AddressDO;
import site.geekie.shop.shoppingmall.vo.AddressVO;

import java.util.List;

/**
 * 地址实体转换器
 * 使用 MapStruct 将 AddressDO 转换为 AddressVO
 *
 * 转换规则：
 *   - 自动映射同名字段：id, userId, receiverName, phone, province, city, district, detailAddress, isDefault, createdAt
 *   - 计算字段：fullAddress（通过 AfterMapping 实现）
 */
@Mapper(componentModel = "spring")
public interface AddressConverter {

    /**
     * 将 AddressDO 转换为 AddressVO
     * 自动计算 fullAddress = province + city + district + detailAddress
     *
     * @param address 地址实体
     * @return 地址视图对象
     */
    @Mapping(target = "fullAddress", expression = "java(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress())")
    AddressVO toVO(AddressDO address);

    /**
     * 批量转换 AddressDO 列表为 AddressVO 列表
     *
     * @param addresses 地址实体列表
     * @return 地址视图对象列表
     */
    List<AddressVO> toVOList(List<AddressDO> addresses);
}

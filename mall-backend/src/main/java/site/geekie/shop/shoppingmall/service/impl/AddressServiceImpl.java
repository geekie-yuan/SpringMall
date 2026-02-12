package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.AddressDTO;
import site.geekie.shop.shoppingmall.entity.AddressDO;
import site.geekie.shop.shoppingmall.vo.AddressVO;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.AddressMapper;
import site.geekie.shop.shoppingmall.service.AddressService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 地址服务实现类
 * 实现收货地址的CRUD业务逻辑
 *
 * 核心功能：
 *   - 地址查询：支持列表查询、默认地址查询
 *   - 地址管理：新增、修改、删除地址
 *   - 默认地址管理：设置默认地址，自动取消其他默认
 *   - 权限控制：确保用户只能操作自己的地址
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    // 地址数据访问对象
    private final AddressMapper addressMapper;

    @Override
    public List<AddressVO> getAddressList(Long userId) {
        List<AddressDO> addresses = addressMapper.findByUserId(userId);
        return addresses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AddressVO getDefaultAddress(Long userId) {
        AddressDO address = addressMapper.findDefaultByUserId(userId);
        return address != null ? convertToResponse(address) : null;
    }

    @Override
    public AddressVO getAddressById(Long id, Long userId) {
        AddressDO address = getAddressAndCheckOwner(id, userId);
        return convertToResponse(address);
    }

    @Override
    @Transactional
    public AddressVO addAddress(AddressDTO request, Long userId) {
        AddressDO address = new AddressDO();
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());

        // 如果是第一个地址或者指定为默认，设为默认地址
        int count = addressMapper.countByUserId(userId);
        if (count == 0 || (request.getIsDefault() != null && request.getIsDefault() == 1)) {
            if (count > 0) {
                addressMapper.cancelDefaultByUserId(userId);
            }
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }

        addressMapper.insert(address);
        return convertToResponse(address);
    }

    @Override
    @Transactional
    public AddressVO updateAddress(Long id, AddressDTO request, Long userId) {
        AddressDO address = getAddressAndCheckOwner(id, userId);

        address.setReceiverName(request.getReceiverName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());

        // 如果要设置为默认地址
        if (request.getIsDefault() != null && request.getIsDefault() == 1 && address.getIsDefault() == 0) {
            addressMapper.cancelDefaultByUserId(address.getUserId());
            address.setIsDefault(1);
        }

        addressMapper.updateById(address);
        return convertToResponse(addressMapper.findById(id));
    }

    @Override
    @Transactional
    public void deleteAddress(Long id, Long userId) {
        AddressDO address = getAddressAndCheckOwner(id, userId);
        boolean wasDefault = address.getIsDefault() == 1;

        addressMapper.deleteById(id);

        // 如果删除的是默认地址，将第一个地址设为默认
        if (wasDefault) {
            List<AddressDO> addresses = addressMapper.findByUserId(address.getUserId());
            if (!addresses.isEmpty()) {
                addressMapper.setDefault(addresses.get(0).getId());
            }
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long id, Long userId) {
        AddressDO address = getAddressAndCheckOwner(id, userId);

        // 取消当前用户的所有默认地址
        addressMapper.cancelDefaultByUserId(address.getUserId());

        // 设置新的默认地址
        addressMapper.setDefault(id);
    }

    /**
     * 获取地址并检查所有权
     * 确保地址存在且属于当前用户
     *
     * @param id 地址ID
     * @param userId 当前用户ID
     * @return 地址实体
     * @throws BusinessException 当地址不存在或不属于当前用户时抛出
     */
    private AddressDO getAddressAndCheckOwner(Long id, Long userId) {
        AddressDO address = addressMapper.findById(id);
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }

        return address;
    }

    /**
     * 将Address实体转换为AddressVO对象
     *
     * @param address 地址实体
     * @return 地址响应对象
     */
    private AddressVO convertToResponse(AddressDO address) {
        return new AddressVO(
                address.getId(),
                address.getUserId(),
                address.getReceiverName(),
                address.getPhone(),
                address.getProvince(),
                address.getCity(),
                address.getDistrict(),
                address.getDetailAddress(),
                address.getIsDefault(),
                address.getCreatedAt()
        );
    }
}

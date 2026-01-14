package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.request.AddressRequest;
import site.geekie.shop.shoppingmall.dto.response.AddressResponse;
import site.geekie.shop.shoppingmall.entity.Address;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.AddressMapper;
import site.geekie.shop.shoppingmall.security.SecurityUser;
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
    public List<AddressResponse> getAddressList() {
        Long userId = getCurrentUserId();
        List<Address> addresses = addressMapper.findByUserId(userId);
        return addresses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponse getDefaultAddress() {
        Long userId = getCurrentUserId();
        Address address = addressMapper.findDefaultByUserId(userId);
        return address != null ? convertToResponse(address) : null;
    }

    @Override
    public AddressResponse getAddressById(Long id) {
        Address address = getAddressAndCheckOwner(id);
        return convertToResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse addAddress(AddressRequest request) {
        Long userId = getCurrentUserId();

        Address address = new Address();
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
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        Address address = getAddressAndCheckOwner(id);

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
    public void deleteAddress(Long id) {
        Address address = getAddressAndCheckOwner(id);
        boolean wasDefault = address.getIsDefault() == 1;

        addressMapper.deleteById(id);

        // 如果删除的是默认地址，将第一个地址设为默认
        if (wasDefault) {
            List<Address> addresses = addressMapper.findByUserId(address.getUserId());
            if (!addresses.isEmpty()) {
                addressMapper.setDefault(addresses.get(0).getId());
            }
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long id) {
        Address address = getAddressAndCheckOwner(id);

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
     * @return 地址实体
     * @throws BusinessException 当地址不存在或不属于当前用户时抛出
     */
    private Address getAddressAndCheckOwner(Long id) {
        Address address = addressMapper.findById(id);
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }

        Long userId = getCurrentUserId();
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }

        return address;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return securityUser.getUser().getId();
    }

    /**
     * 将Address实体转换为AddressResponse对象
     *
     * @param address 地址实体
     * @return 地址响应对象
     */
    private AddressResponse convertToResponse(Address address) {
        return new AddressResponse(
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

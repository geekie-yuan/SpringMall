package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.AddressDTO;
import site.geekie.shop.shoppingmall.vo.AddressVO;

import java.util.List;

/**
 * 地址服务接口
 * 提供收货地址的CRUD功能
 *
 * 主要功能：
 *   - 查询当前用户的所有地址列表
 *   - 获取默认地址
 *   - 新增、修改、删除地址
 *   - 设置默认地址
 */
public interface AddressService {

    /**
     * 获取当前用户的所有地址列表
     * 按默认地址优先、创建时间倒序排列
     *
     * @param userId 当前用户 ID
     * @return 地址列表
     */
    List<AddressVO> getAddressList(Long userId);

    /**
     * 获取当前用户的默认地址
     *
     * @param userId 当前用户 ID
     * @return 默认地址，不存在则返回null
     */
    AddressVO getDefaultAddress(Long userId);

    /**
     * 根据地址ID获取地址详情
     * 仅允许查询当前用户自己的地址
     *
     * @param id 地址ID
     * @param userId 当前用户 ID
     * @return 地址详情
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当地址不存在或不属于当前用户时抛出
     */
    AddressVO getAddressById(Long id, Long userId);

    /**
     * 新增收货地址
     * 如果是第一个地址，自动设为默认
     *
     * @param request 地址请求
     * @param userId 当前用户 ID
     * @return 新增的地址信息
     */
    AddressVO addAddress(AddressDTO request, Long userId);

    /**
     * 修改收货地址
     * 仅允许修改当前用户自己的地址
     *
     * @param id 地址ID
     * @param request 地址请求
     * @param userId 当前用户 ID
     * @return 修改后的地址信息
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当地址不存在或不属于当前用户时抛出
     */
    AddressVO updateAddress(Long id, AddressDTO request, Long userId);

    /**
     * 删除收货地址
     * 仅允许删除当前用户自己的地址
     * 如果删除的是默认地址，会自动将第一个地址设为默认
     *
     * @param id 地址ID
     * @param userId 当前用户 ID
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当地址不存在或不属于当前用户时抛出
     */
    void deleteAddress(Long id, Long userId);

    /**
     * 设置默认地址
     * 会先取消当前用户的所有默认地址，再设置新的默认地址
     *
     * @param id 地址ID
     * @param userId 当前用户 ID
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当地址不存在或不属于当前用户时抛出
     */
    void setDefaultAddress(Long id, Long userId);
}

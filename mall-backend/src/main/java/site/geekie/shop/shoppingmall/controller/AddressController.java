package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.annotation.CurrentUserId;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.AddressDTO;
import site.geekie.shop.shoppingmall.vo.AddressVO;
import site.geekie.shop.shoppingmall.service.AddressService;

import java.util.List;

/**
 * 收货地址控制器
 * 处理收货地址管理相关接口
 *
 * 接口路径前缀：/api/v1/addresses
 * 认证要求：所有接口需要USER角色认证
 * 主要功能：
 *   - 地址列表查询：GET /
 *   - 地址详情查询：GET /{id}
 *   - 默认地址查询：GET /default
 *   - 新增地址：POST /
 *   - 修改地址：PUT /{id}
 *   - 删除地址：DELETE /{id}
 *   - 设置默认地址：PUT /{id}/default
 */
@Tag(name = "Address", description = "收货地址接口")
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {

    private final AddressService addressService;

    /**
     * 获取当前用户的地址列表
     * 返回所有地址，按默认地址优先、创建时间倒序排列
     *
     * @param userId 当前登录用户ID（自动注入）
     * @return 包含地址列表的统一响应对象
     */
    @Operation(summary = "获取地址列表")
    @GetMapping
    public Result<List<AddressVO>> getAddressList(@Parameter(hidden = true) @CurrentUserId Long userId) {
        List<AddressVO> addresses = addressService.getAddressList(userId);
        return Result.success(addresses);
    }

    /**
     * 获取当前用户的默认地址
     *
     * @param userId 当前登录用户ID（自动注入）
     * @return 包含默认地址的统一响应对象，无默认地址时data为null
     */
    @Operation(summary = "获取默认地址")
    @GetMapping("/default")
    public Result<AddressVO> getDefaultAddress(@Parameter(hidden = true) @CurrentUserId Long userId) {
        AddressVO address = addressService.getDefaultAddress(userId);
        return Result.success(address);
    }

    /**
     * 根据ID获取地址详情
     * 仅允许查询当前用户自己的地址
     *
     * @param id 地址ID
     * @param userId 当前登录用户ID（自动注入）
     * @return 包含地址详情的统一响应对象
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当地址不存在或不属于当前用户时抛出
     */
    @Operation(summary = "获取地址详情")
    @GetMapping("/{id}")
    public Result<AddressVO> getAddressById(@PathVariable Long id, @Parameter(hidden = true) @CurrentUserId Long userId) {
        AddressVO address = addressService.getAddressById(id, userId);
        return Result.success(address);
    }

    /**
     * 新增收货地址
     * 如果是第一个地址，自动设为默认
     *
     * @param request 地址请求
     * @param userId 当前登录用户ID（自动注入）
     * @return 包含新增地址信息的统一响应对象
     */
    @Operation(summary = "新增地址")
    @PostMapping
    public Result<AddressVO> addAddress(@Valid @RequestBody AddressDTO request, @Parameter(hidden = true) @CurrentUserId Long userId) {
        AddressVO address = addressService.addAddress(request, userId);
        return Result.success("地址添加成功", address);
    }

    /**
     * 修改收货地址
     * 仅允许修改当前用户自己的地址
     *
     * @param id 地址ID
     * @param request 地址请求
     * @param userId 当前登录用户ID（自动注入）
     * @return 包含修改后地址信息的统一响应对象
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当地址不存在或不属于当前用户时抛出
     */
    @Operation(summary = "修改地址")
    @PutMapping("/{id}")
    public Result<AddressVO> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressDTO request, @Parameter(hidden = true) @CurrentUserId Long userId) {
        AddressVO address = addressService.updateAddress(id, request, userId);
        return Result.success("地址修改成功", address);
    }

    /**
     * 删除收货地址
     * 仅允许删除当前用户自己的地址
     * 如果删除的是默认地址，会自动将第一个地址设为默认
     *
     * @param id 地址ID
     * @param userId 当前登录用户ID（自动注入）
     * @return 统一响应对象
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当地址不存在或不属于当前用户时抛出
     */
    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id, @Parameter(hidden = true) @CurrentUserId Long userId) {
        addressService.deleteAddress(id, userId);
        return Result.success("地址删除成功", null);
    }

    /**
     * 设置默认地址
     * 会自动取消当前用户的其他默认地址
     *
     * @param id 地址ID
     * @param userId 当前登录用户ID（自动注入）
     * @return 统一响应对象
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当地址不存在或不属于当前用户时抛出
     */
    @Operation(summary = "设置默认地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAddress(@PathVariable Long id, @Parameter(hidden = true) @CurrentUserId Long userId) {
        addressService.setDefaultAddress(id, userId);
        return Result.success("默认地址设置成功", null);
    }
}

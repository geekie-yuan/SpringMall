package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.UpdatePasswordRequest;
import site.geekie.shop.shoppingmall.dto.response.UserResponse;
import site.geekie.shop.shoppingmall.entity.User;
import site.geekie.shop.shoppingmall.service.UserService;

/**
 * 用户控制器
 * 处理用户信息查询、更新和密码修改等接口
 *
 * 接口路径前缀：/api/v1/user
 * 认证要求：所有接口需要Bearer Token认证
 * 主要功能：
 *   - 获取当前用户信息：GET /profile
 *   - 根据ID查询用户信息：GET /{id}
 *   - 更新用户信息：PUT /profile
 *   - 修改密码：PUT /password
 *
 */
@Tag(name = "User", description = "用户接口")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    // 用户服务
    private final UserService userService;

    /**
     * 获取当前登录用户信息
     * 从JWT令牌中提取当前用户身份并返回用户详细信息
     *
     * 请求路径：GET /api/v1/user/profile
     * 认证：需要Bearer Token
     * 响应：用户基本信息（不包含密码）
     *
     * @return 包含用户信息的统一响应对象
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当用户不存在时抛出
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserResponse> getProfile() {
        UserResponse user = userService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 根据用户ID查询用户信息
     * 通过用户ID获取指定用户的基本信息
     *
     * 请求路径：GET /api/v1/user/{id}
     * 认证：需要Bearer Token
     * 响应：用户基本信息（不包含密码）
     *
     * @param id 用户ID
     * @return 包含用户信息的统一响应对象
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当用户不存在时抛出
     */
    @Operation(summary = "根据ID查询用户信息")
    @GetMapping("/{id}")
    public Result<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 更新当前登录用户信息
     * 允许用户更新自己的个人信息（邮箱、手机号、头像等）
     *
     * 请求路径：PUT /api/v1/user/profile
     * 认证：需要Bearer Token
     * 权限：用户只能更新自己的信息
     * 说明：用户ID由服务端从认证上下文中获取，客户端提供的ID将被忽略
     *
     * @param user 包含待更新字段的用户对象
     * @return 包含更新后用户信息的统一响应对象
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user) {
        User updatedUser = userService.updateUser(user);
        return Result.success(updatedUser);
    }

    /**
     * 修改当前登录用户密码
     * 验证旧密码后更新为新密码
     *
     * 请求路径：PUT /api/v1/user/password
     * 认证：需要Bearer Token
     * 验证规则：
     *   - 旧密码：必须正确
     *   - 新密码：6-20字符
 *
     * @param request 密码修改请求，包含旧密码和新密码
     * @return 统一响应对象，密码修改成功返回成功消息
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当旧密码错误时抛出
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(request);
        return Result.success("密码修改成功", null);
    }
}

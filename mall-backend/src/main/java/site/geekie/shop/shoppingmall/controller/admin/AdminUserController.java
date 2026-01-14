package site.geekie.shop.shoppingmall.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.response.UserResponse;
import site.geekie.shop.shoppingmall.service.UserService;

import java.util.List;

/**
 * 管理员-用户管理控制器
 * 提供用户管理的REST API（仅管理员可访问）
 *
 * 基础路径：/api/v1/admin/users
 * 所有接口都需要ADMIN角色权限
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    /**
     * 获取所有用户（管理员）
     * GET /api/v1/admin/users
     *
     * @return 用户列表
     */
    @GetMapping
    public Result<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return Result.success(users);
    }

    /**
     * 获取用户详情（管理员）
     * GET /api/v1/admin/users/{id}
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 更新用户状态（管理员）
     * PUT /api/v1/admin/users/{id}/status
     *
     * @param id 用户ID
     * @param status 用户状态（1-正常，0-禁用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    /**
     * 更新用户角色（管理员）
     * PUT /api/v1/admin/users/{id}/role
     *
     * @param id 用户ID
     * @param role 用户角色（USER/ADMIN）
     * @return 操作结果
     */
    @PutMapping("/{id}/role")
    public Result<Void> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        userService.updateUserRole(id, role);
        return Result.success();
    }
}

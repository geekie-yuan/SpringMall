package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.request.UpdatePasswordRequest;
import site.geekie.shop.shoppingmall.dto.response.UserResponse;
import site.geekie.shop.shoppingmall.entity.User;

/**
 * 用户服务接口
 * 提供用户信息查询、更新和密码修改功能
 *
 * 主要功能：
 *   - 获取当前登录用户信息
 *   - 根据ID查询用户信息
 *   - 更新用户信息
 *   - 修改用户密码
 *
 */
public interface UserService {

    /**
     * 获取当前登录用户信息
     * 从Spring Security上下文中获取当前认证用户
     *
     * @return 用户响应对象，包含用户基本信息（不含密码）
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当用户未找到时抛出
     */
    UserResponse getCurrentUser();

    /**
     * 根据用户ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户响应对象，包含用户基本信息（不含密码）
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当用户不存在时抛出
     */
    UserResponse getUserById(Long id);

    /**
     * 更新当前登录用户信息
     * 仅允许用户更新自己的信息
     *
     * @param user 包含待更新字段的用户对象
     * @return 更新后的用户实体
     */
    User updateUser(User user);

    /**
     * 修改当前登录用户密码
     * 需验证旧密码正确性后才能设置新密码
     *
     * @param request 密码修改请求，包含旧密码和新密码
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当旧密码错误时抛出
     */
    void updatePassword(UpdatePasswordRequest request);

    // ===== 管理员方法 =====

    /**
     * 获取所有用户（管理员）
     *
     * @return 用户列表
     */
    java.util.List<UserResponse> getAllUsers();

    /**
     * 更新用户状态（管理员）
     *
     * @param id 用户ID
     * @param status 用户状态（1-正常，0-禁用）
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 更新用户角色（管理员）
     *
     * @param id 用户ID
     * @param role 用户角色（USER/ADMIN）
     */
    void updateUserRole(Long id, String role);
}

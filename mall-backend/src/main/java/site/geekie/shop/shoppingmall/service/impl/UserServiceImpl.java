package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.request.UpdatePasswordRequest;
import site.geekie.shop.shoppingmall.dto.response.UserResponse;
import site.geekie.shop.shoppingmall.entity.User;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.UserMapper;
import site.geekie.shop.shoppingmall.security.SecurityUser;
import site.geekie.shop.shoppingmall.service.UserService;

/**
 * 用户服务实现类
 * 实现用户信息查询、更新和密码修改的业务逻辑
 *
 * 核心功能：
 *   - 获取当前登录用户信息：从Security上下文获取
 *   - 根据ID查询用户信息
 *   - 更新用户信息：仅限当前登录用户
 *   - 修改密码：验证旧密码后更新
 *
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // 用户数据访问对象
    private final UserMapper userMapper;

    // 密码编码器（BCrypt）
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取当前登录用户信息
     * 从Spring Security上下文中获取当前认证用户
     *
     * @return 用户响应对象
     * @throws BusinessException 当用户不存在时抛出
     */
    @Override
    public UserResponse getCurrentUser() {
        User user = getCurrentUserEntity();
        return convertToUserResponse(user);
    }

    /**
     * 根据用户ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户响应对象
     * @throws BusinessException 当用户不存在时抛出
     */
    @Override
    public UserResponse getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToUserResponse(user);
    }

    /**
     * 更新当前登录用户信息
     * 自动设置用户ID为当前登录用户的ID，确保用户只能更新自己的信息
     *
     * @param user 包含待更新字段的用户对象
     * @return 更新后的用户实体
     */
    @Override
    @Transactional
    public User updateUser(User user) {
        User currentUser = getCurrentUserEntity();
        user.setId(currentUser.getId()); // 确保只能更新自己的信息
        userMapper.updateById(user);
        return userMapper.findById(currentUser.getId());
    }

    /**
     * 修改当前登录用户密码
     * 先验证旧密码的正确性，再使用BCrypt加密新密码并更新
     *
     * @param request 密码修改请求
     * @throws BusinessException 当旧密码错误时抛出
     */
    @Override
    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        User currentUser = getCurrentUserEntity();

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS, "旧密码错误");
        }

        // 加密新密码并更新
        String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());
        userMapper.updatePassword(currentUser.getId(), newEncodedPassword);
    }

    /**
     * 获取当前登录用户实体
     * 从Spring Security上下文中提取当前认证用户并从数据库查询完整信息
     *
     * @return 用户实体
     * @throws BusinessException 当用户不存在时抛出
     */
    private User getCurrentUserEntity() {
        // 从Security上下文获取认证用户
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        // 从数据库查询最新用户信息
        User user = userMapper.findById(securityUser.getUser().getId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 将User实体转换为UserResponse对象
     * 排除密码等敏感信息
     *
     * @param user 用户实体
     * @return 用户响应对象
     */
    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    // ===== 管理员方法实现 =====

    /**
     * 获取所有用户（管理员）
     *
     * @return 用户列表
     */
    @Override
    public java.util.List<UserResponse> getAllUsers() {
        java.util.List<User> users = userMapper.findAll();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 更新用户状态（管理员）
     *
     * @param id 用户ID
     * @param status 用户状态（1-正常，0-禁用）
     */
    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        userMapper.updateStatus(id, status);
    }

    /**
     * 更新用户角色（管理员）
     *
     * @param id 用户ID
     * @param role 用户角色（USER/ADMIN）
     */
    @Override
    @Transactional
    public void updateUserRole(Long id, String role) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证角色是否合法
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER, "角色必须为USER或ADMIN");
        }

        userMapper.updateRole(id, role);
    }
}

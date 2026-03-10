package site.geekie.shop.shoppingmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.annotation.LogOperation;
import site.geekie.shop.shoppingmall.common.PageResult;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.converter.UserConverter;
import site.geekie.shop.shoppingmall.dto.UpdatePasswordDTO;
import site.geekie.shop.shoppingmall.dto.UpdateProfileDTO;
import site.geekie.shop.shoppingmall.entity.UserDO;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.UserMapper;
import site.geekie.shop.shoppingmall.security.SecurityUser;
import site.geekie.shop.shoppingmall.service.UserService;
import site.geekie.shop.shoppingmall.util.TokenBlacklistService;
import site.geekie.shop.shoppingmall.util.UserAuthCacheService;
import site.geekie.shop.shoppingmall.vo.UserVO;

/**
 * 用户服务实现类
 * 实现用户信息查询、更新和密码修改的业务逻辑
 *
 * 缓存失效策略：
 *   - updateUserStatus：禁用时 forceLogoutUser + evictUser；启用时 clearForceLogout + evictUser
 *   - updateUserRole：forceLogoutUser + evictUser（角色变更必须重新登录）
 *   - updatePassword：evictUser（前端已有自动 logout 逻辑）
 *   - updateUser：evictUser（仅清缓存，无需踢下线）
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserConverter userConverter;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserAuthCacheService userAuthCacheService;

    /**
     * 获取当前登录用户信息
     *
     * @return 用户响应对象
     * @throws BusinessException 当用户不存在时抛出
     */
    @Override
    public UserVO getCurrentUser() {
        UserDO user = getCurrentUserEntity();
        return userConverter.toVO(user);
    }

    /**
     * 根据用户ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户响应对象
     * @throws BusinessException 当用户不存在时抛出
     */
    @Override
    public UserVO getUserById(Long id) {
        UserDO user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return userConverter.toVO(user);
    }

    /**
     * 更新当前登录用户信息
     * 仅允许更新 username、email、phone、avatar，防止越权修改 role/status
     * 对 username、email、phone 进行唯一性校验
     * 变更后清除认证缓存，下次请求重新加载。
     *
     * @param dto 包含待更新字段的 DTO
     * @return 更新后的用户视图对象
     */
    @Override
    @Transactional
    public UserVO updateUser(UpdateProfileDTO dto) {
        UserDO currentUser = getCurrentUserEntity();
        Long currentUserId = currentUser.getId();

        // 唯一性校验：用户名
        if (dto.getUsername() != null && !dto.getUsername().equals(currentUser.getUsername())) {
            UserDO existing = userMapper.findByUsername(dto.getUsername());
            if (existing != null && !existing.getId().equals(currentUserId)) {
                throw new BusinessException(ResultCode.INVALID_PARAMETER, "该用户名已被使用");
            }
        }

        // 唯一性校验：邮箱
        if (dto.getEmail() != null && !dto.getEmail().equals(currentUser.getEmail())) {
            UserDO existing = userMapper.findByEmail(dto.getEmail());
            if (existing != null && !existing.getId().equals(currentUserId)) {
                throw new BusinessException(ResultCode.INVALID_PARAMETER, "该邮箱已被使用");
            }
        }

        // 唯一性校验：手机号
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()
                && !dto.getPhone().equals(currentUser.getPhone())) {
            UserDO existing = userMapper.findByPhone(dto.getPhone());
            if (existing != null && !existing.getId().equals(currentUserId)) {
                throw new BusinessException(ResultCode.INVALID_PARAMETER, "该手机号已被使用");
            }
        }

        UserDO updateDO = new UserDO();
        updateDO.setId(currentUserId);
        updateDO.setUsername(dto.getUsername());
        updateDO.setEmail(dto.getEmail());
        updateDO.setPhone(dto.getPhone());
        updateDO.setAvatar(dto.getAvatar());
        userMapper.updateById(updateDO);

        // 清除认证缓存，下次请求重新从 DB 加载最新信息
        userAuthCacheService.evictUser(currentUserId);

        return userConverter.toVO(userMapper.findById(currentUserId));
    }

    /**
     * 修改当前登录用户密码
     * 先验证旧密码的正确性，再使用BCrypt加密新密码并更新。
     * 变更后清除认证缓存（前端已有自动 logout 逻辑，无需踢下线）。
     *
     * @param request 密码修改请求
     * @throws BusinessException 当旧密码错误时抛出
     */
    @Override
    @Transactional
    @LogOperation(value = "修改密码", module = "用户")
    public void updatePassword(UpdatePasswordDTO request) {
        UserDO currentUser = getCurrentUserEntity();

        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS, "旧密码错误");
        }

        String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());
        userMapper.updatePassword(currentUser.getId(), newEncodedPassword);

        // 清除认证缓存（当前 token 的黑名单由 Controller 层的 logout 接口处理）
        userAuthCacheService.evictUser(currentUser.getId());
    }

    /**
     * 获取当前登录用户实体
     * 从Spring Security上下文中提取当前认证用户并从数据库查询完整信息
     *
     * @return 用户实体
     * @throws BusinessException 当用户不存在时抛出
     */
    private UserDO getCurrentUserEntity() {
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        UserDO user = userMapper.findById(securityUser.getUser().getId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    // ===== 管理员方法实现 =====

    /**
     * 获取所有用户（管理员）
     *
     * @return 用户列表
     */
    @Override
    public PageResult<UserVO> getAllUsers(int page, int size, String keyword, String role, Integer status) {
        PageHelper.startPage(page, size);
        java.util.List<UserDO> users = userMapper.findAllWithFilter(keyword, role, status);
        PageInfo<UserDO> pageInfo = new PageInfo<>(users);
        java.util.List<UserVO> list = userConverter.toVOList(users);
        return new PageResult<>(list, pageInfo.getTotal(), page, size);
    }

    /**
     * 更新用户状态（管理员）
     * 禁用时：写入 force-logout 标记（使所有已签发 token 立即失效）+ 清除认证缓存
     * 启用时：清除 force-logout 标记 + 清除认证缓存（允许用户重新登录）
     *
     * @param id     用户ID
     * @param status 用户状态（1-正常，0-禁用）
     */
    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        UserDO user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        userMapper.updateStatus(id, status);

        if (Integer.valueOf(0).equals(status)) {
            // 禁用用户：强制所有已签发 token 立即失效
            tokenBlacklistService.forceLogoutUser(id);
        } else {
            // 启用用户：清除强制登出标记，允许重新登录
            tokenBlacklistService.clearForceLogout(id);
        }
        // 无论启用/禁用，都清除认证缓存
        userAuthCacheService.evictUser(id);
    }

    /**
     * 更新用户角色（管理员）
     * 角色变更后强制用户重新登录以获取新角色的 token。
     *
     * @param id   用户ID
     * @param role 用户角色（USER/ADMIN）
     */
    @Override
    @Transactional
    public void updateUserRole(Long id, String role) {
        UserDO user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER, "角色必须为USER或ADMIN");
        }

        userMapper.updateRole(id, role);

        // 角色变更：强制所有已签发 token 失效，用户必须重新登录获取包含新角色的 token
        tokenBlacklistService.forceLogoutUser(id);
        userAuthCacheService.evictUser(id);
    }
}

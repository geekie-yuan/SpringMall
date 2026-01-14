package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.request.LoginRequest;
import site.geekie.shop.shoppingmall.dto.request.RegisterRequest;
import site.geekie.shop.shoppingmall.dto.response.LoginResponse;
import site.geekie.shop.shoppingmall.dto.response.UserResponse;
import site.geekie.shop.shoppingmall.entity.User;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.UserMapper;
import site.geekie.shop.shoppingmall.security.JwtTokenProvider;
import site.geekie.shop.shoppingmall.security.SecurityUser;
import site.geekie.shop.shoppingmall.service.AuthService;

/**
 * 认证服务实现类
 * 实现用户注册和登录的业务逻辑
 *
 * 核心功能：
 *   - 用户注册：验证唯一性约束，加密密码，创建用户账户
 *   - 用户登录：Spring Security认证，生成JWT令牌
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // 用户数据访问对象
    private final UserMapper userMapper;

    // 密码编码器（BCrypt）
    private final PasswordEncoder passwordEncoder;

    // JWT令牌提供者
    private final JwtTokenProvider tokenProvider;

    // Spring Security认证管理器
    private final AuthenticationManager authenticationManager;

    /**
     * 用户注册
     * 验证用户信息唯一性后创建新用户账户
     *
     * 业务流程：
     *   1. 验证用户名是否已存在
     *   2. 验证邮箱是否已被注册
     *   3. 验证手机号是否已被注册（如果提供）
     *   4. 使用BCrypt加密密码
     *   5. 设置默认角色为USER，状态为启用
     *   6. 保存用户到数据库
     *
     * @param request 注册请求
     * @throws BusinessException 当用户名、邮箱或手机号已存在时抛出
     */
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }

        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        if (request.getPhone() != null && userMapper.findByPhone(request.getPhone()) != null) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setStatus(1);

        userMapper.insert(user);
    }

    /**
     * 用户登录
     * 通过Spring Security验证用户凭证并生成JWT令牌
     *
     * 业务流程：
     *   1. 使用AuthenticationManager验证用户名和密码
     *   2. 从认证结果中获取用户详情
     *   3. 生成JWT访问令牌
     *   4. 构造登录响应（包含令牌和用户信息）
     *
     * @param request 登录请求
     * @return 登录响应，包含JWT令牌和用户信息
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         当用户名或密码错误时抛出
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // Spring Security认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 获取认证后的用户详情
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 生成JWT令牌
        String token = tokenProvider.generateToken(userDetails);

        // 提取用户实体并转换为响应对象
        User user = ((SecurityUser) userDetails).getUser();
        UserResponse userResponse = convertToUserResponse(user);

        return new LoginResponse(token, userResponse);
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
}

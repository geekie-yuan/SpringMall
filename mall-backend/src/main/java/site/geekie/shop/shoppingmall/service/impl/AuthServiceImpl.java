package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.request.LoginRequest;
import site.geekie.shop.shoppingmall.dto.request.RegisterRequest;
import site.geekie.shop.shoppingmall.entity.UserDO;
import site.geekie.shop.shoppingmall.vo.LoginVO;
import site.geekie.shop.shoppingmall.vo.UserVO;
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
@Slf4j
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
    public void register(RegisterRequest request) {if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }

        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        if (request.getPhone() != null && userMapper.findByPhone(request.getPhone()) != null) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS);
        }

        UserDO user = new UserDO();
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
     * @throws BusinessException 当用户名或密码错误时抛出
     */
    @Override
    public LoginVO login(LoginRequest request) {
        // ====================================================================================================
        // 【V1不异常处理】
        // ====================================================================================================
        /*
        // Spring Security认证账号密码
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
        UserDO user = ((SecurityUser) userDetails).getUser();
        UserVO userResponse = convertToUserVO(user);

        return new LoginVO(token, userResponse);

        */
        // ====================================================================================================

        // ====================================================================================================
        // 【V2 - 使用异常处理和日志记录】
        // ====================================================================================================
        try {
            log.info("【登录尝试】用户名: {}", request.getUsername());

            // Spring Security认证账号密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            log.info("【认证成功】用户名: {}", request.getUsername());

            // 获取认证后的用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // 生成JWT令牌
            String token = tokenProvider.generateToken(userDetails);

            // 提取用户实体并转换为响应对象
            UserDO user = ((SecurityUser) userDetails).getUser();
            UserVO userResponse = convertToUserVO(user);

            log.info("【登录成功】用户名: {}，JWT Token 已生成", request.getUsername());

            // 打印生成的JWT Token（用于调试）
            log.debug("【JWT Token】{}", token);

            return new LoginVO(token, userResponse);

        } catch (BadCredentialsException e) {
            // 账号或密码错误
            // 需要在认证前检查账号是否存在
            UserDO user = userMapper.findByUsername(request.getUsername());

            if (user == null) {
                // 账号不存在
                log.warn("【登录失败】用户名: {}，原因: 账号不存在，异常信息: {}",
                        request.getUsername(), e.getMessage());
            } else {
                // 账号存在，密码错误
                log.warn("【登录失败】用户名: {}，原因: 密码错误，异常信息: {}",
                        request.getUsername(), e.getMessage());
            }
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS);

        } catch (AuthenticationException e) {
            // 其他认证异常（如用户被禁用等）
            String exceptionType = e.getClass().getSimpleName();
            log.warn("【登录失败】用户名: {}，异常类型: {}，原因: {}，详细信息: {}",
                    request.getUsername(), exceptionType, e.getMessage(), e.toString());
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS);

        } catch (Exception e) {
            // 其他未预期的异常
            log.error("【系统错误】用户登录异常，用户名: {}，异常类型: {}，详细信息: {}",
                    request.getUsername(), e.getClass().getSimpleName(), e.getMessage(), e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR);
        }
        // ====================================================================================================
    }

    /**
     * 将User实体转换为UserVO对象
     * 排除密码等敏感信息
     *
     * @param user 用户实体
     * @return 用户响应对象
     */

    private UserVO convertToUserVO(UserDO user) {
        return new UserVO(
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

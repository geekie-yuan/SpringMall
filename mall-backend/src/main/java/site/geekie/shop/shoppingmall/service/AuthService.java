package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.request.LoginRequest;
import site.geekie.shop.shoppingmall.dto.request.RegisterRequest;
import site.geekie.shop.shoppingmall.dto.response.LoginResponse;

/**
 * 认证服务接口
 * 提供用户注册和登录功能
 *
 * 主要功能：
 *   - 用户注册：验证用户信息唯一性，创建新用户账户
 *   - 用户登录：验证用户凭证，生成JWT令牌
 *
 */
public interface AuthService {

    /**
     * 用户注册
     * 验证用户名、邮箱、手机号的唯一性，创建新用户账户
     *
     * @param request 注册请求，包含用户名、密码、邮箱等信息
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当用户名、邮箱或手机号已存在时抛出
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     * 验证用户凭证，生成并返回JWT访问令牌
     *
     * @param request 登录请求，包含用户名和密码
     * @return 登录响应，包含JWT令牌和用户信息
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         当用户名或密码错误时抛出
     */
    LoginResponse login(LoginRequest request);
}

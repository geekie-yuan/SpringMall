package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.request.LoginRequest;
import site.geekie.shop.shoppingmall.dto.request.RegisterRequest;
import site.geekie.shop.shoppingmall.dto.response.LoginResponse;
import site.geekie.shop.shoppingmall.service.AuthService;

/**
 * 认证控制器
 * 处理用户注册、登录、登出等认证相关接口
 *
 * 接口路径前缀：/api/v1/auth
 * 主要功能：
 *   - 用户注册：POST /register
 *   - 用户登录：POST /login
 *   - 用户登出：POST /logout
 *
 */
@Tag(name = "Authentication", description = "认证接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    // 认证服务
    private final AuthService authService;

    /**
     * 用户注册接口
     * 创建新用户账户
     *
     * 请求路径：POST /api/v1/auth/register
     * 验证规则：
     *   - 用户名：3-20字符，仅允许字母、数字和下划线
     *   - 密码：6-20字符
     *   - 邮箱：有效的邮箱格式
     *   - 手机号：可选，11位数字（中国手机号格式）
 *
     * @param request 注册请求，包含用户名、密码、邮箱等信息
     * @return 统一响应对象，注册成功返回成功消息
     * @throws site.geekie.shop.shoppingmall.exception.BusinessException
     *         当用户名、邮箱或手机号已存在时抛出
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录接口
     * 验证用户凭证并返回JWT访问令牌
     *
     * 请求路径：POST /api/v1/auth/login
     * 认证流程：
     *   - 验证用户名和密码
     *   - 生成JWT访问令牌
     *   - 返回令牌和用户信息
 *
     * @param request 登录请求，包含用户名和密码
     * @return 包含JWT令牌和用户信息的统一响应对象
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         当用户名或密码错误时抛出
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户登出接口
     * 由于JWT是无状态的，实际登出由客户端处理（删除本地存储的token）
     *
     * 请求路径：POST /api/v1/auth/logout
     * 说明：
     *   - 服务端不维护token状态，无需处理token失效
     *   - 客户端应删除本地存储的token以完成登出
     *   - 本接口主要用于统一API设计和客户端交互
 *
     * @return 统一响应对象，返回登出成功消息
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT是无状态的，登出由客户端处理（删除token）
        return Result.success("登出成功", null);
    }
}

package site.geekie.shop.shoppingmall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import site.geekie.shop.shoppingmall.annotation.RateLimiter;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.dto.LoginDTO;
import site.geekie.shop.shoppingmall.dto.RegisterDTO;
import site.geekie.shop.shoppingmall.service.AuthService;
import site.geekie.shop.shoppingmall.vo.LoginVO;

/**
 * 认证控制器
 * 处理用户注册、登录、登出等认证相关接口
 *
 * 接口路径前缀：/api/v1/auth
 */
@Tag(name = "Authentication", description = "认证接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册接口
     *
     * @param request 注册请求，包含用户名、密码、邮箱等信息
     * @return 统一响应对象
     */
    @Operation(summary = "用户注册")
    @RateLimiter(count = 5, period = 60)
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO request) {
        authService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录接口
     *
     * @param request 登录请求，包含用户名和密码
     * @return 包含JWT令牌和用户信息的统一响应对象
     */
    @Operation(summary = "用户登录")
    @RateLimiter(count = 5, period = 60)
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO request) {
        LoginVO response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户登出接口
     * 将当前 token 加入黑名单并清除认证缓存。
     * 无效 token 也返回成功（幂等设计）。
     *
     * @param httpRequest HTTP请求，用于提取 Authorization header
     * @return 统一响应对象
     */
    @Operation(summary = "用户登出")
    @RateLimiter(count = 5, period = 60)
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        authService.logout(token);
        return Result.success("登出成功", null);
    }

    /**
     * 从请求头提取 JWT token（去掉 "Bearer " 前缀）
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

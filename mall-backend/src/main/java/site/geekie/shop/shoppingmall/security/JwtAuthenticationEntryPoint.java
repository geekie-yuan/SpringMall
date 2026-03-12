package site.geekie.shop.shoppingmall.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.common.ResultCode;

import java.io.IOException;

/**
 * JWT 认证入口点
 * 当未认证的请求访问受保护资源时触发，返回 HTTP 401 响应。
 *
 * 覆盖 Spring Security 默认的 Http403ForbiddenEntryPoint，
 * 将"未登录"返回 401 状态码，
 * 响应体采用统一的 Result<T> JSON 格式。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 处理未认证访问
     *
     * @param request       触发认证异常的 HTTP 请求
     * @param response      HTTP 响应
     * @param authException Spring Security 抛出的认证异常
     * @throws IOException 写响应时的 IO 异常
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.debug("未认证请求被拦截: {} {}", request.getMethod(), request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED, "请登录后访问");
        objectMapper.writeValue(response.getWriter(), result);
    }
}

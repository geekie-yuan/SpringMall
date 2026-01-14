package site.geekie.shop.shoppingmall.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 在每个HTTP请求中验证JWT Token，并设置Spring Security认证上下文
 *
 * 工作流程：
 *   - 从请求头中提取JWT Token
 *   - 解析Token获取用户名
 *   - 加载用户详情
 *   - 验证Token有效性
 *   - 设置Security认证上下文
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT Token提供者
    private final JwtTokenProvider tokenProvider;

    // 用户详情服务
    private final UserDetailsService userDetailsService;

    /**
     * 过滤器内部处理逻辑
     * 每个请求执行一次
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求中提取JWT Token
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 从Token中获取用户名
                String username = tokenProvider.getUsernameFromToken(jwt);

                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 验证Token
                if (tokenProvider.validateToken(jwt, userDetails)) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置到Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求中提取JWT Token
     * Token格式：Authorization: Bearer <token>
     *
     * @param request HTTP请求
     * @return JWT Token字符串，如果不存在则返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

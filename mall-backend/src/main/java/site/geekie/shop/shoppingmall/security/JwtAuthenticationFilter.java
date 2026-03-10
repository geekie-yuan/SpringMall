package site.geekie.shop.shoppingmall.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import site.geekie.shop.shoppingmall.dto.UserAuthCache;
import site.geekie.shop.shoppingmall.entity.UserDO;
import site.geekie.shop.shoppingmall.mapper.UserMapper;
import site.geekie.shop.shoppingmall.util.TokenBlacklistService;
import site.geekie.shop.shoppingmall.util.UserAuthCacheService;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * JWT认证过滤器
 * 在每个HTTP请求中验证JWT Token，并设置Spring Security认证上下文
 *
 * 验证流程：
 *   1. 从请求头提取JWT Token
 *   2. JJWT 签名 + 过期校验（parseSignedClaims 内部完成）
 *   3. 检查 token 黑名单（Redis GET）
 *   4. 检查 force-logout 标记（Redis GET，比对 token.issuedAt）
 *   5. 从 Redis 加载 UserAuthCache（未命中则查 DB 回填）
 *   6. 校验 status==1、id 一致、createdAt 一致
 *   7. 设置 SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserAuthCacheService userAuthCacheService;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 步骤 1：JJWT 签名 + 过期校验（异常则直接跳过认证）
                Long userId = tokenProvider.getUserIdFromToken(jwt);

                // 步骤 2：检查 token 是否在黑名单中（logout 时写入）
                if (tokenBlacklistService.isBlacklisted(jwt)) {
                    log.debug("token 已在黑名单中，拒绝认证 - userId: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 步骤 3：检查用户级强制登出标记（管理员禁用/改角色时写入）
                long issuedAt = tokenProvider.getIssuedAtFromToken(jwt);
                if (tokenBlacklistService.isForceLoggedOut(userId, issuedAt)) {
                    log.debug("用户已被强制登出，token 签发时间早于强制登出时间 - userId: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 步骤 4：从 Redis 加载 UserAuthCache，未命中则查 DB 回填
                UserAuthCache cache = userAuthCacheService.getUser(userId);
                if (cache == null) {
                    UserDO user = userMapper.findById(userId);
                    if (user != null) {
                        userAuthCacheService.putUser(user);
                        cache = buildCacheFromDO(user);
                    }
                }

                if (cache == null) {
                    log.debug("用户不存在，拒绝认证 - userId: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 步骤 5：校验账户状态
                if (!Integer.valueOf(1).equals(cache.getStatus())) {
                    log.debug("用户已被禁用，拒绝认证 - userId: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 步骤 6：校验 id 一致 + createdAt 一致（防止 token 伪造）
                Long tokenCreatedAt = tokenProvider.getCreatedAtFromToken(jwt);
                if (!userId.equals(cache.getId()) || !tokenCreatedAt.equals(cache.getCreatedAtMillis())) {
                    log.debug("token 载荷与缓存不一致，拒绝认证 - userId: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 步骤 7：构造 SecurityUser 并设置 SecurityContext
                UserDO userDO = buildUserDOFromCache(cache);
                SecurityUser securityUser = new SecurityUser(userDO);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                securityUser,
                                null,
                                securityUser.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 从 UserDO 构建轻量缓存对象（避免重复依赖 UserAuthCacheService 内部逻辑）
     */
    private UserAuthCache buildCacheFromDO(UserDO user) {
        UserAuthCache cache = new UserAuthCache();
        cache.setId(user.getId());
        cache.setUsername(user.getUsername());
        cache.setEmail(user.getEmail());
        cache.setRole(user.getRole());
        cache.setStatus(user.getStatus());
        cache.setCreatedAtMillis(
                user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
        return cache;
    }

    /**
     * 从缓存对象构建 UserDO（仅用于 SecurityUser 构造，不含 password）
     */
    private UserDO buildUserDOFromCache(UserAuthCache cache) {
        UserDO user = new UserDO();
        user.setId(cache.getId());
        user.setUsername(cache.getUsername());
        user.setEmail(cache.getEmail());
        user.setRole(cache.getRole());
        user.setStatus(cache.getStatus());
        LocalDateTime createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(cache.getCreatedAtMillis()),
                ZoneId.systemDefault()
        );
        user.setCreatedAt(createdAt);
        return user;
    }
}

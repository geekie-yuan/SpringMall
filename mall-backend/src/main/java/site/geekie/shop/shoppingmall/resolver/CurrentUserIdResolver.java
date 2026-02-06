package site.geekie.shop.shoppingmall.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import site.geekie.shop.shoppingmall.annotation.CurrentUserId;
import site.geekie.shop.shoppingmall.security.SecurityUser;

/**
 * 当前用户 ID 参数解析器
 * 自动从 SecurityContext 中提取当前登录用户的 ID
 *
 * @author backend-dev
 * @since 2026-02-06
 */
@Component
public class CurrentUserIdResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否支持该参数
     * 只有标记了 @CurrentUserId 注解的 Long 类型参数才支持
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && Long.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析参数值
     * 从 SecurityContext 中提取当前用户 ID
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                 ModelAndViewContainer mavContainer,
                                 NativeWebRequest webRequest,
                                 WebDataBinderFactory binderFactory) {
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return securityUser.getUser().getId();
    }
}

package site.geekie.shop.shoppingmall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import site.geekie.shop.shoppingmall.annotation.CurrentUserId;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.security.SecurityUser;

/**
 * 当前用户 ID 参数解析器
 *
 * 从 SecurityContext 提取当前登录用户 ID，注入到标注 @CurrentUserId 的 Controller 方法参数
 *
 * @author springMall
 * @since 1.0.0
 * @see CurrentUserId
 */
@Slf4j
@Component
public class CurrentUserIdResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否支持解析该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && Long.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析当前用户 ID
     *
     * @return 当前登录用户 ID
     * @throws BusinessException 用户未登录或认证信息异常
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        // 获取认证信息
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("SecurityContext 中未找到 Authentication");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        // 校验 Principal 类型
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof SecurityUser securityUser)) {
            log.warn("Principal 类型错误: {}", principal.getClass().getSimpleName());
            throw new BusinessException(ResultCode.UNAUTHORIZED, "认证信息异常");
        }

        // 提取用户 ID
        Long userId = securityUser.getUser().getId();
        if (userId == null) {
            log.error("用户 ID 缺失: {}", securityUser.getUsername());
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "用户 ID 缺失");
        }

        log.debug("解析用户 ID: {}", userId);
        return userId;
    }
}

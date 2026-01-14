package site.geekie.shop.shoppingmall.exception;

import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常，返回标准的错误响应格式
 *
 * 处理的异常类型：
 *   - BusinessException: 业务异常
 *   - MethodArgumentNotValidException: 参数校验异常
 *   - BindException: 参数绑定异常
 *   - BadCredentialsException: 错误凭证异常
 *   - AuthenticationException: 认证异常
 *   - AccessDeniedException: 访问拒绝异常
 *   - IllegalArgumentException: 非法参数异常
 *   - Exception: 其他未捕获的异常
 *
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("Business exception: {}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 注解触发）
     *
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 将所有字段错误消息用分号连接
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("Validation exception: {}", errorMessage, e);
        return Result.error(ResultCode.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e 参数绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("Bind exception: {}", errorMessage, e);
        return Result.error(ResultCode.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理错误凭证异常（用户名或密码错误）
     *
     * @param e 错误凭证异常
     * @return 错误响应
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Bad credentials: {}", e.getMessage(), e);
        return Result.error(ResultCode.INVALID_CREDENTIALS);
    }

    /**
     * 处理认证异常
     *
     * @param e 认证异常
     * @return 错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleAuthenticationException(AuthenticationException e) {
        log.error("Authentication exception: {}", e.getMessage(), e);
        return Result.error(ResultCode.UNAUTHORIZED, e.getMessage());
    }

    /**
     * 处理访问拒绝异常（权限不足）
     *
     * @param e 访问拒绝异常
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage(), e);
        return Result.error(ResultCode.FORBIDDEN);
    }

    /**
     * 处理非法参数异常
     *
     * @param e 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument: {}", e.getMessage(), e);
        return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理未捕获的其他异常
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("Internal server error: {}", e.getMessage(), e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
    }
}

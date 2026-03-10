package site.geekie.shop.shoppingmall.exception;

import site.geekie.shop.shoppingmall.common.Result;
import site.geekie.shop.shoppingmall.common.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常，返回标准的错误响应格式
 *
 * 处理的异常类型：
 *   - BusinessException: 业务异常
 *   - MethodArgumentNotValidException: 参数校验异常（返回结构化字段错误）
 *   - BindException: 参数绑定异常（返回结构化字段错误）
 *   - ConstraintViolationException: 约束违反异常（返回结构化字段错误）
 *   - HttpMessageNotReadableException: 请求体缺失或格式错误
 *   - MissingServletRequestParameterException: 缺少请求参数
 *   - HttpRequestMethodNotSupportedException: 不支持的 HTTP 方法
 *   - NoHandlerFoundException: 未找到处理器
 *   - MaxUploadSizeExceededException: 上传文件超限
 *   - BadCredentialsException: 错误凭证异常
 *   - AuthenticationException: 认证异常
 *   - AccessDeniedException: 访问拒绝异常
 *   - IllegalArgumentException: 非法参数异常
 *   - Exception: 其他未捕获的异常（区分 dev/非 dev 环境）
 *
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

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
     * 处理参数校验异常（@Valid 注解触发），返回结构化字段错误 Map
     *
     * @param e 参数校验异常
     * @return 包含字段名→错误消息 Map 的错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.error("Validation exception: {}", fieldErrors, e);
        return Result.error(ResultCode.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    /**
     * 处理参数约束校验异常（@Validated + @Max 等触发），返回结构化字段错误 Map
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.contains(".")
                    ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                    : propertyPath;
            fieldErrors.put(fieldName, violation.getMessage());
        }
        log.error("Constraint violation: {}", fieldErrors, e);
        return Result.error(ResultCode.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    /**
     * 处理参数绑定异常，返回结构化字段错误 Map
     *
     * @param e 参数绑定异常
     * @return 包含字段名→错误消息 Map 的错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.error("Bind exception: {}", fieldErrors, e);
        return Result.error(ResultCode.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    /**
     * 处理请求体缺失或格式错误
     *
     * @param e 请求体不可读异常
     * @return 错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("Request body not readable: {}", e.getMessage(), e);
        return Result.error(ResultCode.BAD_REQUEST, "Request body is missing or malformed");
    }

    /**
     * 处理缺少请求参数异常，返回缺少的参数名和类型
     *
     * @param e 缺少请求参数异常
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("Missing request parameter: name={}, type={}", e.getParameterName(), e.getParameterType(), e);
        String message = "Missing required parameter '" + e.getParameterName() + "' of type " + e.getParameterType();
        return Result.error(ResultCode.BAD_REQUEST, message);
    }

    /**
     * 处理不支持的 HTTP 方法
     *
     * @param e 方法不支持异常
     * @return 错误响应
     */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(
            org.springframework.web.HttpRequestMethodNotSupportedException e) {
        log.error("Method not allowed: {}", e.getMethod(), e);
        return Result.error(ResultCode.BAD_REQUEST, "Method not allowed: " + e.getMethod());
    }

    /**
     * 处理未找到处理器（404）
     *
     * @param e 未找到处理器异常
     * @return 错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("No handler found: {} {}", e.getHttpMethod(), e.getRequestURL(), e);
        return Result.error(ResultCode.NOT_FOUND, "No handler found for " + e.getHttpMethod() + " " + e.getRequestURL());
    }

    /**
     * 处理上传文件超限
     *
     * @param e 上传文件超限异常
     * @return 错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("Upload file size exceeds limit: {}", e.getMessage(), e);
        return Result.error(ResultCode.BAD_REQUEST, "Upload file size exceeds limit");
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
     * dev 环境返回异常类名和消息，非 dev 环境只返回通用错误信息
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unhandled exception", e);
        if ("dev".equals(activeProfile)) {
            String detail = e.getClass().getSimpleName() + ": " + e.getMessage();
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR, detail);
        }
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
    }
}

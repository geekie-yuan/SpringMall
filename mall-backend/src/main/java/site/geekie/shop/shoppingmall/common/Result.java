package site.geekie.shop.shoppingmall.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果封装类
 * 用于封装所有API接口的返回结果，提供统一的响应格式
 *
 * @param <T> 响应数据的泛型类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    // 响应状态码
    private int code;

    // 响应消息
    private String message;

    // 响应数据
    private T data;

    /**
     * 成功响应（无数据）
     *
     * @param <T> 响应数据的泛型类型
     * @return 成功的响应结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T> 响应数据的泛型类型
     * @return 成功的响应结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息和数据）
     *
     * @param message 自定义响应消息
     * @param data 响应数据
     * @param <T> 响应数据的泛型类型
     * @return 成功的响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 错误响应（使用预定义错误码）
     *
     * @param resultCode 错误码枚举
     * @param <T> 响应数据的泛型类型
     * @return 错误的响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 错误响应（使用预定义错误码和自定义消息）
     *
     * @param resultCode 错误码枚举
     * @param message 自定义错误消息
     * @param <T> 响应数据的泛型类型
     * @return 错误的响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    /**
     * 错误响应（自定义错误码和消息）
     *
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 响应数据的泛型类型
     * @return 错误的响应结果
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}

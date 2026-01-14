package site.geekie.shop.shoppingmall.exception;

import site.geekie.shop.shoppingmall.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 * 用于封装业务逻辑中的异常信息
 *
 * 使用场景：
 *   - 用户不存在、用户名已存在等用户相关异常
 *   - 商品不存在、库存不足等商品相关异常
 *   - 订单状态不合法等订单相关异常
 *   - 其他业务规则验证失败的场景
 *
 */
@Getter
public class BusinessException extends RuntimeException {
    // 错误码
    private final int code;

    // 错误消息
    private final String message;

    /**
     * 构造业务异常（使用预定义错误码）
     *
     * @param resultCode 错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 构造业务异常（使用预定义错误码和自定义消息）
     *
     * @param resultCode 错误码枚举
     * @param message 自定义错误消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    /**
     * 构造业务异常（自定义错误码和消息）
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}

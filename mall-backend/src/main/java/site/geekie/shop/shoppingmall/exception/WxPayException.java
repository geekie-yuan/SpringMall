package site.geekie.shop.shoppingmall.exception;

import site.geekie.shop.shoppingmall.common.ResultCode;

/**
 * 微信支付业务异常
 * 用于处理微信支付相关的业务异常情况
 */
public class WxPayException extends BusinessException {

    /**
     * 使用错误码和默认消息构造异常
     *
     * @param resultCode 错误码枚举
     */
    public WxPayException(ResultCode resultCode) {
        super(resultCode);
    }

    /**
     * 使用错误码和自定义消息构造异常
     *
     * @param resultCode 错误码枚举
     * @param message 自定义错误消息
     */
    public WxPayException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}

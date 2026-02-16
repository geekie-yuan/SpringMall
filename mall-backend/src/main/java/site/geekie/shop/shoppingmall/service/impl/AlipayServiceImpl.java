package site.geekie.shop.shoppingmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.config.AlipayConfig;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.service.AlipayService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayServiceImpl implements AlipayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;

    @Override
    public String createPayment(String paymentNo, String orderNo, String subject, String body, String totalAmount) {
        try {
            // 创建API请求对象
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

            // 设置异步通知地址
            request.setNotifyUrl(alipayConfig.getNotifyUrl());

            // 设置同步返回地址
            request.setReturnUrl(alipayConfig.getReturnUrl());

            // 创建业务请求参数模型
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(paymentNo);              // 商户订号(使用支付流水号)
            model.setTotalAmount(totalAmount);            // 订单总金额
            model.setSubject(subject);                    // 订单标题
            model.setBody(body);                          // 订单描述
            model.setProductCode("FAST_INSTANT_TRADE_PAY");  // 产品码(PC网站支付固定值)

            // 设置业务参数
            request.setBizModel(model);

            // 调用SDK生成支付表单
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

            if (response.isSuccess()) {
                log.info("支付宝支付表单生成成功 - 支付流水号: {}, 订单号: {}, 商品: {}", paymentNo, orderNo, subject);
                return response.getBody();  // 返回HTML表单
            } else {
                log.error("支付宝支付表单生成失败 - 错误码: {}, 错误信息: {}",
                         response.getCode(), response.getMsg());
                throw new BusinessException(ResultCode.PAYMENT_FAILED);
            }

        } catch (AlipayApiException e) {
            log.error("支付宝API调用异常 - 支付流水号: {}", paymentNo, e);
            throw new BusinessException(ResultCode.PAYMENT_FAILED);
        }
    }

    @Override
    public String queryPaymentStatus(String paymentNo) {
        try {
            // 创建API请求对象
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

            // 创建业务请求参数模型
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(paymentNo);  // 商户订单号

            // 设置业务参数
            request.setBizModel(model);

            // 调用SDK查询支付状态
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                log.info("支付宝支付状态查询成功 - 支付流水号: {}, 状态: {}", paymentNo, tradeStatus);
                return tradeStatus;
            } else {
                log.error("支付宝支付状态查询失败 - 错误码: {}, 错误信息: {}",
                         response.getCode(), response.getMsg());
                return null;
            }

        } catch (AlipayApiException e) {
            log.error("支付宝支付状态查询异常 - 支付流水号: {}", paymentNo, e);
            return null;
        }
    }

    @Override
    public String handleNotify(Map<String, String> params) {
        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (!signVerified) {
                log.error("支付宝异步通知验签失败 - 参数: {}", params);
                return "failure";
            }

            // 获取通知参数
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");  // 支付流水号
            String tradeNo = params.get("trade_no");         // 支付宝交易号
            String totalAmount = params.get("total_amount");

            log.info("支付宝异步通知 - 支付流水号: {}, 交易号: {}, 状态: {}, 金额: {}",
                     outTradeNo, tradeNo, tradeStatus, totalAmount);

            // 交易成功或交易结束,支付成功
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                return "success";
            } else {
                log.warn("支付宝交易状态异常 - 状态: {}", tradeStatus);
                return "failure";
            }

        } catch (AlipayApiException e) {
            log.error("支付宝异步通知处理异常", e);
            return "failure";
        }
    }

    @Override
    public void closePayment(String paymentNo) {
        try {
            // 创建API请求对象
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();

            // 创建业务请求参数模型
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(paymentNo);  // 商户订单号

            // 设置业务参数
            request.setBizModel(model);

            // 调用SDK关闭支付
            AlipayTradeCloseResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                log.info("支付宝支付关闭成功 - 支付流水号: {}", paymentNo);
            } else {
                log.error("支付宝支付关闭失败 - 错误码: {}, 错误信息: {}",
                         response.getCode(), response.getMsg());
                throw new BusinessException(ResultCode.PAYMENT_CLOSE_FAILED);
            }

        } catch (AlipayApiException e) {
            log.error("支付宝支付关闭异常 - 支付流水号: {}", paymentNo, e);
            throw new BusinessException(ResultCode.PAYMENT_CLOSE_FAILED);
        }
    }

    @Override
    public boolean refund(String refundNo, String tradeNo, BigDecimal refundAmount, String refundReason) {
        try {
            // 创建API请求对象
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

            // 创建业务请求参数模型
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setTradeNo(tradeNo);                         // 支付宝交易号
            model.setRefundAmount(refundAmount.toString());    // 退款金额
            model.setRefundReason(refundReason);               // 退款原因
            model.setOutRequestNo(refundNo);                   // 退款请求号(我们的退款流水号)

            // 设置业务参数
            request.setBizModel(model);

            // 调用SDK申请退款
            AlipayTradeRefundResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                log.info("支付宝退款成功 - 退款流水号: {}, 支付宝交易号: {}, 退款金额: {}",
                         refundNo, tradeNo, refundAmount);
                return true;
            } else {
                log.error("支付宝退款失败 - 错误码: {}, 错误信息: {}, 退款流水号: {}, 支付宝交易号: {}",
                         response.getCode(), response.getMsg(), refundNo, tradeNo);
                return false;
            }

        } catch (AlipayApiException e) {
            log.error("支付宝退款异常 - 退款流水号: {}, 支付宝交易号: {}", refundNo, tradeNo, e);
            return false;
        }
    }
}

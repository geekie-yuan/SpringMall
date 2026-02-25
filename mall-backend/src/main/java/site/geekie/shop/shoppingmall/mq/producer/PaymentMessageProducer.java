package site.geekie.shop.shoppingmall.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import site.geekie.shop.shoppingmall.config.RabbitMQConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付消息生产者
 * 负责发送支付相关的延迟消息，用于掉单补偿检查
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发送掉单检查延迟消息
     * 消息将在 TTL（5min）到期后由死信队列转发到 payment.check.queue，触发支付状态查询补偿逻辑
     *
     * @param orderNo   订单号
     * @param paymentId 支付记录 ID
     */
    public void sendPaymentCheckDelayMessage(String orderNo, Long paymentId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderNo", orderNo);
            payload.put("paymentId", paymentId);

            String message = objectMapper.writeValueAsString(payload);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_DELAY_EXCHANGE,
                    RabbitMQConfig.PAYMENT_DELAY_ROUTING_KEY,
                    message
            );
            log.info("已发送掉单检查延迟消息 - 订单号: {}, 支付ID: {}", orderNo, paymentId);
        } catch (JsonProcessingException e) {
            log.error("序列化掉单检查消息失败 - 订单号: {}, 支付ID: {}", orderNo, paymentId, e);
        } catch (Exception e) {
            log.error("发送掉单检查延迟消息失败 - 订单号: {}, 支付ID: {}", orderNo, paymentId, e);
        }
    }
}

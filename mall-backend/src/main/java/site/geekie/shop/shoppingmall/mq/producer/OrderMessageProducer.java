package site.geekie.shop.shoppingmall.mq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import site.geekie.shop.shoppingmall.config.RabbitMQConfig;

/**
 * 订单消息生产者
 * 负责发送订单相关的延迟消息，用于订单超时自动关闭
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送订单超时关闭延迟消息
     * 消息将在 TTL（15min）到期后由死信队列转发到 order.close.queue，触发订单关闭逻辑
     *
     * @param orderNo 订单号
     */
    public void sendOrderCloseDelayMessage(String orderNo) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_DELAY_EXCHANGE,
                    RabbitMQConfig.ORDER_DELAY_ROUTING_KEY,
                    orderNo
            );
            log.info("已发送订单超时关闭延迟消息 - 订单号: {}", orderNo);
        } catch (Exception e) {
            log.error("发送订单超时关闭延迟消息失败 - 订单号: {}", orderNo, e);
        }
    }
}

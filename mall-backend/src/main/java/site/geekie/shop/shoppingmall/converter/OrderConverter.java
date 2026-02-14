package site.geekie.shop.shoppingmall.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import site.geekie.shop.shoppingmall.common.OrderStatus;
import site.geekie.shop.shoppingmall.entity.OrderDO;
import site.geekie.shop.shoppingmall.mapper.OrderItemMapper;
import site.geekie.shop.shoppingmall.vo.OrderItemVO;
import site.geekie.shop.shoppingmall.vo.OrderVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Order 转换器接口
 * 处理 OrderDO -> OrderVO 转换（包含复杂的statusDesc转换和条件性items加载）
 */
@Mapper(componentModel = "spring")
public interface OrderConverter {

    /**
     * 将 OrderDO 转换为 OrderVO（不加载订单明细）
     * 17个基础字段直接映射，statusDesc和items通过default方法处理
     *
     * @param order 订单DO
     * @return 订单VO（items为null）
     */
    @Mapping(target = "statusDesc", ignore = true)
    @Mapping(target = "items", ignore = true)
    OrderVO toVO(OrderDO order);

    /**
     * 将 OrderDO 转换为 OrderVO（不加载订单明细）
     * 带statusDesc枚举转换的完整实现
     *
     * @param order 订单DO
     * @return 订单VO（items为null，statusDesc已填充）
     */
    default OrderVO toVOComplete(OrderDO order) {
        if (order == null) {
            return null;
        }

        OrderVO vo = toVO(order);

        // 设置状态描述，使用try-catch处理枚举转换
        try {
            OrderStatus orderStatus = OrderStatus.fromCode(order.getStatus());
            vo.setStatusDesc(orderStatus.getDescription());
        } catch (IllegalArgumentException e) {
            // 异常时fallback到原status值
            vo.setStatusDesc(order.getStatus());
        }

        return vo;
    }

    /**
     * 将 OrderDO 转换为 OrderVO（加载订单明细）
     * 条件性查询订单明细并填充items列表
     *
     * @param order 订单DO
     * @param orderItemMapper 订单明细Mapper
     * @param orderItemConverter 订单明细转换器
     * @return 订单VO（包含items列表和statusDesc）
     */
    default OrderVO toVOWithItems(OrderDO order,
                                   OrderItemMapper orderItemMapper,
                                   OrderItemConverter orderItemConverter) {
        if (order == null) {
            return null;
        }

        // 先执行基础转换
        OrderVO vo = toVOComplete(order);

        // 条件性加载订单明细
        if (order.getId() != null) {
            List<OrderItemVO> items = orderItemMapper.findByOrderId(order.getId())
                    .stream()
                    .map(orderItemConverter::toVO)
                    .collect(Collectors.toList());
            vo.setItems(items);
        }

        return vo;
    }

    /**
     * 批量转换订单列表（不加载明细）
     *
     * @param orders 订单DO列表
     * @return 订单VO列表（不含items）
     */
    default List<OrderVO> toVOList(List<OrderDO> orders) {
        if (orders == null) {
            return null;
        }
        return orders.stream()
                .map(this::toVOComplete)
                .collect(Collectors.toList());
    }
}

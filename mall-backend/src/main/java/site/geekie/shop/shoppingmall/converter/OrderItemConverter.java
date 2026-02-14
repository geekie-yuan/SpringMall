package site.geekie.shop.shoppingmall.converter;

import org.mapstruct.Mapper;
import site.geekie.shop.shoppingmall.entity.OrderItemDO;
import site.geekie.shop.shoppingmall.vo.OrderItemVO;

import java.util.List;

/**
 * OrderItem 转换器接口
 * 处理 OrderItemDO -> OrderItemVO 转换
 */
@Mapper(componentModel = "spring")
public interface OrderItemConverter {

    /**
     * 将 OrderItemDO 转换为 OrderItemVO
     * 7个字段的简单映射: id, productId, productName, productImage, unitPrice, quantity, totalPrice
     *
     * @param item 订单明细DO
     * @return 订单明细VO
     */
    OrderItemVO toVO(OrderItemDO item);

    /**
     * 批量转换订单明细列表
     *
     * @param items 订单明细DO列表
     * @return 订单明细VO列表
     */
    List<OrderItemVO> toVOList(List<OrderItemDO> items);
}

package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.OrderItem;

import java.util.List;

/**
 * 订单明细Mapper接口
 * 提供订单明细的数据访问方法
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 根据订单ID查询所有订单明细
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 批量插入订单明细
     *
     * @param items 订单明细列表
     * @return 影响行数
     */
    int batchInsert(@Param("items") List<OrderItem> items);

    /**
     * 插入单个订单明细
     *
     * @param item 订单明细
     * @return 影响行数
     */
    int insert(OrderItem item);
}

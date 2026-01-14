package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.Order;

import java.util.List;

/**
 * 订单Mapper接口
 * 提供订单的数据访问方法
 */
@Mapper
public interface OrderMapper {

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息，不存在返回null
     */
    Order findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据用户ID查询所有订单
     * 按创建时间倒序排列
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和订单状态查询订单
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询所有订单（管理员用）
     * 按创建时间倒序排列
     *
     * @return 订单列表
     */
    List<Order> findAll();

    /**
     * 根据订单状态查询所有订单（管理员用）
     * 按创建时间倒序排列
     *
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findAllByStatus(@Param("status") String status);

    /**
     * 插入订单
     *
     * @param order 订单信息
     * @return 影响行数
     */
    int insert(Order order);

    /**
     * 更新订单状态
     *
     * @param orderNo 订单号
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") String status);

    /**
     * 更新支付时间
     *
     * @param orderNo 订单号
     * @return 影响行数
     */
    int updatePaymentTime(@Param("orderNo") String orderNo);

    /**
     * 更新发货时间
     *
     * @param orderNo 订单号
     * @return 影响行数
     */
    int updateShipTime(@Param("orderNo") String orderNo);

    /**
     * 更新完成时间
     *
     * @param orderNo 订单号
     * @return 影响行数
     */
    int updateCompleteTime(@Param("orderNo") String orderNo);

    /**
     * 统计用户订单数
     *
     * @param userId 用户ID
     * @return 订单数
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计所有订单数（管理员用）
     *
     * @return 订单数
     */
    int countAll();
}

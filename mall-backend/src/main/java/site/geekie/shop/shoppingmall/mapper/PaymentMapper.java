package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.PaymentDO;

/**
 * 支付记录Mapper
 */
@Mapper
public interface PaymentMapper {

    /**
     * 根据ID查询支付记录
     */
    PaymentDO findById(@Param("id") Long id);

    /**
     * 根据支付流水号查询支付记录
     */
    PaymentDO findByPaymentNo(@Param("paymentNo") String paymentNo);

    /**
     * 根据订单号查询支付记录
     */
    PaymentDO findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 插入支付记录
     */
    int insert(PaymentDO payment);

    /**
     * 更新支付记录
     */
    int updateById(PaymentDO payment);

    /**
     * 更新支付状态
     */
    int updateStatus(@Param("paymentNo") String paymentNo,
                     @Param("paymentStatus") String paymentStatus,
                     @Param("tradeNo") String tradeNo);
}

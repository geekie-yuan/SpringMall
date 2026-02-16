package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.RefundDO;

import java.time.LocalDateTime;

/**
 * 退款记录Mapper
 */
@Mapper
public interface RefundMapper {

    /**
     * 根据ID查询退款记录
     */
    RefundDO findById(@Param("id") Long id);

    /**
     * 根据退款流水号查询退款记录
     */
    RefundDO findByRefundNo(@Param("refundNo") String refundNo);

    /**
     * 根据订单号查询退款记录
     */
    RefundDO findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据支付流水号查询退款记录
     */
    RefundDO findByPaymentNo(@Param("paymentNo") String paymentNo);

    /**
     * 插入退款记录
     */
    int insert(RefundDO refund);

    /**
     * 更新退款记录
     */
    int updateById(RefundDO refund);

    /**
     * 更新退款状态
     */
    int updateStatus(@Param("refundNo") String refundNo,
                     @Param("refundStatus") String refundStatus,
                     @Param("refundTime") LocalDateTime refundTime);
}

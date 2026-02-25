package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.PaymentDO;

import java.util.List;

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
     * 查询订单下 SUCCESS 状态的支付记录（用于退款、已支付校验）
     *
     * @param orderNo 订单号
     * @return SUCCESS 状态的支付记录，不存在时返回 null
     */
    PaymentDO findSuccessByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据第三方交易号查询支付记录
     *
     * @param tradeNo 第三方交易号
     * @return 支付记录
     */
    PaymentDO findByTradeNo(@Param("tradeNo") String tradeNo);

    /**
     * 查询订单下所有 PENDING 状态的支付记录（用于支付方式互斥）
     *
     * @param orderNo 订单号
     * @return PENDING 状态的支付记录列表
     */
    List<PaymentDO> findPendingByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询订单下所有 SUCCESS 状态的支付记录（按创建时间升序）
     *
     * @param orderNo 订单号
     * @return SUCCESS 状态的支付记录列表
     */
    List<PaymentDO> findSuccessListByOrderNo(@Param("orderNo") String orderNo);

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

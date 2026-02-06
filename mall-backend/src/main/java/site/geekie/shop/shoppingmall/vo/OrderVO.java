package site.geekie.shop.shoppingmall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freight;
    private String status;
    private String statusDesc;
    private LocalDateTime paymentTime;
    private LocalDateTime shipTime;
    private LocalDateTime completeTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private List<OrderItemVO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

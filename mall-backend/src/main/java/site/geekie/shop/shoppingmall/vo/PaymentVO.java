package site.geekie.shop.shoppingmall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVO {
    private String orderNo;
    private BigDecimal payAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String message;
    private String transactionNo;
}

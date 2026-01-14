package site.geekie.shop.shoppingmall.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 订单号生成工具类
 * 生成唯一的订单号
 *
 * 订单号格式：yyyyMMddHHmmss + 6位随机数
 * 例如：20260107125959123456
 */
public class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    /**
     * 生成订单号
     *
     * @return 订单号
     */
    public static String generateOrderNo() {
        // 获取当前时间戳部分（14位）
        String timestamp = LocalDateTime.now().format(FORMATTER);

        // 生成6位随机数
        int randomNum = RANDOM.nextInt(900000) + 100000; // 100000-999999

        // 拼接订单号
        return timestamp + randomNum;
    }
}

package site.geekie.shop.shoppingmall.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志数据对象
 * 用于记录系统中所有关键操作的审计日志
 *
 * @author yuan
 * @since 2026-02-06
 */
@Data
public class AuditLogDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 操作用户ID（未登录时为NULL）
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作描述
     * 例如："创建订单"、"修改商品价格"、"删除用户"
     */
    private String operation;

    /**
     * 操作类型
     * CREATE/UPDATE/DELETE/READ/LOGIN/PAYMENT
     */
    private String type;

    /**
     * 方法签名
     * 例如："OrderServiceImpl.createOrder"
     */
    private String method;

    /**
     * 请求参数（JSON格式）
     */
    private String params;

    /**
     * 执行结果
     * SUCCESS/FAILURE
     */
    private String result;

    /**
     * 错误信息（失败时记录）
     */
    private String errorMsg;

    /**
     * 执行耗时（毫秒）
     */
    private Integer duration;

    /**
     * 操作IP地址
     */
    private String ip;

    /**
     * 用户代理
     * 记录浏览器或客户端信息
     */
    private String userAgent;

    /**
     * 操作时间
     */
    private LocalDateTime createdAt;
}

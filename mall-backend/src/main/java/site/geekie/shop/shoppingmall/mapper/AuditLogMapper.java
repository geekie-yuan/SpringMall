package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.AuditLogDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志 Mapper
 * 负责审计日志的数据库操作
 *
 * @author yuan
 * @since 2026-02-06
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入审计日志
     *
     * @param auditLog 审计日志对象
     * @return 影响行数
     */
    int insert(AuditLogDO auditLog);

    /**
     * 根据ID查询审计日志
     *
     * @param id 审计日志ID
     * @return 审计日志对象
     */
    AuditLogDO findById(@Param("id") Long id);

    /**
     * 根据用户ID查询审计日志列表
     *
     * @param userId 用户ID
     * @return 审计日志列表
     */
    List<AuditLogDO> findByUserId(@Param("userId") Long userId);

    /**
     * 根据操作类型查询审计日志列表
     *
     * @param type 操作类型
     * @return 审计日志列表
     */
    List<AuditLogDO> findByType(@Param("type") String type);

    /**
     * 根据日期范围查询审计日志列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 审计日志列表
     */
    List<AuditLogDO> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 查询审计日志总数
     *
     * @return 总数
     */
    long countAll();
}

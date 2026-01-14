package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.User;

/**
 * 用户数据访问接口
 * MyBatis Mapper接口，提供用户表的CRUD
 *
 * 映射文件：UserMapper.xml
 * 数据库表：user
 * 主要功能：
 *   - 查询：根据ID、用户名、邮箱、手机号查询用户
 *   - 插入：创建新用户记录
 *   - 更新：更新用户信息、密码、状态
 *
 */
public interface UserMapper {

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体，不存在则返回null
     */
    User findById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     * 用户名具有唯一性约束
     *
     * @param username 用户名
     * @return 用户实体，不存在则返回null
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     * 邮箱具有唯一性约束
     *
     * @param email 邮箱地址
     * @return 用户实体，不存在则返回null
     */
    User findByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     * 手机号具有唯一性约束
     *
     * @param phone 手机号
     * @return 用户实体，不存在则返回null
     */
    User findByPhone(@Param("phone") String phone);

    /**
     * 插入新用户记录
     * 使用数据库自动生成的主键ID
     *
     * @param user 用户实体（插入后会自动填充ID）
     * @return 影响的行数（成功为1）
     */
    int insert(User user);

    /**
     * 根据用户ID更新用户信息
     * 使用动态SQL，仅更新非null字段
     *
     * @param user 包含待更新字段的用户实体（必须包含ID）
     * @return 影响的行数（成功为1）
     */
    int updateById(User user);

    /**
     * 更新用户密码
     *
     * @param id 用户ID
     * @param password 新密码（应已加密）
     * @return 影响的行数（成功为1）
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新用户状态
     * 用于启用或禁用用户账户
     *
     * @param id 用户ID
     * @param status 用户状态（1-正常，0-禁用）
     * @return 影响的行数（成功为1）
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // ===== 管理员方法 =====

    /**
     * 查询所有用户（管理员）
     * 按创建时间倒序排列
     *
     * @return 用户列表
     */
    java.util.List<User> findAll();

    /**
     * 更新用户角色（管理员）
     *
     * @param id 用户ID
     * @param role 用户角色（USER/ADMIN）
     * @return 影响的行数（成功为1）
     */
    int updateRole(@Param("id") Long id, @Param("role") String role);
}

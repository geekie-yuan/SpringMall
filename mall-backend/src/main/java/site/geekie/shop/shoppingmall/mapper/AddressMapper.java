package site.geekie.shop.shoppingmall.mapper;

import org.apache.ibatis.annotations.Param;
import site.geekie.shop.shoppingmall.entity.Address;

import java.util.List;

/**
 * 收货地址数据访问接口
 * MyBatis Mapper接口，提供收货地址表的CRUD操作
 *
 * 映射文件：AddressMapper.xml
 * 数据库表：address
 * 主要功能：
 *   - 查询：根据ID、用户ID查询地址，获取默认地址
 *   - 插入：创建新地址记录
 *   - 更新：更新地址信息、设置默认地址
 *   - 删除：删除地址
 */
public interface AddressMapper {

    /**
     * 根据地址ID查询地址
     *
     * @param id 地址ID
     * @return 地址实体，不存在则返回null
     */
    Address findById(@Param("id") Long id);

    /**
     * 根据用户ID查询该用户的所有地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> findByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的默认地址
     *
     * @param userId 用户ID
     * @return 默认地址，不存在则返回null
     */
    Address findDefaultByUserId(@Param("userId") Long userId);

    /**
     * 插入新地址记录
     * 使用数据库自动生成的主键ID
     *
     * @param address 地址实体（插入后会自动填充ID）
     * @return 影响的行数（成功为1）
     */
    int insert(Address address);

    /**
     * 根据地址ID更新地址信息
     * 使用动态SQL，仅更新非null字段
     *
     * @param address 包含待更新字段的地址实体（必须包含ID）
     * @return 影响的行数（成功为1）
     */
    int updateById(Address address);

    /**
     * 取消用户的所有默认地址
     * 在设置新的默认地址前调用
     *
     * @param userId 用户ID
     * @return 影响的行数
     */
    int cancelDefaultByUserId(@Param("userId") Long userId);

    /**
     * 设置地址为默认地址
     *
     * @param id 地址ID
     * @return 影响的行数（成功为1）
     */
    int setDefault(@Param("id") Long id);

    /**
     * 删除地址
     *
     * @param id 地址ID
     * @return 影响的行数（成功为1）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计用户的地址数量
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    int countByUserId(@Param("userId") Long userId);
}

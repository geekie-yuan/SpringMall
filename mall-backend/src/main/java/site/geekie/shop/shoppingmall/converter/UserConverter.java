package site.geekie.shop.shoppingmall.converter;

import org.mapstruct.Mapper;
import site.geekie.shop.shoppingmall.entity.UserDO;
import site.geekie.shop.shoppingmall.vo.UserVO;

import java.util.List;

/**
 * 用户实体转换器
 * 使用 MapStruct 将 UserDO 转换为 UserVO
 *
 * 转换规则：
 *   - UserVO 中不包含 password 字段，自动过滤敏感信息
 *   - 自动映射同名字段：id, username, email, phone, avatar, role, status, createdAt
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * 将 UserDO 转换为 UserVO
     * 自动映射同名字段，UserVO 中无 password 字段，自动排除
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    UserVO toVO(UserDO user);

    /**
     * 批量转换 UserDO 列表为 UserVO 列表
     *
     * @param users 用户实体列表
     * @return 用户视图对象列表
     */
    List<UserVO> toVOList(List<UserDO> users);
}

package site.geekie.shop.shoppingmall.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import site.geekie.shop.shoppingmall.entity.User;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security用户详情实现类
 * 将自定义User实体适配为Spring Security所需的UserDetails接口
 *
 * 该类作为适配器，将业务实体User包装成Spring Security认证体系所需的UserDetails对象。
 * 主要职责：
 *   - 提供用户认证所需的核心信息（用户名、密码、权限）
 *   - 控制账户状态（是否过期、锁定、启用等）
 *   - 桥接业务层User实体和安全层UserDetails接口
 */
@Data
@AllArgsConstructor
public class SecurityUser implements UserDetails {

    // 用户实体对象，包含用户的所有业务信息
    private User user;

    /**
     * 获取用户的权限列表
     * 将用户角色转换为Spring Security的GrantedAuthority格式
     *
     * @return 用户权限集合，包含角色信息（格式：ROLE_角色名）
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    /**
     * 获取用户密码
     *
     * @return 加密后的用户密码
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 账户是否未过期
     * 本系统账户永不过期
     *
     * @return 始终返回true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     * 根据用户状态判断账户是否被锁定
     *
     * @return true-账户未锁定（状态为1），false-账户已锁定（状态为0）
     */
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() == 1;
    }

    /**
     * 凭证（密码）是否未过期
     * 本系统凭证永不过期
     *
     * @return 始终返回true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否启用
     * 根据用户状态判断账户是否启用
     *
     * @return true-账户已启用（状态为1），false-账户已禁用（状态为0）
     */
    @Override
    public boolean isEnabled() {
        return user.getStatus() == 1;
    }
}

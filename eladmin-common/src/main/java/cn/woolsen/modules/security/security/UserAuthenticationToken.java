package cn.woolsen.modules.security.security;

import cn.woolsen.modules.system.domain.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.Set;

/**
 * @author Woolsen
 * @since 2022/12/8 09:02
 */
public class UserAuthenticationToken implements Authentication {

    private final UserDto user;
    private final String password;
    private final Set<GrantedAuthority> authorities;
    private boolean authenticated;

    public UserAuthenticationToken(
            UserDto user,
            String password,
            Set<? extends GrantedAuthority> authorities,
            boolean authenticated
    ) {
        this.user = user;
        this.password = password;
        this.authorities = Collections.unmodifiableSet(authorities);
        this.authenticated = authenticated;
    }

    @Override
    public String getCredentials() {
        return password;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public UserDto getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public Set<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * @return 用户名
     */
    @Override
    public String getName() {
        return user.getUsername();
    }
}

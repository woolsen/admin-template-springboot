package cn.woolsen.modules.security.service;

import cn.woolsen.exception.BadRequestException;
import cn.woolsen.exception.EntityNotFoundException;
import cn.woolsen.modules.security.config.bean.LoginProperties;
import cn.woolsen.modules.security.config.bean.SecurityProperties;
import cn.woolsen.modules.security.domain.dto.AuthorityDto;
import cn.woolsen.modules.security.domain.dto.AuthorizationDto;
import cn.woolsen.modules.security.security.TokenProvider;
import cn.woolsen.modules.security.security.UserAuthenticationToken;
import cn.woolsen.modules.system.domain.dto.UserDto;
import cn.woolsen.modules.system.service.RoleService;
import cn.woolsen.modules.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author Woolsen
 * @since 2022/12/8 08:35
 */
@RequiredArgsConstructor
@Service
public class AuthorizationService {

    private final TokenProvider tokenProvider;
    private final OnlineUserService onlineUserService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RoleService roleService;
    private final SecurityProperties properties;
    @Resource
    private LoginProperties loginProperties;

    public AuthorizationDto loginByUsername(String username, String password, HttpServletRequest request) {
        UserDto user;
        try {
            user = userService.findByUsername(username);
        } catch (EntityNotFoundException e) {
            throw new BadRequestException("用户名或密码错误");
        }
        if (!user.getEnabled()) {
            throw new BadRequestException("账号未激活！");
        } else if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }
        String token = tokenProvider.createToken(user.getId());
        final Set<AuthorityDto> authorities = roleService.mapToGrantedAuthorities(user);
        UserAuthenticationToken authentication = new UserAuthenticationToken(
                user, token, authorities, true
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        onlineUserService.save(user, token, request);
        if (loginProperties.isSingleLogin()) {
            //踢掉之前已经登录的token
            onlineUserService.checkLoginOnUser(user.getId(), token);
        }
        return new AuthorizationDto(
                properties.getTokenStartWith() + token,
                user
        );
    }

}

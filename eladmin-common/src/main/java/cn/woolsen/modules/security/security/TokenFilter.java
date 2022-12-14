/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package cn.woolsen.modules.security.security;

import cn.woolsen.config.config.bean.SecurityProperties;
import cn.woolsen.modules.security.domain.dto.AuthorityDto;
import cn.woolsen.modules.security.service.OnlineUserService;
import cn.woolsen.modules.system.domain.dto.UserDto;
import cn.woolsen.modules.system.service.RoleService;
import cn.woolsen.modules.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

/**
 * @author /
 */
@Component
@RequiredArgsConstructor
public class TokenFilter extends GenericFilterBean {
    private static final Logger log = LoggerFactory.getLogger(TokenFilter.class);

    private final TokenProvider tokenProvider;
    private final SecurityProperties properties;
    private final OnlineUserService onlineUserService;
    private final RoleService roleService;
    private final UserService userService;

//    private final UserCacheManager userCacheManager;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String token = resolveToken(httpServletRequest);
        // ?????? Token ???????????????????????? Redis
        if (token == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        Long userId = onlineUserService.getUserIdByToken(token);
        if (userId != null && StringUtils.hasText(token)) {
            UserDto user = userService.findById(userId);
            Set<AuthorityDto> authorities = roleService.mapToGrantedAuthorities(user);
            Authentication authentication = new UserAuthenticationToken(user, token, authorities, true);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Token ??????
            tokenProvider.checkRenewal(token);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * ????????????Token
     *
     * @param request /
     * @return /
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(properties.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(properties.getTokenStartWith())) {
            // ??????????????????
            return bearerToken.replace(properties.getTokenStartWith(), "");
        } else {
            log.debug("??????Token???{}", bearerToken);
        }
        return null;
    }
}

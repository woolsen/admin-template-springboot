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
package cn.woolsen.modules.security.service;

import cn.woolsen.base.PageDTO;
import cn.woolsen.config.config.bean.SecurityProperties;
import cn.woolsen.modules.security.domain.dto.OnlineUserDto;
import cn.woolsen.modules.security.security.TokenProvider;
import cn.woolsen.modules.system.domain.dto.UserDto;
import cn.woolsen.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Zheng Jie
 * @date 2019年10月26日21:56:27
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class OnlineUserService {

    private final SecurityProperties properties;
    private final RedisUtils redisUtils;

    /**
     * 保存在线用户信息
     *
     * @param jwtUserDto /
     * @param token      /
     * @param request    /
     */
    public void save(UserDto user, String token, HttpServletRequest request) {
        String dept = user.getDept().getName();
        String ip = StringUtils.getIp(request);
        String browser = StringUtils.getBrowser(request);
        String address = StringUtils.getCityInfo(ip);
        OnlineUserDto onlineUserDto = null;
        try {
            onlineUserDto = new OnlineUserDto(user.getId(), user.getUsername(), user.getNickname(), dept, browser, ip, address, EncryptUtils.desEncrypt(token), new Date());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        Long userId = user.getId();
        long expireInSec = properties.getTokenValidityInSeconds() / 1000;
        redisUtils.set(TokenProvider.ONLINE_TOKEN_KEY + token, userId, expireInSec);
        redisUtils.set(TokenProvider.ONLINE_USER_KEY + userId, onlineUserDto, expireInSec);
    }

    /**
     * 查询全部数据
     *
     * @param filter   /
     * @param pageable /
     * @return /
     */
    public PageDTO<OnlineUserDto> getAll(String filter, Pageable pageable) {
        List<OnlineUserDto> onlineUserDtos = getAll(filter);
        return PageUtil.toPage(
                PageUtil.toPage(pageable.getPageNumber(), pageable.getPageSize(), onlineUserDtos),
                (long) onlineUserDtos.size()
        );
    }

    /**
     * 查询全部数据，不分页
     *
     * @param filter /
     * @return /
     */
    public List<OnlineUserDto> getAll(String filter) {
        List<String> keys = redisUtils.scan(TokenProvider.ONLINE_USER_KEY + "*");
        Collections.reverse(keys);
        List<OnlineUserDto> onlineUserDtos = new ArrayList<>();
        for (String key : keys) {
            OnlineUserDto onlineUserDto = (OnlineUserDto) redisUtils.get(key);
            if (StringUtils.isNotBlank(filter)) {
                if (onlineUserDto.toString().contains(filter)) {
                    onlineUserDtos.add(onlineUserDto);
                }
            } else {
                onlineUserDtos.add(onlineUserDto);
            }
        }
        onlineUserDtos.sort((o1, o2) -> o2.getLoginTime().compareTo(o1.getLoginTime()));
        return onlineUserDtos;
    }

    public OnlineUserDto get(Long userId) {
        return (OnlineUserDto) redisUtils.get(TokenProvider.ONLINE_USER_KEY + userId);
    }

    /**
     * 通过Token获取用户ID
     */
    @Nullable
    public Long getUserIdByToken(@NonNull String token) {
        String key = TokenProvider.ONLINE_TOKEN_KEY + token;
        return (Long) redisUtils.get(key);
    }

    /**
     * 踢出用户
     *
     * @param token /
     */
    public void kickOutByToken(String token) {
        logoutByToken(token);
    }

    /**
     * 踢出用户
     *
     * @param userId UserId
     */
    public void kickOutByUserId(Long userId) {
        String key = TokenProvider.ONLINE_USER_KEY + userId;
        OnlineUserDto onlineUserDto = (OnlineUserDto) redisUtils.get(key);
        redisUtils.del(key);
        try {
            String token = EncryptUtils.desDecrypt(onlineUserDto.getEncryptedToken());
            redisUtils.del(TokenProvider.ONLINE_TOKEN_KEY + token);
        } catch (Exception e) {
            log.error("kickUser is error", e);
        }
    }

    /**
     * 退出登录
     *
     * @param token /
     */
    public void logoutByToken(String token) {
        String key = TokenProvider.ONLINE_TOKEN_KEY + token;
        Long userId = (Long) redisUtils.get(key);
        redisUtils.del(key);
        redisUtils.del(TokenProvider.ONLINE_USER_KEY + userId);
    }

    /**
     * 导出
     *
     * @param all      /
     * @param response /
     * @throws IOException /
     */
    public void download(List<OnlineUserDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (OnlineUserDto user : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", user.getUserName());
            map.put("部门", user.getDept());
            map.put("登录IP", user.getIp());
            map.put("登录地点", user.getAddress());
            map.put("浏览器", user.getBrowser());
            map.put("登录日期", user.getLoginTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    /**
     * 查询用户
     *
     * @param token /
     * @return /
     */
    public OnlineUserDto getByToken(String token) {
        String key = TokenProvider.ONLINE_TOKEN_KEY + token;
        Long userId = (Long) redisUtils.get(key);
        if (userId == null) {
            return null;
        }
        return (OnlineUserDto) redisUtils.get(TokenProvider.ONLINE_USER_KEY + userId);
    }

    /**
     * 检测用户是否在之前已经登录，已经登录踢下线
     *
     * @param userId 用户ID
     */
    public void checkLoginOnUser(Long userId, String ignoreToken) {
        OnlineUserDto onlineUserDto = get(userId);
        if (onlineUserDto == null) {
            return;
        }
        try {
            String token = EncryptUtils.desDecrypt(onlineUserDto.getEncryptedToken());
            if (StringUtils.isNotBlank(ignoreToken) && !ignoreToken.equals(token)) {
                this.kickOutByToken(token);
            } else if (StringUtils.isBlank(ignoreToken)) {
                this.kickOutByToken(token);
            }
        } catch (Exception e) {
            log.error("checkUser is error", e);
        }
    }

//    /**
//     * 根据用户名强退用户
//     *
//     * @param username /
//     */
//    @Async
//    public void kickOutForUsername(String username) throws Exception {
//        List<OnlineUserDto> onlineUsers = getAll(username);
//        for (OnlineUserDto onlineUser : onlineUsers) {
//            if (onlineUser.getUserName().equals(username)) {
//                String token = EncryptUtils.desDecrypt(onlineUser.getKey());
//                kickOut(token);
//            }
//        }
//    }
}

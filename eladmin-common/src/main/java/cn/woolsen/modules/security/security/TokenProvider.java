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

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.woolsen.config.config.bean.SecurityProperties;
import cn.woolsen.utils.RedisUtils;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author /
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {

    public static final String AUTHORITIES_KEY = "user";
    public static final String ONLINE_TOKEN_KEY = "online:token:";
    public static final String ONLINE_USER_KEY = "online:user:";
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private JwtBuilder jwtBuilder;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getBase64Secret());
        Key key = Keys.hmacShaKeyFor(keyBytes);
        jwtBuilder = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS512);
    }

    /**
     * ??????Token ?????????????????????
     * Token ????????????????????????Redis ??????
     *
     * @param userId /
     * @return /
     */
    public String createToken(Long userId) {
        return jwtBuilder
                // ??????ID??????????????? Token ????????????
                .setId(IdUtil.simpleUUID())
                .claim(AUTHORITIES_KEY, userId)
                .setSubject(userId.toString())
                .compact();
    }


    /**
     * @param token ???????????????token
     */
    public void checkRenewal(String token) {
        // ??????????????????token,??????token???????????????
        long time = redisUtils.getExpire(ONLINE_USER_KEY + token) * 1000;
        Date expireDate = DateUtil.offset(new Date(), DateField.MILLISECOND, (int) time);
        // ?????????????????????????????????????????????
        long differ = expireDate.getTime() - System.currentTimeMillis();
        // ?????????????????????????????????????????????
        if (differ <= properties.getDetect()) {
            long renew = time + properties.getRenew();
//            redisUtils.expire(ONLINE_USER_KEY + token, renew, TimeUnit.MILLISECONDS);
            //TOKEN???
            redisUtils.expire(ONLINE_TOKEN_KEY + token, renew, TimeUnit.MILLISECONDS);
        }
    }

    public String getToken(HttpServletRequest request) {
        final String requestHeader = request.getHeader(properties.getHeader());
        if (requestHeader != null && requestHeader.startsWith(properties.getTokenStartWith())) {
            return requestHeader.substring(7);
        }
        return null;
    }
}

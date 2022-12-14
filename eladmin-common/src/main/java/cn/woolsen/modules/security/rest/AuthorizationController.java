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
package cn.woolsen.modules.security.rest;

import cn.hutool.core.util.IdUtil;
import cn.woolsen.annotation.Log;
import cn.woolsen.annotation.rest.AnonymousDeleteMapping;
import cn.woolsen.annotation.rest.AnonymousGetMapping;
import cn.woolsen.annotation.rest.AnonymousPostMapping;
import cn.woolsen.config.RsaProperties;
import cn.woolsen.config.config.bean.LoginCodeEnum;
import cn.woolsen.config.config.bean.LoginProperties;
import cn.woolsen.config.config.bean.SecurityProperties;
import cn.woolsen.exception.BadRequestException;
import cn.woolsen.modules.security.domain.dto.AuthUserDto;
import cn.woolsen.modules.security.domain.dto.AuthorizationDto;
import cn.woolsen.modules.security.security.TokenProvider;
import cn.woolsen.modules.security.service.AuthorizationService;
import cn.woolsen.modules.security.service.OnlineUserService;
import cn.woolsen.modules.system.domain.dto.UserDto;
import cn.woolsen.utils.RedisUtils;
import cn.woolsen.utils.RsaUtils;
import cn.woolsen.utils.SecurityUtils;
import cn.woolsen.utils.StringUtils;
import com.wf.captcha.base.Captcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 * ???????????????token????????????????????????
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Api(tags = "???????????????????????????")
public class AuthorizationController {
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final OnlineUserService onlineUserService;
    private final TokenProvider tokenProvider;
    private final AuthorizationService authorizationService;
    @Resource
    private LoginProperties loginProperties;


    @Log("????????????")
    @ApiOperation("????????????")
    @AnonymousPostMapping(value = "/login")
    public AuthorizationDto login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) throws Exception {
        // ????????????
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
        // ???????????????
        String code = (String) redisUtils.get(authUser.getUuid());
        // ???????????????
        redisUtils.del(authUser.getUuid());
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("??????????????????????????????");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("???????????????");
        }
        return authorizationService.loginByUsername(authUser.getUsername(), password, request);
    }

    @ApiOperation("??????????????????")
    @GetMapping(value = "/info")
    public UserDto getUserInfo() {
        return SecurityUtils.getCurrentUser();
    }

    @ApiOperation("???????????????")
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // ?????????????????????
        Captcha captcha = loginProperties.getCaptcha();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        //????????????????????? arithmetic???????????? >= 2 ??????captcha.text()??????????????????????????????
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.ARITHMETIC.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        // ??????
        redisUtils.set(uuid, captchaValue, loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
        // ???????????????
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {{
            put("img", captcha.toBase64());
            put("uuid", uuid);
        }};
        return ResponseEntity.ok(imgResult);
    }

    @ApiOperation("????????????")
    @AnonymousDeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logoutByToken(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

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
import cn.woolsen.exception.BadRequestException;
import cn.woolsen.modules.security.config.bean.LoginCodeEnum;
import cn.woolsen.modules.security.config.bean.LoginProperties;
import cn.woolsen.modules.security.config.bean.SecurityProperties;
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
 * 授权、根据token获取用户详细信息
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Api(tags = "系统：系统授权接口")
public class AuthorizationController {
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final OnlineUserService onlineUserService;
    private final TokenProvider tokenProvider;
    private final AuthorizationService authorizationService;
    @Resource
    private LoginProperties loginProperties;


    @Log("用户登录")
    @ApiOperation("登录授权")
    @AnonymousPostMapping(value = "/login")
    public AuthorizationDto login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) throws Exception {
        // 密码解密
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
        // 查询验证码
        String code = (String) redisUtils.get(authUser.getUuid());
        // 清除验证码
        redisUtils.del(authUser.getUuid());
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("验证码不存在或已过期");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("验证码错误");
        }
        return authorizationService.loginByUsername(authUser.getUsername(), password, request);
    }

    @ApiOperation("获取用户信息")
    @GetMapping(value = "/info")
    public UserDto getUserInfo() {
        return SecurityUtils.getCurrentUser();
    }

    @ApiOperation("获取验证码")
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // 获取运算的结果
        Captcha captcha = loginProperties.getCaptcha();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        //当验证码类型为 arithmetic时且长度 >= 2 时，captcha.text()的结果有几率为浮点型
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.ARITHMETIC.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        // 保存
        redisUtils.set(uuid, captchaValue, loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
        // 验证码信息
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {{
            put("img", captcha.toBase64());
            put("uuid", uuid);
        }};
        return ResponseEntity.ok(imgResult);
    }

    @ApiOperation("退出登录")
    @AnonymousDeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logoutByToken(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

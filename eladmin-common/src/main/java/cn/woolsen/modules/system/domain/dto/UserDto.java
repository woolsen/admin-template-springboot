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
package cn.woolsen.modules.system.domain.dto;

import cn.woolsen.base.BaseDTO;
import cn.woolsen.modules.security.domain.dto.AuthorityDto;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Getter
@Setter
public class UserDto extends BaseDTO implements Serializable {

    private Long id;

    private Set<RoleSmallDto> roles;

    private Set<JobSmallDto> jobs;

    private Set<AuthorityDto> authorities;

    private Set<Long> dataScopes;

    private DeptSmallDto dept;

    private Long deptId;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String gender;

    private String avatarName;

    private String avatarPath;

    @JSONField(serialize = false)
    private String password;

    private Boolean enabled;

    @JSONField(serialize = false)
    private Boolean isAdmin = false;

    private Date pwdResetTime;
}

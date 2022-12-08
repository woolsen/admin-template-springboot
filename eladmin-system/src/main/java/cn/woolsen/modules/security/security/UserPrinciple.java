package cn.woolsen.modules.security.security;

import lombok.Data;

/**
 * @author Woolsen
 * @since 2022/12/8 15:46
 */
@Data
public class UserPrinciple {

    private Long id;

    private String nickname;

    private String username;

    private String phone;
}

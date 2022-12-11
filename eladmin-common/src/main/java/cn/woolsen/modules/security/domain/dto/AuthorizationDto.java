package cn.woolsen.modules.security.domain.dto;


import cn.woolsen.modules.system.domain.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorizationDto {

    private String token;

    private UserDto user;

}

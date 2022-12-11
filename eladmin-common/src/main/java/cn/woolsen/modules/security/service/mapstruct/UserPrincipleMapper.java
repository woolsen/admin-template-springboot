package cn.woolsen.modules.security.service.mapstruct;

import cn.woolsen.modules.security.security.UserPrinciple;
import cn.woolsen.modules.system.domain.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author Woolsen
 * @since 2022/12/8 16:57
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPrincipleMapper {

    UserPrinciple toPrinciple(UserDto user);
}

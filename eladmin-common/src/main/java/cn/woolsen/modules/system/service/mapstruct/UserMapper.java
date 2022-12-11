package cn.woolsen.modules.system.service.mapstruct;

import cn.woolsen.base.BaseMapper;
import cn.woolsen.modules.system.domain.User;
import cn.woolsen.modules.system.domain.dto.UserDto;
import cn.woolsen.modules.system.service.DataService;
import cn.woolsen.modules.system.service.RoleService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, DeptMapper.class, JobMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper implements BaseMapper<UserDto, User> {

    @Autowired
    protected RoleService roleService;

    @Autowired
    protected DataService dataService;

    @Override
    @Mapping(target = "authorities", expression = "java(roleService.mapToGrantedAuthorities(entity))")
    @Mapping(target = "dataScopes", expression = "java(dataService.getDeptIds(userDto))")
    public abstract UserDto toDto(User entity);

}

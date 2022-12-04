package ${package}.service.mapstruct;

import cn.woolsen.base.BaseMapper;
import ${package}.domain.${className};
import ${package}.domain.dto.${className}Dto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
* @website https://eladmin.vip
* @author ${author}
* @date ${date}
**/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ${className}Mapper extends BaseMapper<${className}Dto, ${className}> {

}
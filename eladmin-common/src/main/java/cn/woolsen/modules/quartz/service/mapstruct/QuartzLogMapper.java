package cn.woolsen.modules.quartz.service.mapstruct;

import cn.woolsen.base.BaseMapper;
import cn.woolsen.modules.quartz.domain.QuartzLog;
import cn.woolsen.modules.quartz.domain.dto.QuartzLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author Woolsen
 * @since 2022/12/11 19:23
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuartzLogMapper extends BaseMapper<QuartzLogDto, QuartzLog> {
}

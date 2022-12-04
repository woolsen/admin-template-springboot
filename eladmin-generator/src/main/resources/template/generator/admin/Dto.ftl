package ${package}.domain.dto;

import lombok.Data;
<#if hasTimestamp>
import java.sql.Timestamp;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>
import java.io.Serializable;
import io.swagger.annotations.ApiModelProperty;
<#if !auto && pkColumnType = 'Long'>
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
</#if>

/**
* @website https://eladmin.vip
* @description /
* @author ${author}
* @date ${date}
**/
@Data
public class ${className}Dto implements Serializable {
<#if columns??>
    <#list columns as column>

    <#if column.columnKey = 'PRI'>
    <#if !auto && pkColumnType = 'Long'>
    /** 防止精度丢失 */
    @JSONField(serializeUsing = ToStringSerializer.class)
    </#if>
    </#if>
    <#if column.remark != ''>
    @ApiModelProperty("${column.remark}")
    </#if>
    private ${column.columnType} ${column.changeColumnName};
    </#list>
</#if>
}
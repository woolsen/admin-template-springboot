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
package ${package}.domain.dto;

import lombok.Data;
import io.swagger.annotations.ApiModelProperty;
<#if queryHasTimestamp>
import java.sql.Timestamp;
</#if>
<#if queryHasBigDecimal>
import java.math.BigDecimal;
</#if>
<#if betweens??>
import java.util.List;
</#if>
<#if queryColumns??>
import cn.woolsen.annotation.Query;
</#if>

/**
* @website https://eladmin.vip
* @author ${author}
* @date ${date}
**/
@Data
public class ${className}QueryCriteria{
<#if queryColumns??>
    <#list queryColumns as column>

<#if column.queryType = '='>
    <#if column.remark != ''>
    @ApiModelProperty("${column.remark}(精确)")
    </#if>
    @Query
</#if>
<#if column.queryType = 'Like'>
    <#if column.remark != ''>
    @ApiModelProperty("${column.remark}(模糊)")
    </#if>
    @Query(type = Query.Type.INNER_LIKE)
</#if>
<#if column.queryType = '!='>
    <#if column.remark != ''>
    @ApiModelProperty("${column.remark}(不等于)")
    </#if>
    @Query(type = Query.Type.NOT_EQUAL)
</#if>
<#if column.queryType = 'NotNull'>
    <#if column.remark != ''>
    @ApiModelProperty("${column.remark}(不为空)")
    </#if>
    @Query(type = Query.Type.NOT_NULL)
</#if>
<#if column.queryType = '>='>
    <#if column.remark != ''>
    @ApiModelProperty("${column.remark}(大于等于)")
    </#if>
    @Query(type = Query.Type.GREATER_THAN)
</#if>
<#if column.queryType = '<='>
    <#if column.remark != ''>
    @ApiModelProperty("${column.remark}(小于等于)")
    </#if>
    @Query(type = Query.Type.LESS_THAN)
</#if>
    private ${column.columnType} ${column.changeColumnName};
    </#list>
</#if>
<#if betweens??>
    <#list betweens as column>
    <#if column.remark != ''>
        @ApiModelProperty("${column.remark}(区间)")
    </#if>
    @Query(type = Query.Type.BETWEEN)
    private List<${column.columnType}> ${column.changeColumnName};
    </#list>
</#if>
}
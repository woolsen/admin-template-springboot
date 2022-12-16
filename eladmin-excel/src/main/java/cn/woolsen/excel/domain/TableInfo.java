package cn.woolsen.excel.domain;

import lombok.Data;

import java.util.List;

/**
 * @author Woolsen
 * @since 2022/12/11 20:58
 */
@Data
public class TableInfo {

    private String name;

    private Integer height;

    private List<ColumnInfo> columns;
}

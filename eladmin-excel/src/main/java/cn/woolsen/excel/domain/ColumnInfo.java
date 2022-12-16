package cn.woolsen.excel.domain;

import cn.woolsen.excel.handler.Dict;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Woolsen
 * @since 2022/12/11 21:03
 */
@Data
public class ColumnInfo {

    @Nullable
    private String filed;
    @Nullable
    private String prefix;
    @Nullable
    private String suffix;
    @Nullable
    private Integer precision;
    @Nullable
    private String title;
    @Nullable
    private Integer width;
    private int sortNum;
    private String dateFormat;
    private boolean wrapText;
    @Nullable
    private List<Dict> dicts;
    private List<String> rows;
}

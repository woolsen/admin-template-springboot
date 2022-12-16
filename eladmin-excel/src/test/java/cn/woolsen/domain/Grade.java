package cn.woolsen.domain;

import cn.woolsen.excel.annotation.Excel;
import lombok.Data;

/**
 * @author Woolsen
 * @since 2022/12/13 15:16
 */
@Data
public class Grade {

    @Excel("班级名称")
    private String name;

}

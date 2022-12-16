package cn.woolsen.excel.experiment;

import cn.woolsen.excel.handler.Dict;
import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Field;

/**
 * @author Woolsen
 * @since 2022/12/12 18:36
 */
@Data
public class Title {

    private int sortNum;

    private String name;

    private Field field;

    private Dict[] dicts;

    private Integer width;

    private int columns;

    /**
     * 深度。当children为空时，深度为1；否则为children中最大深度+1。
     */
    private int depth;

    @ToString.Exclude
    private Title[] children;

}

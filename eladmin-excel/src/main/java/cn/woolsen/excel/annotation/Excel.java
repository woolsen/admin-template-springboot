package cn.woolsen.excel.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel导出基本注释<p>
 * Excel属性优先级: 属性注解 > 类注解 > 属性的类注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Excel {
    int UNSET = -1;

    /**
     * 标题
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 标题
     */
    @AliasFor("value")
    String name() default "";

    /**
     * 列表是否垂直显示
     */
    boolean verticalList() default false;

    /**
     * 字典标识码
     */
    String[] dictCode() default {};

    /**
     * 单元格宽度。当有嵌套类型时不生效。
     */
    int width() default UNSET;

    /**
     * 单元格高度。仅当注解在类时生效。
     */
    int height() default UNSET;

    String dateFormat() default "";

    /**
     * 当作为属性注解时，用于区该属性内field的值
     */
    String field() default "";

    /**
     * 前缀
     */
    String prefix() default "";

    /**
     * 后缀
     */
    String suffix() default "";

    /**
     * 浮点数精度，保留小数点后多少位
     */
    int precision() default UNSET;

    /**
     * 下标。仅当注解在属性时生效。<p>
     * 按sortNum值从小到大，列从左到右排序。未设置sortNum值则按属性在代码中定义从上到下，列从左到右排序。
     */
    int sortNum() default UNSET;

    /**
     * 特性
     */
    Feature[] features() default {};

    /**
     * 通过注解定义字典
     */
    DictEntry[] dict() default {};

    enum Feature {
        /**
         * 不自动换行
         */
        NO_WRAP_TEXT,
        /**
         * 自动换行
         */
        WRAP_TEXT,
    }
}

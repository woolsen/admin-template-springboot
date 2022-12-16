package cn.woolsen.domain;

import cn.woolsen.excel.annotation.DictEntry;
import cn.woolsen.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Woolsen
 * @since 2022/12/11 21:38
 */
@Data
@Excel(name = "学生表", dateFormat = "yyyy年MM月dd日", precision = 3, dictCode = "like")
public class Student {

    @Excel(name = "名字", sortNum = 0)
    private String name;

    @Excel(name = "性别", sortNum = 2, dict = {
            @DictEntry(key = "nan", value = "男"),
            @DictEntry(key = "nv", value = "女")})
    private String sex;

    @Excel(name = "出生日期", sortNum = 1)
    private Date birth;

    @Excel(name = "身高", suffix = "米", precision = 2)
    private Double height;

    @Excel(name = "身高2", suffix = "米")
    private Double height2;

    @Excel(name = "教育", dictCode = "education")
    private int education;

    @Excel(name = "教育2", prefix = "教育:[", suffix = "]", dict = {
            @DictEntry(key = "1", value = "小学"),
            @DictEntry(key = "2", value = "初中"),
            @DictEntry(key = "3", value = "高中"),
            @DictEntry(key = "4", value = "大学"),
            @DictEntry(key = "5", value = "硕士"),
            @DictEntry(key = "6", value = "博士")})
    private List<Integer> education2;

    @Excel(name = "个人介绍")
    private String desc;

    @Excel(field = "name")
    private Grade grade;

    @Excel(value = "兴趣爱好")
    private List<String> like;

    private String ignore;

    public Student(String name, String sex, Date birth, Double height, List<Integer> education, Grade grade, List<String> like, String desc) {
        this.name = name;
        this.sex = sex;
        this.birth = birth;
        this.height = height;
        this.height2 = height;
        this.education = education.get(0);
        this.education2 = education;
        this.like = like;
        this.desc = desc;
        this.grade = grade;
    }

}

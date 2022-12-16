package cn.woolsen;

import cn.woolsen.domain.Grade;
import cn.woolsen.domain.Student;
import cn.woolsen.excel.domain.ColumnInfo;
import cn.woolsen.excel.domain.TableInfo;
import cn.woolsen.excel.experiment.ExcelUtil2;
import cn.woolsen.excel.experiment.Title;
import cn.woolsen.excel.util.ExcelUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

class ExcelExportUtilsTest {

//    @Test
//    void test1() {
//        List<String> list = new ArrayList<>();
//        list.add("1111111111");
//        list.add("2222222222");
//        list.add("3333333333");
//        TableInfo info = ExcelUtil.export(list);
//        System.out.println("========= " + info.getName() + " =========");
//        info.getColumns().forEach((column) -> {
//            System.out.println(column.getTitle() + ": " + column);
//        });
//    }

    @Test
    void test() {
        List<Student> students = new ArrayList<>();
        System.out.println(students.getClass());
        final ParameterizedType type = (ParameterizedType) students.getClass().getGenericSuperclass();
        final Type argument = type.getActualTypeArguments()[0];
        System.out.println(argument);
    }

    @SneakyThrows
    @Test
    void test2() {
        ExcelUtil.setDictHandler(new DictHandlerImpl());
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

        Grade grade = new Grade();
        grade.setName("六年级");

        List<String> like = List.of("1", "2", "3", "4");

        StringBuilder sb = new StringBuilder();
        for (char c = 'a'; c <= 'z'; c++) {
            sb.append(c);
            sb.append(c);
            sb.append(c);
            sb.append(c);
        }
        for (char c = 'z'; c >= 'a'; c--) {
            sb.append(c);
            sb.append(c);
            sb.append(c);
            sb.append(c);
        }
        String desc = sb.toString();

        List<Student> students = new ArrayList<>();
        students.add(new Student("小明", "nan", format.parse("1999/04/20"), 1.8838, List.of(1, 2), grade, like, desc));
        students.add(new Student("小绿", "nan", format.parse("2000/01/01"), 1.7232, List.of(1, 2, 3), grade, like, desc));
        students.add(new Student("小红", "nv", format.parse("2001/12/30"), 1.5818, List.of(1, 2, 3, 4, 5), grade, like, desc));
        students.add(new Student("小黄", null, format.parse("2001/12/30"), 1.5818, List.of(1), grade, like, desc));

        students.addAll(students);
        students.addAll(students);

        String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        File excelFile = new File(jarPath + "/" + "test.xls");
        System.out.println(excelFile.getAbsolutePath());
        try (FileOutputStream os = new FileOutputStream(excelFile)) {
            ExcelUtil.export(Student.class, students, os);
        }

        TableInfo info = ExcelUtil.export(Student.class, students);
        System.out.println("========= " + info.getName() + " =========");

        if (!info.getColumns().isEmpty()) {
            String[][] table = new String[info.getColumns().get(0).getRows().size() + 1][info.getColumns().size()];
            for (int i = 0; i < info.getColumns().size(); i++) {
                ColumnInfo column = info.getColumns().get(i);
                table[0][i] = column.getTitle();
                for (int j = 0; j < column.getRows().size(); j++) {
                    String row = column.getRows().get(j);
                    table[j + 1][i] = row;
                }
            }
            for (String[] strings : table) {
                for (String string : strings) {
                    System.out.print(string);
                    System.out.print("  ");
                }
                System.out.println();
            }
        }
    }

    @Test
    void test3() {
        final Title[] titles = ExcelUtil2.analysisTitle(Student.class);
        if (titles != null) {
            for (Title title : titles) {
                print(title, 0);
            }
        }
    }

    void print(Title title, int wrap) {
        for (int i = 0; i < wrap; i++) {
            System.out.print(' ');
        }
        System.out.println(title);
        if (title.getChildren() != null) {
            for (Title child : title.getChildren()) {
                print(child, wrap + 4);
            }
        }
    }

}

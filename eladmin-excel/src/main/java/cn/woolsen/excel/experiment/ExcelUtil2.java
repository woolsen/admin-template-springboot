package cn.woolsen.excel.experiment;

import cn.woolsen.excel.annotation.Excel;
import cn.woolsen.excel.domain.ColumnInfo;
import cn.woolsen.excel.domain.TableInfo;
import cn.woolsen.excel.handler.Dict;
import cn.woolsen.excel.handler.DictHandler;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Woolsen
 * @since 2022/12/11 20:19
 */
public class ExcelUtil2 {

    private static final List<Class<?>> PRIMITIVE_TYPES = Arrays.asList(
            Boolean.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Character.class,
            String.class,
            Void.class
    );

    private static final int MAX_CHARS = 80;

    private static DictHandler dictHandler;

    public static void setDictHandler(DictHandler dictHandler) {
        ExcelUtil2.dictHandler = dictHandler;
    }

    public static void export(OutputStream outputStream, List<?> rows) throws IOException {
        TableInfo tableInfo = new TableInfo();
        if (rows.isEmpty()) {
            return;
        }
        final Class<?> clazz = rows.get(0).getClass();

        if (clazz.isPrimitive() || PRIMITIVE_TYPES.contains(clazz)) {
            List<String> rows1 = rows.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setRows(rows1);
            tableInfo.setColumns(Collections.singletonList(columnInfo));
        } else {
            Workbook workbook = new HSSFWorkbook();
            final Sheet sheet = workbook.createSheet();
        }
        export(outputStream, tableInfo);
    }

    public static Title[] analysisTitle(Class<?> clazz) {
        final Field[] fields = clazz.getDeclaredFields();
        if (fields.length == 0) {
            return null;
        }
        Excel classAnnotation = clazz.getAnnotation(Excel.class);

        AtomicInteger index = new AtomicInteger(0);
        return Arrays.stream(fields)
                .map(field -> analysisTitle(index.getAndIncrement(), classAnnotation, field))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Title::getSortNum))
                .toArray(Title[]::new);
    }

    public static Title analysisTitle(int index, @Nullable Excel classAnnotation, Field field) {
        final Excel fieldAnnotation = field.getAnnotation(Excel.class);
        if (fieldAnnotation == null) {
            return null;
        }

        final Title title = new Title();
        title.setField(field);
        title.setName(fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name());
        title.setSortNum(fieldAnnotation.sortNum() == Excel.UNSET ? 1000000 + index : fieldAnnotation.sortNum());
        title.setWidth(getAnnotationIntValue(classAnnotation, fieldAnnotation, Excel::width));
        if (dictHandler != null && fieldAnnotation.dictCode().length != 0) {
            List<Dict> dicts = new ArrayList<>();
            for (String code : fieldAnnotation.dictCode()) {
                Dict dict = dictHandler.findDictByCode(code);
                if (dict != null) {
                    dicts.add(dict);
                }
            }
        }

        AtomicInteger childIndex = new AtomicInteger(0);
        final Title[] titleChildren = Arrays.stream(field.getType().getDeclaredFields())
                .map(childField -> analysisTitle(childIndex.getAndIncrement(), classAnnotation, childField))
                .filter(Objects::nonNull)
                .toArray(Title[]::new);
        title.setChildren(titleChildren);

        int maxChildDepth = 0;
        for (Title titleChild : titleChildren) {
            if (maxChildDepth < titleChild.getDepth()) {
                maxChildDepth = titleChild.getDepth();
            }
        }
        title.setDepth(maxChildDepth + 1);

        if (titleChildren.length == 0) {
            title.setColumns(1);
        } else {
            int columns = 0;
            for (Title titleChild : titleChildren) {
                columns += titleChild.getColumns();
            }
            title.setColumns(columns);
        }

        return title;
    }

    private static void drawTitle(Sheet sheet, Title[] titles) {
        drawTitle(0, 0, sheet, titles);
    }

    private static void drawTitle(int row, int column, Sheet sheet, Title[] titles) {
        int maxDepth = 0;
        for (Title title : titles) {
            if (title.getDepth() > maxDepth) {
                maxDepth = title.getDepth();
            }
        }
        Row[] rows = new Row[1];
        for (int i = 0; i < maxDepth; i++) {
            sheet.createRow(row + i);
        }
    }

    private static Row getRow(Sheet sheet, int rowNum) {
        Row row = sheet.getRow(rowNum);
        return row != null ? row : sheet.createRow(rowNum);
    }

    private static void export(OutputStream outputStream, TableInfo tableInfo) throws IOException {
        if (tableInfo.getColumns().isEmpty()) {
            return;
        }
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        final CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setAlignment(HorizontalAlignment.CENTER);
        borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        final CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.cloneStyleFrom(borderStyle);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        final CellStyle[] columnStyles = new CellStyle[tableInfo.getColumns().size()];

        Row[] rows = new HSSFRow[tableInfo.getColumns().get(0).getRows().size() + 1];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = sheet.createRow(i);
        }
        for (int i = 0; i < tableInfo.getColumns().size(); i++) {
            ColumnInfo column = tableInfo.getColumns().get(i);
            columnStyles[i] = workbook.createCellStyle();
            columnStyles[i].cloneStyleFrom(borderStyle);
            columnStyles[i].setWrapText(true);
            Cell titleCell = rows[0].createCell(i);
            titleCell.setCellStyle(titleStyle);
            int maxChars = 2;
            if (column.getTitle() != null) {
                titleCell.setCellValue(column.getTitle());
                if (!column.getTitle().isEmpty()) {
                    maxChars = column.getTitle().getBytes(StandardCharsets.UTF_8).length;
                }
            }
            for (int j = 0; j < column.getRows().size(); j++) {
                String value = column.getRows().get(j);
                Cell cell = rows[j + 1].createCell(i);
                cell.setCellValue(value);
                cell.setCellStyle(columnStyles[i]);
                int chars = value.getBytes(StandardCharsets.UTF_8).length;
                if (chars > maxChars) {
                    maxChars = chars;
                }
            }
            maxChars = Math.min(maxChars, MAX_CHARS);
            sheet.setColumnWidth(i, column.getWidth() != null ? column.getWidth() : maxChars * 256);
        }
        sheet.createFreezePane(0, 1, 0, 1);

        workbook.setActiveSheet(0);
        workbook.write(outputStream);
    }


    @Nullable
    private static ColumnInfo drawColumn(int index, @Nullable Excel classAnnotation, Field field, List<?> rows, Map<String, Object> cache) {
        ColumnInfo info = new ColumnInfo();
        Excel fieldAnnotation = field.getAnnotation(Excel.class);
        if (fieldAnnotation == null) {
            return null;
        }
        List<String> columnRowValues = rows.stream()
                .map(row -> getColumnRowValue(classAnnotation, fieldAnnotation, field, row, cache))
                .collect(Collectors.toList());
        info.setRows(columnRowValues);
        info.setSortNum(fieldAnnotation.sortNum() == Excel.UNSET ? 1000000 + index : fieldAnnotation.sortNum());
        info.setTitle(fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name());
        if (fieldAnnotation.width() != Excel.UNSET) {
            info.setWidth(fieldAnnotation.width());
        } else if (classAnnotation != null && classAnnotation.width() != Excel.UNSET) {
            info.setWidth(classAnnotation.width());
        }
        return info;
    }

    private static String getColumnRowValue(@Nullable Excel classAnnotation, @Nonnull Excel filedAnnotation, Field field, Object row, Map<String, Object> cache) {
        Object value;
        try {
            field.setAccessible(true);
            value = field.get(row);
        } catch (IllegalAccessException e) {
            return "";
        }
        if (value == null) {
            return "";
        }

        String prefix = getAnnotationStringValue(classAnnotation, filedAnnotation, Excel::prefix);
        String suffix = getAnnotationStringValue(classAnnotation, filedAnnotation, Excel::suffix);
        String dateFormat = getAnnotationStringValue(classAnnotation, filedAnnotation, Excel::dateFormat);
        if (dateFormat == null) {
            dateFormat = "yyyy/MM/dd HH:mm:ss";
        }

        final Class<?> type = field.getType();
        if (type.isAssignableFrom(Date.class) || type.isAssignableFrom(Calendar.class)) {
            String cacheKey = "SimpleDateFormat:" + dateFormat;
            SimpleDateFormat dateFormat1 = (SimpleDateFormat) cache.get(cacheKey);
            if (dateFormat1 == null) {
                dateFormat1 = new SimpleDateFormat(dateFormat);
                cache.put(cacheKey, dateFormat1);
            }
            value = dateFormat1.format(value);
        } else if (type.isAssignableFrom(TemporalAccessor.class)) {
            String cacheKey = "DateTimeFormatter:" + dateFormat;
            DateTimeFormatter dateFormat1 = (DateTimeFormatter) cache.get(cacheKey);
            if (dateFormat1 == null) {
                dateFormat1 = DateTimeFormatter.ofPattern(dateFormat);
                cache.put(cacheKey, dateFormat1);
            }
            value = dateFormat1.format((TemporalAccessor) value);
        } else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(Float.class) || type.isAssignableFrom(BigDecimal.class)) {
            final Integer precision = getAnnotationIntValue(classAnnotation, filedAnnotation, Excel::precision);
            if (precision == null) {
                return value.toString();
            }
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(precision);
            value = nf.format(value);
        }

//        if (dictHandler != null && !filedAnnotation.dictCode().isEmpty()) {
//            String cacheKey = "dict:" + filedAnnotation.dictCode();
//            Dict dict = (Dict) cache.get(cacheKey);
//            if (dict == null) {
//                dict = dictHandler.findDictByCode(filedAnnotation.dictCode());
//                if (dict != null) {
//                    cache.put(cacheKey, dict);
//                }
//            }
//            if (dict != null) {
//                String dictValue = dict.get(value);
//                if (dictValue != null) {
//                    value = dictValue;
//                }
//            }
//        }
        String retValue = value.toString();
        if (prefix != null) {
            retValue = prefix + retValue;
        }
        if (suffix != null) {
            retValue = retValue + suffix;
        }
        return retValue;
    }

    private static String getAnnotationStringValue(@Nullable Excel classAnnotation, @Nonnull Excel filedAnnotation, Function<Excel, String> get) {
        if (!get.apply(filedAnnotation).isEmpty()) {
            return get.apply(filedAnnotation);
        } else if (classAnnotation != null && !get.apply(classAnnotation).isEmpty()) {
            return get.apply(classAnnotation);
        } else {
            return null;
        }
    }

    private static Integer getAnnotationIntValue(@Nullable Excel classAnnotation, @Nonnull Excel filedAnnotation, Function<Excel, Integer> get) {
        if (get.apply(filedAnnotation) != Excel.UNSET) {
            return get.apply(filedAnnotation);
        } else if (classAnnotation != null && get.apply(classAnnotation) != Excel.UNSET) {
            return get.apply(classAnnotation);
        } else {
            return null;
        }
    }

}

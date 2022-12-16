package cn.woolsen.excel.util;

import cn.woolsen.excel.annotation.DictEntry;
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
 * TODO 支持集合
 *
 * @author Woolsen
 * @since 2022/12/11 20:19
 */
public class ExcelUtil {

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
        ExcelUtil.dictHandler = dictHandler;
    }

    public static <T> TableInfo export(Class<T> clazz, List<T> list) {
        TableInfo tableInfo = new TableInfo();

        if (PRIMITIVE_TYPES.contains(clazz)) {
            List<String> rows1 = list.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setRows(rows1);
            tableInfo.setColumns(Collections.singletonList(columnInfo));
        } else {
            Excel classAnnotation = clazz.getAnnotation(Excel.class);
            if (classAnnotation != null) {
                tableInfo.setName(classAnnotation.value().isEmpty() ? classAnnotation.name() : classAnnotation.value());
                tableInfo.setHeight(classAnnotation.height() == -1 ? null : classAnnotation.height());
            }

            Map<String, Object> cache = new HashMap<>();

            AtomicInteger index = new AtomicInteger(0);
            List<ColumnInfo> columnInfos = Arrays.stream(clazz.getDeclaredFields())
                    .map(field -> getColumnInfo(index.getAndIncrement(), classAnnotation, field, list, cache))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(ColumnInfo::getSortNum))
                    .collect(Collectors.toList());
            tableInfo.setColumns(columnInfos);
        }
        return tableInfo;
    }

    public static <T> void export(Class<T> clazz, List<T> list, OutputStream outputStream) throws IOException {
        TableInfo tableInfo = export(clazz, list);
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
            columnStyles[i].setWrapText(column.isWrapText());
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
            sheet.setColumnWidth(i, column.getWidth() != null ? column.getWidth() * 256 : maxChars * 256);
        }
        sheet.createFreezePane(0, 1, 0, 1);

        workbook.setActiveSheet(0);
        workbook.write(outputStream);
        workbook.close();
    }

    @Nullable
    private static ColumnInfo getColumnInfo(int index, @Nullable Excel classAnnotation, Field field, List<?> rows, Map<String, Object> cache) {
        Excel fieldAnnotation = field.getAnnotation(Excel.class);
        if (fieldAnnotation == null) {
            return null;
        }
        ColumnInfo info = new ColumnInfo();
        info.setFiled(fieldAnnotation.field());
        info.setTitle(fieldAnnotation.name().isEmpty() ? fieldAnnotation.value().isEmpty() ? field.getName() : fieldAnnotation.value() : fieldAnnotation.name());
        info.setPrefix(getAnnotationStringValue(classAnnotation, fieldAnnotation, Excel::prefix));
        info.setSuffix(getAnnotationStringValue(classAnnotation, fieldAnnotation, Excel::suffix));
        info.setPrecision(getAnnotationIntValue(classAnnotation, fieldAnnotation, Excel::precision));
        info.setDateFormat(getAnnotationStringValue(classAnnotation, fieldAnnotation, Excel::dateFormat));
        info.setSortNum(fieldAnnotation.sortNum() == Excel.UNSET ? 1000000 + index : fieldAnnotation.sortNum());

        boolean wrapText;
        if (contain(fieldAnnotation.features(), Excel.Feature.WRAP_TEXT)) {
            wrapText = true;
        } else if (contain(fieldAnnotation.features(), Excel.Feature.NO_WRAP_TEXT)) {
            wrapText = false;
        } else if (classAnnotation == null) {
            wrapText = false;
        } else if (contain(classAnnotation.features(), Excel.Feature.WRAP_TEXT)) {
            wrapText = true;
        } else if (contain(classAnnotation.features(), Excel.Feature.NO_WRAP_TEXT)) {
            wrapText = false;
        } else {
            wrapText = false;
        }
        info.setWrapText(wrapText);

        List<Dict> dicts = new ArrayList<>();
        Map<String, String> annotationDefineDict = new HashMap<>();
        if (classAnnotation != null && classAnnotation.dict().length != 0) {
            for (DictEntry entry : classAnnotation.dict()) {
                annotationDefineDict.put(entry.key(), entry.value());
            }
        }
        if (fieldAnnotation.dict().length != 0) {
            for (DictEntry entry : fieldAnnotation.dict()) {
                annotationDefineDict.put(entry.key(), entry.value());
            }
        }
        if (!annotationDefineDict.isEmpty()) {
            dicts.add(new DictImpl(annotationDefineDict));
        }
        if (dictHandler != null) {
            Set<String> dictCodes = new HashSet<>(List.of(fieldAnnotation.dictCode()));
            if (classAnnotation != null) {
                dictCodes.addAll(List.of(classAnnotation.dictCode()));
            }
            for (String dictCode : dictCodes) {
                dicts.add(dictHandler.findDictByCode(dictCode));
            }
        }
        info.setDicts(dicts);

        if (fieldAnnotation.width() != Excel.UNSET) {
            info.setWidth(fieldAnnotation.width());
        } else if (classAnnotation != null && classAnnotation.width() != Excel.UNSET) {
            info.setWidth(classAnnotation.width());
        }

        List<String> columnRowValues = rows.stream()
                .map(row -> getColumnRowValue(info, field, row, cache))
                .collect(Collectors.toList());
        info.setRows(columnRowValues);

        return info;
    }

    private static String getColumnRowValue(ColumnInfo columnInfo, Field field, Object o, Map<String, Object> cache) {
        Object value;
        try {
            field.setAccessible(true);
            value = field.get(o);
            if (columnInfo.getFiled() != null && !columnInfo.getFiled().isEmpty()) {
                String[] fields = columnInfo.getFiled().split("\\.");
                for (String f : fields) {
                    field = value.getClass().getDeclaredField(f);
                    field.setAccessible(true);
                    value = field.get(value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return "";
        }
        if (value == null) {
            return "";
        }

        Class<?> clazz = field.getType();
        String returnValue;
        if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object v : (Collection<?>) value) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(getStringValue(v, columnInfo, cache));
            }
            returnValue = sb.toString();
        } else {
            returnValue = getStringValue(value, columnInfo, cache);
        }

        return returnValue;
    }

    private static String getStringValue(Object o, ColumnInfo columnInfo, Map<String, Object> cache) {
        Class<?> clazz = o.getClass();
        String str;
        if (Date.class.isAssignableFrom(clazz) || Calendar.class.isAssignableFrom(clazz)) {
            String cacheKey = "SimpleDateFormat:" + columnInfo.getDateFormat();
            SimpleDateFormat dateFormat1 = (SimpleDateFormat) cache.get(cacheKey);
            if (dateFormat1 == null) {
                dateFormat1 = new SimpleDateFormat(columnInfo.getDateFormat());
                cache.put(cacheKey, dateFormat1);
            }
            str = dateFormat1.format(o);
        } else if (TemporalAccessor.class.isAssignableFrom(clazz)) {
            String cacheKey = "DateTimeFormatter:" + columnInfo.getDateFormat();
            DateTimeFormatter dateFormat1 = (DateTimeFormatter) cache.get(cacheKey);
            if (dateFormat1 == null) {
                dateFormat1 = DateTimeFormatter.ofPattern(columnInfo.getDateFormat());
                cache.put(cacheKey, dateFormat1);
            }
            str = dateFormat1.format((TemporalAccessor) o);
        } else if (Number.class.isAssignableFrom(clazz)) {
            if (columnInfo.getPrecision() == null) {
                return o.toString();
            }
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(columnInfo.getPrecision());
            str = nf.format(o);
        } else {
            str = o.toString();
        }
        if (columnInfo.getDicts() != null) {
            for (Dict dict : columnInfo.getDicts()) {
                String d = dict.get(str);
                if (d != null) {
                    return d;
                }
            }
        }
        return str;
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

    private static boolean contain(Object[] t, Object o) {
        for (Object o1 : t) {
            if (o1.equals(o)) {
                return true;
            }
        }
        return false;
    }

    static class DictImpl implements Dict {

        private final Map<String, String> map;

        public DictImpl(Map<String, String> map) {
            this.map = map;
        }

        @Override
        public String get(String key) {
            return map.get(key);
        }
    }
}

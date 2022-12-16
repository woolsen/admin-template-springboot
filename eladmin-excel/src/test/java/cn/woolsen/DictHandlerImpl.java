package cn.woolsen;

import cn.woolsen.excel.handler.Dict;
import cn.woolsen.excel.handler.DictHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Woolsen
 * @since 2022/12/12 15:38
 */
public class DictHandlerImpl implements DictHandler {

    private static final Map<String, String> EDU_CN = new HashMap<>() {{
        put("1", "小学");
        put("2", "初中");
        put("3", "高中");
        put("4", "大学");
        put("5", "硕士");
        put("6", "博士");
    }};

    private static final Map<String, String> LIKE = new HashMap<>() {{
        put("1", "唱");
        put("2", "跳");
        put("3", "RAP");
        put("4", "篮球");
    }};

    @Override
    public Dict findDictByCode(String code) {
        if (Objects.equals(code, "education")) {
            return EDU_CN::get;
        } else if (Objects.equals(code, "like")) {
            return LIKE::get;
        }
        return null;
    }
}

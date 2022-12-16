package cn.woolsen.excel.handler;

/**
 * @author Woolsen
 * @since 2022/12/12 14:52
 */
public interface DictHandler {

    /**
     * 通过字典标识码查找字典
     *
     * @param code 字典标识码
     * @return 字典。未找到则返回null
     */
    Dict findDictByCode(String code);

}

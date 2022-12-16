package cn.woolsen.excel.handler;

public interface Dict {

    /**
     * 查找字典值
     *
     * @return 字典值。未找到则返回null
     */
    String get(String key);

}

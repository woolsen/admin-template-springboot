/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package cn.woolsen.utils;

import cn.woolsen.base.PageDTO;
import org.springframework.data.domain.Page;

import java.util.*;

/**
 * 分页工具
 *
 * @author Zheng Jie
 * @date 2018-12-10
 */
public class PageUtil extends cn.hutool.core.util.PageUtil {

    /**
     * List 分页
     */
    public static <T> List<T> toPage(int page, int size, List<T> list) {
        int fromIndex = page * size;
        int toIndex = page * size + size;
        if (fromIndex > list.size()) {
            return new ArrayList<>();
        } else if (toIndex >= list.size()) {
            return list.subList(fromIndex, list.size());
        } else {
            return list.subList(fromIndex, toIndex);
        }
    }

    /**
     * Page 数据处理，预防redis反序列化报错
     */
    public static <T> PageDTO<T> toPage(Page<T> page) {
        return PageDTO.by(page);
    }

    /**
     * 自定义分页
     */
    public static <T> PageDTO<T> toPage(List<T> object, Long totalElements) {
        return PageDTO.by(totalElements, object);
    }

}

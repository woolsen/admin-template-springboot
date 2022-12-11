package cn.woolsen.modules.system.service;

import cn.woolsen.modules.system.domain.dto.UserDto;

import java.util.Set;

/**
 * 数据权限服务类
 * @author Zheng Jie
 * @date 2020-05-07
 */
public interface DataService {

    /**
     * 获取数据权限
     * @param user /
     * @return /
     */
    Set<Long> getDeptIds(UserDto user);
}

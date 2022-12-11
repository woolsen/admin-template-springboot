package cn.woolsen.utils;

import cn.woolsen.exception.BadRequestException;
import cn.woolsen.modules.security.domain.dto.AuthorityDto;
import cn.woolsen.modules.security.security.UserAuthenticationToken;
import cn.woolsen.modules.system.domain.dto.UserDto;
import cn.woolsen.utils.enums.DataScopeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

/**
 * 获取当前登录的用户
 */
@Slf4j
public class SecurityUtils {

    /**
     * 获取当前用户的数据权限
     *
     * @return /
     */
    public static Set<Long> getCurrentUserDataScope() {
        return getCurrentUser().getDataScopes();
    }

    /**
     * 获取数据权限级别
     *
     * @return 级别
     */
    public static String getDataScopeType() {
        Set<Long> dataScopes = getCurrentUserDataScope();
        if (dataScopes.size() != 0) {
            return "";
        }
        return DataScopeEnum.ALL.getValue();
    }

    /**
     * 获取当前登录的用户
     */
    public static UserDto getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BadRequestException(HttpStatus.UNAUTHORIZED, "当前登录状态过期");
        }
        if (authentication instanceof UserAuthenticationToken) {
            return (UserDto) authentication.getPrincipal();
        }
        throw new BadRequestException(HttpStatus.UNAUTHORIZED, "找不到当前登录的信息");
    }

    /**
     * 获取当前登录的用户
     */
    public static UserDto getCurrentUserOrNull() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (authentication instanceof UserAuthenticationToken) {
            return (UserDto) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取系统用户ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }


    /**
     * 获取系统用户ID
     */
    public static Long getCurrentUserIdOrNull() {
        final UserDto userDto = getCurrentUserOrNull();
        return userDto == null ? null : userDto.getId();
    }

    /**
     * 获取系统用户名称
     */
    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * 获取系统用户名称
     */
    public static String getCurrentUsernameOrNull() {
        final UserDto userDto = getCurrentUserOrNull();
        return userDto == null ? null : userDto.getUsername();
    }

    public static boolean hasPermission(String permission) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof UserAuthenticationToken)) {
            return false;
        } else {
            return authentication.getAuthorities().contains(new AuthorityDto(permission));
        }
    }

    public static boolean hasAnyPermission(String... permissions) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UserAuthenticationToken) {
            for (String permission : permissions) {
                if (authentication.getAuthorities().contains(new AuthorityDto(permission))) {
                    return true;
                }
            }
        }
        return false;
    }
}

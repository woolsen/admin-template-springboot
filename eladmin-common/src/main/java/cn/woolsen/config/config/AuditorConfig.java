package cn.woolsen.config.config;

import cn.woolsen.modules.system.domain.User;
import cn.woolsen.utils.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class AuditorConfig implements AuditorAware<User> {

    /**
     * 返回操作员标志信息
     *
     * @return /
     */
    @Override
    @NonNull
    public Optional<User> getCurrentAuditor() {
        final User user = new User();
        try {
            user.setId(SecurityUtils.getCurrentUserId());
        } catch (Exception ignored) {
            user.setId(0L);
        }
        // 用户定时任务，或者无Token调用的情况
        return Optional.of(user);
    }
}

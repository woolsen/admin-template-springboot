package cn.woolsen.base;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Woolsen
 */
@Data
public class PageDTO<T> {

    private long totalElements;

    private List<T> content;

    public PageDTO(Long totalElements, List<T> content) {
        this.totalElements = totalElements;
        this.content = content;
    }

    public static <T> PageDTO<T> by(Page<T> page) {
        return new PageDTO<>(page.getTotalElements(), page.getContent());
    }

    public static <T> PageDTO<T> by(Long totalElements, List<T> content) {
        return new PageDTO<>(totalElements, content);
    }
}

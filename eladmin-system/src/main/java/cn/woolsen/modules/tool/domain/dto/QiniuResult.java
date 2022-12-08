package cn.woolsen.modules.tool.domain.dto;

import lombok.Data;

/**
 * @author Woolsen
 */
@Data
public class QiniuResult {

    private Long id;

    private int errno;

    private String[] data;

}

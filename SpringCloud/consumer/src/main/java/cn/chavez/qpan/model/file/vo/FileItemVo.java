package cn.chavez.qpan.model.file.vo;

import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/8 20:33
 */
@Data
public class FileItemVo  {
    private String uuid;

    private String name;

    private Integer type;

    private long size;

    private String lastModified;
}

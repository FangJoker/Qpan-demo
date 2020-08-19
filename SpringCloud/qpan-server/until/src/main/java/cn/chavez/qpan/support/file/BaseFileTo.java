package cn.chavez.qpan.support.file;

import lombok.Data;

import java.text.DecimalFormat;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/8 20:27
 */
@Data
public class BaseFileTo {

    protected String name;

    protected String path;

    protected Integer type;

    protected Long size;

    protected String lastModified;

}

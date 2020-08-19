package cn.chavez.qpan.support.file;

import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/8 20:32
 */
@Data
public class FolderVo extends BaseFileTo {
    /**
     * 可能是个普通文件list，也可能是个目录list
     */
    private Object data;
}

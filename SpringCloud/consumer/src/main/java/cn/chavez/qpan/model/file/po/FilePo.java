package cn.chavez.qpan.model.file.po;

import cn.chavez.qpan.model.BassPo;
import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/6 15:55
 */
@Data
public class FilePo extends BassPo {
    private String uuid;
    private String userUuid;
    private String fileName;
    private String filePath;
    private long totalBytes;
    private int fileType;
}

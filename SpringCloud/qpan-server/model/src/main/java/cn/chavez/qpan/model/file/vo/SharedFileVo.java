package cn.chavez.qpan.model.file.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/13 0:57
 */
@Data
@ApiModel("搜索返回的分享内容")
public class SharedFileVo {
    /**
     * 分享记录uuid
     */
    private String uuid;

    private String fileUuid;

    private int fileType;

    private String fileName;

    private long  totalBytes;

    private String authorUuid;

    private String authorName;
}

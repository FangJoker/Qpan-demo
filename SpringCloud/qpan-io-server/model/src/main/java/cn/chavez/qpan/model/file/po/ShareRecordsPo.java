package cn.chavez.qpan.model.file.po;

import cn.chavez.qpan.model.BassPo;
import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/12 17:23
 */
@Data
public class ShareRecordsPo extends BassPo {
    private String uuid;
    private String fileUuid;
    private String userUuid;
    private String linkUuid;

}

package cn.chavez.qpan.model.file.po;

import cn.chavez.qpan.model.BassPo;
import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/12 17:11
 */
@Data
public class FileLinkPo extends BassPo {
    private String uuid;
    private String fileUuid;
    private String link;
    private String password;
    /**
     * 0 - 正常 1-过期
     */
    private int status;
    private String validityDate;


}

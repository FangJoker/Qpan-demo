package cn.chavez.qpan.model.user.vo.user;

import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/23 17:33
 */
@Data
public class ShareFileListVo  {
    /**
     * fileLink UUID
     */
    String uuid;
    String title;
    String type;
    String author;
    String updateTime;
    String link;
    int status;
}

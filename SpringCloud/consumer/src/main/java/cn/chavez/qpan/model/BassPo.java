package cn.chavez.qpan.model;

import lombok.Data;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/3 16:11
 */
@Data
public class BassPo {
    private String uuid;
    private String createTime;
    private String updateTime;
    private int isDeleted;
}

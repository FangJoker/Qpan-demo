package cn.chavez.qpan.model.file.vo;

import lombok.Data;

import java.text.DecimalFormat;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/8 20:27
 */
@Data
public class BaseFileVo {

    private String name;

    private String path;

    private Integer type;

    private long size;

    private String lastModified;

    public String getFormatSize() {
        DecimalFormat df = new DecimalFormat("0.00");
        long byteToKB = size / 1024;
        long byteToMB = size / (1024 * 1024);
        long byteToGB = size / (1024 * 1024 * 1024);

        if (byteToKB >= 0 && byteToMB <= 0) {
            return df.format((float) size / 1024) + " KB";
        } else if (byteToMB >= 0 && byteToGB <= 0) {
            return df.format((float) size / (1024 * 1024)) + " MB";
        } else {
            return df.format((float) size / (1024 * 1024 * 1024)) + " GB";
        }
    }
}

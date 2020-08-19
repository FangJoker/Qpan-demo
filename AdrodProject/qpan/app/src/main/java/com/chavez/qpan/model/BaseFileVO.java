package com.chavez.qpan.model;

import java.text.DecimalFormat;

/**
 * @Author Chavez Qiu
 * @Date 19-12-30.
 * Email：qiuhao1@meizu.com
 * Description：
 */
public class BaseFileVO {
    private String name;
    
    private String uuid;

    /**
     * 本地路径
     */
    private String path;

    private Integer type;

    private long size;

    private String lastModified;

    public boolean checked = false;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Integer getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public String getLastModified() {
        return lastModified;
    }

    public boolean getChecked(){
        return checked;
    }

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

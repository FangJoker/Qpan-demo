package com.chavez.qpan.model;

/**
 * @Author Chavez Qiu
 * @Date 20-5-14.
 * Email：qiuhao1@meizu.com
 * Description：
 */
public class ShareFileVo {
    String uuid;
    String fileName;
    String authorUuid;
    String authorName;
    Long totalBytes;
    int type;

    public Long getTotalBytes() {
        return totalBytes;
    }

    public int getType() {
        return type;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorUuid() {
        return authorUuid;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorUuid(String authorUuid) {
        this.authorUuid = authorUuid;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setTotalBytes(Long totalBytes) {
        this.totalBytes = totalBytes;
    }


    public void setType(int type) {
        this.type = type;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

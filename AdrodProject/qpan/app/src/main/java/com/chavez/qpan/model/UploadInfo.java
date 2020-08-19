package com.chavez.qpan.model;

import android.database.Cursor;

import com.chavez.qpan.providers.UploadColumns;

import java.text.DecimalFormat;

/**
 * @Author Chavez Qiu
 * @Date 20-5-03.
 * Email：qiuhao1@meizu.com
 * Description：
 */
public class UploadInfo {
    public final static int READY = 100;
    public final static int SUCCESS = 200;
    public final static int PAUSE = 300;
    public final static int RUNNING = 400;
    public final static int FALIED = 500;

    private long uploadId;

    private Long totalBytes;

    private Long currentBytes;
    /**
     * ready:100 success:200 pause:300 falied:500
     **/
    private int status;

    private int type;

    private int chunk;

    private int chunks;
    /**
     * 本地目录地址
     **/
    private String url;

    private String title;
    /**
     * 存放uri
     **/
    private String data;

    private String createTime;

    private String uploadPath;


    public UploadInfo() {

    }

    public static class Reader {
        Cursor cursor;


        public Reader(Cursor c) {
            this.cursor = c;
        }

        public UploadInfo newUploadInfo() {
            UploadInfo info = new UploadInfo();
            updateFromDatabase(info);
            return info;
        }

        private void updateFromDatabase(UploadInfo info) {
            info.setUploadId(getUploadId());
            info.setTitle(getTitle());
            info.setStatus(getStatus());
            info.setData(getData());
            info.setChunk(getChunk());
            info.setChunks(getChunks());
            info.setCurrentBytes(getCurrentByte());
            info.setTotalBytes(getTotalBytes());
            info.setType(getType());
            info.setUrl(getUrl());
            info.setCreateTime(getCreateTime());
            info.setUploadPath(getUploadPath());
        }

        private long getUploadId() {
            return cursor != null ? cursor.getLong(cursor.getColumnIndex(UploadColumns._ID)) : null;
        }

        private long getTotalBytes() {
            return cursor != null ? cursor.getLong(cursor.getColumnIndex(UploadColumns.COLUMN_TOTAL_BYTES)) : null;
        }

        private long getCurrentByte() {
            return cursor != null ? cursor.getLong(cursor.getColumnIndex(UploadColumns.COLUMN_CURRENT_BYTE)) : null;
        }

        private int getStatus() {
            return cursor != null ? cursor.getInt(cursor.getColumnIndex(UploadColumns.COLUMN_STATUS)) : null;
        }

        private int getType() {
            return cursor != null ? cursor.getInt(cursor.getColumnIndex(UploadColumns.COLUMN_TYPE)) : null;
        }

        private int getChunk() {
            return cursor != null ? Integer.parseInt(cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_CHUNK))) : null;
        }

        private int getChunks() {
            return cursor != null ? cursor.getInt(cursor.getColumnIndex(UploadColumns.COLUMN_CHUNKS)) : null;
        }

        private String getUrl() {
            return cursor != null ? cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_URL)) : null;
        }

        private String getTitle() {
            return cursor != null ? cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_TITEL)) : null;
        }

        private String getData() {
            return cursor != null ? cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_DATA)) : null;
        }

        private String getCreateTime() {
            return cursor != null ? cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_CREATE_TIME)) : null;
        }

        private String getUploadPath() {
            return cursor != null ? cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_UPLOAD_PATH)) : null;
        }
    }

    public Long getCurrentBytes() {
        return currentBytes;
    }

    public int getStatus() {
        return status;
    }

    public long getUploadId() {
        return uploadId;
    }

    public int getChunk() {
        return chunk;
    }

    public int getChunks() {
        return chunks;
    }

    public Long getTotalBytes() {
        return totalBytes;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public String getFormatSize() {
        DecimalFormat df = new DecimalFormat("0.00");
        long byteToKB = totalBytes / 1024;
        long byteToMB = totalBytes / (1024 * 1024);
        long byteToGB = totalBytes / (1024 * 1024 * 1024);

        if (byteToKB >= 0 && byteToMB <= 0) {
            return df.format((float) totalBytes / 1024) + " KB";
        } else if (byteToMB >= 0 && byteToGB <= 0) {
            return df.format((float) totalBytes / (1024 * 1024)) + " MB";
        } else {
            return df.format((float) totalBytes / (1024 * 1024 * 1024)) + " GB";
        }
    }


    public String getTitle() {
        return title;
    }


    public String getUrl() {
        return url;
    }

    public String getData() {
        return data;
    }

    public int getType() {
        return type;
    }


    public void setCurrentBytes(Long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTotalBytes(Long totalBytes) {
        this.totalBytes = totalBytes;
    }


    public void setUploadId(long uploadId) {
        this.uploadId = uploadId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }

    public void setChunks(int chunks) {
        this.chunks = chunks;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }
}

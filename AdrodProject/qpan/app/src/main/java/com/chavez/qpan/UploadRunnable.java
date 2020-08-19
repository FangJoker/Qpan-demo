package com.chavez.qpan;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.chavez.qpan.activity.LoginActivity;
import com.chavez.qpan.exception.HttpStopRequestException;
import com.chavez.qpan.model.UploadInfo;
import com.chavez.qpan.providers.UploadColumns;
import com.chavez.qpan.providers.UploadProvider;
import com.chavez.qpan.util.support.date.DateSupport;
import com.chavez.qpan.util.support.http.HttpSupport;
import com.chavez.qpan.util.support.http.IHttpResponseHandle;
import com.chavez.qpan.util.support.http.ResponseEntity;
import com.chavez.qpan.util.support.web.ServiceApi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class UploadRunnable implements Runnable {
    public static final String TAG = "UploadRunnable";
    public UploadNotificationManager mUploadNotificationManager;
    private Long uploadId;
    private String token;
    private String pathUuid;
    private Context mContext;
    /**
     * 默认切割4等份
     **/
    public static final int chunks = 4;

    private UploadHandler mUploadHandler;

    public interface UploadHandler {
        void onSuccess();

        void onError(int httpStatus, String msg);
    }

    public void setUploadHandler(UploadHandler handler) {
        this.mUploadHandler = handler;
    }

    public UploadRunnable(Context context, long id, String token, String pathUuid) {
        this.uploadId = id;
        this.pathUuid = pathUuid;
        this.token = token;
        this.mContext = context;
        this.mUploadNotificationManager = UploadNotificationManager.getInstance(mContext);
    }

    @Override
    public void run() {
        System.out.println("start upload thread:" + uploadId);
        Uri uri = Uri.parse(UploadProvider.CONTENT_URI.toString() + "/" + uploadId);
        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{
                UploadColumns._ID,
                UploadColumns.COLUMN_CURRENT_BYTE,
                UploadColumns.COLUMN_TOTAL_BYTES,
                UploadColumns.COLUMN_DATA,
                UploadColumns.COLUMN_TITEL,
                UploadColumns.COLUMN_URL,
                UploadColumns.COLUMN_STATUS,
                UploadColumns.COLUMN_TYPE,
                UploadColumns.COLUMN_CHUNK,
                UploadColumns.COLUMN_CHUNKS,
                UploadColumns.COLUMN_UPLOAD_PATH,
                UploadColumns.COLUMN_CREATE_TIME
        }, null, null, null, null);
        if (cursor.moveToNext()) {

            System.out.println(" id: " + cursor.getString(cursor.getColumnIndex(UploadColumns._ID)));
            System.out.println(" file Name: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_TITEL)));
            System.out.println(" file path: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_URL)));
            System.out.println(" file URI: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_DATA)));
            System.out.println(" file totalBytes: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_TOTAL_BYTES)));
            System.out.println(" file type: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_TYPE)));
            System.out.println("file status: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_STATUS)));

            UploadInfo.Reader reader = new UploadInfo.Reader(cursor);
            UploadInfo uploadInfo = reader.newUploadInfo();
            if (uploadInfo.getStatus() == UploadInfo.READY) {
                Map<String, String> param = new HashMap<>();
                param.put("token", token);
                //
                if (pathUuid != null) {
                    param.put("pathUuid", pathUuid);
                } else {
                    param.put("pathUuid", "");
                }
                System.out.println("上传文件:" + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_URL))
                        + " token: " + token + " pathUuid: " + pathUuid);
                try {
                    File file = new File(uploadInfo.getUrl());
                    // The uploaded file is larger than 10MB sharding
                    // 上传的文件大于10MB 分片
                    if (file.length() > 1024 * 1024 * 10) {
                        int chunks = cutFile(file.getAbsolutePath());
                        for (int i = 0; i < chunks; i++) {
                            param.put("chunks", String.valueOf(chunks));
                            param.put("chunk", String.valueOf(i));
                            File partFile = new File(file.getParent() + "/parts/" + file.getName() + "_" + String.valueOf(i) + ".part");
                            executeUpload(ServiceApi.File.UPLOAD_FILE,
                                    param,
                                    partFile,
                                    uploadInfo);
                        }
                    } else {
                        // So here's demo, and I'm going to slice it and write it  as 1
                        // 这里是demo ，分片写死为1
                        param.put("chunks", "1");
                        param.put("chunk", "1");
                        executeUpload(ServiceApi.File.UPLOAD_FILE, param, file, uploadInfo);
                    }
                    if (mUploadHandler != null) {
                        mUploadHandler.onSuccess();
                    }
                } catch (HttpStopRequestException e) {
                    if (mUploadHandler != null) {
                        mUploadHandler.onError(e.getHttpStatusCode(), e.getMessage());
                    }
                    ContentValues values = new ContentValues();
                    values.put(UploadColumns.COLUMN_STATUS, UploadInfo.FALIED);
                    mContext.getContentResolver().update(Uri.parse(uploadInfo.getData()), values, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            }
        } else {
            Log.e(TAG, "no upload result!");
        }
    }

    private static final String PREFIX = "--";                            //前缀 The prefix
    private static final String BOUNDARY = "#boundary";  //边界标识 随机生成  Boundary identifiers are randomly generated
    private static final String CONTENT_TYPE = "multipart/form-data;";     //内容类型  Content type
    private static final String LINE_END = "\r\n";

    void executeUpload(String webAddress, Map<String, String> param, File file, UploadInfo uploadInfo) throws HttpStopRequestException {
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        DataInputStream fileDatanputStream = null;
        String responseString = null;
        int statusCode = -1;
        if (file.exists()) {
            try {
                URL url = new URL(webAddress);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);         // Post 请求不能使用缓存 Post requests cannot use caching
                connection.setInstanceFollowRedirects(true);   //设置本次连接是否自动重定向 Sets whether the connection is automatically redirected
                connection.addRequestProperty("Connection", "Keep-Alive");//设置与服务器保持连接  keep alive
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("Content-Type", CONTENT_TYPE + "boundary=" + BOUNDARY);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                // Write the JSON parameter
                // 写入json参数
                out.writeBytes(getStrParams(param).toString());
                out.flush();
                // Write file
                // 写入文件流
                StringBuilder fileSb = new StringBuilder();
                fileSb.append(PREFIX)
                        .append(BOUNDARY)
                        .append(LINE_END)
                        /**
                         * Important note here: the value in the name is the key required by the server. Only this key can get the corresponding file
                         * Filename is the name of the file, with a suffix such as :abc.png
                         *
                         * 这里重点注意： name里面的值为服务端需要的key 只有这个key 才可以得到对应的文件
                         * filename是文件的名字，包含后缀名的 比如:abc.png
                         */
                        .append("Content-Disposition: form-data; name=\"file\"; filename=\""
                                + file.getName() + "\"" + LINE_END)
                        //此处的ContentType不同于 请求头 中Content-Type
                        .append("Content-Type: application/octet-stream; charset=utf-8" + LINE_END)
                        .append("Content-Transfer-Encoding: 8bit" + LINE_END)
                        .append(LINE_END);
                out.writeBytes(fileSb.toString());
                out.flush();
                fileDatanputStream = new DataInputStream(new FileInputStream(file));
                int bytes = 0;
                long countBytes = 0;
                byte[] buffer = new byte[1024 * 2];
                while ((bytes = fileDatanputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes);
                    // Update upload progress
                    // 更新上传进度
                    countBytes = countBytes + bytes;
                    uploadInfo.setCurrentBytes(countBytes);
                    reportProgress(uploadInfo);
                }
                out.writeBytes(LINE_END);
                out.writeBytes(PREFIX + BOUNDARY + PREFIX + LINE_END);
                out.flush();
                out.close();
                // The output response
                // 输出响应
                statusCode = connection.getResponseCode();
                // If the HTTP status code is not 200, the Connection's input stream cannot be obtained
                // 坑的地方，如果http状态码不是200会无法获取connection的输入流
                if (statusCode == 200) {
                     // Send upload completion notification
                    // 发送下上传完成通知
                    reportUploadSuccess(uploadInfo);
                    StringBuilder response = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    readLine(reader, response);
                    responseString = response.toString();
                    Log.i(TAG, "http post to :" + webAddress + " ========>response: " + responseString);
                } else {
                    responseString = "网络异常";
                    mUploadNotificationManager.cannelNotification(uploadId);
                    throw new HttpStopRequestException(statusCode, responseString);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileDatanputStream != null) {
                    try {
                        fileDatanputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("File: " + uploadInfo.getTitle() + " does not exit!");
            throw new HttpStopRequestException(500, "does not exit!");
        }
    }

    private static StringBuilder getStrParams(Map<String, String> strParams) {
        StringBuilder strSb = new StringBuilder();
        for (Map.Entry<String, String> entry : strParams.entrySet()) {
            strSb.append(PREFIX)
                    .append(BOUNDARY)
                    .append(LINE_END)
                    .append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END)
                    .append("Content-Type: text/plain; charset=" + "utf-8" + LINE_END)
                    .append("Content-Transfer-Encoding: 8bit" + LINE_END)
                    .append(LINE_END)
                    // After the parameter header is set, you need two line feeds, followed by the parameter content
                    // 参数头设置完以后需要两个换行，然后才是参数内容
                    .append(entry.getValue())
                    .append(LINE_END);
        }
        return strSb;
    }

    private void readLine(BufferedReader reader, StringBuilder result) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
    }

    private void reportProgress(UploadInfo info) {
        if (info.getTotalBytes() - info.getCurrentBytes() >= 0) {
            ContentValues values = new ContentValues();
            values.put(UploadColumns.COLUMN_STATUS, UploadInfo.RUNNING);
            values.put(UploadColumns.COLUMN_CURRENT_BYTE, info.getCurrentBytes());
            mUploadNotificationManager.postProgressNotification(info);
            mContext.getContentResolver().update(Uri.parse(info.getData()), values, null, null);
        }
    }

    private void reportUploadSuccess(UploadInfo info) {
        ContentValues values = new ContentValues();
        values.put(UploadColumns.COLUMN_STATUS, UploadInfo.SUCCESS);
        values.put(UploadColumns.COLUMN_CREATE_TIME, DateSupport.getSimpleDateString(new Date()));
        System.out.println("===update create time:" + DateSupport.getSimpleDateString(new Date()));
        mUploadNotificationManager.postUploadCompletedNotification(info);
        mContext.getContentResolver().update(Uri.parse(info.getData()), values, null, null);
    }

    /**File fragmentation，Each shard is 1MB
     * 文件分片，每个分片为1MB
     * @param fileName
     * @return
     * @throws IOException
     */
    public static int cutFile(String fileName) throws IOException {
        File file = new File(fileName);
        // Defines the input stream to read the data in the file
        //定义输入流读取文件中的数据
        FileInputStream fis = new FileInputStream(fileName);
        // Defines the name of the variable to act as the fragment file
        //定义变量充当碎片文件的名称
        int count = 1;
        // Define an array with fixed array size of 1MB and each shard of 1MB
        //定义数组，固定数组的大小为1MB,每个分片为1MB
        byte[] buf = new byte[1024 * 1024];
        int len = 0;
        while ((len = fis.read(buf)) != -1) {
            // Define an output stream that writes the 100KB read data to a file
            //定义输出流，将读取到的100kb数据写到文件中
            FileOutputStream fos = new FileOutputStream(file.getParent() + "/parts/" + file.getName() + "_" + count + ".part");
            // Write the data
            //写数据
            fos.write(buf, 0, len);
            // close the stream
            //关流
            fos.close();
            // The name variable that ACTS as the shard file changes
            //充当碎片文件的名称变量变化
            count++;
        }
        //关闭输入流
        fis.close();
        return count;
    }

}

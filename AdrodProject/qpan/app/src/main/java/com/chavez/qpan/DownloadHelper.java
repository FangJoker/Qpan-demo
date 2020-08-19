package com.chavez.qpan;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;


/**
 * @Author Chavez Qiu
 * @Date 19-12-24.
 * Email：qiuhao1@meizu.com
 * Description：
 */
public class DownloadHelper {
    private DownloadManager mDownloadManager;
    private Context mContext;
    private Long mDownloadId;
    public static DownloadHelper instance;

    private DownloadHelper() {

    }

    public DownloadHelper(Context context) {
        this.mContext = context;
        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public static DownloadHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadHelper.class) {
                if (instance == null) {
                    instance = new DownloadHelper(context);
                    return instance;
                } else {
                    return instance;
                }
            }
        } else {
            return instance;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(mDownloadId);
            Cursor cursor = mDownloadManager.query(query);
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch (status) {
                    case DownloadManager.STATUS_PAUSED:
                        break;
                    case DownloadManager.STATUS_PENDING:
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();
                        mContext.unregisterReceiver(receiver);
                        break;
                    case DownloadManager.STATUS_FAILED:
                        Toast.makeText(mContext, "下载失败: "+cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)), Toast.LENGTH_SHORT).show();
                        mContext.unregisterReceiver(receiver);
                        break;
                    default:
                        break;
                }
                cursor.close();
            }
        }
    };


    public Boolean download(String url, String name) {
        if (!url.contains("http://") && !url.contains("https://")) {
            url = "http://" + url;
        }
        System.out.println("download url:" + url);
        Toast.makeText(mContext,"正在下载",Toast.LENGTH_SHORT).show();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
        request.setDestinationUri(Uri.fromFile(file));
        request.setTitle(name);
        request.setAllowedOverRoaming(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        if (mDownloadManager != null) {
            try {
                mDownloadId = mDownloadManager.enqueue(request);
                System.out.println("downloadId" + mDownloadId);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        // Register to listen for downloads
        //注册广播监听下载情况
        mContext.registerReceiver(receiver, new IntentFilter((DownloadManager.ACTION_DOWNLOAD_COMPLETE)));
        return true;
    }


}


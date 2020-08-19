package com.chavez.qpan;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.chavez.qpan.model.UploadInfo;

import java.text.DecimalFormat;
import java.util.HashSet;

/**
 * 上传任务通知管理器
 */
public class UploadNotificationManager {
    private Context mContext;
    private long id;
    private HashSet<Long> notificationIdSet = new HashSet<>();
    static DecimalFormat df = new DecimalFormat("0.00");
    private static final String UPLOAD_CHANNEL = "upload_channel";
    private static final String UPLOAD_CHANNEL_ID = "upload_channel";
    private static final String GROUP_KEY = "upload_group";
    private static final int GROUP_ID = -1;
    private NotificationManager mNotificationManager;

    public static volatile UploadNotificationManager instance;


    private UploadNotificationManager() {
    }

    public static UploadNotificationManager getInstance(Context context) {
        if (instance == null) {
            synchronized (UploadNotificationManager.class) {
                if (instance == null) {
                    instance = new UploadNotificationManager(context);
                    return instance;
                } else {
                    return instance;
                }
            }
        } else {
            return instance;
        }
    }

    private UploadNotificationManager(Context context) {
        this.mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNotificationManager = mContext.getSystemService(NotificationManager.class);
        }else{
            mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    UPLOAD_CHANNEL_ID,
                    UPLOAD_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound(null, null);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private Notification.Builder getBuilder() {
        final Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(mContext, UPLOAD_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(mContext);
        }
        return builder;
    }

    public void postProgressNotification(UploadInfo info) {
        String persent = df.format(info.getCurrentBytes().doubleValue() / info.getTotalBytes().doubleValue());
        if ( (int)(Double.valueOf(persent)*100) %10 == 0){
            final Notification.Builder builder = getBuilder();
            builder.setContentTitle("正在上传...");
            builder.setContentText(info.getTitle() + "已传输: " + 100 * Double.valueOf(persent) + "%");
            builder.setSmallIcon(R.mipmap.logo);
            notificationIdSet.add(info.getUploadId());
            // 每百分之10 通知栏才更新一次
            mNotificationManager.notify((int) info.getUploadId(), builder.build());
        }
    }

    private void postUploadCompletedNotificationSummary() {
        final Notification.Builder builder = getBuilder();
        builder.setSmallIcon(R.mipmap.logo)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true);
        mNotificationManager.notify(GROUP_ID, builder.build());
    }

    public void postUploadCompletedNotification(UploadInfo info) {
        final Notification.Builder builder = getBuilder();
        builder.setContentTitle("上传成功");
        builder.setContentText(info.getTitle() + "已传输完成");
        builder.setSmallIcon(R.mipmap.logo);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setGroup(GROUP_KEY);
        }
        mNotificationManager.cancel((int)info.getUploadId());
        notificationIdSet.remove(info.getUploadId());
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mNotificationManager.notify((int)info.getUploadId(),builder.build());
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            postUploadCompletedNotificationSummary();
        }
    }

    public void cannelNotification(long id){
        if (notificationIdSet.remove(id)){
            mNotificationManager.cancel((int)id);
        }
    }
}


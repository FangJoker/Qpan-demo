package com.chavez.qpan.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.chavez.qpan.ThreadExecutor;
import com.chavez.qpan.UploadRunnable;
import com.chavez.qpan.activity.LoginActivity;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class UploadService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    private static final String ACTION_UPLOAD = "com.chavez.qpan.service.action.upload";
    private static final String PARAM_UPLOAD_ID = "uploadId";
    private static final String PARAM_TOKEN = "token";
    private static final String PARAM_PATH_UUID = "pathUuid";

    private Handler mUiHandler = new Handler(Looper.getMainLooper());
    public UploadService() {
        super("UploadService");
    }


    // TODO: Customize helper method
    public static void startActionUpload(Context context, long uploadId, String token, String pathUuid) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(PARAM_UPLOAD_ID, uploadId);
        intent.putExtra(PARAM_PATH_UUID, pathUuid);
        intent.putExtra(PARAM_TOKEN, token);
        System.out.println("===start service====uploadId: " + uploadId);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                long uploadId = intent.getExtras().getLong(PARAM_UPLOAD_ID);
                String token = intent.getExtras().getString(PARAM_TOKEN);
                String pathUuid = intent.getExtras().getString(PARAM_PATH_UUID);
                System.out.println("===handleActionUpload====uploadId: " + uploadId);
                handleActionUpload(uploadId, token, pathUuid);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload(Long uploadId, String token, String pathUuid) {
        updateFromProvider(uploadId, token, pathUuid);
    }

    private ThreadExecutor executor = new ThreadExecutor(
            5,
            8,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(10));

    private void updateFromProvider(Long uploadId, String token, String pathUuid) {
        UploadRunnable updateRunnable = new UploadRunnable(this, uploadId, token, pathUuid);
        updateRunnable.setUploadHandler(new UploadRunnable.UploadHandler() {
            @Override
            public void onSuccess() {
                mUiHandler.post(() -> Toast.makeText(getApplicationContext(), "上传成功", Toast.LENGTH_SHORT).show());
                System.out.println("===上传成功=====");
            }

            @Override
            public void onError(int httpStatus, String msg) {
                if (httpStatus == 401) {
                    // Return to login page
                    // 返回登录页面
                    Intent activityIntent = new Intent();
                    activityIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    activityIntent.setClass(getApplicationContext(), LoginActivity.class);
                    getApplicationContext().startActivity(activityIntent);
                } else if (httpStatus == 403) {
                   mUiHandler.post(() -> Toast.makeText(getApplicationContext(), "上传失败,参数错误", Toast.LENGTH_SHORT).show());
                } else {
                    mUiHandler.post(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show());
                }
                System.out.println("===上传失败:" + httpStatus + " " + msg);
            }
        });
        executor.execute(updateRunnable);
    }

}

package com.chavez.qpan;

import android.content.Context;
import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Database operation class
 * 数据库操作类
 */
public class DBEngine<T> {
    private Context mContext;
    private DataBaseHandle mDataBaseHandle;

    private ExecutorService mDataBaseExecutor;
    private Handler mUiHandle;


    public DBEngine(Context context) {
        this.mContext = context;
        mDataBaseExecutor = Executors.newSingleThreadExecutor();

        mUiHandle = new Handler(mContext.getMainLooper());
    }


    public interface DataBaseHandle<T> {
        T doInBackground();

        void doInUiThread(T result);
    }

    public void setDataBaseHandle(DataBaseHandle handle) {
        this.mDataBaseHandle = handle;
    }

    public void execute() {
        mDataBaseExecutor.execute(() -> {
            if (mDataBaseHandle != null) {
                postResult((T) mDataBaseHandle.doInBackground());
            }
        });
    }

    private void postResult(T result) {
        mUiHandle.post(() -> {
            mDataBaseHandle.doInUiThread(result);
        });
    }

}

package com.chavez.qpan.fragment;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chavez.qpan.DBEngine;
import com.chavez.qpan.R;
import com.chavez.qpan.adapater.FileListAdapter;
import com.chavez.qpan.adapater.UploadCompletedListAdapter;
import com.chavez.qpan.adapater.UploadListAdapter;
import com.chavez.qpan.animation.AnimationHelper;
import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.model.UploadInfo;
import com.chavez.qpan.providers.UploadColumns;
import com.chavez.qpan.providers.UploadProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UploadCompletedListFragment extends Fragment {
    public final static String TAG = "uploadCompletedList";
    private Context mContext;

    private View mUploadCompletedListLayout;
    private View mEmptyView;
    private View mLoadingView;
    private ImageView mLoadingIconImage;
    private RecyclerView uploadCompletedListRy;
    private List<UploadInfo> mUploadInfos;
    private UploadCompletedListAdapter adapter;

    private UiHandler uiHandler;
    public final static int MSG_UPDATE_DATA = 0;
    private Handler mUploadProviderObserverHandler;
    private UploadProviderObserver mUploadProviderObserver;

    private static String[] queryUploadColumn;

    static {
        queryUploadColumn = new String[]{
                UploadColumns._ID,
                UploadColumns.COLUMN_TYPE,
                UploadColumns.COLUMN_STATUS,
                UploadColumns.COLUMN_URL,
                UploadColumns.COLUMN_TITEL,
                UploadColumns.COLUMN_DATA,
                UploadColumns.COLUMN_CURRENT_BYTE,
                UploadColumns.COLUMN_TOTAL_BYTES,
                UploadColumns.COLUMN_CHUNK,
                UploadColumns.COLUMN_CHUNKS,
                UploadColumns.COLUMN_UPLOAD_PATH,
                UploadColumns.COLUMN_CREATE_TIME
        };
    }

    public UploadCompletedListFragment(Context context) {
        this.mContext = context;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mUploadCompletedListLayout == null) {
            mUploadCompletedListLayout = inflater.inflate(R.layout.upload_completed_list_view, container, false);
        }
        findView();
        initView();
        registerUploadProviderObserver();
        return mUploadCompletedListLayout;
    }


    @Override
    public void onResume() {
        super.onResume();
        showLoadingIfNeed();
        requestLocalData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterUploadProviderObserver();
    }

    void findView() {
        uploadCompletedListRy = mUploadCompletedListLayout.findViewById(R.id.upload_completed_list);
        mEmptyView = mUploadCompletedListLayout.findViewById(R.id.empty_view);
        mLoadingView = mUploadCompletedListLayout.findViewById(R.id.loading_view);
        mLoadingIconImage = mUploadCompletedListLayout.findViewById(R.id.loading_image);
    }

    void initView() {
        adapter = new UploadCompletedListAdapter(mContext, mUploadInfos);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        uploadCompletedListRy.setLayoutManager(layoutManager);
        uploadCompletedListRy.setItemAnimator(null);
        uploadCompletedListRy.setHasFixedSize(true);
        uploadCompletedListRy.setAdapter(adapter);
        mUploadProviderObserverHandler = new UploadProviderObserverHandler(this);
        mUploadProviderObserver = new UploadProviderObserver(mUploadProviderObserverHandler);

        uiHandler = new UiHandler(this);

    }

    void registerUploadProviderObserver() {
        if (mUploadProviderObserver != null) {
            mContext.getContentResolver().registerContentObserver(UploadProvider.CONTENT_URI,
                    true, mUploadProviderObserver);
        }
    }

    void unregisterUploadProviderObserver() {
        if (mUploadProviderObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mUploadProviderObserver);
        }
    }

    void requestLocalData() {
        DBEngine dbEngine = new DBEngine(mContext);
        dbEngine.setDataBaseHandle(new DBEngine.DataBaseHandle() {
            @Override
            public Object doInBackground() {
                Cursor cursor = mContext.getContentResolver().query(UploadProvider.CONTENT_URI, queryUploadColumn, UploadColumns.COLUMN_STATUS + " = ?", new String[]{String.valueOf(UploadInfo.SUCCESS)}, null);
                mUploadInfos = new ArrayList<>();
                while (cursor.moveToNext()) {

                    System.out.println(" id: " + cursor.getString(cursor.getColumnIndex(UploadColumns._ID)));
                    System.out.println(" file Name: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_TITEL)));
                    System.out.println(" file path: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_URL)));
                    System.out.println(" file URI: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_DATA)));
                    System.out.println(" file totalBytes: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_TOTAL_BYTES)));
                    System.out.println(" file type: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_TYPE)));
                    System.out.println("file status: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_STATUS)));
                    System.out.println("file create_time: " + cursor.getString(cursor.getColumnIndex(UploadColumns.COLUMN_CREATE_TIME)));

                    UploadInfo.Reader uploadReader = new UploadInfo.Reader(cursor);
                    UploadInfo uploadInfo = uploadReader.newUploadInfo();
                    mUploadInfos.add(uploadInfo);
                }
                cursor.close();
                return mUploadInfos;
            }

            @Override
            public void doInUiThread(Object result) {
                uiHandler.sendResult(UiHandler.REFRESH, null);
            }
        });
        dbEngine.execute();
    }

    void showLoadingIfNeed() {
        if (mLoadingView != null) {
            if (mUploadInfos == null) {
                showLoading();
            }
        }
    }

    void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        AnimationHelper.translateAnimation(getActivity(), mLoadingIconImage, R.anim.load_ani, false, View.VISIBLE);
    }

    void hideLoading() {
        if (mLoadingView != null) {
            mLoadingIconImage.animate().cancel();
            mLoadingView.setVisibility(View.GONE);
        }
    }

    private void buildMap() {
        adapter.updateData(mUploadInfos);
        hideLoading();
    }

    static class UploadProviderObserverHandler extends Handler {
        private final WeakReference<UploadCompletedListFragment> fragmentReference;

        public UploadProviderObserverHandler(UploadCompletedListFragment fragmentReference) {
            this.fragmentReference = new WeakReference<>(fragmentReference);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            UploadCompletedListFragment uploadListFragment = fragmentReference.get();
            switch (msg.what) {
                case MSG_UPDATE_DATA:
                    uploadListFragment.mUploadInfos = (List<UploadInfo>) msg.obj;
                    if (uploadListFragment.mUploadInfos != null && uploadListFragment.mUploadInfos.size() > 0) {
                        uploadListFragment.mEmptyView.setVisibility(View.GONE);
                    } else {
                        uploadListFragment.mEmptyView.setVisibility(View.VISIBLE);
                    }
                    uploadListFragment.buildMap();
                    break;
                default:
                    break;
            }
        }
    }

    class UploadProviderObserver extends ContentObserver {
        Handler handler;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public UploadProviderObserver(Handler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            System.out.println("===upload completed list UploadProvider onChange");
            DBEngine dbEngine = new DBEngine(mContext);
            dbEngine.setDataBaseHandle(new DBEngine.DataBaseHandle() {
                @Override
                public Object doInBackground() {
                    Cursor cursor = mContext.getContentResolver().query(UploadProvider.CONTENT_URI, queryUploadColumn, UploadColumns.COLUMN_STATUS + " = ?", new String[]{String.valueOf(UploadInfo.SUCCESS)}, null);
                    if (cursor != null) {
                        List<UploadInfo> uploadInfos = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            UploadInfo.Reader reader = new UploadInfo.Reader(cursor);
                            UploadInfo uploadInfo = reader.newUploadInfo();
                            uploadInfos.add(uploadInfo);
                        }
                        cursor.close();
                        return uploadInfos;
                    }
                    return null;
                }

                @Override
                public void doInUiThread(Object result) {
                    if (result != null) {
                        Message message = handler.obtainMessage();
                        message.what = MSG_UPDATE_DATA;
                        List<UploadInfo> uploadInfos = (List<UploadInfo>) result;
                        message.obj = uploadInfos;
                        handler.sendMessage(message);
                    }
                }
            });
            dbEngine.execute();
        }
    }

    static class UiHandler extends Handler {
        private static final int REFRESH = 0;
        private final WeakReference<UploadCompletedListFragment> reference;

        public UiHandler(UploadCompletedListFragment fragment) {
            reference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d(TAG, "FileListHandler---handleMessage + msg.what = " + msg.what);
            UploadCompletedListFragment fragment = reference.get();
            if (fragment != null) {
                switch (msg.what) {
                    case REFRESH:
                        if (fragment.mUploadInfos != null && fragment.mUploadInfos.size() > 0) {
                            fragment.mEmptyView.setVisibility(View.GONE);
                        } else {
                            fragment.mEmptyView.setVisibility(View.VISIBLE);
                        }
                        fragment.buildMap();
                        break;
                }
            }
        }

        private void sendResult(int result, Object obj) {
            UploadCompletedListFragment fragment = reference.get();
            if (fragment != null) {
                Message message = new Message();
                message.what = result;
                message.obj = obj;
                sendMessage(message);
            }
        }
    }
}

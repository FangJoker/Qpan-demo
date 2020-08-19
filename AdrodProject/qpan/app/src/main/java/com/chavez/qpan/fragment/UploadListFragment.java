package com.chavez.qpan.fragment;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chavez.qpan.DBEngine;
import com.chavez.qpan.R;
import com.chavez.qpan.activity.TransmissionListActivity;
import com.chavez.qpan.adapater.UploadListAdapter;
import com.chavez.qpan.animation.AnimationHelper;
import com.chavez.qpan.model.UploadInfo;
import com.chavez.qpan.providers.UploadColumns;
import com.chavez.qpan.providers.UploadProvider;
import com.chavez.qpan.util.support.date.DateSupport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UploadListFragment extends Fragment {
    public final static String TAG = "uploadListFragment";
    private Context mContext;
    private View mUploadListLayout;
    private RecyclerView mUploadRecycleView;
    private View mEmptyView;
    private View mLoadingView;
    private ImageView mLoadingIconImage;
    private UploadListAdapter adapter;
    private List<UploadInfo> mUploadInfos;
    private UiHandler uiHandler;

    public final static int MSG_UPDATE_PROGRESS = 0;
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

    static class UploadProviderObserverHandler extends Handler {
        private final WeakReference<UploadListFragment> fragmentReference;

        public UploadProviderObserverHandler(UploadListFragment fragementReference) {
            this.fragmentReference = new WeakReference<>(fragementReference);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            UploadListFragment uploadListFragment = fragmentReference.get();
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS:
                    uploadListFragment.mUploadInfos = (List<UploadInfo>)msg.obj;
                    if ( uploadListFragment.mUploadInfos != null &&  uploadListFragment.mUploadInfos.size() > 0) {
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
            System.out.println("===UploadList UploadProvider onChange");
            DBEngine dbEngine = new DBEngine(mContext);
            dbEngine.setDataBaseHandle(new DBEngine.DataBaseHandle() {
                @Override
                public Object doInBackground() {
                    Cursor cursor = mContext.getContentResolver().query(UploadProvider.CONTENT_URI, queryUploadColumn, UploadColumns.COLUMN_STATUS +" = ?", new String[]{String.valueOf(UploadInfo.RUNNING)}, null);
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
                        message.what = MSG_UPDATE_PROGRESS;
                        List<UploadInfo> uploadInfos = (List<UploadInfo>) result;
                        message.obj = uploadInfos;
                        handler.sendMessage(message);
                    }
                }
            });
            dbEngine.execute();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mUploadListLayout == null) {
            mUploadListLayout = inflater.inflate(R.layout.upload_list, container, false);
        }
        findView();
        initView();
        registerUploadProviderObserver();
        return mUploadListLayout;
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

    public UploadListFragment(Context context) {
        mContext = context;
    }

    void requestLocalData() {
        DBEngine dbEngine = new DBEngine(mContext);
        dbEngine.setDataBaseHandle(new DBEngine.DataBaseHandle() {
            @Override
            public Object doInBackground() {
                Cursor cursor = mContext.getContentResolver().query(UploadProvider.CONTENT_URI, queryUploadColumn, UploadColumns.COLUMN_STATUS +" = ?", new String[]{String.valueOf(UploadInfo.RUNNING)}, null);
                mUploadInfos = new ArrayList<>();
                while (cursor.moveToNext()) {
                    UploadInfo.Reader uploadReader = new UploadInfo.Reader(cursor);
                    UploadInfo uploadInfo = uploadReader.newUploadInfo();
                    mUploadInfos.add(uploadInfo);
                }
                cursor.close();
                return mUploadInfos;
            }

            @Override
            public void doInUiThread(Object result) {
                uiHandler.sendResult(UiHandler.REFRESH,null);
            }
        });
        dbEngine.execute();
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


    void findView() {
        mUploadRecycleView = mUploadListLayout.findViewById(R.id.transmission_list);
        mEmptyView = mUploadListLayout.findViewById(R.id.empty_view);
        mLoadingView = mUploadListLayout.findViewById(R.id.loading_view);
        mLoadingIconImage = mUploadListLayout.findViewById(R.id.loading_image);
    }

    void initView() {
        adapter = new UploadListAdapter(mUploadInfos, mContext);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mUploadRecycleView.setLayoutManager(layoutManager);
        mUploadRecycleView.setItemAnimator(null);
        mUploadRecycleView.setHasFixedSize(true);
        mUploadRecycleView.setAdapter(adapter);
        mUploadProviderObserverHandler = new UploadProviderObserverHandler(this);
        mUploadProviderObserver = new UploadProviderObserver(mUploadProviderObserverHandler);

        uiHandler = new UiHandler(this);

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


    static class UiHandler extends Handler {
        private static final int REFRESH = 0;
        private final WeakReference<UploadListFragment> reference;

        public UiHandler (UploadListFragment fragment) {
            reference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d(TAG, "FileListHandler---handleMessage + msg.what = " + msg.what);
            UploadListFragment fragment = reference.get();
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
            UploadListFragment fragment= reference.get();
            if (fragment!= null) {
                Message message = new Message();
                message.what = result;
                message.obj = obj;
                sendMessage(message);
            }
        }
    }

    private void buildMap() {
        adapter.updateData(mUploadInfos);
        hideLoading();
    }

}

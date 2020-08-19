package com.chavez.qpan.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.chavez.qpan.DBEngine;
import com.chavez.qpan.R;
import com.chavez.qpan.activity.LocalFileActivity;
import com.chavez.qpan.activity.LoginActivity;
import com.chavez.qpan.adapater.UserMainFunctionListAdapter;
import com.chavez.qpan.model.UserMainFunctionVO;
import com.chavez.qpan.providers.UserColumns;
import com.chavez.qpan.providers.UserInfoProvider;
import com.chavez.qpan.util.support.file.FileManagerSupport;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class UserMineFragment extends Fragment {
    public final static int RECYCLE_BIN_INDEX = -1;
    public final static int LOCAL_DOWNLOAD_INDEX = 0;
    public final static int EXIT =2;
    DecimalFormat df = new DecimalFormat("0.00");
    private String account;
    private Long personalFreeBytes;
    private Long getPersonalTotalBytes;
    private TextView useraAccountTv;
    private TextView freeBytesTv;
    private ProgressBar freeByteProgressBar;
    private View mUserMineView;
    private ListView mFunctionListView;
    private static List<UserMainFunctionVO> userMainFunctionVOList = new ArrayList<>();

    private Context mContext;

    static {
//        UserMainFunctionVO item1 = new UserMainFunctionVO();
        UserMainFunctionVO item2 = new UserMainFunctionVO();
        UserMainFunctionVO item3 = new UserMainFunctionVO();
        UserMainFunctionVO item4 = new UserMainFunctionVO();
//        item1.setIconResourceId(R.drawable.recycle_bin_icon);
//        item1.setIndexNameResourceId(R.string.recycle_bin);
        item2.setIconResourceId(R.drawable.local_download);
        item2.setIndexNameResourceId(R.string.local_download);
        item3.setIndexNameResourceId(R.string.disk_promote);
        item3.setIconResourceId(R.drawable.disk);
        item4.setIndexNameResourceId(R.string.out);
        item4.setIconResourceId(R.drawable.out);
//        userMainFunctionVOList.add(item1);
        userMainFunctionVOList.add(item2);
        userMainFunctionVOList.add(item3);
        userMainFunctionVOList.add(item4);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mUserMineView = inflater.inflate(R.layout.user_mian, container, false);
        findView();
        init();
        setCustomActionBar();
        requestLocalData();
        return mUserMineView;
    }

    private void findView() {
        mFunctionListView = mUserMineView.findViewById(R.id.user_main_function_list);
        mFunctionListView.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case RECYCLE_BIN_INDEX:
                    break;
                case LOCAL_DOWNLOAD_INDEX:
                    Intent activityIntent = new Intent();
                    activityIntent.setClass(mContext, LocalFileActivity.class);
                    startActivity(activityIntent);
                    break;
                case  EXIT:
                    mContext.getContentResolver().delete(UserInfoProvider.CONTENT_URI,null,null);
                    Intent loginActivityIntent = new Intent();
                    loginActivityIntent.setClass(mContext, LoginActivity.class);
                    startActivity(loginActivityIntent);
                    break;
                default:
                    break;
            }
        });
        useraAccountTv = mUserMineView.findViewById(R.id.user_main_username_text);
        freeBytesTv = mUserMineView.findViewById(R.id.user_main_free_bytes);
        freeByteProgressBar = mUserMineView.findViewById(R.id.user_main_free_bytes_progress);
    }

    private void init() {
        UserMainFunctionListAdapter userMainFunctionListAdapter = new UserMainFunctionListAdapter(mContext);
        userMainFunctionListAdapter.setData(userMainFunctionVOList);
        mFunctionListView.setAdapter(userMainFunctionListAdapter);
    }

    private void requestLocalData() {
        DBEngine dbEngine = new DBEngine(mContext);
        dbEngine.setDataBaseHandle(new DBEngine.DataBaseHandle() {
            @Override
            public Object doInBackground() {
                Cursor cursor = mContext.getContentResolver().query(UserInfoProvider.CONTENT_URI,
                        new String[]{UserColumns.COLUMN_FREE_BYTES,
                                UserColumns.COLUMN_USER_ACCOUNT,
                                UserColumns.COLUMN_TOTAL_BYTES},
                        null,
                        null,
                        null);
                if (cursor.moveToLast()) {
                    account = cursor.getString(cursor.getColumnIndex(UserColumns.COLUMN_USER_ACCOUNT));
                    personalFreeBytes = cursor.getLong(cursor.getColumnIndex(UserColumns.COLUMN_FREE_BYTES));
                    getPersonalTotalBytes = cursor.getLong(cursor.getColumnIndex(UserColumns.COLUMN_TOTAL_BYTES));
                }
                return null;
            }

            @Override
            public void doInUiThread(Object result) {
                String persent = df.format(personalFreeBytes / getPersonalTotalBytes);
                freeByteProgressBar.setProgress((int) (Float.valueOf(persent) * 100));
                useraAccountTv.setText(account);
                freeBytesTv.setText(FileManagerSupport.getFormatSize(personalFreeBytes));
            }
        });
        dbEngine.execute();
    }

    public UserMineFragment(Context context) {
        mContext = context;
    }

    private void setCustomActionBar() {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.LEFT);
        lp.setMargins(0, 0, 0, 0);
        View localDownloadFileActionBarView = LayoutInflater.from(mContext).inflate(R.layout.user_mian_action_bar, null);
        AppCompatActivity activity = (AppCompatActivity) mContext;
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(localDownloadFileActionBarView, lp);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.show();
        }
    }

}

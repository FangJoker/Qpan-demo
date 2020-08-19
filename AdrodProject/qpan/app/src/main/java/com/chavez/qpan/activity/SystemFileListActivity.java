package com.chavez.qpan.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chavez.qpan.BuildConfig;
import com.chavez.qpan.R;
import com.chavez.qpan.adapater.SystemFileListAdapter;
import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.model.UploadInfo;
import com.chavez.qpan.providers.UploadColumns;
import com.chavez.qpan.providers.UploadProvider;
import com.chavez.qpan.util.support.file.FileConst;
import com.chavez.qpan.util.support.file.FileManagerSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SystemFileListActivity extends AppCompatActivity {
    static int BACK_TIME = 0;
    private List<Integer> positionList = new LinkedList<>();
    private FileManagerSupport mFileManagerSupport = new FileManagerSupport();
    private List<BaseFileVO> fileVOList = new ArrayList<>();
    private RecyclerView mFileListView;
    private SystemFileListAdapter fileListAdapter;
    private String token;
    private String currentPathUuid;

    private View mEmptyView;
    private Button mUploadActionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.token = getIntent().getExtras().getString("token");
        this.currentPathUuid = getIntent().getExtras().getString("currentPathUuid");
        setContentView(R.layout.activity_system_file_list);
        setActionBar();
        findView();
        initView();
        checkPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        positionList.clear();
    }

    /**
     * 运行时确认权限
     */
    void checkPermission() {
        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            // 如果第一次拒绝，则会返回True
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // 再次请求权限
                Toast.makeText(this, "需要读取文件目录", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        } else {
            fileVOList = mFileManagerSupport.getFileList(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (fileListAdapter != null) {
                fileListAdapter.updateData(fileVOList);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "用户拒绝授权", Toast.LENGTH_LONG).show();
            finish();
        } else {
            fileVOList = mFileManagerSupport.getFileList(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (fileListAdapter != null) {
                fileListAdapter.updateData(fileVOList);
            }
        }
    }

    void findView() {
        mFileListView = findViewById(R.id.system_file_list);
        mEmptyView = findViewById(R.id.empty_view);
        mUploadActionBtn = findViewById(R.id.upload_action);
    }

    void initView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mFileListView.setLayoutManager(layoutManager);
        mFileListView.setHasFixedSize(true);
        mFileListView.setItemAnimator(null);
        mFileListView.setFocusable(true);
        fileListAdapter = new SystemFileListAdapter(this, fileVOList);
        fileListAdapter.setItemOnClickListener((view, position) -> {
            BaseFileVO fileItem = fileVOList.get(position);
            // 判断是否进入文件夹
            if (fileItem.getType() == FileConst.IS_FOLDER) {
                // 获取子目录
                fileVOList = mFileManagerSupport.getFileList(fileItem.getPath());
                fileListAdapter.updateData(fileVOList);
                // 重新选择
                positionList.clear();
                mUploadActionBtn.setText("确认上传");
                if (fileVOList.size() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        });
        fileListAdapter.setItemCheckBoxOnCheckListener((button, position) -> addOrRemoveOnSelectItem(position));
        mFileListView.setAdapter(fileListAdapter);
        mUploadActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int index : positionList) {
                    insetNewDownload(getUploadInfo(new File(fileVOList.get(index).getPath())));
                }
                Intent activityIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("page", String.valueOf(TransmissionListActivity.RESULT_UPLOAD_PAGE));
                activityIntent.putExtras(bundle);
                activityIntent.setClass(SystemFileListActivity.this, TransmissionListActivity.class);
                startActivityForResult(activityIntent, TransmissionListActivity.RESULT_UPLOAD_PAGE);
                finish();
            }
        });
    }

    UploadInfo getUploadInfo(File file) {
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.setUrl(file.getAbsolutePath());
        uploadInfo.setType(FileManagerSupport.getFileType(file));
        uploadInfo.setChunk(1);
        uploadInfo.setChunks(1);
        uploadInfo.setTotalBytes(file.length());
        uploadInfo.setCurrentBytes(Long.parseLong("0"));
        uploadInfo.setStatus(UploadInfo.READY);
        uploadInfo.setTitle(file.getName());
        uploadInfo.setUploadPath(currentPathUuid);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            dataUri = FileProvider.getUriForFile(this,
//                    BuildConfig.APPLICATION_ID + ".fileProvider", new File(file.getAbsolutePath()));
//        } else {
//            dataUri = Uri.fromFile(new File(file.getAbsolutePath()));
//        }
        return uploadInfo;
    }


    void insetNewDownload(UploadInfo info) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UploadColumns.COLUMN_URL, info.getUrl());
        contentValues.put(UploadColumns.COLUMN_CHUNK, info.getChunk());
        contentValues.put(UploadColumns.COLUMN_CHUNKS, info.getChunk());
        contentValues.put(UploadColumns.COLUMN_CURRENT_BYTE, info.getCurrentBytes());
        contentValues.put(UploadColumns.COLUMN_TOTAL_BYTES, info.getTotalBytes());
        contentValues.put(UploadColumns.COLUMN_DATA, info.getData());
        contentValues.put(UploadColumns.COLUMN_STATUS, UploadInfo.READY);
        contentValues.put(UploadColumns.COLUMN_TITEL, info.getTitle());
        contentValues.put(UploadColumns.COLUMN_TYPE, info.getType());
        contentValues.put(UploadColumns.COLUMN_UPLOAD_PATH,info.getUploadPath());
        Uri uri = getContentResolver().insert(UploadProvider.CONTENT_URI, contentValues);
        System.out.println("==systemFileActivity insert: " + uri.toString());
        // 更新uri
        ContentValues updateValues = new ContentValues();
        updateValues.put(UploadColumns.COLUMN_DATA,uri.toString());
        getContentResolver().update(uri,updateValues,null,null);
    }

    void addOrRemoveOnSelectItem(int position) {
        System.out.println("postion:" + position + "is  on click:");
        if (positionList.contains(new Integer(position))) {
            positionList.remove(new Integer(position));
        } else {
            positionList.add(new Integer(position));
        }
        if (positionList.size() == 0) {
            mUploadActionBtn.setText("确认上传");
        } else {
            mUploadActionBtn.setText("确认上传" + positionList.size() + "个文件");
        }
    }


    @Override
    public void onBackPressed() {
        int curDirDepth = getCurrentDirDepth();
        if (curDirDepth == 1) {
            if (++BACK_TIME < 2) {
                Toast.makeText(this, "再按一次返回上一步", Toast.LENGTH_LONG).show();
            } else {
                BACK_TIME = 0;
                backDir();
                finish();
            }
        } else {
            backDir();
        }
    }


    public void backDir() {
        if (mEmptyView.getVisibility() == View.VISIBLE) {
            mEmptyView.setVisibility(View.GONE);
        }
        if (mFileManagerSupport.getDirManager().dirStackPull() != null) {
            fileVOList = mFileManagerSupport.getDirManager().getCurDir();
            fileListAdapter.updateData(fileVOList);
        }
    }

    public int getCurrentDirDepth() {
        return mFileManagerSupport.getDirManager().getDirDepth();
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("选择文件上传");
    }
}

package com.chavez.qpan.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.chavez.qpan.BuildConfig;
import com.chavez.qpan.DBEngine;
import com.chavez.qpan.DownloadHelper;
import com.chavez.qpan.R;
import com.chavez.qpan.adapater.FileListAdapter;
import com.chavez.qpan.animation.AnimationHelper;
import com.chavez.qpan.fragment.FileListFragment;
import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.providers.UploadColumns;
import com.chavez.qpan.receiver.action.QpanAction;
import com.chavez.qpan.util.support.SyncSupport;
import com.chavez.qpan.util.support.file.DirManager;
import com.chavez.qpan.util.support.file.FileConst;
import com.chavez.qpan.util.support.file.FileManagerSupport;
import com.chavez.qpan.util.support.web.ServiceApi;
import com.chavez.qpan.view.CustomDialog;
import com.chavez.qpan.view.MultiChoiceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LocalFileActivity extends AppCompatActivity {
    private DirManager dirManager;
    private List<Integer> actionBarChoicePositionList = new LinkedList<>();
    private FileManagerSupport mFileManager = new FileManagerSupport();
    private List<BaseFileVO> fileVOList;
    List<BaseFileVO> fileVOListCache;
    private RecyclerView mFileListView;
    private FileListAdapter fileListAdapter;
    private FileListHandler mFileListHandler;
    private ActionMode actionMode;

    private View menuLayout;
    private View mLoadingView;
    private ImageView mLoadingIconImage;
    private Button shareBtn;
    private Button downloadBtn;
    private Button deleteBtn;

    private View mEmptyView;


    static class FileListHandler extends Handler {
        private static final int REFRESH = 0;
        private final WeakReference<LocalFileActivity> reference;

        public FileListHandler( LocalFileActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            LocalFileActivity activity = reference.get();
            if (activity != null) {
                switch (msg.what) {
                    case REFRESH:
                        activity.fileVOList = (List<BaseFileVO>) msg.obj;
                        if (activity.fileVOList != null && activity.fileVOList.size() > 0) {
                            activity.mEmptyView.setVisibility(View.GONE);
                        } else {
                            activity.mEmptyView.setVisibility(View.VISIBLE);
                        }
                        activity.buildMap();
                        break;
                }
            }
        }

        private void sendResult(int result, Object obj) {
            LocalFileActivity activity = reference.get();
            if (activity != null) {
                Message message = new Message();
                message.what = result;
                message.obj = obj;
                sendMessage(message);
            }
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_file);
        setCustomActionBar();
        findView();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFileListIfNeed();
        showLoadingIfNeed();
    }

    void findView() {
        dirManager = mFileManager.getDirManager();
        menuLayout = findViewById(R.id.file_list_menu);
        shareBtn = findViewById(R.id.menu_share_icon);
        downloadBtn = findViewById(R.id.menu_download_icon);
        deleteBtn = findViewById(R.id.menu_delete_icon);
        mEmptyView =findViewById(R.id.empty_view);
        mLoadingView = findViewById(R.id.loading_view);
        mLoadingIconImage = findViewById(R.id.loading_image);
        mFileListView = findViewById(android.R.id.list);
    }

    void initView(){
        shareBtn.setOnClickListener(new MenuButtonOnclickListener());
        deleteBtn.setOnClickListener(new MenuButtonOnclickListener());
        downloadBtn.setOnClickListener(new  MenuButtonOnclickListener());
        // 设置recyclerView 默认垂直方向
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mFileListView.setLayoutManager(layoutManager);
        // 设置item动画
        mFileListView.setItemAnimator(null);
        mFileListView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        //所以往往是直接先设置这个为true，当需要布局重新计算宽高的时候才调用nofityDataSetChange
        mFileListView.setHasFixedSize(true);
        // 设置适配器
        fileListAdapter = new FileListAdapter(this, null);
        fileListAdapter.setOnItemOnClickListener(new FileListAdapter.OnItemOnClickListener() {
            @Override
            public void itemOnClick(View item, int position) {
                // 判断是否进入多选模式
                if (actionMode != null) {
                    addOrRemoveOnSelectItem(position);
                } else {
                    BaseFileVO fileItem = fileVOList.get(position);
                    // 判断是否进入文件夹
                    if (fileItem.getType() == FileConst.IS_FOLDER) {
                        // 获取子目录
                        requestLocalData(fileItem.getPath());
                        showLoading();
                        mFileListHandler.sendResult(FileListHandler.REFRESH, null);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String type = "*/*";
                        if (fileItem.getType() == FileConst.IS_APK_FILE) {
                            type = "application/vnd.android.package-archive";
                        }
                        if (fileItem.getType() == FileConst.IS_AUDIO_FILE) type = "audio/*";
                        if (fileItem.getType() == FileConst.IS_VIDEO_FILE) type = "video/*";
                        if (fileItem.getType() == FileConst.IS_IMAGE_FILE) type = "image/*";
                        Uri contentUri;
                        //打开本地文件
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            contentUri = FileProvider.getUriForFile(LocalFileActivity.this,
                                    BuildConfig.APPLICATION_ID + ".fileProvider", new File(fileVOList.get(position).getPath()));
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            contentUri = Uri.fromFile(new File(fileItem.getPath()));
                        }
                        intent.setDataAndType(contentUri, type);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void itemOnLongClick(View item, int position) {
                if (actionMode == null) {
                    actionMode = LocalFileActivity.this.startActionMode(new ItemActionModeCallback());
                    addOrRemoveOnSelectItem(position);
                }
            }
        });
        mFileListView.setAdapter(fileListAdapter);
        mFileListView.setFocusable(true);
        mFileListHandler = new FileListHandler(this);
    }

    void requestLocalData(String path){
        new SyncSupport<List<BaseFileVO>>() {
            @Override
            public List<BaseFileVO> doInWorkerThread() {
              fileVOList =  mFileManager.getFileList(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
              if (fileVOList !=null && fileVOList.size()!=0){
                  return fileVOList;
              }else {
                  return null;
              }
            }
            @Override
            public void doInUiThread(List<BaseFileVO> result) {
                mFileListHandler.sendResult(FileListHandler.REFRESH,result);
            }
        }.executeSync();
    }

    void getFileListIfNeed() {
        if (fileVOList == null) {
            requestLocalData(null);
        } else {
            fileListAdapter.updateData(fileVOList);
        }
    }



    void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        AnimationHelper.translateAnimation(this, mLoadingIconImage, R.anim.load_ani, false, View.VISIBLE);
    }

    void showLoadingIfNeed() {
        if (mLoadingView != null) {
            if (fileVOList == null) {
                showLoading();
            }
        }
    }

    void hideLoading() {
        if (mLoadingView != null) {
            mLoadingIconImage.animate().cancel();
            mLoadingView.setVisibility(View.GONE);
        }
    }

    private void buildMap() {
        fileListAdapter.updateData(fileVOList);
        hideLoading();
    }

    private class MenuButtonOnclickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_share_icon:
                    System.out.println("分享");
                    break;
                case R.id.menu_download_icon:
                    DownloadHelper downloadHelper = DownloadHelper.getInstance(LocalFileActivity.this);
                    for(int index: actionBarChoicePositionList){
                        System.out.println("下载文件:"+fileVOList.get(index).getName());
                        if (actionMode!=null){
                            actionMode.finish();
                        }
                    }
                    break;
                case R.id.menu_delete_icon:
                    deleteConfirmDialog(actionBarChoicePositionList);
                    break;
                default:
                    break;
            }
        }
    }

    private void deleteConfirmDialog(List<Integer> position) {
        CustomDialog dialog = new CustomDialog.Builder(LocalFileActivity.this)
                .setTitle(getResources().getString(R.string.alert))
                .setMessage(getResources().getString(R.string.confirm_delete, position.size()))
                .setAlertType(CustomDialog.MESSAGE_TYPE_WARNING)
                .setPositiveBtnText(getResources().getString(R.string.confirm))
                .setNegativeBtnText(getResources().getString(R.string.cancel))
                .setBtnOnclickListener(new CustomDialog.DialogOnClickListen() {
                    @Override
                    public void onPositive(CustomDialog dialog) {
                        String[] fileNames = new String[position.size()];
                        List<BaseFileVO> removeFiles = new ArrayList<>();
                        for (int i = 0; i < position.size(); i++) {
                            BaseFileVO item = fileVOList.get(position.get(i));
                            fileNames[i] = item.getPath();
                            removeFiles.add(item);
                            System.out.println("删除文件:" + item.getPath());
                        }
                        try {
                            FileManagerSupport.deleteFileIfExists(fileNames);
                            fileVOList.removeAll(removeFiles);
                            mFileListHandler.sendResult(FileListHandler.REFRESH, null);
                        } catch (FileNotFoundException e) {
                        } finally {
                            dialog.cancel();
                            actionMode.finish();
                        }
                    }

                    @Override
                    public void onNegative(CustomDialog dialog) {
                        dialog.cancel();
                    }
                }).create();
        dialog.show();
    }


    /**
     * 唤出Menu 动画
     */
    void menuStartAnim() {
        sendBroadcast(new Intent(QpanAction.fileActionMenuStart));
        menuLayout.setVisibility(View.GONE);
        AnimationHelper.translateAnimation(this, menuLayout, R.anim.menu_start_ani, false, View.VISIBLE);
    }

    /**
     * Menu 退出动画
     */
    void menuOutAnim() {
        sendBroadcast(new Intent(QpanAction.fileActionMenuEnd));
        menuLayout.setVisibility(View.VISIBLE);
        AnimationHelper.translateAnimation(this, menuLayout, R.anim.menu_out_ani, false, View.GONE);
    }

    /**
     * actionMode ActionBar动画唤出
     */
    void customActionBarStartAnim() {
        if (actionMode != null) {
            customActionBar = new MultiChoiceView(this);
            actionMode.setCustomView(customActionBar);
            AnimationHelper.translateAnimation(this, customActionBar, R.anim.actionbar_start_ani, false, View.VISIBLE);
            customActionBar.setSelectAllListener(new MultiChoiceView.SelectAllListener() {
                @Override
                public void selectAll() {
                    actionBarChoicePositionList.clear();
                    for (int i = 0; i < fileListAdapter.getItemCount(); i++) {
                        actionBarChoicePositionList.add(i);
                    }
                    updateCustomActionBarTitle(getResources().getString(R.string.file_action_bar_multi_choice_title, actionBarChoicePositionList.size()));
                    fileListAdapter.setSelectAll();
                }

                @Override
                public void cancel() {
                    fileListAdapter.setCancelSelectAll();
                    if (actionMode != null) {
                        actionMode.finish();
                        actionBarChoicePositionList.clear();
                        menuOutAnim();
                    }
                }
            });
        }
    }


    private MultiChoiceView customActionBar;

    /**
     * ActionMode 实现类
     */
    class ItemActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (actionMode == null) {
                actionMode = mode;
                menuStartAnim();
                customActionBarStartAnim();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.share_file:
//                    Log.i(TAG, "分享文件");
//                    break;
//                case R.id.download_file:
//                    Log.i(TAG, "下载文件");
//                    break;
//                case R.id.delete_file:
//                    Log.i(TAG, "删除文件");
//                    break;
//                default:
//                    return false;
//            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            fileListAdapter.setCancelSelectAll();
            actionBarChoicePositionList.clear();
            menuOutAnim();
//            mFileActionBar.setVisibility(View.VISIBLE);
        }

    }

    void addOrRemoveOnSelectItem(int position) {
        if (actionBarChoicePositionList.contains(new Integer(position))) {
            actionBarChoicePositionList.remove(new Integer(position));
            fileListAdapter.setItemChecked(false, position);
        } else {
            actionBarChoicePositionList.add(new Integer(position));
            fileListAdapter.setItemChecked(true, position);
        }
        if (actionBarChoicePositionList.size() == 0) {
            actionMode.finish();
            menuOutAnim();
        } else {
            updateCustomActionBarTitle(getResources().getString(R.string.file_action_bar_multi_choice_title, actionBarChoicePositionList.size()));
        }

    }

    void updateCustomActionBarTitle(String info) {
        if (customActionBar != null) {
            customActionBar.setTitle(info);
            // 更新视图
            customActionBar.requestLayout();
        }
    }

    /**
     * 返回上层目录
     */
    public void backDir() {
        if (dirManager.dirStackPull() != null) {
            mEmptyView.setVisibility(View.GONE);
            fileVOList = dirManager.getCurDir();
            fileListAdapter.updateData(fileVOList);
        }
    }

    /**
     * 返回当前目录深度
     *
     * @return
     */
    public int getCurrentDirDepth() {
        return dirManager.getDirDepth();
    }

    private void setCustomActionBar() {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.LEFT);
        lp.setMargins(0, 0, 0, 0);
        View localDownloadFileActionBarView = LayoutInflater.from(this).inflate(R.layout.local_download_file_action_bar, null);
        ActionBar actionBar = getSupportActionBar();
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

package com.chavez.qpan.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chavez.qpan.DownloadHelper;
import com.chavez.qpan.R;
import com.chavez.qpan.activity.SystemFileListActivity;
import com.chavez.qpan.activity.TransmissionListActivity;
import com.chavez.qpan.adapater.FileListAdapter;
import com.chavez.qpan.animation.AnimationHelper;
import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.model.ShareVo;
import com.chavez.qpan.receiver.action.QpanAction;
import com.chavez.qpan.util.support.file.DirManager;
import com.chavez.qpan.util.support.file.FileConst;
import com.chavez.qpan.util.support.file.FileManagerSupport;
import com.chavez.qpan.util.support.http.HttpSupport;
import com.chavez.qpan.util.support.http.IHttpResponseHandle;
import com.chavez.qpan.util.support.http.ResponseEntity;
import com.chavez.qpan.util.support.web.ServiceApi;
import com.chavez.qpan.view.CustomDialog;
import com.chavez.qpan.view.MultiChoiceView;


import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chavezQiu
 */
public class FileListFragment extends Fragment {
    DirManager dirManager;

    static final String PATH2 = "storage/emulated/0/Download/";
    static final String TAG = "FileListFragment";
    private String accessToken;
    private List<Integer> actionBarChoicePositionList = new LinkedList<>();
    private FileManagerSupport mFileManager = new FileManagerSupport();
    private List<BaseFileVO> fileVOList;
    /**
     * 文件目录信息备份，用户搜索结束的时候返回。
     */
    private List<BaseFileVO> fileVOListCache;

    /**
     * 当前所在云端目录的uuid，用于上传文件到云端
     */
    private String currentPathUuid = null;

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

    private View mFileListLayout;
    private View mEmptyView;

    private Context mContext;

    EditText searchFileEt;
    TextView cancelSearchFileTv;

    public FileListFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mFileListLayout == null)
            mFileListLayout = inflater.inflate(R.layout.file_list, container, false);
        initView();
        setFileListCustomActionBar();
        return mFileListLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        requestFileList(null);
        showLoadingIfNeed();
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    void getFileListIfNeed() {
        if (fileVOList == null) {
            requestFileList(null);
        } else {
            fileListAdapter.updateData(fileVOList);
        }
    }

    void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        AnimationHelper.translateAnimation(getActivity(), mLoadingIconImage, R.anim.load_ani, false, View.VISIBLE);
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

    public FileListFragment(Context context) {
        this.mContext = context;
    }


    public void initView() {
        dirManager = mFileManager.getDirManager();
        menuLayout = mFileListLayout.findViewById(R.id.file_list_menu);
        shareBtn = mFileListLayout.findViewById(R.id.menu_share_icon);
        downloadBtn = mFileListLayout.findViewById(R.id.menu_download_icon);
        deleteBtn = mFileListLayout.findViewById(R.id.menu_delete_icon);
        mEmptyView = mFileListLayout.findViewById(R.id.empty_view);
        mLoadingView = mFileListLayout.findViewById(R.id.loading_view);
        mLoadingIconImage = mFileListLayout.findViewById(R.id.loading_image);
        mFileListView = mFileListLayout.findViewById(android.R.id.list);
        shareBtn.setOnClickListener(new MenuButtonOnclickListener());
        deleteBtn.setOnClickListener(new MenuButtonOnclickListener());
        downloadBtn.setOnClickListener(new MenuButtonOnclickListener());
        // 设置recyclerView 默认垂直方向
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mFileListView.setLayoutManager(layoutManager);
        // 设置item动画
        mFileListView.setItemAnimator(null);
        //所以往往是直接先设置这个为true，当需要布局重新计算宽高的时候才调用nofityDataSetChange
        mFileListView.setHasFixedSize(true);
        // 设置适配器
        fileListAdapter = new FileListAdapter(getContext(), null);
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
                        requestFileList(fileItem.getUuid());
                        currentPathUuid = fileItem.getUuid();
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
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            contentUri = FileProvider.getUriForFile(getActivity(),
//                                    BuildConfig.APPLICATION_ID + ".fileProvider", new File(fileVOList.get(position).getPath()));
//                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        } else {
//                            contentUri = Uri.fromFile(new File(fileItem.getPath()));
//                        }
                        String resourceUrl = ServiceApi.File.DOWNLOAD_FILE + "/" + fileItem.getUuid() + "?token=" + accessToken;
                        Log.d(TAG, "itemOnClick: open resource:" + resourceUrl);
                        contentUri = Uri.parse(resourceUrl);
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
                    actionMode = getActivity().startActionMode(new ItemActionModeCallback());
                    addOrRemoveOnSelectItem(position);
                }
            }
        });
        mFileListView.setAdapter(fileListAdapter);
        mFileListView.setFocusable(true);
        mFileListHandler = new FileListHandler(this);
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
                    shareConfirmDialog();
                    break;
                case R.id.menu_download_icon:
                    DownloadHelper downloadHelper = DownloadHelper.getInstance(mContext);
                    for (int index : actionBarChoicePositionList) {
                        System.out.println("size:" + actionBarChoicePositionList.size());
                        System.out.println("index: " + index);
                        String fileName = fileVOList.get(index).getName();
                        String requestUrl = ServiceApi.File.DOWNLOAD_FILE + "/" + fileVOList.get(index).getUuid() + "?token=" + accessToken;
                        downloadHelper.download(requestUrl, fileName);
                    }
                    if (actionMode != null) {
                        actionMode.finish();
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

    private void shareConfirmDialog() {
        CustomDialog dialog = new CustomDialog.Builder(getActivity())
                .setTitle("分享文件")
                .setMessage("输入分享密码")
                .setAlertType(CustomDialog.MESSAGE_TYPE_NORMAL)
                .setPositiveBtnText(getResources().getString(R.string.confirm))
                .setNegativeBtnText(getResources().getString(R.string.cancel))
                .setEditText("分享密码")
                .setBtnOnclickListener(new CustomDialog.DialogOnClickListen() {
                    @Override
                    public void onPositive(CustomDialog dialog) {
                        try {
                            HttpSupport httpSupport = new HttpSupport(new IHttpResponseHandle() {
                                @Override
                                public void success(ResponseEntity entity) {
                                    JSONObject responseJsonObject = JSONObject.parseObject(entity.getResponse());
                                    String key = responseJsonObject.getString("key");
                                    showShareKeyDialog(key);
                                    Toast.makeText(mContext, "分享成功", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void error(ResponseEntity entity) {
                                    Toast.makeText(mContext, "分享失败: " + entity.getResponse(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            ShareVo shareVo = new ShareVo();
                            List<String> shareUuids = new ArrayList<>();
                            for (int index : actionBarChoicePositionList) {
                                System.out.println("size:" + actionBarChoicePositionList.size());
                                System.out.println("index: " + index);
                                BaseFileVO item = fileVOList.get(index);
                                shareUuids.add(item.getUuid());
                            }
                            shareVo.setUuids(shareUuids);
                            shareVo.setPassWord(dialog.getEditText());
                            JSONObject paramJson = new JSONObject();
                            paramJson.put("uuids", shareVo.getUuids());
                            paramJson.put("passWord", shareVo.getPassWord());
                            List<Map<String, String>> headers = HttpSupport.obtainTokenHead(accessToken);
                            httpSupport.doPost(ServiceApi.User.SHARE_FILE, paramJson, headers);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        } finally {
                            dialog.cancel();
                        }
                    }

                    @Override
                    public void onNegative(CustomDialog dialog) {
                        dialog.cancel();
                    }
                }).create();
        dialog.show();
    }

    private void showShareKeyDialog(String key) {
        CustomDialog dialog = new CustomDialog.Builder(getActivity())
                .setTitle("分享密匙")
                .setIsSingle(true)
                .setMessage(key)
                .setAlertType(CustomDialog.MESSAGE_TYPE_WARNING)
                .setPositiveBtnText(getResources().getString(R.string.confirm))
                .setNegativeBtnText(getResources().getString(R.string.cancel))
                .setBtnOnclickListener(new CustomDialog.DialogOnClickListen() {
                    @Override
                    public void onPositive(CustomDialog dialog) {
                        dialog.cancel();
                    }

                    @Override
                    public void onNegative(CustomDialog dialog) {
                        dialog.cancel();
                    }
                }).create();

        dialog.show();
    }

    private void deleteConfirmDialog(List<Integer> position) {
        CustomDialog dialog = new CustomDialog.Builder(getActivity())
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
                        HttpSupport httpSupport = new HttpSupport(new IHttpResponseHandle() {
                            @Override
                            public void success(ResponseEntity entity) {
                                fileVOList.removeAll(removeFiles);
                                mFileListHandler.sendResult(FileListHandler.REFRESH, null);
                                Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                                actionMode.finish();
                            }

                            @Override
                            public void error(ResponseEntity entity) {
                                Toast.makeText(mContext, "删除失败: " + entity.getResponse(), Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                                actionMode.finish();
                            }
                        });
                        for (int i = 0; i < removeFiles.size(); i++) {
                            String requestUrl = ServiceApi.File.DELETE_FILE + "/" + removeFiles.get(i).getUuid();
                            List<Map<String, String>> heads = HttpSupport.obtainTokenHead(accessToken);
                            httpSupport.doGet(requestUrl, heads);
                        }
                    }

                    @Override
                    public void onNegative(CustomDialog dialog) {
                        dialog.cancel();
                    }
                }).create();
        dialog.show();
    }

    private void newFileConfirmDialog() {
        final int item1 = 0;
        final int item2 = 1;
        final HashMap<String, Integer> checkMap = new HashMap<>(1);
        CustomDialog dialog = new CustomDialog.Builder(getActivity())
                .setTitle("新增文件")
                .setAlertType(CustomDialog.MESSAGE_TYPE_NORMAL)
                .setPositiveBtnText(getResources().getString(R.string.confirm))
                .setNegativeBtnText(getResources().getString(R.string.cancel))
                .setCheckItem1Text("新建文件夹")
                .setCheckItem2Text("上传本地文件")
                .setSelectViewEditHint("新建文件夹")
                .setCheckBoyOnCheckListener(new CustomDialog.DialogOnCheckListen() {
                    @Override
                    public void onItem1Check(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkMap.put("result", item1);
                        }
                    }

                    @Override
                    public void onItem2Check(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkMap.put("result", item2);
                        }
                    }
                })
                .setBtnOnclickListener(new CustomDialog.DialogOnClickListen() {
                    @Override
                    public void onPositive(CustomDialog dialog) {
                        int checkResult = checkMap.get("result");
                        if (checkResult == item1) {
                            HttpSupport httpSupport = new HttpSupport(new IHttpResponseHandle() {
                                @Override
                                public void success(ResponseEntity entity) {
                                    dialog.cancel();
                                    requestFileList(null);
                                }

                                @Override
                                public void error(ResponseEntity entity) {
                                    dialog.cancel();
                                }
                            });
                            JSONObject param = new JSONObject();
                            param.put("name", dialog.getSelectViewEditText());
                            httpSupport.doPost(ServiceApi.File.NEW_FLODER, param, HttpSupport.obtainTokenHead(accessToken));
                        }
                        if (checkResult == item2) {
                            Bundle bundle = new Bundle(0);
                            bundle.putString("token", accessToken);
                            bundle.putString("currentPathUuid", currentPathUuid);
                            Intent intent = new Intent();
                            intent.putExtras(bundle);
                            intent.setClass(getActivity(), SystemFileListActivity.class);
                            startActivity(intent);
                        }
                        dialog.cancel();
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
        mContext.sendBroadcast(new Intent(QpanAction.fileActionMenuStart));
        menuLayout.setVisibility(View.GONE);
        AnimationHelper.translateAnimation(getContext(), menuLayout, R.anim.menu_start_ani, false, View.VISIBLE);
    }

    /**
     * Menu 退出动画
     */
    void menuOutAnim() {
        mContext.sendBroadcast(new Intent(QpanAction.fileActionMenuEnd));
        menuLayout.setVisibility(View.VISIBLE);
        AnimationHelper.translateAnimation(getContext(), menuLayout, R.anim.menu_out_ani, false, View.GONE);
    }

    /**
     * actionMode ActionBar动画唤出
     */
    void customActionBarStartAnim() {
        if (actionMode != null) {
            customActionBar = new MultiChoiceView(getContext());
            actionMode.setCustomView(customActionBar);
            AnimationHelper.translateAnimation(getContext(), customActionBar, R.anim.actionbar_start_ani, false, View.VISIBLE);
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
////                case R.id.share_file:
////                    Log.i(TAG, "分享文件");
////                    break;
////                case R.id.download_file:
////                    Log.i(TAG, "下载文件");
////                    break;
////                case R.id.delete_file:
////                    Log.i(TAG, "删除文件");
////                    break;
////                default:
////                    return false;
////            }
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
     * Returns the current directory depth
     * 返回当前目录深度
     *
     * @return
     */
    public int getCurrentDirDepth() {
        return dirManager.getDirDepth();
    }

    /**
     * Pull data from the server
     * 从服务端拉取数据
     *
     * @param pathUuid
     */
    void requestFileList(String pathUuid) {
        HttpSupport httpSupport = new HttpSupport(new IHttpResponseHandle() {
            @Override
            public void success(ResponseEntity entity) {
                fileVOList = new ArrayList<>();
                JSONObject responseJson = (JSONObject) JSONObject.parse(entity.getResponse());
                JSONArray responseJsonArray = (JSONArray) responseJson.get("file_list");
                for (int i = 0; i < responseJsonArray.size(); i++) {
                    BaseFileVO item = new BaseFileVO();
                    JSONObject responseItem = (JSONObject) responseJsonArray.get(i);
                    item.setUuid(responseItem.getString("uuid"));
                    item.setLastModified(responseItem.getString("lastModified"));
                    item.setName(responseItem.getString("name"));
                    //The back end turns the small number into an Int, but this requires the Long type to be stored
                    // 后端会把小的数字转成Int，但这里要求存储Long类型
                    item.setSize(Long.parseLong((String.valueOf(responseItem.get("size")))));
                    item.setType((int) responseItem.get("type"));
                    fileVOList.add(item);
                }
                mFileManager.getDirManager().dirStackPush(fileVOList);
                buildMap();
                mFileListHandler.sendResult(FileListHandler.REFRESH, null);
            }

            @Override
            public void error(ResponseEntity entity) {
                fileVOList = null;
                mFileListHandler.sendResult(FileListHandler.REFRESH, null);
            }
        });
        // add header
        // 添加header
        List<Map<String, String>> heads = HttpSupport.obtainTokenHead(accessToken);
        if (pathUuid != null) {
            Map<String, String> param = new HashMap<>(1);
            param.put("pathUuid", pathUuid);
            List<Map<String, String>> params = HttpSupport.obtainGetMethodParamList(param);
            httpSupport.doGet(ServiceApi.User.GET_INDIVIDUAL_DIR, params, heads);
        } else {
            httpSupport.doGet(ServiceApi.User.GET_INDIVIDUAL_DIR, null, heads);
        }
    }

    static class FileListHandler extends Handler {
        private static final int REFRESH = 0;
        private final WeakReference<FileListFragment> reference;

        public FileListHandler(FileListFragment fileListFragment) {
            reference = new WeakReference<>(fileListFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d(TAG, "FileListHandler---handleMessage + msg.what = " + msg.what);
            FileListFragment fileListFragment = reference.get();
            if (fileListFragment != null) {
                switch (msg.what) {
                    case REFRESH:
                        if (fileListFragment.fileVOList != null && fileListFragment.fileVOList.size() > 0) {
                            fileListFragment.mEmptyView.setVisibility(View.GONE);
                        } else {
                            fileListFragment.mEmptyView.setVisibility(View.VISIBLE);
                        }
                        fileListFragment.buildMap();
                        break;
                }
            }
        }

        private void sendResult(int result, Object obj) {
            FileListFragment fileListFragment = reference.get();
            if (fileListFragment != null) {
                Message message = new Message();
                message.what = result;
                message.obj = obj;
                sendMessage(message);
            }
        }
    }

    private void setFileListCustomActionBar() {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.LEFT);
        lp.setMargins(0, 0, 0, 0);
        View fileListActionBarView = LayoutInflater.from(mContext).inflate(R.layout.filelist_actionbar, null);
        AppCompatActivity activity = (AppCompatActivity) mContext;
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(fileListActionBarView, lp);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.show();
        }
        cancelSearchFileTv = fileListActionBarView.findViewById(R.id.cancel_search);
        cancelSearchFileTv.setOnClickListener(v -> {
            cancelSearchFileTv.setVisibility(View.GONE);
            fileVOList = fileVOListCache;
            mFileListHandler.sendResult(FileListHandler.REFRESH, null);
        });
        searchFileEt = fileListActionBarView.findViewById(R.id.search_file_et);
        searchFileEt.setOnEditorActionListener((v, actionId, event) -> {
            // Listening to the search
            // 监听搜索
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String fileName = searchFileEt.getText().toString();
                HttpSupport httpSupport = new HttpSupport(new IHttpResponseHandle() {
                    @Override
                    public void success(ResponseEntity entity) {
                        try {
                            fileVOListCache = fileVOList;
                            List<BaseFileVO> resultFileList = new ArrayList<>();
                            JSONObject responseJson = (JSONObject) JSONObject.parse(entity.getResponse());
                            JSONArray responseResult = responseJson.getJSONArray("result");
                            for (int i = 0; i < responseResult.size(); i++) {
                                BaseFileVO fileVO = new BaseFileVO();
                                JSONObject item = (JSONObject) responseResult.get(i);
                                fileVO.setType(item.getInteger("type"));
                                fileVO.setSize(item.getLong("size"));
                                fileVO.setPath(item.getString("path"));
                                fileVO.setName(item.getString("name"));
                                fileVO.setLastModified(item.getString("lastModified"));
                                resultFileList.add(fileVO);
                            }
                            fileVOList = resultFileList;
                            cancelSearchFileTv.setVisibility(View.VISIBLE);
                            hideLoading();
                            mFileListHandler.sendResult(FileListHandler.REFRESH, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void error(ResponseEntity entity) {

                    }
                });
                List<Map<String, String>> heads = HttpSupport.obtainTokenHead(accessToken);
                Map<String, String> param = new HashMap<>(1);
                param.put("target", fileName);
                List<Map<String, String>> params = HttpSupport.obtainGetMethodParamList(param);
                httpSupport.doGet(ServiceApi.User.SEARCH_FILE, params, heads);
                showLoading();
                return true;
            }
            return false;
        });
        ImageView transmissionListImageBtn = fileListActionBarView.findViewById(R.id.transmission_list_image_btn);
        transmissionListImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(getActivity(), TransmissionListActivity.class);
            startActivity(intent);
        });
        ImageView addBtn = fileListActionBarView.findViewById(R.id.add_image_btn);
        addBtn.setOnClickListener(v -> {
            newFileConfirmDialog();
        });
    }


}

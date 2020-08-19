package com.chavez.qpan.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chavez.qpan.DownloadHelper;
import com.chavez.qpan.R;
import com.chavez.qpan.adapater.ShareFileListAdapter;
import com.chavez.qpan.animation.AnimationHelper;
import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.model.ShareFileVo;
import com.chavez.qpan.util.support.file.FileManagerSupport;
import com.chavez.qpan.util.support.http.HttpSupport;
import com.chavez.qpan.util.support.http.IHttpResponseHandle;
import com.chavez.qpan.util.support.http.ResponseEntity;
import com.chavez.qpan.util.support.web.ServiceApi;
import com.chavez.qpan.view.CustomDialog;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ShareFoundFragment extends Fragment {
    private final static String TAG ="ShareFoundFragment";
    private Context mContext;
    private List<ShareFileVo> mFileVOList = new LinkedList<>();
    private String accessToken;

    private FileListHandler fileListHandler;
    private View mShareFoundView;
    private ImageView mSearchFileActionBtn;
    private View mWaitingSearchView;
    private View mLoadingView;
    private ImageView mLoadingViewIcon;
    private RecyclerView mShareFileRyv;
    private ShareFileListAdapter adapter;
    private EditText mSearchEt;

    public ShareFoundFragment(Context context) {
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mShareFoundView == null) {
            mShareFoundView = inflater.inflate(R.layout.share_found_list, container, false);
        }
        hideActionBar();
        findView();
        initView();
        return mShareFoundView;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    void findView() {
        mWaitingSearchView = mShareFoundView.findViewById(R.id.waiting_search_view);
        mShareFileRyv = mShareFoundView.findViewById(R.id.share_file_list);
        mSearchEt = mShareFoundView.findViewById(R.id.search_file_et);
        mSearchFileActionBtn = mShareFoundView.findViewById(R.id.search_file_image_btn);
        mLoadingView = mShareFoundView.findViewById(R.id.loading_view);
        mLoadingViewIcon = mShareFoundView.findViewById(R.id.loading_image);
    }

    void initView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mShareFileRyv.setLayoutManager(layoutManager);
        mShareFileRyv.setItemAnimator(null);
        mShareFileRyv.setHasFixedSize(true);
        adapter = new ShareFileListAdapter(mContext, mFileVOList);
        adapter.setItemOnClickListener((itemView, position) -> {
            ShareFileVo item = mFileVOList.get(position);
            if (item!=null){
                String fileUuid =item.getUuid();
                String fileNmae = item.getFileName();
                downloadConfirmDialog(fileUuid,fileNmae);
            }
        });
        mShareFileRyv.setAdapter(adapter);

        HttpSupport searchHttpSupport  = new HttpSupport(new IHttpResponseHandle() {
            @Override
            public void success(ResponseEntity entity) {
                JSONArray responseJsonArray = JSONArray.parseArray(entity.getResponse());
                if ( mFileVOList!=null && mFileVOList.size()!=0){
                    mFileVOList.clear();
                    buildMap();
                }
                if (responseJsonArray.size()==0){
                    Toast.makeText(mContext,"无搜索结果",Toast.LENGTH_LONG).show();
                }
                for (int i = 0; i < responseJsonArray.size(); i++) {
                    JSONObject itemJson = responseJsonArray.getJSONObject(i);
                    ShareFileVo shareFileVo = new ShareFileVo();
                    shareFileVo.setFileName(itemJson.getString("fileName"));
                    shareFileVo.setAuthorName(itemJson.getString("authorName"));
                    shareFileVo.setAuthorUuid(itemJson.getString("authorUuid"));
                    shareFileVo.setTotalBytes(itemJson.getLong("totalBytes"));
                    shareFileVo.setUuid(itemJson.getString("uuid"));
                    shareFileVo.setType(itemJson.getInteger("fileType"));
                    mFileVOList.add(shareFileVo);
                }
                hideWaitSearchView();
                fileListHandler.sendResult(FileListHandler.REFRESH,null);
            }

            @Override
            public void error(ResponseEntity entity) {
                Log.e(TAG,entity.getResponse());
            }
        });

        mSearchFileActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = mSearchEt.getText().toString();
                List<Map<String, String>> heads = HttpSupport.obtainTokenHead(accessToken);
                Map<String, String> param = new HashMap<>(1);
                param.put("key", key);
                List<Map<String, String>> params = HttpSupport.obtainGetMethodParamList(param);
                searchHttpSupport.doGet(ServiceApi.User.SEARCH_SHARE_FILE, params, heads);
                showLoading();
            }
        });
        mSearchEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String key = mSearchEt.getText().toString();
                List<Map<String, String>> heads = HttpSupport.obtainTokenHead(accessToken);
                Map<String, String> param = new HashMap<>(1);
                param.put("key", key);
                List<Map<String, String>> params = HttpSupport.obtainGetMethodParamList(param);
                searchHttpSupport.doGet(ServiceApi.User.SEARCH_SHARE_FILE, params, heads);
                showLoading();
                return true;
            }
            return false;
        });

        fileListHandler = new FileListHandler(this);
    }

    private void downloadConfirmDialog(String uuid, String fileName) {
        CustomDialog dialog = new CustomDialog.Builder(getActivity())
                .setTitle("下载分享文件")
                .setMessage("输入密码")
                .setAlertType(CustomDialog.MESSAGE_TYPE_NORMAL)
                .setPositiveBtnText(getResources().getString(R.string.confirm))
                .setNegativeBtnText(getResources().getString(R.string.cancel))
                .setEditText("...")
                .setBtnOnclickListener(new CustomDialog.DialogOnClickListen() {
                    @Override
                    public void onPositive(CustomDialog dialog) {
                        try {
                            String password = dialog.getEditText();
                            String downloadUrl = ServiceApi.File.DOWNLOAD_SHARED_FILE + "/"+uuid +"?password="+password;
                            DownloadHelper.getInstance(mContext).download(downloadUrl,fileName);
                            fileListHandler.sendResult(FileListHandler.REFRESH, null);
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


    private void hideActionBar() {
        AppCompatActivity activity = (AppCompatActivity) mContext;
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.hide();
    }

    static class FileListHandler extends Handler {
        private static final int REFRESH = 0;
        private final WeakReference<ShareFoundFragment> reference;

        public FileListHandler(ShareFoundFragment fileListFragment) {
            reference = new WeakReference<>(fileListFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            ShareFoundFragment fileListFragment = reference.get();
            if (fileListFragment != null) {
                switch (msg.what) {
                    case REFRESH:
                        if (fileListFragment.mFileVOList != null && fileListFragment.mFileVOList.size() > 0) {
                            fileListFragment.mWaitingSearchView.setVisibility(View.GONE);
                        } else {
                            fileListFragment.mWaitingSearchView.setVisibility(View.VISIBLE);
                        }
                        fileListFragment.buildMap();
                        break;
                    default:
                        break;
                }
            }
        }

        private void sendResult(int result, Object obj) {
            ShareFoundFragment fileListFragment = reference.get();
            if (fileListFragment != null) {
                Message message = new Message();
                message.what = result;
                message.obj = obj;
                sendMessage(message);
            }
        }
    }

    private void hideWaitSearchView() {
        if (mWaitingSearchView != null) {
            mWaitingSearchView.setVisibility(View.GONE);
        }
    }

    private void showWaitSearchView() {
        if (mWaitingSearchView != null) {
            mWaitingSearchView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        AnimationHelper.translateAnimation(getActivity(), mLoadingViewIcon, R.anim.load_ani, false, View.VISIBLE);
    }

    private void showLoadingIfNeed() {
        if (mLoadingView != null) {
            if (mFileVOList == null) {
                showLoading();
            }
        }
    }

    private void hideLoading() {
        if (mLoadingView != null) {
            mLoadingViewIcon.animate().cancel();
            mLoadingView.setVisibility(View.GONE);
        }
    }

    private void buildMap() {
        if (adapter != null) {
            adapter.updateData(mFileVOList);
            hideLoading();
        }
    }

}

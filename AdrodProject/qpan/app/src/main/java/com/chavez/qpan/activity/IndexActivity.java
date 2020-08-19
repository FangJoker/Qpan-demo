package com.chavez.qpan.activity;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.chavez.qpan.R;
import com.chavez.qpan.fragment.FileListFragment;
import com.chavez.qpan.fragment.ShareFoundFragment;
import com.chavez.qpan.fragment.UserMineFragment;
import com.chavez.qpan.receiver.action.QpanAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class IndexActivity extends AppCompatActivity implements View.OnClickListener {
    View fileListBtn;
    View mineBtn;
    View exploreBtn;
    View navView;
    // file list ActionBar
    EditText searchFileEt;

    FragmentTransaction fragmentTransaction;
    FileListFragment fileListFragmentView = null;
    UserMineFragment userMineFragmentView = null;
    ShareFoundFragment shareFoundFragment = null;

    BroadcastReceiver menuActionReceiver;

    static int CURRENT_LIST_INDEX = 0;
    final static int FILE_LIST_INDEX = 1;
    final static int MINE_INDEX = 2;
    final static int SHARE_LIST = 3;
    private String accessToken;

    ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        accessToken = getIntent().getExtras().get("token").toString();
//        hideActionBar();
        findView();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(menuActionReceiver);
    }

    void findView() {
        menuActionReceiver = new MenuActionReceiver();
        navView = findViewById(R.id.index_nav);
        fileListBtn = findViewById(R.id.index_filelist_btn);
        mineBtn = findViewById(R.id.index_mine_btn);
        exploreBtn = findViewById(R.id.index_explore_btn);
    }

    void init() {
        mineBtn.setOnClickListener(this);
        fileListBtn.setOnClickListener(this);
        exploreBtn.setOnClickListener(this);
        IntentFilter intentFilter = new IntentFilter(QpanAction.fileActionMenuStart);
        intentFilter.addAction(QpanAction.fileActionMenuEnd);
        registerReceiver(menuActionReceiver, intentFilter);
        setSelectTap(MINE_INDEX);
//        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            networkCallback = new NetworkCallbackImpl();
//            connectivityManager.registerDefaultNetworkCallback(networkCallback);
//        }
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
//            if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
//                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
//            }
//        }

    }

    void setSelectTap(int index) {
        if (index == FILE_LIST_INDEX) {
            changeTap(FILE_LIST_INDEX, FileListFragment.class);
        }
        if (index == MINE_INDEX) {
            changeTap(MINE_INDEX, UserMineFragment.class);
        }
        if (index == SHARE_LIST){
            changeTap(SHARE_LIST,ShareFoundFragment.class);
        }
    }

    private void changeTap(int index, Class fragmentClass) {
        BACK_TIME = 0;
        int ContainResourceId = R.id.index_frame_layout;
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        if (index == FILE_LIST_INDEX) fragment = fileListFragmentView;
        if (index == MINE_INDEX) fragment = userMineFragmentView;
        if (fragment == null) {
            try {
                Constructor<?> cons = fragmentClass.getConstructor(Context.class);
                fragment = (Fragment) cons.newInstance(IndexActivity.this);
                if (index == FILE_LIST_INDEX) {
                    fileListFragmentView = (FileListFragment) fragment;
                    fileListFragmentView.setAccessToken(accessToken);
                }
                if (index == MINE_INDEX) userMineFragmentView = (UserMineFragment) fragment;
                if (index ==SHARE_LIST ) {
                    shareFoundFragment = (ShareFoundFragment) fragment;
                    shareFoundFragment.setAccessToken(accessToken);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            // if the previous fragment is exist
            if (CURRENT_LIST_INDEX != 0 && CURRENT_LIST_INDEX != index) {
                fragmentTransaction.replace(ContainResourceId, fragment);
            } else {
                fragmentTransaction.add(ContainResourceId, fragment);
            }
        } else if (CURRENT_LIST_INDEX == index) {
            // Show to prevent the onCreateView of the fragment from being repeated if the current fragment has not already been displayed by the GC
            // 如果当前的fragment还没被GC 仍然还是展示当前fragment就直接show 防止重复执行fragment的onCreateView
            fragmentTransaction.show(fragment);
            return;
        } else {
            fragmentTransaction.replace(ContainResourceId, fragment);
        }
        fragmentTransaction.commit();
        CURRENT_LIST_INDEX = index;
    }


    @Override
    public void onClick(View v) {
        System.out.println("点击:"+v.getId());
        switch (v.getId()) {
            case R.id.index_filelist_btn:
                setSelectTap(FILE_LIST_INDEX);
                break;
            case R.id.index_mine_btn:
                setSelectTap(MINE_INDEX);
                break;
            case R.id.index_explore_btn:
                System.out.println("点击发现");
                setSelectTap(SHARE_LIST);
                break;
        }
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    static int BACK_TIME = 0;

    @Override
    public void onBackPressed() {
        if (CURRENT_LIST_INDEX == FILE_LIST_INDEX) {
            if (fileListFragmentView != null) {
                int curDirDepth = fileListFragmentView.getCurrentDirDepth();
                if (curDirDepth == 1) {
                    if (++BACK_TIME < 2) {
                        Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
                    } else {
                        BACK_TIME = 0;
                        fileListFragmentView.backDir();
                        finish();
                    }
                } else {
                    fileListFragmentView.backDir();
                }
            }
        } else {
            if (++BACK_TIME < 2) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
            } else {
                BACK_TIME = 0;
                finish();
            }
        }
    }

    View fileActionMenuView;

    class MenuActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (fileActionMenuView == null) {
                fileActionMenuView = LayoutInflater.from(context).inflate(R.layout.file_list_menu, null);
            }
            if (intent.getAction().equals(QpanAction.fileActionMenuStart)) {
                navView.setVisibility(View.GONE);
            }
            if (intent.getAction().equals(QpanAction.fileActionMenuEnd)) {
                navView.setVisibility(View.VISIBLE);
            }
        }
    }

//    private Network currentNetwork;
//
//    class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
//        @Override
//        public void onAvailable(@NonNull Network network) {
//            super.onAvailable(network);
//            Toast.makeText(IndexActivity.this, "网络已连接", Toast.LENGTH_SHORT).show();
//            currentNetwork = network;
//            System.out.println("===network: " + network);
//        }
//
//        @Override
//        public void onLosing(@NonNull Network network, int maxMsToLive) {
//            super.onLosing(network, maxMsToLive);
//            Toast.makeText(IndexActivity.this, "网络正在断开。。。", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onLost(@NonNull Network network) {
//            super.onLost(network);
//            Toast.makeText(IndexActivity.this, "网络已断开", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onUnavailable() {
//            super.onUnavailable();
//            Toast.makeText(IndexActivity.this, "网络请求超时", Toast.LENGTH_SHORT).show();
//        }
//
//        public Network waitForAvailable() throws InterruptedException {
//            return mAvailableLatch.await(30, TimeUnit.SECONDS) ? currentNetwork : null;
//        }
//
//    }

    @Override
    protected void onPause() {
        super.onPause();
//        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    public boolean isNetAvailable() {
        if (Build.VERSION.SDK_INT >= 24) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                Toast.makeText(IndexActivity.this, "网络连接正常", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(IndexActivity.this, "网络连接异常", Toast.LENGTH_SHORT).show();
            }
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                Toast.makeText(IndexActivity.this, "网络连接正常", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(IndexActivity.this, "网络连接异常", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }


}
package com.chavez.qpan;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
    private Context mContext;

    public NetworkCallbackImpl(Context context){
        this.mContext = context;
    }
    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        Toast.makeText(mContext, "网络已连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
        Toast.makeText(mContext, "网络正在断开", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        Toast.makeText(mContext, "无网络连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
        Toast.makeText(mContext, "网络请求超时", Toast.LENGTH_SHORT).show();
    }

//    public Network waitForAvailable() throws InterruptedException {
//        return mAvailableLatch.await(30, TimeUnit.SECONDS) ? currentNetwork : null;
//    }
}

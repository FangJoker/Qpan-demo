package com.chavez.qpan;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

public class Application extends android.app.Application {
    private static Context mContext;
    ConnectivityManager connectivityManager;
    private  NetworkCallbackImpl mNetWorkCallBack;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 24) {
            mNetWorkCallBack = new  NetworkCallbackImpl(mContext);
            connectivityManager.registerDefaultNetworkCallback(mNetWorkCallBack);
        }else{
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                Toast.makeText(mContext, "网络连接正常", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "网络连接异常", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        connectivityManager.unregisterNetworkCallback(mNetWorkCallBack);
    }
}

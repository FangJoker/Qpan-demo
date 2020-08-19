package com.chavez.qpan.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.chavez.qpan.R;

public class MenuReceiver extends BroadcastReceiver {
    View fileActionMenuView;
    View naview;
    @Override
    public void onReceive(Context context, Intent intent) {
       if (fileActionMenuView ==null){
           fileActionMenuView = LayoutInflater.from(context).inflate(R.layout.file_list_menu,null);
       }
    }
}

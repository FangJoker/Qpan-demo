package com.chavez.qpan.util.support;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public abstract class SyncSupport<T> {
    private final  static Handler uiHandler = new Handler(Looper.getMainLooper());
    private static ExecutorService workThreadPool =  Executors.newCachedThreadPool();


   public abstract T doInWorkerThread();
   public abstract void doInUiThread(T result);

   private void postResult(T result){
       uiHandler.post(() -> doInUiThread(result));
   }

   public void executeSync(){
        workThreadPool.execute(() -> {
            try{
                postResult(doInWorkerThread());
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }



}

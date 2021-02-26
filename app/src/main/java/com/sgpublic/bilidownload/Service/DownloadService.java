package com.sgpublic.bilidownload.Service;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.sgpublic.bilidownload.BangumiAPI.DownloadHelper;
import com.sgpublic.bilidownload.Unit.DownloadTaskManager;
import com.sgpublic.bilidownload.Unit.MyLog;

public class DownloadService extends JobIntentService {
    private final Object service_detect_lock = new Object();
    private BroadcastReceiver receiver;

    public static boolean service_doing = false;

    public static void startService(Context context){
        if (!service_doing){
            Intent intent = new Intent(context, DownloadService.class);
            enqueueWork(context.getApplicationContext(), DownloadService.class, 1000, intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service_doing = true;
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                synchronized (service_detect_lock){
                    service_detect_lock.notifyAll();
                }
            }
        };
        registerReceiver(receiver, intentFilter);
        MyLog.d(DownloadService.class, "DownloadService onCreate()");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        MyLog.d(DownloadService.class, "DownloadService onHandleWork()");
        while (!DownloadTaskManager.isAllTaskFinished(DownloadService.this)){
            int task_parallel_count = getSharedPreferences("user", MODE_PRIVATE)
                    .getInt("task_parallel_count", 1);
            while (DownloadTaskManager.countDoingTask(DownloadService.this) < task_parallel_count){
                DownloadTaskManager.startNextTask(DownloadService.this);
            }
            try {
                synchronized (service_detect_lock) {
                    service_detect_lock.wait(300000);
                }
                MyLog.v(DownloadService.class, "DownloadService is running, time now: " + System.currentTimeMillis());
            } catch (InterruptedException ignore) {
                break;
            }
        }
        MyLog.d(DownloadService.class, "all task is finished, DownloadService prepare for destroy");
    }

    @Override
    public void onDestroy() {
        MyLog.d(DownloadService.class, "DownloadService onDestroy()");
        service_doing = false;
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}

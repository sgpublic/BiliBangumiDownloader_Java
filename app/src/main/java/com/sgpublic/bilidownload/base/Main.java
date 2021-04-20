package com.sgpublic.bilidownload.base;

import android.app.Application;

import com.sgpublic.bilidownload.BuildConfig;
import com.sgpublic.bilidownload.util.CrashHandler;
import com.sgpublic.bilidownload.util.MyLog;

//import com.umeng.analytics.MobclickAgent;
//import com.umeng.commonsdk.UMConfigure;


public class Main extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG){
            MyLog.setup();
        }
        CrashHandler.getInstance().init(this);
//        initSDK(this);
    }

//    private void initSDK(Context context) {
//        UMConfigure.setLogEnabled(false);
//        UMConfigure.init(context, UmengSDK.SECRET, UmengSDK.CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, "");
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO);
//    }
}

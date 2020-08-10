package com.sgpublic.bilidownload.BaseService;

import android.app.Application;
import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import okhttp3.OkHttpClient;
import okio.Buffer;

public class Main extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initSDK(this);
    }

    private void initSDK(Context context) {
        UMConfigure.setLogEnabled(false);
        UMConfigure.init(context, UmengSDK.SECRET, UmengSDK.CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, "");
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO);
    }
}

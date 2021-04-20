package com.sgpublic.bilidownload.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class ConfigManager {
    private static final String TAG = "ConfigManager";

    private static final String[] types_pack = {
            "tv.danmaku.bili",
            "com.bilibili.app.blue",
            "com.bilibili.app.in"
    };
    private static final String[] types_string = {
            "正式版",
            "概念版",
            "国际版"
    };

    private static final String[] quality_description = {
            "4K 超清",
            "1080P 高码率",
            "1080P 高清",
            "720P 高清",
            "480P 清晰",
            "360P 流畅"
    };
    private static final int[] quality_int = {
            120, 112, 80, 64, 32, 16
    };

    private static boolean checkAppInstalled(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        return packageInfo != null;
    }

    public static String getDownloadDir(Context context){
        String default_dir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            default_dir = "/storage/emulated/0/Download/";
        } else {
            default_dir = "/storage/emulated/0/Android/data/";
        }
        return context.getSharedPreferences("user", MODE_PRIVATE)
                .getString("location", default_dir);
    }

    public static ClientItem checkClient(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE);
        ArrayList<ClientItem> clientItems = getInstalledClients(context);
        String package_name = sharedPreferences.getString("package", types_pack[0]);
        int package_index = -1;
        ClientItem result = null;
        if (clientItems.size() > 0){
            for (int index = 0; index < clientItems.size(); index++){
                if (clientItems.get(index).getPackageName().equals(package_name)){
                    package_index = index;
                    break;
                }
            }
            if (package_index < 0){
                package_index = 0;
            }
            package_name = clientItems.get(package_index).getPackageName();
            sharedPreferences.edit()
                    .putString("package", package_name)
                    .apply();
            result = new ClientItem(types_string[package_index], package_name);
        }
        return result;
    }

    public static int checkTaskCount(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE);
        int result = sharedPreferences.getInt("task_parallel_count", 1);
        if (result > 3 | result < 1){
            result = 1;
            sharedPreferences.edit()
                    .putInt("task_parallel_count", 1)
                    .apply();
        }
        return result;
    }

    public static boolean checkTaskAutoStart(Context context){
        return context.getSharedPreferences("user", MODE_PRIVATE)
                .getBoolean("task_auto_start", true);
    }

    public static ArrayList<ClientItem> getInstalledClients(Context context){
        ArrayList<ClientItem> clientItems = new ArrayList<>();
        for (int index = 0; index < types_pack.length; index++){
            if (checkAppInstalled(context, types_pack[index])){
                clientItems.add(new ClientItem(types_string[index], types_pack[index]));
            }
        }
        return clientItems;
    }

    public static QualityItem checkQuality(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE);
        int quality_index = 2;
        int quality_set = sharedPreferences.getInt("quality", quality_int[quality_index]);
        MyLog.d(ConfigManager.class, "quality_set: " + quality_set);
        for (int index = 0; index < quality_description.length; index++){
            int quality = quality_int[index];
            if (quality == quality_set) {
                MyLog.d(ConfigManager.class, "quality: " + quality);
                quality_index = index;
                break;
            }
        }
        MyLog.d(ConfigManager.class, "quality_index: " + quality_index);
        sharedPreferences.edit()
                .putInt("quality", quality_int[quality_index])
                .apply();
        return new QualityItem(
                quality_description[quality_index],
                quality_int[quality_index],
                quality_index
        );
    }

    public static ArrayList<QualityItem> getQualities() {
        ArrayList<QualityItem> qualityItems = new ArrayList<>();
        for (int index = 0; index < quality_int.length; index++){
            qualityItems.add(new QualityItem(
                    quality_description[index],
                    quality_int[index],
                    index
            ));
        }
        return qualityItems;
    }

    public static class ClientItem {
        private final String name;
        private final String package_name;

        ClientItem(String name, String package_name){
            this.name = name;
            this.package_name = package_name;
        }

        public String getName() {
            return name;
        }

        public String getPackageName() {
            return package_name;
        }
    }

    public static class QualityItem {
        private final String name;
        private final int quality;
        private final int index;

        QualityItem(String name, int quality, int index){
            this.name = name;
            this.quality = quality;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public int getQuality() {
            return quality;
        }

        public String getName() {
            return name;
        }
    }
}

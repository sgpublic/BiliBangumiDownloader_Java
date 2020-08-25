package com.sgpublic.bilidownload.BaseService;

import android.util.Log;

public class MyLog {
    private static boolean out = false;

    public static void d(String tag, String message){
        if (out){
            doLog(Log::d, tag, message);
        }
    }

    private static void doLog(DoLog doLog, String tag, String message){
        if (message.length() > 2048){
            int index;
            for (index = 0; index < message.length() - 2048; index = index + 2048){
                String out = message.substring(index, index + 2048);
                doLog.onLog(tag, out);
            }
            doLog.onLog(tag, message.substring(index));
        } else {
            doLog.onLog(tag, message);
        }
    }

    private interface DoLog {
        void onLog(String tag, String message);
    }
}

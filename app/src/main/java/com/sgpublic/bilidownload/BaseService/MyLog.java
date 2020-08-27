package com.sgpublic.bilidownload.BaseService;

import android.util.Log;

import com.sgpublic.bilidownload.BuildConfig;

public class MyLog {
    private final static boolean out = BuildConfig.DEBUG;

    public static void v(String tag, String message){
        if (out){
            doLog(Log::v, tag, message);
        }
    }

    public static void v(String tag, String message, Throwable e){
        if (out){
            doLog(Log::v, tag, message, e);
        }
    }

    public static void d(String tag, String message){
        if (out){
            doLog(Log::d, tag, message);
        }
    }

    public static void d(String tag, String message, Throwable e){
        if (out){
            doLog(Log::d, tag, message, e);
        }
    }

    public static void i(String tag, String message){
        if (out){
            doLog(Log::i, tag, message);
        }
    }

    public static void i(String tag, String message, Throwable e){
        if (out){
            doLog(Log::i, tag, message, e);
        }
    }

    public static void w(String tag, String message){
        if (out){
            doLog(Log::w, tag, message);
        }
    }

    public static void w(String tag, String message, Throwable e){
        if (out){
            doLog(Log::w, tag, message, e);
        }
    }

    public static void e(String tag, String message){
        if (out){
            doLog(Log::e, tag, message);
        }
    }

    public static void e(String tag, String message, Throwable e){
        if (out){
            doLog(Log::e, tag, message, e);
        }
    }

    private static void doLog(DoLogSimplify doLog, String tag, String message){
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

    private static void doLog(DoLog doLog, String tag, String message, Throwable e){
        if (message.length() > 2048){
            int index;
            for (index = 0; index < message.length() - 2048; index = index + 2048){
                String out = message.substring(index, index + 2048);
                doLog.onLog(tag, out, e);
            }
            doLog.onLog(tag, message.substring(index), e);
        } else {
            doLog.onLog(tag, message, e);
        }
    }

    private interface DoLogSimplify {
        void onLog(String tag, String message);
    }

    private interface DoLog {
        void onLog(String tag, String message, Throwable e);
    }
}

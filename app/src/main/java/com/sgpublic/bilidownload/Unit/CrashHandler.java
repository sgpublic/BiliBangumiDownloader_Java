package com.sgpublic.bilidownload.Unit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.sgpublic.bilidownload.BaseStation.BaseActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static final CrashHandler INSTANCE = new CrashHandler();
    private Application context;

    private CrashHandler() { }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Application context) {
        this.context = context;
        if (mDefaultHandler == null){
            mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    @Override
    public void uncaughtException(@NotNull Thread thread, @NotNull Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            ActivityController.finishAll();
        }
    }

    private boolean handleException(Throwable e) {
        if (e == null) {
            return false;
        }
        saveExplosion(context, e, -100);
        return true;
    }

    public static void saveExplosion(Context context, Throwable e, int code) {
        try {
            if (e != null) {
                JSONObject exception_log;
                JSONArray exception_log_content = new JSONArray();
                File exception = new File(
                        Objects.requireNonNull(context.getApplicationContext().getExternalFilesDir("log")).getPath(),
                        "exception.json"
                );

                String log_content;
                try {
                    FileInputStream fileInputStream = new FileInputStream(exception);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    log_content = stringBuilder.toString();
                } catch (IOException e1) {
                    log_content = "";
                }
                if (!log_content.equals("")) {
                    exception_log = new JSONObject(log_content);
                    if (!exception_log.isNull("logs")) {
                        exception_log_content = exception_log.getJSONArray("logs");
                    }
                }

                StackTraceElement[] elements = e.getStackTrace();

                JSONObject crash_msg_json = new JSONObject();
                JSONArray crash_msg_array = new JSONArray();
                JSONObject crash_msg_array_index = new JSONObject();
                JSONArray crash_stack_trace = new JSONArray();

                for (StackTraceElement element_index : elements) {
                    JSONObject crash_stack_trace_index = new JSONObject();
                    crash_stack_trace_index.put("class", element_index.getClassName());
                    crash_stack_trace_index.put("line", element_index.getLineNumber());
                    crash_stack_trace_index.put("method", element_index.getMethodName());
                    crash_stack_trace.put(crash_stack_trace_index);
                }

                StringBuilder config_string = new StringBuilder(e.toString());
                for (int config_index = 0; config_index < 3; config_index++) {
                    config_string.append("\nat ").append(elements[config_index].toString());
                }
                SharedPreferences.Editor editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
                editor.putString("last_exception", config_string.toString());
                editor.apply();

                crash_msg_array_index.put("code", code);
                crash_msg_array_index.put("message", e.toString());
                crash_msg_array_index.put("stack_trace", crash_stack_trace);
                crash_msg_array.put(crash_msg_array_index);
                for (int exception_log_index = 0; exception_log_index < exception_log_content.length() && exception_log_index < 2; exception_log_index++) {
                    JSONObject msg_index = exception_log_content.getJSONObject(exception_log_index);
                    if (!crash_msg_array_index.toString().equals(msg_index.toString())) {
                        crash_msg_array.put(msg_index);
                    }
                }
                crash_msg_json.put("logs", crash_msg_array);
                FileOutputStream fileOutputStream = new FileOutputStream(exception);
                fileOutputStream.write(crash_msg_json.toString().getBytes());
                fileOutputStream.close();
            }
        } catch (JSONException | IOException | IllegalArgumentException ignore) { }
    }
}

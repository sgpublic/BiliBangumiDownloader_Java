package com.sgpublic.bilidownload.BaseService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sgpublic.bilidownload.R;

//import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    protected String TAG;
    protected SharedPreferences sharedPreferences;

    private Timer timer;
    private int image_index;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityController.addActivity(this);
        TAG = getClass().getSimpleName();

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        onUiLoad();
    }

    protected void onUiLoad() {
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //int mode_state = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            //if (mode_state == Configuration.UI_MODE_NIGHT_YES){
            //    this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //} else {
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            //}
        //}
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    protected void startOnLoadingState(ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                image_index = image_index == R.drawable.pic_search_doing_1 ? R.drawable.pic_search_doing_2 : R.drawable.pic_search_doing_1;
                runOnUiThread(() -> imageView.setImageResource(image_index));
            }
        }, 0, 500);
    }

    protected void stopOnLoadingState() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    protected void onToast(final Context context, final String content) {
        runOnUiThread(() -> Toast.makeText(context, content, Toast.LENGTH_SHORT).show());
    }

    protected void onToast(Context context, int content) {
        onToast(context, getResources().getText(content).toString());
    }

    protected void onToast(Context context, int content, int code) {
        String content_show = getResources().getText(content).toString()
                + "(" + code + ")";
        onToast(context, content_show);
    }

    protected void onToast(Context context, int content, String message, int code) {
        if (message != null) {
            String content_show = getResources().getText(content).toString();
            content_show = content_show + "，" + message;
            content_show = content_show + "(" + code + ")";
            onToast(context, content_show);
        } else {
            onToast(context, content, code);
        }
    }

//    @Override
//    public void onResume() {
//        MobclickAgent.onResume(this);
//        super.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        MobclickAgent.onPause(this);
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        ActivityController.removeActivity(this);
//        super.onDestroy();
//    }

    protected void setAnimateState(boolean is_visible, int duration, View view, Runnable callback) {
        runOnUiThread(() -> {
            if (is_visible) {
                view.setVisibility(View.VISIBLE);
                view.animate().alphaBy(0f).alpha(1f).setDuration(duration).setListener(null);
                if (callback != null) {
                    callback.run();
                }
            } else {
                view.animate().alphaBy(1f).alpha(0f).setDuration(duration).setListener(null);
                new Handler().postDelayed(() -> {
                    view.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.run();
                    }
                }, duration);
            }
        });
    }

    protected static int dip2px(Context context, float dpValue) {
        final float scales = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scales + 0.5f);
    }

    protected int getIndex(int[] all_args, int finding_arg) {
        int result = -1;
        boolean find = false;
        for (int arg_index : all_args) {
            result = result + 1;
            if (arg_index == finding_arg) {
                find = true;
                break;
            }
        }
        if (find) {
            return result;
        } else {
            return -1;
        }
    }

    protected void saveExplosion(Throwable e, int code) {
        try {
            if (e != null) {
                JSONObject exception_log;
                JSONArray exception_log_content = new JSONArray();
                File exception = new File(
                        Objects.requireNonNull(getApplicationContext().getExternalFilesDir("log")).getPath(),
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
                StackTraceElement element_index;

                JSONObject crash_msg_json = new JSONObject();
                JSONArray crash_msg_array = new JSONArray();
                JSONObject crash_msg_array_index = new JSONObject();
                JSONArray crash_stack_trace = new JSONArray();

                for (int crash_msg_index = 0; crash_msg_index < elements.length && crash_msg_index < 10; crash_msg_index++) {
                    element_index = e.getStackTrace()[crash_msg_index];
                    JSONObject crash_stack_trace_index = new JSONObject();
                    crash_stack_trace_index.put("class", element_index.getClassName());
                    crash_stack_trace_index.put("line", element_index.getLineNumber());
                    crash_stack_trace_index.put("method", element_index.getMethodName());
                    crash_stack_trace.put(crash_stack_trace_index);
                }

                StringBuilder config_string = new StringBuilder(e.toString());
                for (int config_index = 0; config_index < 3; config_index++) {
                    element_index = elements[config_index];
                    config_string.append("\nat ").append(element_index.toString());
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
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

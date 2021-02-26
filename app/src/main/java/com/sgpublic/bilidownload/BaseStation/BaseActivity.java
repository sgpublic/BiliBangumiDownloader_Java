package com.sgpublic.bilidownload.BaseStation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sgpublic.bilidownload.R;
import com.sgpublic.bilidownload.Unit.ActivityController;
import com.sgpublic.bilidownload.Unit.CrashHandler;
import com.sgpublic.bilidownload.Unit.MyLog;

//import com.umeng.analytics.MobclickAgent;

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
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    protected SharedPreferences sharedPreferences;

    private Timer timer;
    private int image_index;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityController.addActivity(this);

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

    public void onToast(final Context context, final String content) {
        runOnUiThread(() -> Toast.makeText(context, content, Toast.LENGTH_SHORT).show());
    }

    public void onToast(Context context, int content) {
        onToast(context, getResources().getText(content).toString());
    }

    public void onToast(Context context, int content, int code) {
        String content_show = getResources().getText(content).toString()
                + "(" + code + ")";
        onToast(context, content_show);
    }

    public void onToast(Context context, int content, String message, int code) {
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

    @Override
    protected void onDestroy() {
        ActivityController.removeActivity(this);
        super.onDestroy();
    }

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
}

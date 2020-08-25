package com.sgpublic.bilidownload;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.sgpublic.bilidownload.BangumeAPI.LoginHelper;
import com.sgpublic.bilidownload.BangumeAPI.UserManager;
import com.sgpublic.bilidownload.BaseService.BaseActivity;

import static com.sgpublic.bilidownload.BaseService.ActivityController.finishAll;

public class Welcome extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (sharedPreferences.getInt("quality", -1) == -1) {
                editor.putInt("quality", 80);
            }
            if (sharedPreferences.getInt("type", -1) == -1) {
                editor.putInt("type", 0);
            }
            String default_dir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                default_dir = "/storage/emulated/0/Download/";
            } else {
                default_dir = "/storage/emulated/0/Android/data/";
            }
            if (sharedPreferences.getString("location", null) == null) {
                editor.putString("location", default_dir);
            }
            if (!sharedPreferences.getBoolean("alert_dir", false)) {
                editor.putBoolean("alert_dir", false);
            }
            editor.apply();

            if (!sharedPreferences.getBoolean("is_login", false)) {
                new Handler().postDelayed(() -> onSetupFinished(false), 800);
            } else if (sharedPreferences.getLong("expires_in", 0L) > System.currentTimeMillis()) {
                new Handler().postDelayed(() -> {
                    UserManager manager = new UserManager(
                            Welcome.this,
                            sharedPreferences.getString("access_key", ""),
                            sharedPreferences.getLong("mid", 0L)
                    );
                    manager.getInfo(
                            new UserManager.Callback() {
                                @Override
                                public void onFailure(int code, String message, Throwable e) {
                                    onToast(Welcome.this, R.string.error_login);
                                    onSetupFinished(false);
                                    saveExplosion(e, code);
                                }

                                @Override
                                public void onResult(com.sgpublic.bilidownload.DataHelper.UserData data) {
                                    SharedPreferences.Editor editor1 = sharedPreferences.edit();
                                    editor1.putString("name", data.name);
                                    editor1.putString("sign", data.sign);
                                    editor1.putString("face", data.face);
                                    editor1.putInt("sex", data.sex);
                                    editor1.putInt("vip_type", data.vip_type);
                                    editor1.putInt("vip_state", data.vip_state);
                                    editor1.putInt("level", data.level);
                                    editor1.putBoolean("is_login", true);
                                    editor1.apply();
                                    onSetupFinished(true);
                                }
                            });
                    onSetupFinished(true);
                }, 100);
            } else {
                new Handler().postDelayed(() -> {
                    onToast(this, R.string.error_login_refresh);
                    onSetupFinished(false);
                }, 400);
            }
        }, 400);
    }

    private void onSetupFinished(boolean is_login) {
        int[] permissions = new int[]{
                ContextCompat.checkSelfPermission(Welcome.this, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                ContextCompat.checkSelfPermission(Welcome.this, Manifest.permission.READ_PHONE_STATE)
        };
        boolean isAllowed = true;
        for (int permission : permissions) {
            isAllowed = isAllowed && permission == PackageManager.PERMISSION_GRANTED;
        }

        final Intent intent;
        if (!isAllowed) {
            intent = new Intent(Welcome.this, Login.class);
            intent.putExtra("grand", 0);
        } else {
            if (is_login) {
                intent = new Intent(Welcome.this, Main.class);
            } else {
                intent = new Intent(Welcome.this, Login.class);
                intent.putExtra("grand", 1);
            }
        }
        runOnUiThread(() -> Welcome.this.startActivity(intent));
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();
        setContentView(R.layout.activity_welcome);
    }

    long last = -1;

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (last == -1) {
            Toast.makeText(this, "再点击一次退出", Toast.LENGTH_SHORT).show();
            last = now;
        } else {
            if ((now - last) < 2000) {
                finishAll();
            } else {
                last = now;
                Toast.makeText(this, "请再点击一次退出", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

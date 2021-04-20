package com.sgpublic.bilidownload.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.sgpublic.bilidownload.module.LoginModule;
import com.sgpublic.bilidownload.module.UserInfoModule;
import com.sgpublic.bilidownload.base.BaseActivity;
import com.sgpublic.bilidownload.util.ConfigManager;
import com.sgpublic.bilidownload.util.CrashHandler;
import com.sgpublic.bilidownload.module.UpdateModule;
import com.sgpublic.bilidownload.data.TokenData;
import com.sgpublic.bilidownload.data.UserData;
import com.sgpublic.bilidownload.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sgpublic.bilidownload.util.ActivityController.finishAll;

public class Welcome extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (sharedPreferences.getInt("quality", -1) == -1) {
                editor.putInt("quality", 80);
            }
            ConfigManager.checkClient(this);
            String default_dir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                default_dir = "/storage/emulated/0/Download/";
            } else {
                default_dir = "/storage/emulated/0/Android/data/";
            }
            if (sharedPreferences.getString("location", null) == null) {
                editor.putString("location", default_dir);
            }
            editor.apply();

            Runnable refresh_user_info = () -> {
                UserInfoModule manager = new UserInfoModule(
                        Welcome.this,
                        sharedPreferences.getString("access_key", ""),
                        sharedPreferences.getLong("mid", 0L)
                );
                manager.getInfo(new UserInfoModule.Callback() {
                    @Override
                    public void onFailure(int code, String message, Throwable e) {
                        onToast(Welcome.this, R.string.error_login);
                        onSetupFinished(false);
                        CrashHandler.saveExplosion(Welcome.this, e, code);
                    }

                    @Override
                    public void onResult(UserData data) {
                        SharedPreferences.Editor editor1 = sharedPreferences.edit();
                        editor1.putString("name", data.name);
                        editor1.putString("sign", data.sign);
                        editor1.putString("face", data.face);
                        editor1.putInt("sex", data.sex);
                        editor1.putString("vip_label", data.vip_label);
                        editor1.putInt("vip_type", data.vip_type);
                        editor1.putInt("vip_state", data.vip_state);
                        editor1.putInt("level", data.level);
                        editor1.putBoolean("is_login", true);
                        editor1.apply();
                        onSetupFinished(true);
                    }
                });
            };
            if (!sharedPreferences.getBoolean("is_login", false)) {
                new Handler().postDelayed(() -> onSetupFinished(false), 800);
            } else if (sharedPreferences.getLong("expires_in", 0L) > System.currentTimeMillis()) {
                new Handler().postDelayed(refresh_user_info, 100);
            } else {
                String refresh_key = sharedPreferences.getString("refresh_key", "");
                Runnable token_expires = () -> {
                    onToast(this, R.string.error_login_refresh);
                    onSetupFinished(false);
                };
                if (refresh_key.equals("")){
                    new Handler().postDelayed(token_expires, 100);
                    return;
                }
                String access_key = sharedPreferences.getString("access_key", "");
                LoginModule helper = new LoginModule(Welcome.this);
                helper.refreshToken(access_key, refresh_key, new LoginModule.Callback() {
                    @Override
                    public void onFailure(int code, String message, Throwable e) {
                        new Handler().postDelayed(token_expires, 200);
                    }

                    @Override
                    public void onLimited() {
                        new Handler().postDelayed(token_expires, 200);
                    }

                    @Override
                    public void onResult(TokenData token, long mid) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("access_key", token.access_token);
                        editor.putString("refresh_key", token.refresh_token);
                        editor.putLong("mid", mid);
                        editor.putLong("expires_in", token.expires_in);
                        editor.apply();
                        refresh_user_info.run();
                    }
                });
            }
        }, 300);
    }

    private void onSetupFinished(boolean is_login) {
        int[] permissions = new int[]{
//                ContextCompat.checkSelfPermission(Welcome.this, Manifest.permission.READ_PHONE_STATE),
                ContextCompat.checkSelfPermission(Welcome.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

        UpdateModule helper = new UpdateModule(Welcome.this);
        helper.getUpdate(0, new UpdateModule.Callback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                CrashHandler.saveExplosion(Welcome.this, e, code);
                runOnUiThread(() -> Welcome.this.startActivity(intent));
            }

            @Override
            public void onUpToDate() {
                runOnUiThread(() -> Welcome.this.startActivity(intent));
            }

            @Override
            public void onUpdate(int force, String ver_name, String size_string, String changelog, String dl_url) {
                int[] update_header = {
                        R.string.text_update_content,
                        R.string.text_update_content_force,
                        R.string.text_update_content_beta
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
                builder.setTitle(R.string.title_update_get);
                builder.setCancelable(force == 0);
                int header = update_header[force];
                if (ver_name.contains("Build")){
                    header = update_header[2];
                }
                builder.setMessage(String.format(Welcome.this.getString(header), size_string) + "\n" +
                        Welcome.this.getString(R.string.text_update_version) + ver_name + "\n" +
                        Welcome.this.getString(R.string.text_update_changelog) + "\n" + changelog);
                builder.setPositiveButton(R.string.text_ok, (dialog, which) -> {
                    new Thread(() -> {
                        Uri url = Uri.parse(dl_url);
                        DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request req = new DownloadManager.Request(url);
                        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        String apkName = Welcome.this.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".apk";
                        req.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, apkName);
                        req.setVisibleInDownloadsUi(true);
                        req.setTitle(Welcome.this.getString(R.string.title_update_download));
                        req.setMimeType("application/vnd.android.package-archive");
                        if (downloadManager != null) {
                            long referer = downloadManager.enqueue(req);
                            helper.listener(referer);
                        }
                    }).start();
                    Welcome.this.startActivity(intent);
                });
                Runnable runnable = () -> {
                    if (force == 1) {
                        finishAll();
                    } else {
                        runOnUiThread(() -> Welcome.this.startActivity(intent));
                    }
                };
                builder.setNegativeButton(R.string.text_cancel, (dialog, which) -> runnable.run());
                builder.setOnCancelListener(dialog -> runnable.run());
                runOnUiThread(builder::show);
            }

            @Override
            public void onDisabled(long time, String reason) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
                builder.setTitle(R.string.title_update_disable);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
                Date date = new Date();
                date.setTime(time);

                builder.setMessage(String.format(
                        Welcome.this.getString(R.string.text_update_content_disable),
                        reason, sdf.format(date)
                ));
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.text_ok, (dialogInterface, i) -> finishAll());
                runOnUiThread(builder::show);
            }
        });
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

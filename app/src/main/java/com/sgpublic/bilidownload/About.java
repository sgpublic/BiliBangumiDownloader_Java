package com.sgpublic.bilidownload;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.sgpublic.bilidownload.BaseService.BaseActivity;
import com.sgpublic.bilidownload.BaseService.UpdateHelper;
import com.sgpublic.bilidownload.UIHelper.DoubleClickListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sgpublic.bilidownload.BaseService.ActivityController.finishAll;

public class About extends BaseActivity {
    private ProgressBar about_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();

        setContentView(R.layout.activity_about);

        about_progress = findViewById(R.id.about_progress);

        Toolbar about_toolbar = findViewById(R.id.about_toolbar);
        setSupportActionBar(about_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView about_version = findViewById(R.id.about_version);
        try {
            String ver_name = "V" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            about_version.setText(ver_name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        findViewById(R.id.about_license).setOnClickListener(v -> {
            Intent intent = new Intent(About.this, License.class);
            startActivity(intent);
        });

        findViewById(R.id.about_update).setOnClickListener(v -> onUpdate(0));

        findViewById(R.id.about_developer).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(About.this);
            builder.setTitle("鸣谢");
            builder.setMessage(
                    "(排名不分先后)\n" +
                    "APP维护：\n忆丶距\n" +
                    "特别鸣谢：\n柠檬大仙"
            );
            builder.setPositiveButton(R.string.text_ok, null);
            builder.setNegativeButton(R.string.text_cancel, null);
            builder.show();
        });

        findViewById(R.id.about_logo).setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                int vip_type = sharedPreferences.getInt("vip_type", 0);
                int vip_state = sharedPreferences.getInt("vip_state", 0);
                String last_crash = sharedPreferences.getString("last_exception", "null");
                String message = "User Information" + "\n"
                        + "vip_type: " + vip_type + "\n"
                        + "vip_state: " + vip_state + "\n"
                        + "\n" + "Last Exception" + "\n"
                        + last_crash;

                AlertDialog.Builder builder = new AlertDialog.Builder(About.this);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.title_about_pause, (dialog, which) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("Label", message);
                    if (cm != null) {
                        cm.setPrimaryClip(mClipData);
                    }
                });
                builder.setNegativeButton(R.string.text_ok, null);
                builder.show();
            }
        });

        findViewById(R.id.about_logo).setOnLongClickListener(v -> {
            onUpdate(1);
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void onUpdate(int type) {
        about_progress.setVisibility(View.VISIBLE);
        UpdateHelper helper = new UpdateHelper(About.this);
        helper.getUpdate(type, new UpdateHelper.Callback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                runOnUiThread(() -> {
                    onToast(About.this, R.string.error_update, code);
                    about_progress.setVisibility(View.GONE);
                    saveExplosion(e, code);
                });
            }

            @Override
            public void onUpToDate() {
                runOnUiThread(() -> {
                    about_progress.setVisibility(View.GONE);
                    onToast(About.this, R.string.title_update_already);
                });
            }

            @Override
            public void onUpdate(final int force, final String ver_name, final String size_string, final String changelog, final String dl_url) {
                runOnUiThread(() -> {
                    about_progress.setVisibility(View.GONE);
                    int[] update_header = {
                            R.string.text_update_content,
                            R.string.text_update_content_force,
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(About.this);
                    builder.setTitle(R.string.title_update_get);
                    builder.setCancelable(force == 0);
                    builder.setMessage(String.format(About.this.getString(update_header[force]), size_string) + "\n" +
                            About.this.getString(R.string.text_update_version) + ver_name + "\n" +
                            About.this.getString(R.string.text_update_changelog) + "\n" + changelog);
                    builder.setPositiveButton(R.string.text_ok, (dialog, which) -> {
                        Uri url = Uri.parse(dl_url);
                        DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request req = new DownloadManager.Request(url);
                        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        String apkName = About.this.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".apk";
                        req.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, apkName);
                        req.setVisibleInDownloadsUi(true);
                        req.setTitle(About.this.getString(R.string.title_update_download));
                        req.setMimeType("application/vnd.android.package-archive");
                        if (downloadManager != null) {
                            long referer = downloadManager.enqueue(req);
                            helper.listener(referer);
                        }
                    });
                    builder.setNegativeButton(R.string.text_cancel, (dialog, which) -> {
                        if (force == 1) {
                            finishAll();
                        }
                    });
                    builder.show();
                });
            }

            @Override
            public void onDisabled(long time, String reason) {
                AlertDialog.Builder builder = new AlertDialog.Builder(About.this);
                builder.setTitle(R.string.title_update_disable);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
                Date date = new Date();
                date.setTime(time);

                builder.setMessage(String.format(
                        About.this.getString(R.string.text_update_content_disable),
                        reason, sdf.format(date)
                ));
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.text_ok, (dialogInterface, i) -> finishAll());
                runOnUiThread(builder::show);
            }
        });
    }
}

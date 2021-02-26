package com.sgpublic.bilidownload.Unit;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.sgpublic.bilidownload.BangumiAPI.APIHelper;
import com.sgpublic.bilidownload.BangumiAPI.DownloadHelper;
import com.sgpublic.bilidownload.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class UpdateHelper {
    private final String TAG = "UpdateHelper";

    private Callback callback_private;
    private final Context context;

    public UpdateHelper(Context context) {
        this.context = context;
    }

    public void getUpdate(int type, Callback callback) {
        APIHelper helper = new APIHelper();
        this.callback_private = callback;
        String version;
        if (type != 1 && !APIHelper.getTS().endsWith("387")){
            version = "release";
        } else {
            version = "debug";
        }
        Call call = helper.getUpdateRequest(version);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-711, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-712, null, e);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();
                try {
                    int ver_code_now = context.getPackageManager()
                            .getPackageInfo(context.getPackageName(), 0).versionCode;
                    JSONObject object = new JSONObject(result);
                    JSONObject update_table = object.getJSONObject("latest");
                    long disable = update_table.getInt("disable");
                    if (disable == 0){
                        long ver_code = update_table.getInt("ver_code");
                        if (ver_code > ver_code_now) {
                            String url_dl = "https://sgpublic.xyz/bilidl/update/apk/app-"
                                    + version + ".apk";
                            String ver_name = update_table.getString("ver_name");
                            String size_string = DownloadTaskManager.getSizeString(url_dl);
                            if (version.equals("debug")) {
                                SharedPreferences sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE);
                                if (!sharedPreferences.getString("beta", "").equals(ver_name)){
                                    sharedPreferences.edit()
                                            .putString("beta", ver_name)
                                            .apply();
                                    callback_private.onUpdate(
                                            0, ver_name, size_string,
                                            update_table.getString("changelog"), url_dl
                                    );
                                } else {
                                    callback_private.onUpToDate();
                                }
                            } else {
                                int is_force = update_table.getInt("force_ver") > ver_code_now ? 1 : 0;
                                callback_private.onUpdate(
                                        is_force, ver_name, size_string,
                                        update_table.getString("changelog"), url_dl
                                );
                            }
                        } else {
                            callback_private.onUpToDate();
                        }
                    } else {
                        String disable_reason = update_table.getString("disable_reason");
                        if (disable_reason.equals("")){
                            disable_reason = context.getString(R.string.text_update_disable_unknown);
                        }
                        callback_private.onDisabled(
                                disable, disable_reason
                        );
                    }
                } catch (JSONException e) {
                    callback_private.onFailure(-703, null, e);
                } catch (PackageManager.NameNotFoundException e) {
                    callback_private.onFailure(-705, null, e);
                }
            }
        });
    }

    public void listener(final long Id) {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DownloadManager manager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
                long ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (ID == Id) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(Id);

                    Cursor cursor = manager.query(query);
                    if (cursor.moveToFirst()){
                        String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        if (fileName != null){
                            openAPK(fileName);
                        }
                    }
                    cursor.close();
                }
            }
        };
        context.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    private void openAPK(String fileSavePath){
        File file = new File(Objects
                .requireNonNull(Uri.parse(fileSavePath).getPath()));
        String filePath = file.getAbsolutePath();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data = FileProvider.getUriForFile(context.getApplicationContext(), "com.sgpublic.bilidownload.fileprovider", new File(filePath));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            data = Uri.fromFile(file);
        }

        intent.setDataAndType(data, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public interface Callback {
        void onFailure(int code, String message, Throwable e);
        void onUpToDate();
        void onUpdate(int force, String ver_name, String size_string, String changelog, String dl_url);
        void onDisabled(long time, String reason);
    }
}

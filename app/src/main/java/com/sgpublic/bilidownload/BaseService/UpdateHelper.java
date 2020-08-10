package com.sgpublic.bilidownload.BaseService;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sgpublic.bilidownload.BangumeAPI.APIHelper;
import com.sgpublic.bilidownload.BangumeAPI.DownloadHelper;
import com.sgpublic.bilidownload.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.jar.JarOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateHelper {
    private String TAG = "UpdateHelper";

    private Callback callback_private;
    private APIHelper helper;
    private Context context;

    public UpdateHelper(Context context) {
        this.context = context;
    }

    public void getUpdate(int type, Callback callback) {
        helper = new APIHelper();
        this.callback_private = callback;
        final String version;
        if (type == 1) {
            version = "debug";
        } else {
            version = "release";
        }
        Call call = helper.getUpdateRequest(version);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-711, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-712, null, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    int ver_code_now = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                    JSONObject object = new JSONObject(result);
                    JSONObject update_table = object.getJSONObject("latest");
                    long ver_code = update_table.getInt("ver_code");
                    if (ver_code > ver_code_now) {
                        String url_dl = "https://sgpublic.xyz/bilidl/update/apk/app-" + version + ".apk";

                        int is_force = 0;
                        if (version.equals("release")) {
                            is_force = update_table.getInt("force_ver") > ver_code_now ? 1 : 0;
                        }

                        String size_string = new DownloadHelper(context).getSizeString(url_dl);
                        callback_private.onUpdate(
                                is_force,
                                update_table.getString("ver_name"),
                                size_string,
                                update_table.getString("changelog"),
                                url_dl
                        );
                    } else {
                        callback_private.onUpToDate();
                    }
                } catch (JSONException e) {
                    callback_private.onFailure(-703, null, e);
                } catch (PackageManager.NameNotFoundException e) {
                    callback_private.onFailure(-705, null, e);
                }
            }
        });
    }

    public interface Callback {
        void onFailure(int code, String message, Throwable e);

        void onUpToDate();

        void onUpdate(int force, String ver_name, String size_string, String changelog, String dl_url);
    }
}

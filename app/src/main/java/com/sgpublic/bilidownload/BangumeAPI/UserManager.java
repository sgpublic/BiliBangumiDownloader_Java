package com.sgpublic.bilidownload.BangumeAPI;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.sgpublic.bilidownload.DataHelper.UserData;
import com.sgpublic.bilidownload.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

public class UserManager {
    private static final String TAG = "UserManager";

    private String mid;

    private Callback callback_private;
    private APIHelper helper;
    private Context context;

    public UserManager(Context context, String access_key, long mid) {
        this.mid = String.valueOf(mid);
        this.helper = new APIHelper(access_key);
        this.context = context;
    }

    public void getInfo(Callback callback) {
        this.callback_private = callback;
        Call call = helper.getUserInfoRequest(mid);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-201, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-202, e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") == 0) {
                        object = object.getJSONObject("data");
                        UserData data = new UserData();
                        data.face = object.getString("face");
                        data.level = object.getInt("level");
                        data.name = object.getString("name");

                        String sex = object.getString("sex");
                        if (sex.equals("男")) {
                            data.sex = 1;
                        } else if (sex.equals("女")) {
                            data.sex = 2;
                        } else {
                            data.sex = 0;
                        }

                        data.sign = object.getString("sign");

                        object = object.getJSONObject("vip");
                        data.vip_type = object.getInt("type");
                        data.vip_state = object.getInt("status");

                        callback_private.onResult(data);
                    } else {
                        callback_private.onFailure(-204, object.getString("message"), null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback_private.onFailure(-203, null, e);
                }
            }
        });
    }

    public interface Callback {
        void onFailure(int code, String message, Throwable e);

        void onResult(UserData data);
    }
}

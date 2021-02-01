package com.sgpublic.bilidownload.BangumiAPI;

import android.content.Context;
import android.graphics.Color;

import com.sgpublic.bilidownload.DataHelper.FollowData;
import com.sgpublic.bilidownload.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

public class FollowsHelper {
    private static final String TAG = "FollowsHelper";

    private Callback callback_private;
    private final APIHelper helper;

    private final Context context;

    public FollowsHelper(Context context, String access_key) {
        this.helper = new APIHelper(access_key);
        this.context = context;
    }

    public void getFollows(long mid, int page_index, Callback callback) {
        getFollows(mid, page_index, 2, callback);
    }

    public void getFollows(long mid, int page_index, int status, Callback callback) {
        this.callback_private = callback;
        Call call = helper.getFollowsRequest(mid, page_index, status);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-301, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-302, e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") == 0) {
                        object = object.getJSONObject("result");
                        int has_next = object.getInt("has_next");
                        int total = object.getInt("total");
                        FollowData[] followDataArray;
                        if (total == 0) {
                            followDataArray = new FollowData[0];
                        } else {
                            JSONArray array = object.getJSONArray("follow_list");

                            int total_page = array.length();
                            followDataArray = new FollowData[total_page];
                            for (int follow_list_index = 0; follow_list_index < total_page; follow_list_index++) {
                                object = array.getJSONObject(follow_list_index);

                                FollowData followData = new FollowData();
                                followData.season_id = object.getLong("season_id");
                                followData.title = object.getString("title");
                                followData.cover = object.getString("cover");
                                followData.is_finish = object.getInt("is_finish");

                                JSONObject badge = object.getJSONObject("badge_info");
                                followData.badge = badge.getString("text");
                                followData.badge_color = Color.parseColor(
                                        badge.getString("bg_color")
                                );
                                followData.badge_color_night = Color.parseColor(
                                        badge.getString("bg_color_night")
                                );

                                followData.square_cover = object.getString("square_cover");

                                object = object.getJSONObject("new_ep");
                                followData.new_ep_id = object.getLong("id");
                                followData.new_ep_is_new = object.getInt("is_new");
                                followData.new_ep_index_show = object.getString("index_show");
                                followData.new_ep_cover = object.getString("cover");

                                followDataArray[follow_list_index] = followData;
                            }
                        }
                        callback_private.onResult(followDataArray, has_next);
                    } else {
                        callback_private.onFailure(-304, object.getString("message"), null);
                    }
                } catch (JSONException e) {
                    callback_private.onFailure(-303, null, e);
                }
            }
        });
    }

    public interface Callback {
        void onFailure(int code, String message, Throwable e);

        void onResult(FollowData[] followData, int has_next);
    }
}

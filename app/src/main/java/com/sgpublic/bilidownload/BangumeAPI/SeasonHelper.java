package com.sgpublic.bilidownload.BangumeAPI;

import android.content.Context;
import android.util.Log;

import com.sgpublic.bilidownload.DataHelper.Episode.InfoData;
import com.sgpublic.bilidownload.DataHelper.SeasonData;
import com.sgpublic.bilidownload.DataHelper.SeriesData;
import com.sgpublic.bilidownload.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

public class SeasonHelper {
    private static final String TAG = "SeasonHelper";

    private Context context;
    private String access_key;

    private Callback callback_private;

    public SeasonHelper(Context context, String access_key) {
        this.context = context;
        this.access_key = access_key;
    }

    public void getInfoBySid(long sid, Callback callback) {
        this.callback_private = callback;
        APIHelper helper = new APIHelper(access_key);
        Call call = helper.getSeasonInfoRequest(sid);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-401, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-402, e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") != 0) {
                        callback_private.onFailure(-404, object.getString("message"), null);
                    } else {
                        object = object.getJSONObject("result");
                        SeasonData seasonData = new SeasonData();

                        seasonData.actors = object.getString("actors");
                        seasonData.actors_lines = seasonData.actors.split("\n").length;

                        seasonData.alias = object.getString("alias");

                        JSONArray array = object.getJSONArray("seasons");
                        ArrayList<SeriesData> list = new ArrayList<>();
                        for (int array_index = 0; array_index < array.length(); array_index++) {
                            JSONObject object_index = array.getJSONObject(array_index);
                            if (object_index.getLong("season_id") != sid) {
                                SeriesData seriesData = new SeriesData();
                                seriesData.season_id = object_index.getLong("season_id");
                                seriesData.badge = object_index.getString("badge");
                                seriesData.cover = object_index.getString("cover");
                                seriesData.title = object_index.getString("title");
                                list.add(seriesData);
                            }
                        }
                        seasonData.series = list;

                        StringBuilder description = new StringBuilder();
                        JSONArray array_areas = object.getJSONArray("areas");
                        description.append("番剧 | ");
                        for (int areas_index = 0; areas_index < array_areas.length(); areas_index++) {
                            if (areas_index != 0) {
                                description.append("、");
                            }
                            description.append(array_areas.getJSONObject(areas_index).getString("name"));
                        }
                        description.append("\n");
                        JSONObject object1_newest_ep = object.getJSONObject("newest_ep");
                        description.append(object1_newest_ep.getString("desc"));
                        description.append("\n");

                        int area = 1;
                        if (!object.isNull("up_info")) {
                            JSONObject object_up_info = object.getJSONObject("up_info");
                            if (object_up_info.getString("uname").equals("哔哩哔哩番剧出差")) {
                                area = 0;
                            }
                        }

                        if (!object.isNull("payment")) {
                            if (object.getJSONObject("publish").getInt("is_finish") == 1) {
                                seasonData.payment = 1;
                                description.append(context.getResources().getText(R.string.text_season_payment_vip_all));
                            } else {
                                seasonData.payment = 2;
                                description.append(context.getResources().getText(R.string.text_season_payment_vip_newest));
                            }
                        } else {
                            seasonData.payment = 0;
                            if (area == 0) {
                                description.append(context.getResources().getText(R.string.text_season_payment_traveled));
                            } else {
                                description.append(context.getResources().getText(R.string.text_season_payment_normal));
                            }
                        }
                        seasonData.description = description.toString();

                        seasonData.evaluate = object.getString("evaluate");
                        seasonData.staff = object.getString("staff");
                        seasonData.staff_lines = seasonData.staff.split("\n").length;

                        StringBuilder styles = new StringBuilder();
                        JSONArray array_styles = object.getJSONArray("style");
                        description.append("番剧 | ");
                        for (int styles_index = 0; styles_index < array_styles.length(); styles_index++) {
                            if (styles_index != 0) {
                                styles.append("、");
                            }
                            styles.append(array_styles.get(styles_index).toString());
                        }
                        seasonData.styles = styles.toString();
                        seasonData.rating = object.isNull("rating") ? 0D : object.getJSONObject("rating").getDouble("score");

                        array = object.getJSONArray("episodes");
                        ArrayList<InfoData> episodeData = new ArrayList<>();
                        for (int episodes_index = 0; episodes_index < array.length(); episodes_index++) {
                            JSONObject object_episodes_index = array.getJSONObject(episodes_index);
                            if (object_episodes_index.getInt("section_type") == 0) {
                                InfoData episodeData_index = new InfoData();
                                episodeData_index.aid = object_episodes_index.getLong("aid");
                                episodeData_index.cid = object_episodes_index.getLong("cid");
                                episodeData_index.ep_id = object_episodes_index.getLong("ep_id");
                                episodeData_index.cover = object_episodes_index.getString("cover");
                                episodeData_index.episode_status = object_episodes_index.getInt("episode_status");
                                episodeData_index.duration = object_episodes_index.getString("duration");
                                episodeData_index.bvid = object_episodes_index.getString("bvid");
                                episodeData_index.pub_real_time = object_episodes_index
                                        .getString("pub_real_time")
                                        .split(" ")[0];
                                episodeData_index.title = object_episodes_index.getString("index_title");
                                episodeData.add(episodeData_index);
                            }
                        }

                        callback_private.onResult(episodeData, seasonData, area);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback_private.onFailure(-403, e.getMessage(), e);
                }
            }
        });
    }

    public interface Callback {
        void onFailure(int code, String message, Throwable e);

        void onResult(ArrayList<InfoData> episodeData, SeasonData seasonData, int area);
    }
}

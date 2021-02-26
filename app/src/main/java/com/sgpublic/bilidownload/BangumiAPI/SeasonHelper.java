package com.sgpublic.bilidownload.BangumiAPI;

import android.content.Context;
import android.graphics.Color;

import com.sgpublic.bilidownload.Activity.Season;
import com.sgpublic.bilidownload.DataItem.Episode.InfoData;
import com.sgpublic.bilidownload.DataItem.Episode.QualityData;
import com.sgpublic.bilidownload.DataItem.SeasonData;
import com.sgpublic.bilidownload.DataItem.SeriesData;
import com.sgpublic.bilidownload.R;
import com.sgpublic.bilidownload.Unit.MyLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

import static com.sgpublic.bilidownload.DataItem.Episode.InfoData.PAYMENT_NORMAL;

public class SeasonHelper {
    private static final String TAG = "SeasonHelper";

    public static final int AREA_LOCAL = 0;
    public static final int AREA_LIMITED = 1;

    private final Context context;
    private final APIHelper helper;

    private long sid;
    private ArrayList<InfoData> episodeData;
    private final SeasonData seasonData = new SeasonData();

    private Callback callback_private;

    public SeasonHelper(Context context, String access_key) {
        this.context = context;
        helper = new APIHelper(access_key);
    }

    public void getInfoBySid(long sid, Callback callback) {
        this.callback_private = callback;
        this.sid = sid;
        Call call = helper.getSeasonInfoAppRequest(sid);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-401, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-402, e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") != 0) {
                        if (object.getInt("code") == -404){
                            getInfoBySidAppProxy(call);
                        } else {
                            callback_private.onFailure(-404, object.getString("message"), null);
                        }
                    } else {
                        object = object.getJSONObject("result");
                        doParseAppResult(object);
                    }
                } catch (JSONException e) {
                    callback_private.onFailure(-403, e.getMessage(), e);
                }
            }
        });
    }

    private void getInfoBySidWeb(){
        Call call = helper.getSeasonInfoWebRequest(sid);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-411, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-412, e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") != 0) {
                        if (object.getInt("code") == -404){
                            getInfoBySidWebProxy(call);
                        } else {
                            callback_private.onFailure(-414, object.getString("message"), null);
                        }
                    } else {
                        object = object.getJSONObject("result");
                        doParseWebResult(object);
                    }
                } catch (JSONException e) {
                    callback_private.onFailure(-413, e.getMessage(), e);
                }
            }
        });
    }

    private void getInfoBySidAppProxy(Call call){
        APIHelper helper = new APIHelper();
        Call call_proxy = helper.getProxyRequest_iill(call);
        call_proxy.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-421, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-422, e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") != 0) {
                        callback_private.onFailure(-424, object.getString("message"), null);
                    } else {
                        object = object.getJSONObject("result");
                        doParseAppResult(object);
                    }
                } catch (JSONException e) {
                    callback_private.onFailure(-423, e.getMessage(), e);
                }
            }
        });
    }

    private void getInfoBySidWebProxy(Call call){
        APIHelper helper = new APIHelper();
        Call call_proxy = helper.getProxyRequest_iill(call);
        call_proxy.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-431, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-432, e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") != 0) {
                        callback_private.onFailure(-434, object.getString("message"), null);
                    } else {
                        object = object.getJSONObject("result");
                        doParseWebResult(object);
                    }
                } catch (JSONException e) {
                    callback_private.onFailure(-433, e.getMessage(), e);
                }
            }
        });
    }

    private void doParseAppResult(JSONObject object) throws JSONException{
        seasonData.actors = object.getJSONObject("actor").getString("info");
        seasonData.actors_lines = seasonData.actors.split("\n").length;

        seasonData.alias = object.getString("alias");
        seasonData.season_type = object.getInt("type");

        JSONArray array = object.getJSONArray("seasons");
        ArrayList<SeriesData> list = new ArrayList<>();
        for (int array_index = 0; array_index < array.length(); array_index++) {
            JSONObject object_index = array.getJSONObject(array_index);
            SeriesData seriesData = new SeriesData();
            seriesData.badge = object_index.getString("badge");

            JSONObject object_index_badge_info = object_index.getJSONObject("badge_info");
            seriesData.badge_color = Color.parseColor(
                    object_index_badge_info.getString("bg_color")
            );
            seriesData.badge_color_night = Color.parseColor(
                    object_index_badge_info.getString("bg_color_night")
            );

            seriesData.cover = object_index.getString("cover");
            seriesData.title = object_index.getString("title");
            seriesData.season_id = object_index.getLong("season_id");
            if (seriesData.season_id != sid) {
                list.add(seriesData);
            } else {
                seriesData.season_type_name = object.getString("type_name");
                seasonData.base_info = seriesData;
            }
        }
        seasonData.series = list;

        if (!object.isNull("limit") & seasonData.area == AREA_LOCAL) {
            seasonData.area = AREA_LIMITED;
        }

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
        description.append(object.getJSONObject("publish").getString("release_date_show"));
        description.append("\n");
        description.append(object.getJSONObject("publish").getString("time_length_show"));
        seasonData.description = description.toString();

        seasonData.evaluate = object.getString("evaluate");
        seasonData.staff = object.getJSONObject("staff").getString("info");
        seasonData.staff_lines = seasonData.staff.split("\n").length;

        StringBuilder styles = new StringBuilder();
        JSONArray array_styles = object.getJSONArray("styles");
        description.append("番剧 | ");
        for (int styles_index = 0; styles_index < array_styles.length(); styles_index++) {
            if (styles_index != 0) {
                styles.append("、");
            }
            styles.append(array_styles.getJSONObject(styles_index).getString("name"));
        }
        seasonData.styles = styles.toString();
        seasonData.rating = object.isNull("rating") ? 0D : object.getJSONObject("rating").getDouble("score");

        if (object.isNull("limit")){
            array = object.getJSONArray("episodes");
            episodeData = getEpisodesInfo(array);
            getAvailableQuality();
        } else {
            getInfoBySidWeb();
        }
    }

    private void getAvailableQuality(){
        if (episodeData.size() > 0){
            Call call = helper.getEpisodeOfficialRequest(episodeData.get(0).cid, 112);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    if (e instanceof UnknownHostException) {
                        callback_private.onFailure(-441, context.getString(R.string.error_network), e);
                    } else {
                        callback_private.onFailure(-442, e.getMessage(), e);
                    }
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String result = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject object = new JSONObject(result);
                        if (object.getInt("code") != 0) {
                            if (object.getInt("code") == -10403 & episodeData.get(0).payment == PAYMENT_NORMAL){
                                seasonData.area = AREA_LIMITED;
                                getAvailableQualityBiliplus();
                                return;
                            }
                            callback_private.onFailure(-444, object.getString("message"), null);
                        } else {
                            seasonData.qualities = getEpisodeQuality(object);
                            callback_private.onResult(episodeData, seasonData);
                        }
                    } catch (JSONException e) {
                        callback_private.onFailure(-443, null, e);
                    }
                }
            });
        } else {
            callback_private.onResult(episodeData, seasonData);
        }
    }

    private void getAvailableQualityBiliplus(){
        Call call = helper.getEpisodeBiliplusRequest(episodeData.get(0).cid, 112);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-451, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-452, null, e);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 504){
                    getAvailableQualityKghost();
                } else {
                    String result = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject object = new JSONObject(result);
                        if (object.getInt("code") != 0) {
                            callback_private.onFailure(-454, object.getString("message"), null);
                        } else {
                            seasonData.qualities = getEpisodeQuality(object);
                            callback_private.onResult(episodeData, seasonData);
                        }
                    } catch (JSONException e) {
                        callback_private.onFailure(-453, null, e);
                    }
                }
            }
        });
    }

    private void getAvailableQualityKghost(){
        Call call = helper.getEpisodeKghostRequest(episodeData.get(0).cid, 112);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof UnknownHostException) {
                    callback_private.onFailure(-461, context.getString(R.string.error_network), e);
                } else {
                    callback_private.onFailure(-462, null, e);
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") != 0) {
                        callback_private.onFailure(-524, object.getString("message"), null);
                    } else {
                        object = object.getJSONObject("result");
                        seasonData.qualities = getEpisodeQuality(object);
                        callback_private.onResult(episodeData, seasonData);
                    }
                } catch (JSONException e) {
                    callback_private.onFailure(-523, null, e);
                } catch (IndexOutOfBoundsException e){
                    callback_private.onFailure(-524, null, e);
                }
            }
        });
    }

    private ArrayList<QualityData> getEpisodeQuality(JSONObject object) throws JSONException {
        ArrayList<QualityData> arrayList = new ArrayList<>();
        if (object.isNull("support_formats")){
            JSONArray accept_description = object.getJSONArray("accept_description");
            String[] accept_format = object.getString("accept_format").split(",");
            JSONArray accept_quality = object.getJSONArray("accept_quality");
            for (int i = 0; i < accept_description.length(); i++) {
                arrayList.add(new QualityData(
                        accept_quality.getInt(i), accept_description.getString(i), accept_format[i]
                ));
            }
        } else {
            JSONArray support_formats = object.getJSONArray("support_formats");
            for (int array_index = 0; array_index < support_formats.length(); array_index++) {
                JSONObject support_format = support_formats.getJSONObject(array_index);
                String new_description = support_format.getString("new_description");
                int accept_quality = support_format.getInt("quality");
                String accept_format = support_format.getString("format");
                arrayList.add(new QualityData(accept_quality, new_description, accept_format));
            }
        }
        return arrayList;
    }

    private void doParseWebResult(JSONObject object) throws JSONException{
        JSONArray array = object.getJSONArray("episodes");
        episodeData = getEpisodesInfo(array);
        getAvailableQuality();
    }

    @Deprecated
    private void doParseOldResult(JSONObject object) throws JSONException{
        JSONArray array = object.getJSONArray("episodes");
        episodeData = getEpisodesInfo(array);
        getAvailableQuality();
    }

    private ArrayList<InfoData> getEpisodesInfo(JSONArray array) throws JSONException{
        ArrayList<InfoData> episodeData = new ArrayList<>();
        for (int episodes_index = 0; episodes_index < array.length(); episodes_index++) {
            InfoData episodeData_index = new InfoData();

            JSONObject object_episodes_index = array.getJSONObject(episodes_index);
            episodeData_index.aid = object_episodes_index.getLong("aid");
            episodeData_index.cid = object_episodes_index.getLong("cid");
            episodeData_index.ep_id = object_episodes_index.getLong("id");
            episodeData_index.cover = object_episodes_index.getString("cover");
            episodeData_index.payment = object_episodes_index.getInt("status");
            episodeData_index.bvid = object_episodes_index.getString("bvid");
            episodeData_index.area_limit = object_episodes_index.getInt("status");

            JSONObject object_episodes_index_badge = object_episodes_index.getJSONObject("badge_info");
            episodeData_index.badge = object_episodes_index_badge.getString("text");
            episodeData_index.badge_color = Color.parseColor(
                    object_episodes_index_badge.getString("bg_color")
            );
            episodeData_index.badge_color_night = Color.parseColor(
                    object_episodes_index_badge.getString("bg_color_night")
            );

            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            episodeData_index.pub_real_time = dateFormat.format(new Date(object_episodes_index
                    .getLong("pub_time")*1000L));
            episodeData_index.title = object_episodes_index.getString("long_title");

            episodeData_index.index = object_episodes_index.getString("title");

            episodeData.add(episodeData_index);
        }
        return episodeData;
    }

    public interface Callback {
        void onFailure(int code, String message, Throwable e);
        void onResult(ArrayList<InfoData> episodeData, SeasonData seasonData);
    }
}

package com.sgpublic.bilidownload.BangumeAPI;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.sgpublic.bilidownload.DataHelper.SearchData;
import com.sgpublic.bilidownload.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchHelper {
    private static final String TAG = "SearchHelper";

    private APIHelper helper;
    private Context context;

    public SearchHelper(Context context) {
        this.helper = new APIHelper();
        this.context = context;
    }

    public void getHotWord(HotWordCallback callback) {
        Call call = helper.getHotWordRequest();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    callback.onFailure(-801, null, e);
                } else {
                    callback.onFailure(-802, null, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") == 0) {
                        ArrayList<String> hotWords = new ArrayList<>();
                        JSONArray array = object.getJSONArray("list");
                        for (int array_index = 0; array_index < array.length(); array_index++) {
                            object = array.getJSONObject(array_index);
                            hotWords.add(object.getString("keyword"));
                        }
                        callback.onResult(hotWords);
                    } else {
                        callback.onFailure(-814, object.getString("message"), null);
                    }
                } catch (JSONException e) {
                    callback.onFailure(-813, null, e);
                }
            }
        });
    }

    public void suggest(String keyword, SuggestCallback callback) {
        Call call = helper.getSearchSuggestRequest(keyword);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    callback.onFailure(-811, context.getString(R.string.error_network), e);
                } else {
                    callback.onFailure(-812, e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") == 0) {
                        ArrayList<Spannable> suggestions = new ArrayList<>();
                        try {
                            JSONArray array = object.getJSONObject("result").getJSONArray("tag");
                            for (int array_index = 0; array_index < 7 && array_index < array.length(); array_index++) {
                                object = array.getJSONObject(array_index);
                                String value_string = object.getString("value");
                                Spannable value_spannable = new SpannableString(value_string);
                                for (int value_index = 0; value_index < keyword.length(); value_index++) {
                                    String keyword_index = keyword.substring(value_index).substring(0, 1);
                                    int value_string_sub = value_string.indexOf(keyword_index);
                                    if (value_string_sub >= 0) {
                                        value_spannable.setSpan(
                                                new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary)),
                                                value_string_sub, value_string_sub + 1,
                                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                        );
                                    }
                                }
                                suggestions.add(value_spannable);
                            }
                            callback.onResult(suggestions);
                        } catch (JSONException e) {
                            callback.onFailure(-815, null, null);
                        }
                    } else {
                        callback.onFailure(-814, null, null);
                    }
                } catch (JSONException e) {
                    callback.onFailure(-813, null, e);
                }
            }
        });
    }

    public void search(String keyword, SearchCallback callback) {
        Call call = helper.getSearchResultRequest(keyword);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    callback.onFailure(-821, context.getString(R.string.error_network), e);
                } else {
                    callback.onFailure(-822, e.getMessage(), e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getInt("code") == 0) {
                        ArrayList<SearchData> searchDataList = new ArrayList<>();
                        object = object.getJSONObject("data");
                        if (!object.isNull("result")) {
                            JSONArray array = object.getJSONArray("result");
                            for (int array_index = 0; array_index < array.length(); array_index++) {
                                object = array.getJSONObject(array_index);
                                SearchData searchData = new SearchData();
                                searchData.angle_title = object.getString("angle_title");
                                searchData.season_cover = "http:" + object.getString("cover");
                                if (object.isNull("media_score")) {
                                    searchData.media_score = 0D;
                                } else {
                                    searchData.media_score = object.getJSONObject("media_score")
                                            .getDouble("score");
                                }
                                searchData.season_id = object.getLong("season_id");
                                //searchData.season_title = object.getString("season_title");

                                String season_title_string = object.getString("title");
                                Spannable season_title_spannable = new SpannableString(
                                        season_title_string
                                                .replace("<em class=\"keyword\">", "")
                                                .replace("</em>", "")
                                );
                                int season_title_sub_start = season_title_string.indexOf("<em class=\"keyword\">");
                                int season_title_sub_end = season_title_string
                                        .replace("<em class=\"keyword\">", "")
                                        .indexOf("</em>");
                                if (season_title_sub_start >= 0 && season_title_sub_end >= 0) {
                                    season_title_spannable.setSpan(
                                            new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary)),
                                            season_title_sub_start, season_title_sub_end,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    );
                                }

                                searchData.season_title = season_title_spannable;

                                if (object.getLong("pubtime") * 1000 > System.currentTimeMillis()) {
                                    searchData.selection_style = "grid";
                                } else {
                                    searchData.selection_style = object.getString("selection_style");
                                }

                                Date date = new Date(object.getLong("pubtime") * 1000);
                                SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.CHINA);
                                searchData.season_content = format.format(date) + "｜"
                                        + object.getString("season_type_name") + "｜"
                                        + object.getString("areas") + "\n"
                                        + object.getString("styles");

                                JSONArray eps_array = object.getJSONArray("eps");
                                if (eps_array.length() > 0) {
                                    object = eps_array.getJSONObject(0);
                                    searchData.episode_cover = object.getString("cover");

                                    String episode_title_string = object.getString("long_title");
                                    Spannable episode_title_spannable = new SpannableString(
                                            episode_title_string
                                                    .replace("<em class=\"keyword\">", "")
                                                    .replace("</em>", "")
                                    );
                                    int episode_title_sub_start = episode_title_string.indexOf("<em class=\"keyword\">");
                                    int episode_title_sub_end = episode_title_string
                                            .replace("<em class=\"keyword\">", "")
                                            .indexOf("</em>");
                                    if (episode_title_sub_start >= 0 && episode_title_sub_end >= 0) {
                                        episode_title_spannable.setSpan(
                                                new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary)),
                                                episode_title_sub_start, episode_title_sub_end,
                                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                        );
                                    }
                                    searchData.episode_title = episode_title_spannable;

                                    eps_array = object.getJSONArray("badges");
                                    if (eps_array.length() > 0) {
                                        object = eps_array.getJSONObject(0);
                                        searchData.episode_badges = object.getString("text");
                                    } else {
                                        searchData.episode_badges = "";
                                    }
                                }
                                searchDataList.add(searchData);
                            }
                        }
                        callback.onResult(searchDataList);
                    } else {
                        callback.onFailure(-824, object.getString("message"), null);
                    }
                } catch (JSONException e) {
                    callback.onFailure(-823, null, e);
                }
            }
        });
    }

    public interface SearchCallback {
        void onFailure(int code, String message, Throwable e);

        void onResult(ArrayList<SearchData> searchData);
    }

    public interface SuggestCallback {
        void onFailure(int code, String message, Throwable e);

        void onResult(ArrayList<Spannable> suggestions);
    }

    public interface HotWordCallback {
        void onFailure(int code, String message, Throwable e);

        void onResult(ArrayList<String> hotWords);
    }
}

package com.sgpublic.bilidownload.BangumiAPI;

import android.content.Context;

import com.sgpublic.bilidownload.DataHelper.Episode.DASHDownloadData;
import com.sgpublic.bilidownload.DataHelper.Episode.FLVDownloadData;
import com.sgpublic.bilidownload.DataHelper.Episode.QualityData;
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

public class EpisodeHelper {
    private static final String TAG = "EpisodeHelper";

    private Callback callback_private;
    private APIHelper helper;
    private Context context;

    private int qn_private;
    private ArrayList<QualityData> qualityData;

    public EpisodeHelper(Context context, String access_key) {
        this.context = context;
        this.helper = new APIHelper(access_key);
    }

    public void getDownloadInfo(long cid, int area, Callback callback) {
        getDownloadInfo(cid, area, 112, callback);
    }

    public void getDownloadInfo(long cid, final int area, int qn, Callback callback) {
        this.qn_private = qn;
        this.callback_private = callback;
        Call call;
        if (area == 1) {
            call = helper.getEpisodeOfficialRequest(cid, qn);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (e instanceof UnknownHostException) {
                        callback_private.onFailure(-501, context.getString(R.string.error_network), e);
                    } else {
                        callback_private.onFailure(-502, e.getMessage(), e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject object = new JSONObject(result);
                        if (object.getInt("code") != 0) {
                            callback_private.onFailure(-504, object.getString("message"), null);
                        } else {
                            getEpisodeQuality(object);
                            String type = object.getString("type");
                            if (type.equals("FLV")) {
                                getFLVData(object);
                            } else if (type.equals("DASH")) {
                                getDASHData(object);
                            } else {
                                callback_private.onFailure(-505, null, null);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback_private.onFailure(-503, null, e);
                    }
                }
            });
        } else {
            call = helper.getEpisodeBiliplusRequest(cid, qn);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (e instanceof UnknownHostException) {
                        callback_private.onFailure(-511, context.getString(R.string.error_network), e);
                    } else {
                        callback_private.onFailure(-512, null, e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject object = new JSONObject(result);
                        if (object.getInt("code") != 0) {
                            callback_private.onFailure(-514, object.getString("message"), null);
                        } else {
                            getEpisodeQuality(object);
                            if (!object.isNull("durl")) {
                                getFLVData(object);
                            } else if (!object.isNull("dash")) {
                                getDASHData(object);
                            } else {
                                callback_private.onFailure(-515, null, null);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback_private.onFailure(-513, null, e);
                    }
                }
            });
        }
    }

    private void getFLVData(JSONObject object) throws JSONException {
        FLVDownloadData downloadData = new FLVDownloadData();
        downloadData.flv_codecid = object.getInt("video_codecid");
        downloadData.time_length = object.getLong("timelength");

        JSONArray array = object.getJSONArray("durl");
        int size_durl = array.length();

        downloadData.section_count = size_durl;
        downloadData.total_size = 0;

        downloadData.flv_url = new String[size_durl];
        downloadData.flv_backup_url = new String[size_durl][2];
        downloadData.flv_size = new long[size_durl];
        downloadData.flv_length = new long[size_durl];
        downloadData.flv_md5 = new String[size_durl];

        for (int durl_index = 0; durl_index < size_durl; durl_index++) {
            object = array.getJSONObject(durl_index);
            downloadData.total_size = downloadData.total_size + object.getLong("size");
            downloadData.flv_size[durl_index] = object.getLong("size");
            downloadData.flv_url[durl_index] = object.getString("url");
            downloadData.flv_length[durl_index] = object.getLong("length");
            downloadData.flv_backup_url[durl_index][0] = downloadData.flv_url[durl_index];
            downloadData.flv_backup_url[durl_index][1] = downloadData.flv_url[durl_index];
            if (object.isNull("md5")) {
                downloadData.flv_md5[durl_index] = "";
            } else {
                downloadData.flv_md5[durl_index] = object.getString("md5");
            }
        }
        callback_private.onResult(downloadData, qualityData);
    }

    private void getDASHData(JSONObject object) throws JSONException {
        DASHDownloadData downloadData = new DASHDownloadData();
        JSONObject private_object = object.getJSONObject("dash");
        JSONArray array;
        array = private_object.getJSONArray("video");
        JSONObject object_video = null;
        for (int array_index = 0; array_index < array.length(); array_index++) {
            JSONObject object_video_index = array.getJSONObject(array_index);
            int video_qn_pre = object_video == null ? -1 : object_video.getInt("id");
            int video_qn_this = object_video_index.getInt("id");
            if (video_qn_this > video_qn_pre && video_qn_this <= qn_private){
                object_video = object_video_index;
            }
        }
        if (object_video != null){
            downloadData.video_url = object_video.getString("base_url");
            downloadData.video_bandwidth = object_video.getLong("bandwidth");
            downloadData.video_codecid = object_video.isNull("codecid") ? 0 : object_video.getInt("codecid");
            downloadData.video_id = object_video.getInt("id");
            downloadData.video_md5 = object_video.isNull("md5") ? "" : object_video.getString("md5");
            if (object_video.isNull("backup_url")) {
                downloadData.video_backup_url = new String[]{
                        downloadData.video_url,
                        downloadData.video_url
                };
            } else {
                JSONArray object_backup_url = object_video.getJSONArray("backup_url");
                downloadData.video_backup_url = new String[]{
                        object_backup_url.getString(0),
                        object_backup_url.getString(object_backup_url.length() - 1)
                };
            }
            downloadData.video_size = new DownloadHelper(context)
                    .getSizeLong(downloadData.video_url);
        }
        array = private_object.getJSONArray("audio");
        JSONObject object_audio = null;
        for (int array_index = 0; array_index < array.length(); array_index++) {
            JSONObject object_audio_index = array.getJSONObject(array_index);
            int audio_qn_pre = object_audio == null ? -1 : object_audio.getInt("id") - 30200;
            int audio_qn_this = object_audio_index.getInt("id") - 30200;
            if (audio_qn_this > audio_qn_pre && audio_qn_this <= qn_private){
                object_audio = object_audio_index;
            }
        }
        if (object_audio != null) {
            downloadData.audio_url = object_audio.getString("base_url");
            downloadData.audio_bandwidth = object_audio.getLong("bandwidth");
            downloadData.audio_codecid = object_audio.isNull("codecid") ? 0 : object_audio.getInt("codecid");
            downloadData.audio_id = object_audio.getInt("id");
            downloadData.audio_md5 = object_audio.isNull("md5") ? "" : object_audio.getString("md5");
            if (object_audio.isNull("backup_url")) {
                downloadData.audio_backup_url = new String[]{
                        downloadData.video_url,
                        downloadData.video_url
                };
            } else {
                JSONArray object_backup_url = object_audio.getJSONArray("backup_url");
                downloadData.audio_backup_url = new String[]{
                        object_backup_url.getString(0),
                        object_backup_url.getString(object_backup_url.length() - 1)
                };
            }
            downloadData.audio_size = new DownloadHelper(context)
                    .getSizeLong(downloadData.audio_url);
        }
        if (object_video != null && object_audio != null){
            downloadData.total_size = downloadData.audio_size + downloadData.video_size;
            callback_private.onResult(downloadData, qualityData);
        } else {
            callback_private.onFailure(-521, context.getString(R.string.error_download_url), null);
        }
    }

    private void getEpisodeQuality(JSONObject object) throws JSONException {
        ArrayList<QualityData> arrayList = new ArrayList<>();
        JSONArray array_description = object.getJSONArray("accept_description");
        JSONArray array_quality = object.getJSONArray("accept_quality");
        for (int array_index = 0; array_index < array_description.length(); array_index++) {
            String accept_description = array_description.getString(array_index);
            int accept_quality = array_quality.getInt(array_index);
            String accept_format = object.getString("accept_format").split(",")[array_index];
            arrayList.add(new QualityData(accept_quality, accept_description, accept_format));
        }
        qualityData = arrayList;
    }

    public interface Callback {
        //{"code":-10403,"message":"抱歉您所在地区不可观看！"}
        //{"code":-10403,"message":"大会员专享限制"}
        void onFailure(int code, String message, Throwable e);

        void onResult(DASHDownloadData downloadData, ArrayList<QualityData> qualityData);

        void onResult(FLVDownloadData downloadData, ArrayList<QualityData> qualityData);
    }
}

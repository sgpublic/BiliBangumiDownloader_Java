package com.sgpublic.bilidownload.BangumiAPI;

import android.content.Context;

import com.sgpublic.bilidownload.DataItem.Episode.DownloadData;
import com.sgpublic.bilidownload.DataItem.Episode.TaskData;
import com.sgpublic.bilidownload.R;
import com.sgpublic.bilidownload.Unit.DownloadTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;

import okhttp3.Call;
import okhttp3.Response;

import static com.sgpublic.bilidownload.DataItem.Episode.InfoData.PAYMENT_NORMAL;

public class DownloadHelper {
    private final Context context;
    private final APIHelper helper;
    private final DownloadData downloadData = new DownloadData();

    public DownloadHelper(Context context){
        this.context = context;
        String access_token = context.getSharedPreferences("user", Context.MODE_PRIVATE)
                .getString("access_token", "");
        helper = new APIHelper(access_token);
    }

    //{"code":-10403,"message":"抱歉您所在地区不可观看！"}
    //{"code":-10403,"message":"大会员专享限制"}
    public DownloadData getDownloadInfo(TaskData data){
        getOfficialDownloadInfo(data);
        return downloadData;
    }

    private void getOfficialDownloadInfo(TaskData data){
        Call call = helper.getEpisodeOfficialRequest(data.episodeData.cid, data.quality);
        try {
            Response response = call.execute();
            JSONObject object = new JSONObject(response.body().string());
            if (object.getInt("code") != 0) {
                if (object.getInt("code") == -10403 & data.episodeData.payment == PAYMENT_NORMAL){
                    getBiliplusDownloadInfo(data);
                } else {
                    downloadData.code = -501;
                    downloadData.message = object.getString("message");
                }
            } else {
                String type = object.getString("type");
                if (type.equals("FLV")) {
                    downloadData.code = -505;
                    downloadData.message = context.getString(R.string.error_download_flv);
                } else if (type.equals("DASH")) {
                    getDASHData(object, data.quality);
                } else {
                    downloadData.code = -506;
                }
            }
        } catch (IOException e){
            if (e instanceof UnknownHostException) {
                downloadData.code = -501;
                downloadData.message = context.getString(R.string.error_network);
            } else {
                downloadData.code = -502;
                downloadData.message = e.getMessage();
            }
            downloadData.e = e;
        } catch (JSONException e){
            downloadData.code = -503;
            downloadData.e = e;
        }
    }

    private void getBiliplusDownloadInfo(TaskData data){
        Call call = helper.getEpisodeOfficialRequest(data.episodeData.cid, data.quality);
        try {
            Response response = call.execute();
            if (response.code() == -504){
                getKghostDownloadInfo(data);
                return;
            }
            JSONObject object = new JSONObject(response.body().string());
            if (object.getInt("code") != 0) {
                downloadData.code = -514;
                downloadData.message = object.getString("message");
            } else {
                if (!object.isNull("durl")) {
                    downloadData.code = -515;
                    downloadData.message = context.getString(R.string.error_download_flv);
                } else if (!object.isNull("dash")) {
                    getDASHData(object, data.quality);
                } else {
                    downloadData.code = -516;
                }
            }
        } catch (IOException e){
            if (e instanceof UnknownHostException) {
                downloadData.code = -511;
                downloadData.message = context.getString(R.string.error_network);
            } else {
                downloadData.code = -512;
                downloadData.message = e.getMessage();
            }
            downloadData.e = e;
        } catch (JSONException e){
            downloadData.code = -513;
            downloadData.e = e;
        }
    }

    private void getKghostDownloadInfo(TaskData data) {
        Call call = helper.getEpisodeOfficialRequest(data.episodeData.cid, data.quality);
        try {
            Response response = call.execute();
            JSONObject object = new JSONObject(response.body().string());
            if (object.getInt("code") != 0) {
                downloadData.code = -524;
                downloadData.message = object.getString("message");
            } else {
                if (!object.isNull("durl")) {
                    downloadData.code = -525;
                    downloadData.message = context.getString(R.string.error_download_flv);
                } else if (!object.isNull("dash")) {
                    getDASHData(object, data.quality);
                } else {
                    downloadData.code = -526;
                }
            }
        } catch (IOException e){
            if (e instanceof UnknownHostException) {
                downloadData.code = -521;
                downloadData.message = context.getString(R.string.error_network);
            } else {
                downloadData.code = -522;
                downloadData.message = e.getMessage();
            }
            downloadData.e = e;
        } catch (JSONException | IndexOutOfBoundsException e){
            downloadData.code = -523;
            downloadData.e = e;
        }
    }

    private void getDASHData(JSONObject object, int qn) throws JSONException {
        DownloadData.DASHDownloadData downloadData = new DownloadData.DASHDownloadData();
        downloadData.time_length = object.getLong("timelength");

        JSONObject private_object = object.getJSONObject("dash");
        JSONArray array;
        array = private_object.getJSONArray("video");
        JSONObject object_video = null;
        for (int array_index = 0; array_index < array.length(); array_index++) {
            JSONObject object_video_index = array.getJSONObject(array_index);
            int video_qn_pre = object_video == null ? -1 : object_video.getInt("id");
            int video_qn_this = object_video_index.getInt("id");
            if (video_qn_this > video_qn_pre && video_qn_this <= qn){
                object_video = object_video_index;
            }
        }
        if (object_video != null){
            downloadData.video_url = object_video.getString("base_url");
            downloadData.video_bandwidth = object_video.getLong("bandwidth");
            downloadData.video_codecid = object_video.isNull("codecid") ? 0 : object_video.getInt("codecid");
            downloadData.video_id = object_video.getInt("id");
            downloadData.video_md5 = object_video.isNull("md5") ? "" : object_video.getString("md5");
            downloadData.video_size = DownloadTaskManager.getSizeLong(downloadData.video_url);
        }
        array = private_object.getJSONArray("audio");
        JSONObject object_audio = null;
        for (int array_index = 0; array_index < array.length(); array_index++) {
            JSONObject object_audio_index = array.getJSONObject(array_index);
            int audio_qn_pre = object_audio == null ? -1 : object_audio.getInt("id") - 30200;
            int audio_qn_this = object_audio_index.getInt("id") - 30200;
            if (audio_qn_this > audio_qn_pre && audio_qn_this <= qn){
                object_audio = object_audio_index;
            }
        }
        if (object_audio != null) {
            downloadData.audio_url = object_audio.getString("base_url");
            downloadData.audio_bandwidth = object_audio.getLong("bandwidth");
            downloadData.audio_codecid = object_audio.isNull("codecid") ? 0 : object_audio.getInt("codecid");
            downloadData.audio_id = object_audio.getInt("id");
            downloadData.audio_md5 = object_audio.isNull("md5") ? "" : object_audio.getString("md5");
            downloadData.audio_size = DownloadTaskManager.getSizeLong(downloadData.audio_url);
        }
        if (object_video != null && object_audio != null){
            this.downloadData.code = 0;
            downloadData.total_size = downloadData.audio_size + downloadData.video_size;
            this.downloadData.data = downloadData;
        } else {
            this.downloadData.code = -531;
            this.downloadData.message = context.getString(R.string.error_download_url);
        }
    }
}

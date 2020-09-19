package com.sgpublic.bilidownload.BangumiAPI;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import com.sgpublic.bilidownload.DataHelper.Episode.DASHDownloadData;
import com.sgpublic.bilidownload.DataHelper.Episode.FLVDownloadData;
import com.sgpublic.bilidownload.DataHelper.Episode.InfoData;
import com.sgpublic.bilidownload.DataHelper.Episode.QualityData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DownloadHelper {
    private static final String TAG = "DownloadHelper";

    private Context context;
    private SharedPreferences sharedPreferences;
    private String quality_set;
    private String type_set;
    private String type_tag;
    private String root_path;
    private String file_path;

    private long season_id;
    private long ep_id;

    private static final String user_agent = "Bilibili Freedoooooom/MarkII";
    private static final String[] types_pack = {
            "tv.danmaku.bili",
            "com.bilibili.app.blue",
            "com.bilibili.app.in"
    };
    private static final int[] quality_int = {
            112, 80, 64, 32, 16
    };
    private static final String[] quality_format = {
            "hdflv2", "flv", "flv720", "flv480", "mp4"
    };

    public DownloadHelper(Context context) {
        this.context = context;
    }

    public DownloadHelper(Context context, SharedPreferences sharedPreferences, long season_id, long ep_id) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        this.quality_set = quality_format[getIndex(quality_int, sharedPreferences.getInt("quality", 80))];
        this.type_set = types_pack[sharedPreferences.getInt("type", 0)];
        this.season_id = season_id;
        this.ep_id = ep_id;
    }

    public String getRootPath() {
        return root_path;
    }

    public String getFilePath() {
        return file_path;
    }

    public void setFormatJSON(DASHDownloadData downloadData,
                              InfoData infoData,
                              QualityData qualityData,
                              String season_title,
                              String season_cover,
                              int ep_index) throws JSONException, NullPointerException, IOException {
        type_tag = String.valueOf(qualityData.getQuality());
        String default_dir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            default_dir = "/storage/emulated/0/Download/";
        } else {
            default_dir = "/storage/emulated/0/Android/data/";
        }
        root_path = sharedPreferences.getString("location", default_dir) + type_set + "/download/s_"
                + season_id + "/" + ep_id + "/";
        file_path = root_path + type_tag + "/";

        JSONObject object_entry = new JSONObject();
        object_entry.put("media_type", 2);
        object_entry.put("has_dash_audio", true);
        object_entry.put("is_completed", true);
        object_entry.put("total_bytes", downloadData.total_size);
        object_entry.put("downloaded_bytes", downloadData.total_size);
        object_entry.put("title", season_title);
        object_entry.put("type_tag", type_tag);
        object_entry.put("cover", season_cover);
        object_entry.put("prefered_video_quality", qualityData.getQuality());
        object_entry.put("guessed_total_bytes", 0);
        object_entry.put("total_time_milli", downloadData.time_length);
        object_entry.put("danmaku_count", 3000);
        object_entry.put("time_update_stamp", System.currentTimeMillis());
        object_entry.put("time_create_stamp", System.currentTimeMillis());
        object_entry.put("season_id", season_id);
        JSONObject object_entry_source = new JSONObject();
        object_entry_source.put("av_id", infoData.aid);
        object_entry_source.put("cid", infoData.cid);
        object_entry_source.put("website", "bangumi");
        object_entry.put("source", object_entry_source);
        JSONObject object_entry_ep = new JSONObject();
        object_entry_ep.put("av_id", infoData.aid);
        object_entry_ep.put("page", 0);
        object_entry_ep.put("danmaku", infoData.cid);
        object_entry_ep.put("cover", infoData.cover);
        object_entry_ep.put("episode_id", infoData.ep_id);
        object_entry_ep.put("index", ep_index);
        object_entry_ep.put("index_title", infoData.title);
        object_entry_ep.put("from", "bangumi");
        object_entry_ep.put("season_type", 1);
        object_entry_ep.put("width", 0);
        object_entry_ep.put("height", 0);
        object_entry_ep.put("rotate", 0);
        object_entry_ep.put("link", "https://www.bilibili.com/bangumi/play/ep" + infoData.ep_id);
        object_entry_ep.put("bvid", infoData.bvid);
        object_entry.put("ep", object_entry_ep);

        JSONObject object_index = new JSONObject();
        JSONArray object_content_video = new JSONArray();
        JSONObject object_index_video = new JSONObject();
        object_index_video.put("id", downloadData.video_id);
        object_index_video.put("base_url", downloadData.video_url);
        JSONArray object_index_video_backup_url = new JSONArray();
        object_index_video_backup_url.put(downloadData.video_backup_url[0]);
        object_index_video_backup_url.put(downloadData.video_backup_url[1]);
        object_index_video.put("backup_url", object_index_video_backup_url);
        object_index_video.put("bandwidth", downloadData.video_bandwidth);
        object_index_video.put("codecid", downloadData.video_codecid);
        object_index_video.put("size", downloadData.video_size);
        object_index_video.put("md5", downloadData.video_md5);
        object_content_video.put(object_index_video);
        object_index.put("video", object_content_video);

        JSONArray object_content_audio = new JSONArray();
        JSONObject object_index_audio = new JSONObject();
        object_index_audio.put("id", downloadData.audio_id);
        object_index_audio.put("base_url", downloadData.audio_url);
        JSONArray object_index_audio_backup_url = new JSONArray();
        object_index_audio_backup_url.put(downloadData.audio_backup_url[0]);
        object_index_audio_backup_url.put(downloadData.audio_backup_url[1]);
        object_index_audio.put("backup_url", object_index_audio_backup_url);
        object_index_audio.put("bandwidth", downloadData.audio_bandwidth);
        object_index_audio.put("codecid", downloadData.audio_codecid);
        object_index_audio.put("size", downloadData.audio_size);
        object_index_audio.put("md5", downloadData.audio_md5);
        object_content_audio.put(object_index_audio);
        object_index.put("audio", object_content_audio);

        save(object_entry.toString(), getRootPath(), "entry.json");
        save(object_index.toString(), getFilePath(), "index.json");
    }

    public void setFormatJSON(FLVDownloadData downloadData,
                              InfoData infoData,
                              QualityData qualityData,
                              String season_title,
                              String season_cover,
                              int ep_index) throws JSONException, NullPointerException, IOException {
        type_tag = "lua." + quality_set + ".bb2api." + qualityData.getQuality();
        root_path = sharedPreferences.getString("location", "/storage/emulated/0/Android/data/") + type_set + "/download/s_"
                + season_id + "/" + ep_id + "/";
        file_path = root_path + type_tag + "/";

        JSONObject object_entry = new JSONObject();
        object_entry.put("media_type", 1);
        object_entry.put("has_dash_audio", false);
        object_entry.put("is_completed", true);
        object_entry.put("total_bytes", downloadData.flv_size);
        object_entry.put("downloaded_bytes", downloadData.flv_size);
        object_entry.put("title", season_title);
        object_entry.put("type_tag", type_tag);
        object_entry.put("cover", season_cover);
        object_entry.put("prefered_video_quality", qualityData.getQuality());
        object_entry.put("guessed_total_bytes", 0);
        object_entry.put("total_time_milli", downloadData.time_length);
        object_entry.put("danmaku_count", 3000);
        object_entry.put("time_update_stamp", System.currentTimeMillis());
        object_entry.put("time_create_stamp", System.currentTimeMillis());
        object_entry.put("season_id", season_id);
        JSONObject object_entry_source = new JSONObject();
        object_entry_source.put("av_id", infoData.aid);
        object_entry_source.put("cid", infoData.cid);
        object_entry_source.put("website", "bangumi");
        object_entry.put("source", object_entry_source);
        JSONObject object_entry_ep = new JSONObject();
        object_entry_ep.put("av_id", infoData.aid);
        object_entry_ep.put("page", 0);
        object_entry_ep.put("danmaku", infoData.cid);
        object_entry_ep.put("cover", infoData.cover);
        object_entry_ep.put("episode_id", infoData.ep_id);
        object_entry_ep.put("index", ep_index);
        object_entry_ep.put("index_title", infoData.title);
        object_entry_ep.put("from", "bangumi");
        object_entry_ep.put("season_type", 1);
        object_entry_ep.put("width", 0);
        object_entry_ep.put("height", 0);
        object_entry_ep.put("rotate", 0);
        object_entry_ep.put("link", "https://www.bilibili.com/bangumi/play/ep" + infoData.ep_id);
        object_entry_ep.put("bvid", infoData.bvid);
        object_entry.put("ep", object_entry_ep);

        JSONObject object_index = new JSONObject();
        object_index.put("from", "bangumi");
        object_index.put("quality", qualityData.getQuality());
        object_index.put("type_tag", type_tag);
        object_index.put("description", qualityData.getDescription());
        object_index.put("is_stub", false);
        object_index.put("psedo_bitrate", 0);

        JSONArray object_index_segment_list = new JSONArray();
        for (int list_index = 0; list_index < downloadData.section_count; list_index++) {
            JSONObject object_index_segment = new JSONObject();
            object_index_segment.put("url", downloadData.flv_url[list_index]);
            object_index_segment.put("duration", downloadData.flv_length[list_index]);
            object_index_segment.put("bytes", downloadData.flv_size[list_index]);
            object_index_segment.put("meta_url", "");
            object_index_segment.put("ahead", "");
            object_index_segment.put("vhead", "");
            object_index_segment.put("md5", downloadData.flv_md5[list_index]);
            JSONArray object_index_segment_list_backup_urls = new JSONArray();
            object_index_segment_list_backup_urls.put(downloadData.flv_backup_url[list_index][0]);
            object_index_segment_list_backup_urls.put(downloadData.flv_backup_url[list_index][1]);
            object_index_segment.put("backup_urls", object_index_segment_list_backup_urls);
            object_index_segment_list.put(object_index_segment);
        }

        object_index.put("segment_list", object_index_segment_list);
        object_index.put("parse_timestamp_milli", System.currentTimeMillis());
        object_index.put("available_period_milli", 0);
        object_index.put("local_proxy_type", 0);
        object_index.put("user_agent", user_agent);
        object_index.put("is_downloaded", false);
        object_index.put("is_resolved", false);
        JSONArray player_codec_config_list = new JSONArray();
        JSONObject player_codec_config = new JSONObject();
        player_codec_config.put("use_ijk_media_codec", false);
        player_codec_config.put("player", "IJK_PLAYER");
        player_codec_config_list.put(player_codec_config);
        player_codec_config = new JSONObject();
        player_codec_config.put("use_ijk_media_codec", false);
        player_codec_config.put("player", "ANDROID_PLAYER");
        player_codec_config_list.put(player_codec_config);
        object_index.put("player_codec_config_list", player_codec_config_list);
        object_index.put("time_length", downloadData.time_length);
        object_index.put("marlin_token", "");
        object_index.put("video_codec_id", downloadData.flv_codecid);
        object_index.put("video_project", false);

        save(object_entry.toString(), getRootPath(), "entry.json");
        save(object_index.toString(), getFilePath(), "index.json");
    }

    public void handleDownload(String dl_url, String ep_title, String file_path, String file_name) {
        Uri url = Uri.parse(dl_url);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request req = new DownloadManager.Request(url);
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.addRequestHeader("User-Agent", user_agent);
        req.setDestinationUri(Uri.fromFile(new File(file_path, file_name)));
        req.setVisibleInDownloadsUi(true);
        req.setTitle(ep_title + "_" + file_name);
        downloadManager.enqueue(req);
    }

    private void save(String string_data, String file_path, String file_name) throws IOException {
        File file = new File(file_path);
        boolean create = file.exists();
        if (!create) {
            create = file.mkdirs();
        }
        if (create) {
            file = new File(file_path, file_name);
            FileOutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write(string_data);
            writer.flush();
            outputStream.flush();
            writer.close();
            outputStream.close();
        } else {
            throw new IOException(file_path + file_name + " (Can not create dirs)");
        }
    }

    long getSizeLong(String url_string) {
        try {
            URL url = new URL(url_string);
            HttpURLConnection url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestProperty("accept", "*/*");
            url_con.setRequestProperty("connection", "Keep-Alive");
            url_con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            url_con.setRequestProperty("User-Agent", user_agent);
            long size = url_con.getContentLength();
            url_con.disconnect();
            return size;
        } catch (IOException e) {
            return 0;
        }
    }

    public String getSizeString(String url_string) {
        long size = getSizeLong(url_string);
        String size_string;
        BigDecimal fileSize = new BigDecimal(size);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = fileSize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1) {
            size_string = returnValue + " MB";
        } else {
            BigDecimal kilobyte = new BigDecimal(1024);
            returnValue = fileSize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                    .floatValue();
            size_string = returnValue + " KB";
        }
        return size_string;
    }

    private int getIndex(int[] all_args, int finding_arg) {
        int result = -1;
        for (int arg_index : all_args) {
            result = result + 1;
            if (arg_index == finding_arg) {
                break;
            }
        }
        return result;
    }
}

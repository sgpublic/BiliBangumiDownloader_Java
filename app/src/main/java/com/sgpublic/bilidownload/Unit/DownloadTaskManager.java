package com.sgpublic.bilidownload.Unit;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.sgpublic.bilidownload.BangumiAPI.DownloadHelper;
import com.sgpublic.bilidownload.DataItem.DownloadTaskData;
import com.sgpublic.bilidownload.DataItem.Episode.DownloadData;
import com.sgpublic.bilidownload.DataItem.Episode.InfoData;
import com.sgpublic.bilidownload.DataItem.Episode.TaskData;
import com.sgpublic.bilidownload.DataItem.SeriesData;
import com.sgpublic.bilidownload.R;
import com.sgpublic.bilidownload.Service.DownloadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadTaskManager {
    private static final int STATUS_WAITING = 1 << 5;

    private final Context context;
    private final SeriesData seriesData;

    private InfoData infoData;
    private int quality;
    private TaskCreateCallback callback_private;
    private File season_path;
    private File episode_path;

    private static final String user_agent = "Bilibili Freedoooooom/MarkII";

    public DownloadTaskManager(Context context, SeriesData seriesData) {
        this.context = context;
        this.seriesData = seriesData;
    }

    public void addDownloadTask(InfoData infoData, int quality, TaskCreateCallback callback){
        this.infoData = infoData;
        this.quality = quality;
        ConfigManager.ClientItem client = ConfigManager.checkClient(context);
        if (client != null){
            String season_path = ConfigManager.getDownloadDir(context)
                    + client.getPackageName()
                    + "/download/s_" + seriesData.season_id + "/";
            this.season_path = new File(season_path);
            String episode_path = season_path + infoData.ep_id + "/";
            this.episode_path = new File(episode_path);
            File episode_entry_file = new File(this.episode_path, "entry.json");
            if (episode_entry_file.exists()){
                callback.onTaskExist();
            } else {
                callback_private = callback;
                setupTaskDir();
            }
        } else {
            callback.onFailure(-600, context.getString(
                    R.string.error_download_no_client
            ), null);
        }
    }

    private void setupTaskDir(){
        try {
            File season_entry_file = new File(season_path, "entry.json");
            if (!season_entry_file.exists()){
                JSONObject season_entry = new JSONObject();
                season_entry.put("season_id", seriesData.season_id);
                season_entry.put("title", seriesData.title);
                season_entry.put("cover", seriesData.cover);
                season_entry.put("season_type", seriesData.season_type);
                season_entry.put("season_type_name", seriesData.season_type_name);
                season_entry.put("badge_color", seriesData.badge_color);
                season_entry.put("badge_color_night", seriesData.badge_color_night);
                save(season_entry.toString(), season_entry_file);
            }
            File episode_entry_file = new File(episode_path, "entry.json");
            if (!episode_entry_file.exists()){
                JSONObject episode_entry = new JSONObject();
                episode_entry.put("code", 0);
                episode_entry.put("message", "");
                episode_entry.put("total_bytes", 0);
                episode_entry.put("prefered_video_quality", quality);
                episode_entry.put("type_tag", quality);
                episode_entry.put("time_create_stamp", System.currentTimeMillis());
                JSONObject episode_entry_ep = new JSONObject();
                episode_entry_ep.put("av_id", infoData.aid);
                episode_entry_ep.put("episode_id", infoData.ep_id);
                episode_entry_ep.put("bvid", infoData.bvid);
                episode_entry_ep.put("cover", infoData.cover);
                episode_entry_ep.put("index", infoData.index);
                episode_entry_ep.put("index_title", infoData.title);
                episode_entry_ep.put("season_type", seriesData.season_type);
                episode_entry_ep.put("badge", infoData.badge);
                episode_entry_ep.put("badge_color", infoData.badge_color);
                episode_entry_ep.put("badge_color_night", infoData.badge_color_night);
                episode_entry.put("ep", episode_entry_ep);
                JSONObject object_entry_source = new JSONObject();
                object_entry_source.put("episode_id", infoData.ep_id);
                object_entry_source.put("cid", infoData.cid);
                object_entry_source.put("av_id", infoData.aid);
                object_entry_source.put("bvid", infoData.bvid);
                episode_entry.put("source", object_entry_source);
                save(episode_entry.toString(), episode_entry_file);
                DownloadService.startService(context);
                callback_private.onResult();
            } else {
                callback_private.onTaskExist();
            }
        } catch (JSONException | IOException e){
            callback_private.onFailure(-601, e.getMessage(), e);
        }
    }

    public static boolean isAllTaskFinished(Context context){
        ArrayList<TaskData> taskDataList = listAllEpisodeTask(context);
        MyLog.d(DownloadTaskManager.class, "taskDataList count: " + taskDataList.size());
        return taskDataList.stream().map(
                taskData -> taskData.task_info.status == DownloadManager.STATUS_FAILED | taskData.task_info.status == DownloadManager.STATUS_SUCCESSFUL
        ).reduce(true, (a, b) -> a && b);
    }

    public static int countDoingTask(Context context){
        ArrayList<TaskData> taskDataList = listAllEpisodeTask(context);
        int count = (int) taskDataList.stream().filter(taskData ->
                taskData.task_info.status == DownloadManager.STATUS_RUNNING | taskData.task_info.status == DownloadManager.STATUS_PENDING
        ).count();
        MyLog.d(DownloadTaskManager.class, "running task count: " + count);
        return count;
    }

    public static ArrayList<TaskData> listAllEpisodeTask(Context context){
        ArrayList<TaskData> taskDataList = new ArrayList<>();
        ConfigManager.ClientItem client = ConfigManager.checkClient(context);
        if (client == null) {
            MyLog.d(DownloadTaskManager.class, "no client find.");
            return taskDataList;
        }
        String season_path = ConfigManager.getDownloadDir(context)
                + client.getPackageName() + "/download/";
        File season_path_file = new File(season_path);
        File[] season_task_file = season_path_file.listFiles(
                pathname -> pathname.getName().startsWith("s_") & pathname.isDirectory()
        );
        if (season_task_file == null){
            MyLog.d(DownloadTaskManager.class, "no season task find.");
            return taskDataList;
        }
        for (File single_season_task : season_task_file){
            File season_entry_file = new File(single_season_task, "entry.json");
            if (!season_entry_file.exists()){
                MyLog.d(DownloadTaskManager.class, "no season entry file find: " + single_season_task.getName());
                continue;
            }
            SeriesData series_data = new SeriesData();
            JSONObject season_entry_object;
            try {
                season_entry_object = new JSONObject(read(season_entry_file));
                series_data.season_id = season_entry_object.getLong("season_id");
                if (!single_season_task.getName().equals("s_" + series_data.season_id)){
                    MyLog.d(DownloadTaskManager.class, "season_id invalid, path name: " + single_season_task.getName() + "season_id: " + series_data.season_id);
                    continue;
                }
                series_data.title = season_entry_object.getString("title");
                series_data.cover = season_entry_object.getString("cover");
                series_data.season_type_name = season_entry_object.getString("season_type_name");
                series_data.badge_color = season_entry_object.getInt("badge_color");
                series_data.badge_color_night = season_entry_object.getInt("badge_color_night");
            } catch (JSONException ignore){
                MyLog.d(DownloadTaskManager.class, "season entry read failed: " + ignore.getMessage());
                continue;
            }

            File[] episode_task_file = single_season_task.listFiles(File::isDirectory);
            if (episode_task_file == null){
                continue;
            }
            for (File single_episode_task : episode_task_file){
                try {
                    File entry_file = new File(single_episode_task, "entry.json");
                    if (!entry_file.exists()){
                        continue;
                    }
                    JSONObject episode_entry_object = new JSONObject(read(entry_file));
                    TaskData taskData = new TaskData();
                    taskData.code = episode_entry_object.getInt("code");
                    taskData.message = episode_entry_object.getString("message");
                    taskData.quality = episode_entry_object.getInt("prefered_video_quality");

                    DownloadTaskData downloadTaskData = new DownloadTaskData();
                    downloadTaskData.total_bytes = episode_entry_object.getLong("total_bytes");
                    JSONObject entry_ep = episode_entry_object.getJSONObject("ep");
                    InfoData infoData = new InfoData();
                    infoData.title = entry_ep.getString("index_title");
                    infoData.cover = entry_ep.getString("cover");
                    infoData.index = entry_ep.getString("index");
                    infoData.ep_id = entry_ep.getLong("episode_id");
                    infoData.aid = entry_ep.getLong("av_id");
                    infoData.bvid = entry_ep.getString("bvid");

                    JSONObject entry_source = episode_entry_object.getJSONObject("source");
                    infoData.cid = entry_source.getLong("cid");

                    if (!single_episode_task.getName().equals(String.valueOf(infoData.ep_id))){
                        MyLog.d(DownloadTaskManager.class, "ep_id invalid, path name: " + single_episode_task.getName() + "ep_id: " + infoData.ep_id);
                        continue;
                    }
                    infoData.badge = entry_ep.getString("badge");
                    infoData.badge_color = entry_ep.getInt("badge_color");
                    infoData.badge_color_night = entry_ep.getInt("badge_color_night");
                    taskData.episodeData = infoData;
                    taskData.seriesData = series_data;

                    File index_path = new File(single_episode_task.getPath() + "/" + taskData.quality, "index.json");
                    if (!index_path.exists() | downloadTaskData.total_bytes == 0){
                        downloadTaskData.status = DownloadTaskManager.STATUS_WAITING;
                        taskData.task_info = downloadTaskData;
                        taskDataList.add(taskData);
                        continue;
                    }
                    taskData.season_type = season_entry_object.getInt("season_type");
                    taskData.season_type_name = season_entry_object.getString("season_type_name");
                    taskData.media_type = episode_entry_object.getInt("media_type");

                    downloadTaskData.status = DownloadManager.STATUS_FAILED;

                    File file = new File(index_path.getParent(), String.valueOf(taskData.quality));
                    File video_file = new File(file, "video.m4s");
                    File audio_file = new File(file, "audio.m4s");
                    try {
                        if (audio_file.exists() & video_file.exists()){
                            FileInputStream fis;
                            fis = new FileInputStream(audio_file);
                            downloadTaskData.download_bytes = fis.available();
                            fis = new FileInputStream(video_file);
                            downloadTaskData.download_bytes += fis.available();
                            downloadTaskData.status = DownloadManager.STATUS_SUCCESSFUL;
                        }
                    } catch (IOException ignore){ }
                    if (downloadTaskData.status != DownloadManager.STATUS_SUCCESSFUL){
                        JSONObject index = new JSONObject(read(index_path));
                        long video_download_id = index.getJSONArray("video")
                                .getJSONObject(0).getLong("download_id");
                        long audio_download_id = index.getJSONArray("audio")
                                .getJSONObject(0).getLong("download_id");
                        DownloadTaskData video_task_data = inquireDownloadTask(context, video_download_id);
                        DownloadTaskData audio_task_data = inquireDownloadTask(context, audio_download_id);
                        if (video_task_data != null & audio_task_data != null) {
                            downloadTaskData.download_bytes = video_task_data.download_bytes + audio_task_data.download_bytes;
                            if (downloadTaskData.download_bytes == downloadTaskData.total_bytes) {
                                downloadTaskData.status = DownloadManager.STATUS_SUCCESSFUL;
                            } else if (video_task_data.status == DownloadManager.STATUS_RUNNING | audio_task_data.status == DownloadManager.STATUS_RUNNING) {
                                downloadTaskData.status = DownloadManager.STATUS_RUNNING;
                            } else if (video_task_data.status == DownloadManager.STATUS_PENDING | audio_task_data.status == DownloadManager.STATUS_PENDING) {
                                downloadTaskData.status = DownloadManager.STATUS_PENDING;
                            } else if (video_task_data.status == DownloadManager.STATUS_PAUSED | audio_task_data.status == DownloadManager.STATUS_PAUSED) {
                                downloadTaskData.status = DownloadManager.STATUS_PAUSED;
                            }
                        }
                    }
                    downloadTaskData.progress = (int) (downloadTaskData.download_bytes * 100 / downloadTaskData.total_bytes);
                    taskData.task_info = downloadTaskData;
                    taskDataList.add(taskData);
                } catch (JSONException ignore){
                    MyLog.d(DownloadTaskManager.class, "episode entry read failed: " + ignore.getMessage());
                }
            }
        }
        return taskDataList;
    }

    public static void startNextTask(Context context){
        ArrayList<TaskData> taskDataList = listAllEpisodeTask(context);
        AtomicBoolean created = new AtomicBoolean(false);
        taskDataList.forEach(taskData -> {
            if (!created.get() & taskData.task_info.status == DownloadTaskManager.STATUS_WAITING) {
                created.set(true);
                onHandelTask(context, taskData);
            }
        });
    }

    private static void onHandelTask(Context context, TaskData taskData){
        ConfigManager.ClientItem client = ConfigManager.checkClient(context);
        if (client != null){
            String episode_path = ConfigManager.getDownloadDir(context)
                    + client.getPackageName()
                    + "/download/s_" + taskData.seriesData.season_id
                    + "/" + taskData.episodeData.ep_id;
            File episode = new File(episode_path);
            String file_path = episode_path + "/" + taskData.quality + "/";
            File file = new File(file_path);

            try {
                DownloadHelper helper = new DownloadHelper(context);
                DownloadData downloadInfo = helper.getDownloadInfo(taskData);
                if (downloadInfo.code != 0){
                    saveTaskMessage(episode, downloadInfo.code, downloadInfo.message);
                }
                downloadInfo.data.video_download_id = onHandleDownload(context, downloadInfo.data.video_url, taskData.episodeData.title, file, "video.m4s");
                downloadInfo.data.audio_download_id = onHandleDownload(context, downloadInfo.data.audio_url, taskData.episodeData.title, file, "audio.m4s");
                setFormatJSON(downloadInfo.data, taskData.episodeData, taskData.quality, taskData.seriesData, episode, file);
            } catch (JSONException | IOException ignore){
                MyLog.d(DownloadTaskManager.class, "task create failed: " + ignore.getMessage());
            }
        }
    }

    public static long onHandleDownload(Context context, String dl_url, String ep_title, File file_path, String file_name) {
        File file = new File(file_path, file_name);
        if (file.exists()){
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        Uri url = Uri.parse(dl_url);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request req = new DownloadManager.Request(url);
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.addRequestHeader("User-Agent", user_agent);
        req.setDestinationUri(Uri.fromFile(file));
        req.setTitle(ep_title + "_" + file_name);
        req.setVisibleInDownloadsUi(true);
        return downloadManager.enqueue(req);
    }

    public static void setFormatJSON(
            DownloadData.DASHDownloadData downloadData, InfoData infoData, int quality, SeriesData seriesData, File episode_path, File file_path
    ) throws JSONException, NullPointerException, IOException {
        JSONObject object_entry = new JSONObject();
        object_entry.put("code", 0);
        object_entry.put("message", "");
        object_entry.put("media_type", 2);
        object_entry.put("has_dash_audio", true);
        object_entry.put("is_completed", true);
        object_entry.put("total_bytes", downloadData.total_size);
        object_entry.put("downloaded_bytes", downloadData.total_size);
        object_entry.put("title", seriesData.title);
        object_entry.put("type_tag", String.valueOf(quality));
        object_entry.put("cover", infoData.cover);
        object_entry.put("video_quality", quality);
        object_entry.put("prefered_video_quality", quality);
        object_entry.put("guessed_total_bytes", 0);
        object_entry.put("total_time_milli", downloadData.time_length);
        object_entry.put("danmaku_count", 0);
        object_entry.put("time_update_stamp", System.currentTimeMillis());
        object_entry.put("can_play_in_advance", true);
        object_entry.put("interrupt_transform_temp_file", false);
        object_entry.put("quality_pithy_description", "");
        object_entry.put("preferred_audio_quality", 0);
        object_entry.put("audio_quality", 0);
        object_entry.put("cache_version_code", 6190400);
        object_entry.put("quality_superscript", "");
        object_entry.put("season_id", String.valueOf(seriesData.season_id));
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
        object_entry_ep.put("index", infoData.index);
        object_entry_ep.put("index_title", infoData.title);
        object_entry_ep.put("from", "bangumi");
        object_entry_ep.put("season_type", seriesData.season_type);
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
        object_index_video.put("download_id", downloadData.video_download_id);
        object_index_video.put("base_url", downloadData.video_url);
        JSONArray object_index_video_backup_url = new JSONArray();
        object_index_video.put("backup_url", object_index_video_backup_url);
        object_index_video.put("bandwidth", downloadData.video_bandwidth);
        object_index_video.put("codecid", downloadData.video_codecid);
        object_index_video.put("size", downloadData.video_size);
        object_index_video.put("md5", downloadData.video_md5);
        object_index_video.put("no_rexcode", false);
        object_content_video.put(object_index_video);
        object_index.put("video", object_content_video);

        JSONArray object_content_audio = new JSONArray();
        JSONObject object_index_audio = new JSONObject();
        object_index_audio.put("id", downloadData.audio_id);
        object_index_audio.put("download_id", downloadData.audio_download_id);
        object_index_audio.put("base_url", downloadData.audio_url);
        JSONArray object_index_audio_backup_url = new JSONArray();
        object_index_audio.put("backup_url", object_index_audio_backup_url);
        object_index_audio.put("bandwidth", downloadData.audio_bandwidth);
        object_index_audio.put("codecid", downloadData.audio_codecid);
        object_index_audio.put("size", downloadData.audio_size);
        object_index_audio.put("md5", downloadData.audio_md5);
        object_index_audio.put("no_rexcode", false);
        object_content_audio.put(object_index_audio);
        object_index.put("audio", object_content_audio);

        MyLog.d(DownloadTaskManager.class, "episode_path: " + episode_path.getPath());
        MyLog.d(DownloadTaskManager.class, "file_path: " + file_path.getPath());
        save(object_entry.toString(), new File(episode_path, "entry.json"));
        save(object_index.toString(), new File(file_path, "index.json"));
    }

    private static void saveTaskMessage(File episode_path, int code, String message) throws JSONException, IOException {
        File entry = new File(episode_path, "entry.json");
        JSONObject object = new JSONObject(read(entry));
        object.put("code", code);
        object.put("message", message);
        save(object.toString(), entry);
    }

    private static void save(String string_data, File file) throws IOException {
        if (!file.getParentFile().exists() & !Objects.requireNonNull(file.getParentFile()).mkdirs()) {
            MyLog.d(DownloadTaskManager.class, file.getPath() + " (Can not create dir)");
            throw new IOException(file.getPath() + " (Can not create dir)");
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(string_data);
        writer.flush();
        outputStream.flush();
        writer.close();
        outputStream.close();
    }

    private static String read(File file){
        if (!file.exists()){
            return "";
        }
        String log_content;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            log_content = stringBuilder.toString();
        } catch (IOException ignore) {
            log_content = "";
        }
        return log_content;
    }

    private static DownloadTaskData inquireDownloadTask(Context context, long task_id){
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(task_id);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(query);
        if(cursor.moveToFirst()){
            DownloadTaskData data = new DownloadTaskData();
            data.status = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            );
            data.download_bytes = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            );
            data.total_bytes = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            );
            data.progress = (int) (data.download_bytes * 100 / data.total_bytes);
            return data;
        }
        return null;
    }

    public static long getSizeLong(String url_string) {
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

    public static String getSizeString(String url_string) {
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

    public interface TaskCreateCallback {
        void onFailure(int code, String message, Throwable e);
        void onTaskExist();
        void onResult();
    }
}

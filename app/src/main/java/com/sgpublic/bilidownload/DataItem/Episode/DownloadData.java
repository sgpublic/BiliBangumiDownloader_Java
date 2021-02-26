package com.sgpublic.bilidownload.DataItem.Episode;

public class DownloadData {
    public int code = -1;
    public String message = "";
    public Throwable e = null;
    public DASHDownloadData data = new DASHDownloadData();

    public static class DASHDownloadData {
        public long time_length;
        public long total_size;

        public String video_url;
        public long video_size;
        public long video_bandwidth;
        public int video_id;
        public long video_download_id;
        public int video_codecid;
        public String video_md5;

        public String audio_url;
        public long audio_size;
        public long audio_bandwidth;
        public int audio_id;
        public long audio_download_id;
        public int audio_codecid;
        public String audio_md5;
    }
}

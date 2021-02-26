package com.sgpublic.bilidownload.DataItem.Episode;

import com.sgpublic.bilidownload.DataItem.DownloadTaskData;
import com.sgpublic.bilidownload.DataItem.SeriesData;

import java.util.Objects;

public class TaskData {
    public int code;
    public String message;

    public int quality;
    public int season_type;
    public String season_type_name;
    public int media_type;

    public SeriesData seriesData;
    public InfoData episodeData;
    public DownloadTaskData task_info;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskData taskData = (TaskData) o;
        return seriesData.equals(taskData.seriesData) &&
                episodeData.equals(taskData.episodeData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seriesData, episodeData);
    }
}

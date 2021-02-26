package com.sgpublic.bilidownload.DataItem;

import com.sgpublic.bilidownload.DataItem.Episode.QualityData;

import java.util.ArrayList;
import java.util.Objects;

public class SeasonData {
    public int area;

    public SeriesData base_info;

    public String actors;
    public String alias;
    public String evaluate;
    public String staff;
    public String styles;
    public String description;
    public Double rating;

    public int actors_lines;
    public int staff_lines;

    public int season_type;

    public ArrayList<SeriesData> series;
    public ArrayList<QualityData> qualities;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeasonData that = (SeasonData) o;
        return series.equals(that.series);
    }

    @Override
    public int hashCode() {
        return Objects.hash(series);
    }
}

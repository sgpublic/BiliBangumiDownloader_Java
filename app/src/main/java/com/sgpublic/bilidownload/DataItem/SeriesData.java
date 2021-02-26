package com.sgpublic.bilidownload.DataItem;

import java.util.Objects;

public class SeriesData {
    public int season_type;
    public String season_type_name = "";

    public String badge;
    public int badge_color;
    public int badge_color_night;

    public String cover;
    public String title;
    public long season_id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeriesData that = (SeriesData) o;
        return season_id == that.season_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(season_id);
    }
}

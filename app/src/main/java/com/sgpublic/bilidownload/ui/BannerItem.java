package com.sgpublic.bilidownload.ui;

import android.content.Context;

public class BannerItem {

    private final String cover_episode;
    private final String season_cover;
    private final long season_id;
    private final String indicator;
    private final String title;
    private final Context context;
    private final String badge;
    private final int badge_color;

    public BannerItem(Context context, String cover_episode, String season_cover, long season_id, String title, String indicator, String badge, int badge_color) {
        this.context = context;
        this.cover_episode = cover_episode;
        this.season_cover = season_cover;
        this.season_id = season_id;
        this.indicator = indicator;
        this.title = title;
        this.badge = badge;
        this.badge_color = badge_color;
    }

    public Context getContext() {
        return context;
    }

    String getBannerPath() {
        return cover_episode;
    }

    String getIndicatorText() {
        return title + "：" + indicator;
    }

    public long getSeasonId() {
        return season_id;
    }

    public String getTitle() {
        return title;
    }

    public String getSeasonCover() {
        return season_cover;
    }

    String getBadge() {
        return badge;
    }

    public int getBadgeColor() {
        return badge_color;
    }
}
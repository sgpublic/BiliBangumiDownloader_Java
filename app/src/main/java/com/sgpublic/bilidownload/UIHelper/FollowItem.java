package com.sgpublic.bilidownload.UIHelper;

public class FollowItem {
    private final String cover;
    private final String title;
    private final long sid;

    public FollowItem(String cover, String title, long sid) {
        this.cover = cover;
        this.title = title;
        this.sid = sid;
    }

    String getCover() {
        return cover;
    }

    String getTitle() {
        return title;
    }

    long getSid() {
        return sid;
    }
}

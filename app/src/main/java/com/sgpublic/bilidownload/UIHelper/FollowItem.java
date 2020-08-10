package com.sgpublic.bilidownload.UIHelper;

public class FollowItem {
    private String cover;
    private String title;
    private long sid;

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

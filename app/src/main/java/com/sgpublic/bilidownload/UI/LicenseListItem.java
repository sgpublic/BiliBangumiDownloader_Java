package com.sgpublic.bilidownload.UI;

public class LicenseListItem {
    private final String title;
    private final String about;
    private final String author;
    private final String url;

    public LicenseListItem(String title, String about, String author, String url){
        this.about = about;
        this.author = author;
        this.title = title;
        this.url = url;
    }

    public String getProjectAbout() {
        return about;
    }

    public String getProjectAuthor() {
        return author;
    }

    public String getProjectTitle() {
        return title;
    }

    public String getProjectUrl() {
        return url;
    }
}

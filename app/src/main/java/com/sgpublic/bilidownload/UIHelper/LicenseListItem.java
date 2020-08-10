package com.sgpublic.bilidownload.UIHelper;

public class LicenseListItem {
    private String title;
    private String about;
    private String author;
    private String url;

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

package com.sgpublic.bilidownload.DataHelper.Episode;

public class QualityData {
    private int quality;
    private String description;
    private String format;

    public QualityData(int quality, String description, String format) {
        this.description = description;
        this.quality = quality;
        this.format = format;
    }

    public int getQuality() {
        return quality;
    }

    public String getDescription() {
        return description;
    }

    public String getFormat() {
        return format;
    }
}

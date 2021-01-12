package com.sgpublic.bilidownload.DataHelper.Episode;

public class QualityData {
    private final int quality;
    private final String description;
    private final String format;

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

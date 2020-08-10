package com.sgpublic.bilidownload.UIHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExploreFolderItem {
    private String fold_name;
    private long fold_description;

    public ExploreFolderItem(String fold_name, long fold_description) {
        this.fold_name = fold_name;
        this.fold_description = fold_description;
    }


    String getFoldDescription() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return formatter.format(fold_description);
    }

    String getFoldName() {
        return fold_name;
    }
}

package com.sgpublic.bilidownload.Activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.sgpublic.bilidownload.BaseStation.BaseActivity;
import com.sgpublic.bilidownload.R;

public class Download extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();

        setContentView(R.layout.activity_download);

        Toolbar download_toolbar = findViewById(R.id.download_toolbar);
        setSupportActionBar(download_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_mine_setting);
        }
    }
}

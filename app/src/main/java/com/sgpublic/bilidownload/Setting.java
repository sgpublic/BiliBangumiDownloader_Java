package com.sgpublic.bilidownload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.sgpublic.bilidownload.BaseService.BaseActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Setting extends BaseActivity {
    private static final String[] types_string = {
            "正式版",
            "概念版",
            "国际版"
    };
    private static final String[] quality_description = {
            "超清 4K",
            "高清 1080P+",
            "高清 1080P",
            "高清 720P",
            "清晰 480P",
            "流畅 360P"
    };
    private static final int[] quality_int = {
            120, 112, 80, 64, 32, 16
    };

    private TextView setting_type_string;
    private TextView setting_location_string;
    private TextView setting_quality_string;

    private int type_set;
    private int type_set_doing;
    private int quality_set;
    private int quality_set_doing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSetting();
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();
        setContentView(R.layout.activity_setting);

        setting_type_string = findViewById(R.id.setting_type_string);
        setting_location_string = findViewById(R.id.setting_location_string);
        setting_quality_string = findViewById(R.id.setting_quality_string);

        Toolbar setting_toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(setting_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_mine_setting);
        }

        findViewById(R.id.setting_type).setOnClickListener(v -> {
            type_set_doing = type_set;
            AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
            builder.setTitle(R.string.title_setting_type);
            builder.setSingleChoiceItems(types_string, type_set, (dialog, which) -> type_set_doing = which);
            builder.setPositiveButton(R.string.text_ok, (dialog, which) -> {
                sharedPreferences.edit()
                        .putInt("type", type_set_doing)
                        .apply();
                setupSetting();
            });
            builder.setNegativeButton(R.string.text_cancel, null);
            builder.show();
        });

        findViewById(R.id.setting_location).setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, Explore.class);
            Setting.this.startActivityForResult(intent, 0);
        });

        findViewById(R.id.setting_quality).setOnClickListener(v -> {
            quality_set_doing = quality_set;
            AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
            builder.setTitle(R.string.title_setting_quality);
            builder.setSingleChoiceItems(quality_description, quality_set, (dialog, which) -> quality_set_doing = which);
            builder.setPositiveButton(R.string.text_ok, (dialog, which) -> {
                sharedPreferences.edit()
                        .putInt("quality", quality_int[quality_set_doing])
                        .apply();
                setupSetting();
            });
            builder.setNegativeButton(R.string.text_cancel, null);
            builder.show();
        });
    }

    private void setupSetting() {
        type_set = sharedPreferences.getInt("type", 0);
        String location_set = sharedPreferences.getString("location", "/storage/emulated/0/Android/data/");
        quality_set = getIndex(quality_int, sharedPreferences.getInt("quality", 80));
        if (type_set > types_string.length) {
            sharedPreferences.edit()
                    .putInt("type", 0)
                    .apply();
            type_set = 0;
        }
        if (quality_set > quality_description.length) {
            sharedPreferences.edit()
                    .putInt("quality", 80)
                    .apply();
            quality_set = 80;
        }
        setting_type_string.setText(types_string[type_set]);
        setting_location_string.setText(location_set);
        setting_quality_string.setText(quality_description[quality_set]);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setupSetting();
    }
}

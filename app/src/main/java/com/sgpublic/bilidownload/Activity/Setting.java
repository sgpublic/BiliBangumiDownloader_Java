package com.sgpublic.bilidownload.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.sgpublic.bilidownload.BaseStation.BaseActivity;
import com.sgpublic.bilidownload.Unit.ConfigManager;
import com.sgpublic.bilidownload.Unit.ConfigManager.ClientItem;
import com.sgpublic.bilidownload.Unit.ConfigManager.QualityItem;
import com.sgpublic.bilidownload.R;
import com.sgpublic.bilidownload.Unit.MyLog;

import java.util.ArrayList;

public class Setting extends BaseActivity {
    private TextView setting_type_string;
    private TextView setting_location_string;
    private TextView setting_quality_string;
    private TextView setting_task_count_string;
    private SwitchCompat setting_auto_start;

    private ClientItem client_set;
    private QualityItem quality_item;
    private int task_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();
        setContentView(R.layout.activity_setting);

        setting_type_string = findViewById(R.id.setting_type_string);
        setting_location_string = findViewById(R.id.setting_location_string);
        setting_quality_string = findViewById(R.id.setting_quality_string);
        setting_auto_start = findViewById(R.id.setting_auto_start);
        setting_task_count_string = findViewById(R.id.setting_task_count_string);

        Toolbar setting_toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(setting_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_mine_setting);
        }

        ArrayList<ClientItem> clientItems = ConfigManager.getInstalledClients(Setting.this);
        LinearLayout setting_type = findViewById(R.id.setting_type);
        if (clientItems.size() > 1){
            setting_type.setAlpha(1.0F);
            setting_type.setOnClickListener(v -> typeSetting());
            client_set = ConfigManager.checkClient(Setting.this);
        } else {
            setting_type.setAlpha(0.3F);
            setting_type.setClickable(false);
            if (clientItems.size() == 1){
                client_set = clientItems.get(0);
                sharedPreferences.edit()
                        .putString("package", client_set.getPackageName())
                        .apply();
            }
        }
        if (client_set != null){
            setting_type_string.setText(client_set.getName());
        }

        taskCountLoad();
        locationSettingLoad();
        taskAutoStartLoad();
        findViewById(R.id.setting_location).setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, Explore.class);
            Setting.this.startActivityForResult(intent, 0);
        });

        quality_item = ConfigManager.checkQuality(Setting.this);
        setting_quality_string.setText(quality_item.getName());
        findViewById(R.id.setting_task_count).setOnClickListener(v -> taskCountSetting(1, 3));
        findViewById(R.id.setting_quality).setOnClickListener(v -> qualitySetting());
        findViewById(R.id.setting_auto_start_base).setOnClickListener(v ->
                setting_auto_start.setChecked(!setting_auto_start.isChecked())
        );
        setting_auto_start.setOnCheckedChangeListener((buttonView, isChecked) ->
                autoStartSetting(isChecked)
        );
    }

    private void typeSetting(){
        ArrayList<ClientItem> clientItems = ConfigManager.getInstalledClients(Setting.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
        builder.setTitle(R.string.title_setting_type);
        String[] types_string = new String[clientItems.size()];
        int type_set = 0;
        String package_set = sharedPreferences.getString("package",
                clientItems.get(0).getPackageName());
        for (int index = 0; index < clientItems.size(); index++){
            ClientItem item = clientItems.get(index);
            types_string[index] = item.getName();
            if (package_set.equals(item.getPackageName())){
                type_set = index;
                client_set = clientItems.get(index);
            }
        }
        builder.setSingleChoiceItems(types_string, type_set, (dialog, which) ->
                client_set = clientItems.get(which)
        );
        builder.setPositiveButton(R.string.text_ok, (dialog, which) -> {
            sharedPreferences.edit()
                    .putString("package", client_set.getPackageName())
                    .apply();
            setting_type_string.setText(client_set.getName());
        });
        builder.setNegativeButton(R.string.text_cancel, null);
        builder.show();
    }

    private void qualitySetting(){
        quality_item = ConfigManager.checkQuality(Setting.this);
        ArrayList<QualityItem> qualities = ConfigManager.getQualities();
        String[] quality_description = new String[qualities.size()];
        for (int index = 0; index < qualities.size(); index++){
            quality_description[index] = qualities.get(index).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
        builder.setTitle(R.string.title_setting_quality);
        builder.setSingleChoiceItems(quality_description, quality_item.getIndex(),
                (dialog, which) -> quality_item = qualities.get(which));
        builder.setPositiveButton(R.string.text_ok, (dialog, which) -> {
            MyLog.d(Setting.class, quality_item.getQuality());
            sharedPreferences.edit()
                    .putInt("quality", quality_item.getQuality())
                    .apply();
            qualitySettingLoad();
        });
        builder.setNegativeButton(R.string.text_cancel, null);
        builder.show();
    }

    private void autoStartSetting(boolean needAutoStart){
        sharedPreferences.edit()
                .putBoolean("task_auto_start", needAutoStart)
                .apply();
    }

    @SuppressWarnings("SameParameterValue")
    private void taskCountSetting(int min, int max){
        task_count = ConfigManager.checkTaskCount(Setting.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
        View dialog_setting_task_count = LayoutInflater.from(Setting.this).inflate(
                R.layout.dialog_setting_task_count, null, false
        );
        builder.setTitle(R.string.title_setting_task);
        builder.setView(dialog_setting_task_count);
        builder.setPositiveButton(R.string.text_ok, (dialog, which) -> {
            sharedPreferences.edit()
                    .putInt("task_parallel_count", task_count)
                    .apply();
            taskCountLoad();
        });
        builder.setNegativeButton(R.string.text_cancel, null);
        builder.show();
        AppCompatSeekBar dialog_setting_task_seek = dialog_setting_task_count.findViewById(R.id.dialog_setting_task_seek);
        dialog_setting_task_seek.setMin(min);
        dialog_setting_task_seek.setMax(max);
        dialog_setting_task_seek.setProgress(task_count);
        TextView dialog_setting_task_show = dialog_setting_task_count.findViewById(R.id.dialog_setting_task_show);
        dialog_setting_task_show.setText(String.valueOf(task_count));
        dialog_setting_task_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                task_count = progress;
                dialog_setting_task_show.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        TextView dialog_setting_task_min = dialog_setting_task_count.findViewById(R.id.dialog_setting_task_min);
        dialog_setting_task_min.setText(String.valueOf(min));
        TextView dialog_setting_task_max = dialog_setting_task_count.findViewById(R.id.dialog_setting_task_max);
        dialog_setting_task_max.setText(String.valueOf(max));
    }

    private void qualitySettingLoad(){
        setting_quality_string.setText(quality_item.getName());
    }

    private void locationSettingLoad(){
        setting_location_string.setText(ConfigManager.getDownloadDir(Setting.this));
    }

    private void taskCountLoad(){
        setting_task_count_string.setText(String.format(
                getString(R.string.text_setting_task_show), ConfigManager.checkTaskCount(Setting.this)
        ));
    }

    private void taskAutoStartLoad(){
        setting_auto_start.setChecked(ConfigManager.checkTaskAutoStart(Setting.this));
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
        locationSettingLoad();
    }
}

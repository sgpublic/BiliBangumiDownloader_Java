package com.sgpublic.bilidownload.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.sgpublic.bilidownload.BaseService.BaseActivity;
import com.sgpublic.bilidownload.BaseService.ConfigManager;
import com.sgpublic.bilidownload.BaseService.ConfigManager.ClientItem;
import com.sgpublic.bilidownload.BaseService.ConfigManager.QualityItem;
import com.sgpublic.bilidownload.R;

import java.util.ArrayList;

public class Setting extends BaseActivity {
    private TextView setting_type_string;
    private TextView setting_location_string;
    private TextView setting_quality_string;

    private ClientItem client_set;
    private QualityItem quality_item;

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

        Toolbar setting_toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(setting_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_mine_setting);
        }

        ArrayList<ClientItem> clientItems = ConfigManager.getInstalledClients(Setting.this);
        LinearLayout setting_type = findViewById(R.id.setting_type);
        String type_string;
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

        locationSettingLoad();
        findViewById(R.id.setting_location).setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, Explore.class);
            Setting.this.startActivityForResult(intent, 0);
        });

        quality_item = ConfigManager.checkQuality(Setting.this);
        setting_quality_string.setText(quality_item.getName());
        findViewById(R.id.setting_quality).setOnClickListener(v -> qualitySetting());
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

    public void qualitySetting(){
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
            sharedPreferences.edit()
                    .putInt("quality", quality_item.getQuality())
                    .apply();
            qualitySettingLoad();
        });
        builder.setNegativeButton(R.string.text_cancel, null);
        builder.show();
    }

    public void qualitySettingLoad(){
        setting_quality_string.setText(quality_item.getName());
    }

    public void locationSettingLoad(){
        String default_dir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            default_dir = "/storage/emulated/0/Download/";
        } else {
            default_dir = "/storage/emulated/0/Android/data/";
        }
        setting_location_string.setText(sharedPreferences.getString("location", default_dir));
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

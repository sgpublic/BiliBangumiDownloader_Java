package com.sgpublic.bilidownload.Activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.sgpublic.bilidownload.BaseService.BaseActivity;
import com.sgpublic.bilidownload.R;
import com.sgpublic.bilidownload.UIHelper.ExploreFolderAdapter;
import com.sgpublic.bilidownload.UIHelper.ExploreFolderItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Explore extends BaseActivity {
    private TextView explore_path_now;
    private ImageView explore_null;
    private ListView explore_list;
    private ImageView explore_change;

    private String explore_path_string;

    private final static String path_storage = "/storage/emulated/0/";
    private final static String path_sdcard = "/storage/sdcard0/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String default_dir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            default_dir = "/storage/emulated/0/Download/";
        } else {
            default_dir = "/storage/emulated/0/Android/data/";
        }
        explore_path_string = sharedPreferences.getString("location", default_dir);

        loadPath(explore_path_string);
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();
        setContentView(R.layout.activity_explore);

        Toolbar toolbar = findViewById(R.id.explore_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_explore);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        explore_path_now = findViewById(R.id.explore_path_now);
        explore_null = findViewById(R.id.explore_null);
        explore_list = findViewById(R.id.explore_list);

        explore_list.setOnItemClickListener((parent, view, position, id) -> {
            final TextView textView = view.findViewById(R.id.item_explore_title);
            new Handler().postDelayed(() -> Explore.this.loadPath(explore_path_string + textView.getText().toString() + "/"), 100);
        });

        findViewById(R.id.explore_choose).setOnClickListener(v -> Explore.this.onResult(explore_path_now.getText().toString()));

        findViewById(R.id.explore_cancel).setOnClickListener(v -> Explore.this.onResult());

        explore_change = findViewById(R.id.explore_change);
        explore_change.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Explore.this, explore_change);
            popupMenu.getMenuInflater().inflate(R.menu.explore_change, popupMenu.getMenu());
            popupMenu.getMenu().getItem(0).setVisible(
                    new File(path_storage).exists()
            );
            popupMenu.getMenu().getItem(1).setVisible(
                    new File(path_sdcard).exists()
            );
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.explore_change_storage:
                        Explore.this.loadPath(path_storage);
                        break;
                    case R.id.explore_change_sdcard:
                        Explore.this.loadPath(path_sdcard);
                        break;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private void loadPath(String path_need) {
        explore_path_now.setText(path_need);
        explore_path_string = path_need;
        File file = new File(path_need);
        File[] files = file.listFiles();
        ArrayList<ExploreFolderItem> list = new ArrayList<>();
        if (files != null) {
            List<File> fileList = Arrays.asList(files);
            Collections.sort(fileList, (o1, o2) -> {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            });
            for (File file_index : files) {
                String file_name = file_index.getName();
                if (file_index.isDirectory() && !file_name.substring(0, 1).equals(".")) {
                    list.add(new ExploreFolderItem(
                            file_name,
                            file_index.lastModified()
                    ));
                }
            }
        }
        if (list.size() == 0) {
            explore_null.setVisibility(View.VISIBLE);
        } else {
            explore_null.setVisibility(View.GONE);
        }
        explore_list.setAdapter(new ExploreFolderAdapter(
                Explore.this, R.layout.item_explore_list, list
        ));
    }

    @Override
    public void onBackPressed() {
        if (explore_path_string.equals(path_sdcard) || explore_path_string.equals(path_storage)) {
            onResult();
        } else {
            File file = new File(explore_path_string);
            String path_parent = file.getParent();
            if (!(path_parent != null && path_parent.substring(path_parent.length()).equals("/"))) {
                path_parent = path_parent + "/";
            }
            loadPath(path_parent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onResult();
        }
        return true;
    }

    private void onResult() {
        setResult(0);
        finish();
    }

    private void onResult(String path) {
        sharedPreferences.edit()
                .putString("location", path)
                .apply();
        onResult();
    }
}

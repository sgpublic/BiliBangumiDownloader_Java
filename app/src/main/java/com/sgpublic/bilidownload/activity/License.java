package com.sgpublic.bilidownload.activity;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBar;

import com.sgpublic.bilidownload.base.BaseActivity;
import com.sgpublic.bilidownload.R;
import com.sgpublic.bilidownload.ui.LicenseListAdapter;
import com.sgpublic.bilidownload.ui.LicenseListItem;

import java.util.ArrayList;

public class License extends BaseActivity {
    private ListView license_list;
    private Toolbar license_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(license_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_about_license);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        loadLicense();
    }

    private void loadLicense(){
        ArrayList<LicenseListItem> arrayList = new ArrayList<>();
        arrayList.add(new LicenseListItem(
                "BannerViewPager",
                "Android，Base on ViewPager2. 这可能是全网最好用的ViewPager轮播图。简单、高效，一行代码实现循环轮播，一屏三页任意变，指示器样式任你挑。",
                "zhpanvip",
                "https://github.com/zhpanvip/BannerViewPager/blob/master/LICENSE"
        ));
        arrayList.add(new LicenseListItem(
                "CircleImageView",
                "A circular ImageView for Android.",
                "hdodenhof",
                "https://github.com/hdodenhof/CircleImageView/blob/master/LICENSE.txt"
        ));
        arrayList.add(new LicenseListItem(
                "glide",
                "An image loading and caching library for Android focused on smooth scrolling.",
                "bumptech",
                "https://github.com/bumptech/glide/blob/master/LICENSE"
        ));
        arrayList.add(new LicenseListItem(
                "glide-transformations",
                "An Android transformation library providing a variety of image transformations for Glide.",
                "wasabeef",
                "https://github.com/wasabeef/glide-transformations/blob/master/LICENSE"
        ));
        arrayList.add(new LicenseListItem(
                "okhttp",
                "Square’s meticulous HTTP client for Java and Kotlin.",
                "square",
                "https://github.com/square/okhttp/blob/master/LICENSE.txt"
        ));
        license_list.setAdapter(new LicenseListAdapter(
                License.this, R.layout.item_license_list, arrayList
        ));
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();
        setContentView(R.layout.activity_license);

        license_list = findViewById(R.id.license_list);
        license_toolbar = findViewById(R.id.license_toolbar);
    }
}
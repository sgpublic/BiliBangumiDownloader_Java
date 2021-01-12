package com.sgpublic.bilidownload.UIHelper;


import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SeasonPagerAdapter extends PagerAdapter {
    private final ArrayList<View> view_list;
    private final ArrayList<String> tab_titles;

    public SeasonPagerAdapter(ArrayList<View> view_list, ArrayList<String> tab_titles) {
        this.view_list = view_list;
        this.tab_titles = tab_titles;
    }

    @Override
    public int getCount() {
        return view_list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(view_list.get(position));
        return view_list.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(view_list.get(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tab_titles.get(position);
    }
}

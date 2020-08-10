package com.sgpublic.bilidownload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.appcompat.app.ActionBar;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.sgpublic.bilidownload.BangumeAPI.FollowsHelper;
import com.sgpublic.bilidownload.BaseService.BaseActivity;
import com.sgpublic.bilidownload.DataHelper.FollowData;
import com.sgpublic.bilidownload.UIHelper.ObservableScrollView;

public class OtherFollows extends BaseActivity {
    private ImageView others_follow_end;
    private ImageView others_load_state;
    private GridLayout others_grid;
    private LinearLayout others_placeholder;
    private ObservableScrollView others_base;
    private SwipeRefreshLayout others_refresh;

    private String access_key;
    private long mid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        access_key = sharedPreferences.getString("access_key", "");
        mid = sharedPreferences.getLong("mid", 0L);

        others_base.setVisibility(View.INVISIBLE);
        startOnLoadingState(others_load_state);
        getFollowData(1);
    }

    private int list_row_size;
    private boolean scroll_to_end = false;

    private void setGrid(FollowData[] data_array, final int has_next) {
        if (has_next == 0) {
            stopOnLoadingState();
            others_follow_end.setImageResource(R.drawable.pic_nomore);
        } else {
            startOnLoadingState(others_follow_end);
        }
        float list_row_add = data_array.length / 3;
        int row_count = (int) list_row_add;
        if (list_row_add * 3 != data_array.length) {
            row_count = row_count + 1;
        }
        int list_row_size_old = list_row_size;
        list_row_size = list_row_size + row_count;
        others_grid.setRowCount(list_row_size);
        others_grid.setColumnCount(3);

        int view_width = (getResources().getDisplayMetrics().widthPixels - dip2px(OtherFollows.this, 16)) / 3;
        int image_height = (view_width - dip2px(OtherFollows.this, 12)) / 3 * 4;
        int view_height = image_height + dip2px(OtherFollows.this, 38);

        int data_info_index = 0;
        for (final FollowData data_info : data_array) {
            View item_others_follow = LayoutInflater.from(OtherFollows.this).inflate(R.layout.item_bangume_follow, others_grid, false);
            TextView follow_content = item_others_follow.findViewById(R.id.follow_content);
            follow_content.setText(data_info.title);

            TextView item_follow_badges = item_others_follow.findViewById(R.id.item_follow_badges);
            if (data_info.badge.equals("")) {
                item_follow_badges.setVisibility(View.GONE);
            } else {
                item_follow_badges.setVisibility(View.VISIBLE);
                item_follow_badges.setText(data_info.badge);
            }

            ImageView follow_image_placeholder = item_others_follow.findViewById(R.id.follow_image_placeholder);
            ImageView follow_image = item_others_follow.findViewById(R.id.follow_image);
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.pic_doing_v)
                    .error(R.drawable.pic_load_failed)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
            Glide.with(OtherFollows.this)
                    .load(data_info.cover)
                    .apply(requestOptions)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            follow_image_placeholder.animate().alpha(0f).setDuration(400).setListener(null);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    follow_image_placeholder.setVisibility(View.GONE);
                                    follow_image.setVisibility(View.VISIBLE);
                                    follow_image.animate().alpha(1f).setDuration(400).setListener(null);
                                }
                            }, 400);
                            return false;
                        }
                    })
                    //.transition(DrawableTransitionOptions.withCrossFade())
                    .into(follow_image);
            follow_image.getLayoutParams().height = image_height;

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();

            params.rowSpec = GridLayout.spec(data_info_index / 3 + list_row_size_old);
            params.columnSpec = GridLayout.spec(data_info_index % 3);

            params.width = view_width;
            params.height = view_height;

            item_others_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onGetSeason(data_info.title, data_info.season_id, data_info.cover);
                }
            });

            others_grid.addView(item_others_follow, params);
            data_info_index = data_info_index + 1;
        }
        others_base.setScrollViewListener(new ObservableScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (y == 0) {
                    others_refresh.setEnabled(true);
                } else {
                    others_refresh.setEnabled(false);
                }
                if (others_placeholder.getHeight() > y + others_base.getHeight()) {
                    scroll_to_end = false;
                } else if (!scroll_to_end) {
                    scroll_to_end = true;
                    if (has_next == 1) {
                        getFollowData(list_row_size / 6 + 1);
                    }
                }
            }
        });
    }

    private void getFollowData(final int page_index) {
        FollowsHelper helper = new FollowsHelper(this, access_key);
        helper.getFollows(mid, page_index, 0, new FollowsHelper.Callback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                onToast(OtherFollows.this, R.string.error_bangumi_load, message, code);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            stopOnLoadingState();
                            others_load_state.setImageResource(R.drawable.pic_load_failed);
                        } catch (NullPointerException ignored) {
                        }
                    }
                });
                saveExplosion(e, code);
            }

            @Override
            public void onResult(final FollowData[] followData, final int has_next) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopOnLoadingState();
                        try {
                            if (followData.length == 0) {
                                others_load_state.setImageResource(R.drawable.pic_null);
                            } else {
                                others_load_state.setVisibility(View.INVISIBLE);
                                others_base.setVisibility(View.VISIBLE);
                                if (page_index == 1) {
                                    if (others_refresh.isRefreshing()) {
                                        others_refresh.setRefreshing(false);
                                    }
                                    others_grid.removeAllViews();
                                }
                                setGrid(followData, has_next);
                            }
                        } catch (NullPointerException ignored) {
                        }
                    }
                });
            }
        });
    }

    private void onGetSeason(String title, long sid, String cover_url) {
        Intent intent = new Intent(OtherFollows.this, Season.class);
        intent.putExtra("season_id", sid);
        intent.putExtra("cover_url", cover_url);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();

        setContentView(R.layout.activity_other_follows);

        others_follow_end = findViewById(R.id.others_follow_end);
        others_load_state = findViewById(R.id.others_load_state);
        others_grid = findViewById(R.id.others_grid);
        others_base = findViewById(R.id.others_base);
        others_refresh = findViewById(R.id.others_refresh);
        others_placeholder = findViewById(R.id.others_placeholder);

        Toolbar others_toolbar = findViewById(R.id.others_toolbar);
        setSupportActionBar(others_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_mine_other_bangumi);
        }

        others_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list_row_size = 0;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getFollowData(1);
                    }
                }, 1000);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        stopOnLoadingState();
        super.onDestroy();
    }
}

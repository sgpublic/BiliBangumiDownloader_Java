package com.sgpublic.bilidownload.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.sgpublic.bilidownload.BangumiAPI.SeasonHelper;
import com.sgpublic.bilidownload.BaseStation.BaseActivity;
import com.sgpublic.bilidownload.Unit.ConfigManager;
import com.sgpublic.bilidownload.DataItem.Episode.InfoData;
import com.sgpublic.bilidownload.DataItem.Episode.QualityData;
import com.sgpublic.bilidownload.DataItem.SeasonData;
import com.sgpublic.bilidownload.DataItem.SeriesData;
import com.sgpublic.bilidownload.R;
import com.sgpublic.bilidownload.UI.BlurHelper;
import com.sgpublic.bilidownload.UI.SeasonPagerAdapter;
import com.sgpublic.bilidownload.Unit.CrashHandler;
import com.sgpublic.bilidownload.Unit.DownloadTaskManager;

import java.util.ArrayList;

//import com.umeng.analytics.MobclickAgent;

public class Season extends BaseActivity {
    private int is_vip;

//    private int episode_download_count = 0;

    private int[] quality_access_int;
    private ArrayList<String> quality_access_string;

    private ArrayList<InfoData> episodeData;
    private SeasonData seasonData;
    private SeriesData season_info;

    private ImageView season_loading;
    private ViewPager season_viewpager;
    private TextView season_rating_null;
    private TextView season_rating_string;
    private RatingBar season_rating_star;
    private TextView season_content;
    private TextView season_alias;
    private LinearLayout season_alias_base;
    private TextView season_styles;
    private LinearLayout season_styles_base;
    private LinearLayout season_alias_styles_base;
    private TextView season_actors;
    private LinearLayout season_actors_base;
    private TextView season_stuff;
    private LinearLayout season_stuff_base;
    private TextView season_evaluate;
    private LinearLayout season_evaluate_base;
    private GridLayout season_series;
    private LinearLayout season_series_base;
    private Spinner season_quality;
    private GridLayout season_grid;

    private LinearLayout season_episode_list;
    private ImageView season_no_episode;

    private ArrayList<String> tab_titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        season_info = new SeriesData();
        season_info.season_id = intent.getLongExtra("season_id", 0);
        season_info.cover = intent.getStringExtra("cover_url");
        season_info.title = intent.getStringExtra("title");
        super.onCreate(savedInstanceState);

        String access_key = sharedPreferences.getString("access_key", "");
        is_vip = sharedPreferences.getInt("vip_state", 0);

        SeasonHelper helper = new SeasonHelper(Season.this, access_key);
        helper.getInfoBySid(season_info.season_id, new SeasonHelper.Callback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                onToast(Season.this, R.string.error_bangumi_load, message, code);
                runOnUiThread(() -> {
                    stopOnLoadingState();
                    season_loading.setImageResource(R.drawable.pic_load_failed);
                });
                CrashHandler.saveExplosion(Season.this, e, code);
            }

            @Override
            public void onResult(ArrayList<InfoData> episodeData, SeasonData seasonData) {
                Season.this.episodeData = episodeData;
                Season.this.seasonData = seasonData;
                Season.this.season_info = seasonData.base_info;
                runOnUiThread(() -> new Handler().postDelayed(
                        () -> onSetupSeasonInfo(), 500
                ));
            }
        });
    }

    @Deprecated
    private void handleSeasonInfoAction() {
//        Map<String, Object> season_view = new HashMap<>();
//        season_view.put("name", season_title + " (" + season_id + ")");
//        season_view.put("area", season_area == 0 ? "港澳台" : "大陆");
//        MobclickAgent.onEventObject(Season.this, "season_view", season_view);
    }

    private void onSetupSeasonInfo() {
        if (seasonData.qualities != null) {
            this.quality_access_int = new int[seasonData.qualities.size()];
            this.quality_access_string = new ArrayList<>();
            for (int qd_index = 0; qd_index < seasonData.qualities.size(); qd_index++) {
                if (is_vip == 0 && qd_index == 0) {
                    continue;
                }
                QualityData qualityData_index = seasonData.qualities.get(qd_index);
                quality_access_string.add(qualityData_index.getDescription());
                quality_access_int[qd_index] = qualityData_index.getQuality();
            }
        }
        runOnUiThread(() -> {
            setAnimateState(false, 300, season_loading, this::stopOnLoadingState);
            onSetupPager(R.layout.pager_info, R.layout.pager_download);
            new Handler().postDelayed(() -> {
                try {
                    if (episodeData.size() > 0) {
                        season_episode_list.setVisibility(View.VISIBLE);
                        season_no_episode.setVisibility(View.GONE);
                    } else {
                        season_episode_list.setVisibility(View.GONE);
                        season_no_episode.setVisibility(View.VISIBLE);
                    }
                    onSeasonInfoLoad();
                    onEpisodeLoad();
                } catch (NullPointerException ignore) {}
            }, 310);
        });
    }

    private void onSeasonInfoLoad() {
        if (seasonData.rating == 0) {
            season_rating_string.setVisibility(View.INVISIBLE);
            season_rating_null.setVisibility(View.VISIBLE);
            season_rating_star.setProgress(0);
        } else {
            season_rating_null.setVisibility(View.INVISIBLE);
            season_rating_string.setVisibility(View.VISIBLE);
            season_rating_string.setText(String.valueOf(seasonData.rating));
            season_rating_star.setProgress((int) Math.round(seasonData.rating));
        }
        season_content.setText(seasonData.description);
        if (seasonData.alias.equals("")) {
            season_alias_base.setVisibility(View.GONE);
        } else {
            season_alias.setText(seasonData.alias);
        }
        if (seasonData.styles.equals("")) {
            season_styles_base.setVisibility(View.GONE);
        } else {
            season_styles.setText(seasonData.styles);
        }
        if (seasonData.styles.equals("") && seasonData.alias.equals("")) {
            season_alias_styles_base.setVisibility(View.GONE);
        }
        if (seasonData.actors.equals("")) {
            season_actors_base.setVisibility(View.GONE);
        } else {
            season_actors.setText(seasonData.actors);
        }
        if (seasonData.staff.equals("")) {
            season_stuff_base.setVisibility(View.GONE);
        } else {
            season_stuff.setText(seasonData.staff);
        }
        if (seasonData.evaluate.equals("")) {
            season_evaluate_base.setVisibility(View.GONE);
        } else {
            season_evaluate.setText(seasonData.evaluate);
        }
        if (seasonData.series.size() == 0) {
            season_series_base.setVisibility(View.GONE);
        } else {
            float list_row_add = seasonData.series.size() / 3;
            int row_count = (int) list_row_add;
            if (list_row_add * 3 != seasonData.series.size()) {
                row_count = row_count + 1;
            }
            season_series.setRowCount(row_count);
            season_series.setColumnCount(3);

            int view_width = (getResources().getDisplayMetrics().widthPixels - dip2px(Season.this, 56)) / 3;
            int image_height = (view_width - dip2px(Season.this, 12)) / 3 * 4;
            int view_height = image_height + dip2px(Season.this, 38);

            int data_info_index = 0;

            boolean night_mode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

            for (SeriesData data : seasonData.series) {
                View item_bangume_follow = LayoutInflater.from(Season.this).inflate(R.layout.item_bangumi_follow, season_series, false);
                TextView follow_content = item_bangume_follow.findViewById(R.id.follow_content);
                follow_content.setText(data.title);

                CardView episode_vip_background = item_bangume_follow.findViewById(R.id.item_follow_badges_background);
                if (data.badge.equals("")){
                    episode_vip_background.setVisibility(View.GONE);
                } else {
                    episode_vip_background.setVisibility(View.VISIBLE);
                    if (night_mode){
                        episode_vip_background.setCardBackgroundColor(data.badge_color_night);
                    } else {
                        episode_vip_background.setCardBackgroundColor(data.badge_color);
                    }
                    TextView item_follow_badges = item_bangume_follow.findViewById(R.id.item_follow_badges);
                    item_follow_badges.setText(data.badge);
                }

                ImageView follow_image_placeholder = item_bangume_follow.findViewById(R.id.follow_image_placeholder);
                ImageView follow_image = item_bangume_follow.findViewById(R.id.follow_image);
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.pic_doing_v)
                        .error(R.drawable.pic_load_failed)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
                Glide.with(Season.this)
                        .load(data.cover)
                        .apply(requestOptions)
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                follow_image_placeholder.animate().alpha(0f).setDuration(400).setListener(null);
                                new Handler().postDelayed(() -> {
                                    follow_image_placeholder.setVisibility(View.GONE);
                                    follow_image.setVisibility(View.VISIBLE);
                                    follow_image.animate().alpha(1f).setDuration(400).setListener(null);
                                }, 400);
                                return false;
                            }
                        })
                        //.transition(DrawableTransitionOptions.withCrossFade())
                        .into(follow_image);
                follow_image.getLayoutParams().height = image_height;

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();

                params.rowSpec = GridLayout.spec(data_info_index / 3);
                params.columnSpec = GridLayout.spec(data_info_index % 3);

                params.width = view_width;
                params.height = view_height;

                item_bangume_follow.setOnClickListener(v -> startActivity(
                        Season.this, data.title, data.season_id, data.cover
                ));

                season_series.addView(item_bangume_follow, params);
                data_info_index = data_info_index + 1;
            }
        }

        season_stuff.setOnClickListener(v ->
                season_stuff.setMaxLines(seasonData.staff_lines == season_stuff.getMaxLines() ? 3 : seasonData.staff_lines)
        );
        season_actors.setOnClickListener(v ->
                season_actors.setMaxLines(seasonData.actors_lines == season_actors.getMaxLines() ? 3 : seasonData.actors_lines)
        );
    }

    private void onEpisodeLoad() {
        if (episodeData.size() > 0) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    Season.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    quality_access_string
            );
            season_quality.setAdapter(arrayAdapter);
            int quality_set = getIndex(
                    quality_access_int,
                    sharedPreferences.getInt("quality", 80)
            );
            if (quality_set < 0) {
                quality_set = 0;
            }
            season_quality.setSelection(quality_set);

            int view_width = (getResources().getDisplayMetrics().widthPixels - dip2px(Season.this, 36)) / 2;
            int image_height = (view_width - dip2px(Season.this, 12)) / 8 * 5;
            int view_height = image_height + dip2px(Season.this, 60);

            float list_row_add = episodeData.size() / 2;
            int row_count = (int) list_row_add;
            if (list_row_add * 2 != episodeData.size()) {
                row_count = row_count + 1;
            }
            season_grid.setColumnCount(2);
            season_grid.setRowCount(row_count);

            boolean night_mode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

            for (int episode_index = 0; episode_index < episodeData.size(); episode_index++) {
                InfoData episodeData_index = episodeData.get(episode_index);
                View item_season_episode = LayoutInflater.from(this).inflate(R.layout.item_season_episode, season_grid, false);

                TextView episode_title = item_season_episode.findViewById(R.id.episode_title);
                episode_title.setText(episodeData_index.title);

                TextView episode_public_time = item_season_episode.findViewById(R.id.episode_public_time);
                episode_public_time.setText(String.format(
                        getResources().getString(R.string.text_episode_public_time),
                        episodeData_index.pub_real_time
                ));

                TextView episode_index_title = item_season_episode.findViewById(R.id.episode_index_title);
                String index_title = episodeData_index.index;
                try {
                    Float.parseFloat(index_title);
                    index_title = String.format(
                            getString(R.string.text_episode_index), index_title
                    );
                } catch (NumberFormatException ignore){ }
                episode_index_title.setText(index_title);

                CardView episode_vip_background = item_season_episode.findViewById(R.id.episode_vip_background);
                if (episodeData_index.badge.equals("")){
                    episode_vip_background.setVisibility(View.GONE);
                } else {
                    episode_vip_background.setVisibility(View.VISIBLE);
                    if (night_mode){
                        episode_vip_background.setCardBackgroundColor(episodeData_index.badge_color_night);
                    } else {
                        episode_vip_background.setCardBackgroundColor(episodeData_index.badge_color);
                    }
                    TextView episode_vip = item_season_episode.findViewById(R.id.episode_vip);
                    episode_vip.setText(episodeData_index.badge);
                }

                item_season_episode.setOnClickListener(v -> {
                    if (episodeData_index.payment == 13 && is_vip == 0) {
                        onToast(Season.this, R.string.text_episode_vip_needed);
                    } else if (ConfigManager.checkClient(Season.this) == null){
                        onToast(Season.this, R.string.text_episode_no_app_installed);
                    } else {
                        onSetupDownload(episodeData_index, (int)season_quality.getSelectedItemId());
                    }
                });

                ImageView episode_image = item_season_episode.findViewById(R.id.episode_image);
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.pic_doing_h)
                        .error(R.drawable.pic_load_failed)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
                Glide.with(Season.this)
                        .load(episodeData_index.cover)
                        .apply(requestOptions)
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                episode_image.setVisibility(View.VISIBLE);
                                episode_image.animate().alpha(1f).setDuration(400).setListener(null);
                                return false;
                            }
                        })
                        //.transition(DrawableTransitionOptions.withCrossFade())
                        .into(episode_image);
                episode_image.getLayoutParams().height = image_height;

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();

                params.rowSpec = GridLayout.spec(episode_index / 2);
                params.columnSpec = GridLayout.spec(episode_index % 2);

                params.width = view_width;
                params.height = view_height;

                season_grid.addView(item_season_episode, params);
            }
        }
    }


    private void onSetupDownload(InfoData infoData, int quality_index) {
        //TODO
        DownloadTaskManager helper = new DownloadTaskManager(Season.this, season_info);
        int quality = seasonData.qualities.get(quality_index).getQuality();
        helper.addDownloadTask(infoData, quality, new DownloadTaskManager.TaskCreateCallback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                onToast(Season.this, R.string.error_download_start, message, code);
            }

            @Override
            public void onTaskExist() {
                onToast(Season.this, R.string.error_download_task_exist);
            }

            @Override
            public void onResult() {
                onToast(Season.this, R.string.text_download_success);
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
    protected void onUiLoad() {
        super.onUiLoad();

        setContentView(R.layout.activity_season);

        season_loading = findViewById(R.id.season_loading);
        startOnLoadingState(season_loading);

        Toolbar season_toolbar = findViewById(R.id.season_toolbar);
        setSupportActionBar(season_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tab_titles = new ArrayList<>();
        tab_titles.add(getResources().getText(R.string.title_season_info).toString());
        tab_titles.add(getResources().getText(R.string.title_season_download).toString());

        season_viewpager = findViewById(R.id.season_viewpager);
        onSetupPager(R.layout.pager_placeholder, R.layout.pager_placeholder);

        TabLayout season_tab = findViewById(R.id.season_tab);
        season_tab.addTab(season_tab.newTab().setText(tab_titles.get(0)));
        season_tab.addTab(season_tab.newTab().setText(tab_titles.get(1)));
        season_tab.setupWithViewPager(season_viewpager);

        CollapsingToolbarLayout season_collapsing_toolbar = findViewById(R.id.season_collapsing_toolbar);
        season_collapsing_toolbar.setTitle(season_info.title);

        RequestOptions requestOptions = new RequestOptions()
                .error(R.drawable.pic_load_failed)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        ImageView season_cover_background = findViewById(R.id.season_cover_background);
        Glide.with(this)
                .load(season_info.cover)
                .apply(requestOptions)
                .apply(RequestOptions
                        .bitmapTransform(new BlurHelper())
                )
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        new Handler().postDelayed(() -> {
                            season_cover_background.setVisibility(View.VISIBLE);
                            season_cover_background.animate().alpha(1f).setDuration(400).setListener(null);
                        }, 400);
                        return false;
                    }
                })
                //.transition(DrawableTransitionOptions.withCrossFade())
                .into(season_cover_background);

        ImageView season_cover_placeholder = findViewById(R.id.season_cover_placeholder);
        ImageView season_cover = findViewById(R.id.season_cover);
        Glide.with(this)
                .load(season_info.cover)
                .apply(requestOptions
                        .placeholder(R.drawable.pic_doing_v)
                )
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        season_cover_placeholder.animate().alpha(0f).setDuration(400).setListener(null);
                        new Handler().postDelayed(() -> {
                            season_cover_placeholder.setVisibility(View.GONE);
                            season_cover.setVisibility(View.VISIBLE);
                            season_cover.animate().alpha(1f).setDuration(400).setListener(null);
                        }, 400);
                        return false;
                    }
                })
                //.transition(DrawableTransitionOptions.withCrossFade())
                .into(season_cover);
    }

    private void onSetupPager(int view1, int view2) {
        setAnimateState(false, 300, season_viewpager, () -> setAnimateState(true, 500, season_viewpager, () -> {
            ArrayList<View> view_list = new ArrayList<>();
            view_list.add(LayoutInflater.from(Season.this).inflate(view1, season_viewpager, false));
            view_list.add(LayoutInflater.from(Season.this).inflate(view2, season_viewpager, false));
            season_viewpager.setAdapter(new SeasonPagerAdapter(view_list, tab_titles));
            if (view1 != R.layout.pager_placeholder) {
                season_rating_null = findViewById(R.id.season_rating_null);
                season_rating_string = findViewById(R.id.season_rating_string);
                season_rating_star = findViewById(R.id.season_rating_start);
                season_content = findViewById(R.id.season_content);
                season_alias = findViewById(R.id.season_alias);
                season_alias_base = findViewById(R.id.season_alias_base);
                season_styles = findViewById(R.id.season_styles);
                season_styles_base = findViewById(R.id.season_styles_base);
                season_alias_styles_base = findViewById(R.id.season_alias_styles_base);
                season_actors = findViewById(R.id.season_actors);
                season_actors_base = findViewById(R.id.season_actors_base);
                season_stuff = findViewById(R.id.season_stuff);
                season_stuff_base = findViewById(R.id.season_stuff_base);
                season_evaluate = findViewById(R.id.season_evaluate);
                season_evaluate_base = findViewById(R.id.season_evaluate_base);
                season_series = findViewById(R.id.season_series);
                season_series_base = findViewById(R.id.season_series_base);
                season_quality = findViewById(R.id.season_quality);
                season_grid = findViewById(R.id.season_grid);
            }
            if (view2 != R.layout.pager_placeholder) {
                season_episode_list = findViewById(R.id.season_episode_list);
                season_no_episode = findViewById(R.id.season_no_episode);
            }
        }));
    }

    @Override
    protected void onDestroy() {
        stopOnLoadingState();

//        if (qualityData != null && episode_download_count != 0) {
//            Map<String, Object> episode_download = new HashMap<>();
//            episode_download.put("count", episode_download_count);
//            episode_download.put("quality", qualityData
//                    .get((int) season_quality.getSelectedItemId())
//                    .getDescription()
//            );
//            MobclickAgent.onEventObject(Season.this, "episode_download", episode_download);
//        }

        super.onDestroy();
    }

    public static void startActivity(Context context, String title, long sid, String cover_url){
        Intent intent = new Intent(context, Season.class);
        intent.putExtra("season_id", sid);
        intent.putExtra("cover_url", cover_url);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }
}

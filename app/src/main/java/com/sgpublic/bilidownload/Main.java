package com.sgpublic.bilidownload;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sgpublic.bilidownload.BangumeAPI.FollowsHelper;
import com.sgpublic.bilidownload.BaseService.UpdateHelper;
import com.sgpublic.bilidownload.DataHelper.FollowData;
import com.sgpublic.bilidownload.UIHelper.BannerItem;
import com.sgpublic.bilidownload.BaseService.BaseActivity;
import com.sgpublic.bilidownload.UIHelper.ObservableScrollView;
import com.sgpublic.bilidownload.UIHelper.SeasonBannerAdapter;
import com.zhpan.bannerview.BannerViewPager;
import com.zhpan.bannerview.constants.IndicatorGravity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.sgpublic.bilidownload.BaseService.ActivityController.finishAll;

public class Main extends BaseActivity {
    private View layout_bangume;
    private View layout_mine;
    private BannerViewPager<BannerItem, SeasonBannerAdapter> bangume_banner;
    private SwipeRefreshLayout bangume_refresh;
    private GridLayout bangume_follows;
    private ObservableScrollView bangume_base;
    private LinearLayout bangume_placeholder;
    private ImageView bangume_follow_end;
    private ImageView bangume_load_state;
    private CircleImageView mine_vip;
    private TextView mine_vip_string;

    private String access_key;
    private long mid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        access_key = sharedPreferences.getString("access_key", "");
        mid = sharedPreferences.getLong("mid", 0);

        setViewState(1);
        bangume_base.setVisibility(View.INVISIBLE);
        startOnLoadingState(bangume_load_state);
        getFollowData(1);

        onUpdate();
    }

    private void getFollowData(final int page_index) {
        FollowsHelper helper = new FollowsHelper(this, access_key);
        helper.getFollows(mid, page_index, new FollowsHelper.Callback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                onToast(Main.this, R.string.error_bangumi_load, message, code);
                runOnUiThread(() -> {
                    stopOnLoadingState();
                    bangume_load_state.setImageResource(R.drawable.pic_load_failed);
                    setRefreshState(false);
                });
                saveExplosion(e, code);
            }

            @Override
            public void onResult(final FollowData[] followData, final int has_next) {
                runOnUiThread(() -> {
                    stopOnLoadingState();
                    if (followData.length == 0) {
                        bangume_load_state.setImageResource(R.drawable.pic_null);
                        setRefreshState(false);
                    } else {
                        bangume_load_state.setVisibility(View.INVISIBLE);
                        bangume_base.setVisibility(View.VISIBLE);
                        if (page_index == 1) {
                            setupUserData(followData, has_next);
                        } else {
                            setGrid(followData, has_next);
                        }
                    }
                });
            }
        });
    }

    private ArrayList<BannerItem> banner_info_list;

    private void setupUserData(final FollowData[] data_array, final int has_next) {
        is_first_change = true;

        list_row_size = 0;
        banner_info_list = new ArrayList<>();

        for (FollowData data_info : data_array) {
            if (data_info.is_finish == 0 || data_info.new_ep_is_new == 1) {
                banner_info_list.add(new BannerItem(
                        Main.this,
                        data_info.new_ep_cover,
                        data_info.cover,
                        data_info.season_id,
                        data_info.title,
                        data_info.new_ep_index_show,
                        data_info.badge
                ));
                if (banner_info_list.size() > 7) {
                    break;
                }
            }
        }
        int banner_item_count = Math.min(data_array.length, 5);
        while (banner_info_list.size() < banner_item_count) {
            FollowData data_info = data_array[(int) (Math.random() * data_array.length)];
            if (data_info.is_finish == 1) {
                boolean is_equals = false;
                for (BannerItem item_index : banner_info_list) {
                    if (item_index.getSeasonId() == data_info.season_id) {
                        is_equals = true;
                        break;
                    }
                }
                if (!is_equals) {
                    BannerItem item = new BannerItem(
                            Main.this,
                            data_info.new_ep_cover,
                            data_info.cover,
                            data_info.season_id,
                            data_info.title,
                            data_info.new_ep_index_show,
                            data_info.badge
                    );
                    banner_info_list.add(item);
                }
            }
        }

        runOnUiThread(() -> {
            bangume_follows.removeAllViews();
            bangume_banner.showIndicator(true);
            bangume_banner.setIndicatorGravity(IndicatorGravity.CENTER);
            bangume_banner.setOnPageClickListener(i -> {
                BannerItem data_info = banner_info_list.get(i);
                onGetSeason(data_info.getTitle(), data_info.getSeasonId(), data_info.getSeasonCover());
            });
            bangume_banner.setHolderCreator(SeasonBannerAdapter::new);
            bangume_banner.create(banner_info_list);

            CircleImageView mine_avatar = Main.this.findViewById(R.id.mine_avatar);
            Glide.with(Main.this)
                    .load(sharedPreferences.getString("face", ""))
                    .into(mine_avatar);

            int vip_type = sharedPreferences.getInt("vip_type", 0);
            if (vip_type == 0) {
                mine_vip.setVisibility(View.GONE);
                mine_vip_string.setVisibility(View.GONE);
            } else {
                mine_vip.setVisibility(View.VISIBLE);
                mine_vip_string.setVisibility(View.VISIBLE);
                if (vip_type == 2) {
                    mine_vip_string.setText(R.string.text_mine_vip_year);
                } else {
                    mine_vip_string.setText(R.string.text_mine_vip_month);
                }
            }

            int[] image_levels = {
                    R.drawable.ic_level_0,
                    R.drawable.ic_level_1,
                    R.drawable.ic_level_2,
                    R.drawable.ic_level_3,
                    R.drawable.ic_level_4,
                    R.drawable.ic_level_5,
                    R.drawable.ic_level_6,
            };
            ImageView mine_level = Main.this.findViewById(R.id.mine_level);
            mine_level.setImageResource(image_levels[sharedPreferences.getInt("level", 0)]);

            TextView mine_name = Main.this.findViewById(R.id.mine_name);
            mine_name.setText(sharedPreferences.getString("name", "哔哩番剧用户"));

            TextView mine_sign = Main.this.findViewById(R.id.mine_sign);
            mine_sign.setText(sharedPreferences.getString("sign", "这个人很懒，什么也没有留下。"));

            int[] genders = {
                    R.drawable.ic_gender_unknown,
                    R.drawable.ic_gender_male,
                    R.drawable.ic_gender_female
            };
            ImageView mine_gender = Main.this.findViewById(R.id.mine_gender);
            mine_gender.setImageResource(genders[sharedPreferences.getInt("sex", 0)]);

            Main.this.setRefreshState(false);

            Main.this.setGrid(data_array, has_next);
        });
    }

    private int list_row_size;
    private boolean scroll_to_end = false;

    private void setGrid(FollowData[] data_array, final int has_next) {
        if (has_next == 0) {
            stopOnLoadingState();
            bangume_follow_end.setImageResource(R.drawable.pic_nomore);
        } else {
            startOnLoadingState(bangume_follow_end);
        }
        float list_row_add = data_array.length / 3;
        int row_count = (int) list_row_add;
        if (list_row_add * 3 != data_array.length) {
            row_count = row_count + 1;
        }
        int list_row_size_old = list_row_size;
        list_row_size = list_row_size + row_count;
        bangume_follows.setRowCount(list_row_size);
        bangume_follows.setColumnCount(3);

        int view_width = (getResources().getDisplayMetrics().widthPixels - dip2px(Main.this, 20)) / 3;
        int image_height = (view_width - dip2px(Main.this, 12)) / 3 * 4;
        int view_height = image_height + dip2px(Main.this, 38);

        int data_info_index = 0;
        for (FollowData data_info : data_array) {
            View item_bangume_follow = LayoutInflater.from(Main.this).inflate(R.layout.item_bangume_follow, bangume_follows, false);
            TextView follow_content = item_bangume_follow.findViewById(R.id.follow_content);
            follow_content.setText(data_info.title);

            TextView item_follow_badges = item_bangume_follow.findViewById(R.id.item_follow_badges);
            if (data_info.badge.equals("")) {
                item_follow_badges.setVisibility(View.GONE);
            } else {
                item_follow_badges.setVisibility(View.VISIBLE);
                item_follow_badges.setText(data_info.badge);
            }

            ImageView follow_image_placeholder = item_bangume_follow.findViewById(R.id.follow_image_placeholder);
            ImageView follow_image = item_bangume_follow.findViewById(R.id.follow_image);
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.pic_doing_v)
                    .error(R.drawable.pic_load_failed)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
            Glide.with(Main.this)
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

            params.rowSpec = GridLayout.spec(data_info_index / 3 + list_row_size_old);
            params.columnSpec = GridLayout.spec(data_info_index % 3);

            params.width = view_width;
            params.height = view_height;

            item_bangume_follow.setOnClickListener(v ->
                    Main.this.onGetSeason(data_info.title, data_info.season_id, data_info.cover)
            );

            bangume_follows.addView(item_bangume_follow, params);
            data_info_index = data_info_index + 1;
        }

        bangume_base.setScrollViewListener((scrollView, x, y, oldx, oldy) -> {
            //if (!page_scrolling && y == 0) {
            //    bangume_refresh.setEnabled(true);
            //} else {
            //    bangume_refresh.setEnabled(false);
            //}
            if (bangume_placeholder.getHeight() > y + bangume_base.getHeight()) {
                scroll_to_end = false;
            } else if (!scroll_to_end) {
                scroll_to_end = true;
                if (has_next == 1) {
                    Main.this.getFollowData(list_row_size / 6 + 1);
                }
            }
        });
    }

    private void onGetSeason(String title, long sid, String cover_url) {
        Intent intent = new Intent(Main.this, Season.class);
        intent.putExtra("season_id", sid);
        intent.putExtra("cover_url", cover_url);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    boolean is_first_change = true;

    @Override
    protected void onUiLoad() {
        super.onUiLoad();

        setContentView(R.layout.activity_main);

        bangume_refresh = findViewById(R.id.bangume_refresh);
        bangume_follows = findViewById(R.id.bangume_follows);
        bangume_banner = findViewById(R.id.bangume_banner);
        bangume_follow_end = findViewById(R.id.bangume_follow_end);
        bangume_base = findViewById(R.id.bangume_base);
        bangume_placeholder = findViewById(R.id.bangume_placeholder);
        bangume_load_state = findViewById(R.id.bangume_load_state);
        mine_vip_string = findViewById(R.id.mine_vip_string);
        mine_vip = findViewById(R.id.mine_vip);
        layout_bangume = findViewById(R.id.layout_bangume);
        layout_mine = findViewById(R.id.layout_mine);

        findViewById(R.id.mine_more).setOnClickListener(v -> Main.this.onToast(Main.this, R.string.text_mine_developing));

        findViewById(R.id.mine_other_bangume).setOnClickListener(v -> {
            Intent intent = new Intent(Main.this, OtherFollows.class);
            Main.this.startActivity(intent);
        });

        findViewById(R.id.mine_logout).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
            builder.setTitle(R.string.title_mine_logout);
            builder.setMessage(R.string.text_mine_logout_check);
            builder.setPositiveButton(R.string.text_ok, (dialog, witch) -> {
                sharedPreferences.edit()
                        .putBoolean("is_login", false)
                        .apply();
                Intent intent = new Intent(Main.this, Login.class);
                intent.putExtra("grand", 1);
                Main.this.startActivity(intent);
            });
            builder.setNegativeButton(R.string.text_cancel, null);
            builder.show();
        });

        findViewById(R.id.mine_about).setOnClickListener(v -> {
            Intent intent = new Intent(Main.this, About.class);
            startActivity(intent);
        });

        findViewById(R.id.mine_setting).setOnClickListener(v -> {
            Intent intent = new Intent(Main.this, Setting.class);
            startActivity(intent);
        });

        findViewById(R.id.bangume_search).setOnClickListener(v -> {
            Intent intent = new Intent(Main.this, Search.class);
            startActivity(intent);
        });

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_bangume:
                    Main.this.setViewState(1);
                    break;
                case R.id.navigation_mine:
                    Main.this.setViewState(2);
                    break;
            }
            return true;
        });

        bangume_refresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> getFollowData(1), 1000));
    }

    private int viewNowIndex = 0;

    private void setViewState(final int viewIntoIndex) {
        runOnUiThread(() -> {
            if (viewIntoIndex != viewNowIndex) {
                if (layout_bangume.getVisibility() == View.VISIBLE) {
                    layout_bangume.animate().alpha(0f).setDuration(200).setListener(null);
                    layout_bangume.setVisibility(View.INVISIBLE);
                }
                if (layout_mine.getVisibility() == View.VISIBLE) {
                    layout_mine.animate().alpha(0f).setDuration(200).setListener(null);
                    layout_mine.setVisibility(View.INVISIBLE);
                }

                switch (viewIntoIndex) {
                    case 1:
                        layout_bangume.animate().alpha(1f).setDuration(200).setListener(null);
                        layout_bangume.setVisibility(View.VISIBLE);
                        //bangume_banner.setAutoPlay(true);
                        break;
                    case 2:
                        layout_mine.animate().alpha(1f).setDuration(200).setListener(null);
                        layout_mine.setVisibility(View.VISIBLE);
                        //bangume_banner.setAutoPlay(false);
                        break;
                }
            } else if (viewIntoIndex == 1) {
                setRefreshState(true);
                new Handler().postDelayed(() -> getFollowData(1), 1000);
            }
            viewNowIndex = viewIntoIndex;
        });
    }

    private void setRefreshState(boolean is_refresh) {
        bangume_refresh.setRefreshing(is_refresh);
    }

    private void onUpdate() {
        UpdateHelper helper = new UpdateHelper(Main.this);
        helper.getUpdate(0, new UpdateHelper.Callback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
            }

            @Override
            public void onUpToDate() {
            }

            @Override
            public void onUpdate(int force, String ver_name, String size_string, String changelog, String dl_url) {
                runOnUiThread(() -> {
                    int[] update_header = {
                            R.string.text_update_content,
                            R.string.text_update_content_force,
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                    builder.setTitle(R.string.title_update_get);
                    builder.setCancelable(force == 0);
                    builder.setMessage(String.format(Main.this.getString(update_header[force]), size_string) + "\n" +
                            Main.this.getString(R.string.text_update_version) + ver_name + "\n" +
                            Main.this.getString(R.string.text_update_changelog) + "\n" + changelog);
                    builder.setPositiveButton(R.string.text_ok, (dialog, which) -> {
                        Uri url = Uri.parse(dl_url);
                        DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request req = new DownloadManager.Request(url);
                        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        String apkName = Main.this.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".apk";
                        req.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, apkName);
                        req.setVisibleInDownloadsUi(true);
                        req.setTitle(Main.this.getString(R.string.title_update_download));
                        req.setMimeType("application/vnd.android.package-archive");
                        if (downloadManager != null) {
                            downloadManager.enqueue(req);
                        }
                    });
                    builder.setNegativeButton(R.string.text_cancel, (dialog, which) -> {
                        if (force == 1) {
                            finishAll();
                        }
                    });
                    builder.show();
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bangume_banner != null)
            bangume_banner.stopLoop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bangume_banner != null)
            bangume_banner.startLoop();
    }

    long last = -1;

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (last == -1) {
            Toast.makeText(this, "再点击一次退出", Toast.LENGTH_SHORT).show();
            last = now;
        } else {
            if ((now - last) < 2000) {
                stopOnLoadingState();
                finishAll();
            } else {
                last = now;
                Toast.makeText(this, "请再点击一次退出", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

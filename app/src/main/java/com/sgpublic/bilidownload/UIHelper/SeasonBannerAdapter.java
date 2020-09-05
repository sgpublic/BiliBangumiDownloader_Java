package com.sgpublic.bilidownload.UIHelper;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.sgpublic.bilidownload.R;
import com.zhpan.bannerview.holder.ViewHolder;

public class SeasonBannerAdapter implements ViewHolder<BannerItem> {

    @Override
    public int getLayoutId() {
        return R.layout.item_bangumi_banner;
    }

    @Override
    public void onBind(View itemView, BannerItem data, int position, int size) {
        ImageView banner_image_placeholder = itemView.findViewById(R.id.banner_image_placeholder);
        ImageView banner_image = itemView.findViewById(R.id.banner_image);
        ImageView banner_image_foreground = itemView.findViewById(R.id.banner_image_foreground);
        TextView item_banner_badges = itemView.findViewById(R.id.item_banner_badges);
        if (data.getBadges().equals("")) {
            item_banner_badges.setVisibility(View.GONE);
        } else {
            item_banner_badges.setVisibility(View.VISIBLE);
            item_banner_badges.setText(data.getBadges());
        }
        if (data.getBannerPath().equals("")) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.pic_doing_v)
                    .error(R.drawable.pic_load_failed)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
            Glide.with(data.getContext())
                    .load(data.getSeasonCover())
                    .apply(requestOptions)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            banner_image_placeholder.animate().alpha(0f).setDuration(400).setListener(null);
                            new Handler().postDelayed(() -> {
                                banner_image_placeholder.setVisibility(View.GONE);
                                banner_image_foreground.setVisibility(View.VISIBLE);
                                banner_image_foreground.animate().alpha(1f).setDuration(400).setListener(null);
                            }, 400);
                            return false;
                        }
                    })
                    .into(banner_image_foreground);
            Glide.with(data.getContext())
                    .load(data.getSeasonCover())
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
                            banner_image_placeholder.animate().alpha(0f).setDuration(400).setListener(null);
                            new Handler().postDelayed(() -> {
                                banner_image.setVisibility(View.VISIBLE);
                                banner_image.animate().alpha(1f).setDuration(400).setListener(null);
                            }, 400);
                            return false;
                        }
                    })
                    .into(banner_image);
        } else {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.pic_doing_h)
                    .error(R.drawable.pic_load_failed)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
            Glide.with(data.getContext())
                    .load(data.getBannerPath())
                    .apply(requestOptions)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            banner_image_placeholder.animate().alpha(0f).setDuration(400).setListener(null);
                            new Handler().postDelayed(() -> {
                                banner_image.setVisibility(View.VISIBLE);
                                banner_image.animate().alpha(1f).setDuration(400).setListener(null);
                            }, 400);
                            return false;
                        }
                    })
                    .into(banner_image);
        }
        TextView banner_content = itemView.findViewById(R.id.banner_content);
        banner_content.setText(data.getIndicatorText());
    }
}
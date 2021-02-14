package com.sgpublic.bilidownload.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.sgpublic.bilidownload.BangumiAPI.SearchHelper;
import com.sgpublic.bilidownload.BaseService.BaseActivity;
import com.sgpublic.bilidownload.DataItem.SearchData;
import com.sgpublic.bilidownload.R;
import com.sgpublic.bilidownload.UIHelper.FlowLayout;
//import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Search extends BaseActivity {
    private int search_count = 0;

    private Handler handler = new Handler();
    private Runnable runnable = null;

    private EditText search_edit;
    private ImageView search_load_state;
    private LinearLayout search_suggestion_cover;
    private ScrollView search_main;
    private ScrollView search_result;
    private GridLayout search_result_list;
    private LinearLayout search_suggestion_base;
    private GridLayout search_suggestion_result;
    private LinearLayout search_history;
    private FlowLayout search_history_list;
    private LinearLayout search_hot_word;
    private FlowLayout search_hot_word_list;

    private boolean is_suggesting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getHistory();
        getHotWords();
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();
        setContentView(R.layout.activity_search);

        search_load_state = findViewById(R.id.search_load_state);
        search_suggestion_result = findViewById(R.id.search_suggestion_result);
        search_suggestion_base = findViewById(R.id.search_suggestion_base);
        search_history = findViewById(R.id.search_history);
        search_history_list = findViewById(R.id.search_history_list);
        search_hot_word = findViewById(R.id.search_hot_word);
        search_hot_word_list = findViewById(R.id.search_hot_word_list);
        search_main = findViewById(R.id.search_main);
        search_result = findViewById(R.id.search_result);
        search_result_list = findViewById(R.id.search_result_list);


        search_suggestion_cover = findViewById(R.id.search_suggestion_cover);
        search_suggestion_cover.setOnClickListener(v -> search_edit.clearFocus());

        search_edit = findViewById(R.id.search_edit);
        //search_edit.requestFocus();
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (is_suggesting) {
                    if (runnable != null) {
                        handler.removeCallbacks(runnable);
                    }
                    runnable = () -> {
                        getSuggestions();
                    };
                    handler.postDelayed(runnable, 310);
                }
            }
        });
        search_edit.setOnClickListener(v -> {
            if (search_suggestion_cover.getVisibility() == View.GONE) {
                is_suggesting = true;
                getSuggestions();
            }
        });
        search_edit.setOnEditorActionListener((v, actionId, event) -> {
            search_edit.clearFocus();
            onSearch();
            return true;
        });
        search_edit.setOnFocusChangeListener((v, hasFocus) -> {
            setAnimateState(hasFocus, 150, search_suggestion_cover, null);
            this.is_suggesting = hasFocus;
            if (hasFocus) {
                getSuggestions();
            } else {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                View view = getWindow().peekDecorView();
                if (null != v && null != imm) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                setAnimateState(false, 150, search_suggestion_base, null);
            }
        });

        findViewById(R.id.search_back).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.search_history_delete).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Search.this);
            builder.setTitle(R.string.title_search_history_delete);
            builder.setMessage(R.string.text_search_history_delete);
            builder.setNegativeButton(R.string.text_cancel, null);
            builder.setPositiveButton(R.string.text_ok, (dialog, which) -> {
                try {
                    FileOutputStream fileOutputStream = getApplicationContext().openFileOutput("history.json", MODE_PRIVATE);
                    fileOutputStream.write("".getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    saveExplosion(e, -725);
                }
                setAnimateState(false, 500, search_history, null);
            });
            builder.show();
        });
    }

    private void getHotWords() {
        SearchHelper helper = new SearchHelper(Search.this);
        helper.getHotWord(new SearchHelper.HotWordCallback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                if (e instanceof JSONException) {
                    saveExplosion(e, code);
                }
            }

            @Override
            public void onResult(ArrayList<String> hotWords) {
                LinearLayout.LayoutParams params_linear = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                for (int array_index = 0; array_index < hotWords.size(); array_index++) {
                    String history_index = hotWords.get(array_index);
                    View item_search_word = LayoutInflater.from(Search.this).inflate(R.layout.item_search_word, search_hot_word_list, false);
                    TextView item_word_title = item_search_word.findViewById(R.id.item_word_title);
                    item_word_title.setText(history_index);
                    item_word_title.setOnClickListener(v -> onSearch(history_index));
                    runOnUiThread(() -> search_hot_word_list.addView(item_search_word, params_linear));
                }
                setAnimateState(true, 500, search_hot_word, null);
            }
        });
    }

    private void getSuggestions() {
        SearchHelper helper = new SearchHelper(Search.this);
        helper.suggest(search_edit.getText().toString(), new SearchHelper.SuggestCallback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                setAnimateState(false, 150, search_suggestion_base, null);
                if (e instanceof JSONException) {
                    saveExplosion(e, code);
                }
            }

            @Override
            public void onResult(ArrayList<Spannable> suggestions) {
                int row_count = suggestions.size();
                setAnimateState(false, 150, search_suggestion_base, () -> {
                    if (row_count != 0) {
                        setAnimateState(true, 150, search_suggestion_base, () -> {
                            search_suggestion_result.removeAllViews();
                            search_suggestion_result.setColumnCount(1);
                            search_suggestion_result.setRowCount(row_count);

                            int view_height = dip2px(Search.this, 50);
                            int view_width = getResources().getDisplayMetrics().widthPixels - dip2px(Search.this, 40);

                            for (int suggestion_index = 0; suggestion_index < row_count; suggestion_index++) {
                                Spannable spannable_index = suggestions.get(suggestion_index);
                                View view = LayoutInflater.from(Search.this).inflate(R.layout.item_search_suggestion,
                                        search_suggestion_result,
                                        false
                                );
                                TextView item_suggestion_title = view.findViewById(R.id.item_suggestion_title);
                                item_suggestion_title.setText(spannable_index);
                                item_suggestion_title.setOnClickListener(v -> {
                                    search_edit.clearFocus();
                                    onSearch(spannable_index.toString());
                                });

                                GridLayout.LayoutParams params = new GridLayout.LayoutParams();

                                params.rowSpec = GridLayout.spec(suggestion_index);
                                params.columnSpec = GridLayout.spec(0);

                                params.height = view_height;
                                params.width = view_width;

                                view.setLayoutParams(params);

                                search_suggestion_result.addView(view);
                            }
                        });
                    }
                });
            }
        });
    }

    private void onSearch(String keyword) {
        search_edit.setText(keyword);
        onSearch();
    }

    private void onSearch() {
        ScrollView view_visible;
        if (search_main.getVisibility() == View.VISIBLE) {
            view_visible = search_main;
        } else {
            view_visible = search_result;
        }
        setAnimateState(false, 300, view_visible, () -> {
            startOnLoadingState(search_load_state);
            setAnimateState(true, 300, search_load_state, () -> {
                String keyword = search_edit.getText().toString();
                onAddHistory(keyword);
                SearchHelper helper = new SearchHelper(Search.this);
                helper.search(keyword, new SearchHelper.SearchCallback() {
                    @Override
                    public void onFailure(int code, String message, Throwable e) {
                        onToast(Search.this, R.string.error_bangumi_load, message, code);
                        runOnUiThread(() -> {
                            stopOnLoadingState();
                            search_load_state.setImageResource(R.drawable.pic_load_failed);
                        });
                        saveExplosion(e, code);
                    }

                    @Override
                    public void onResult(ArrayList<SearchData> searchData) {
                        search_count++;
                        setAnimateState(false, 300, search_load_state, () -> {
                            stopOnLoadingState();
                            if (searchData.size() > 0) {
                                setAnimateState(true, 300, search_result, () -> {
                                    search_result_list.removeAllViews();
                                    for (int data_index = 0; data_index < searchData.size(); data_index++) {
                                        SearchData data = searchData.get(data_index);
                                        View search_item = null;
                                        if (data.selection_style.equals("grid")) {
                                            search_item = LayoutInflater.from(Search.this).inflate(R.layout.item_search_season, search_result_list, false);
                                            TextView item_search_season_title = search_item.findViewById(R.id.item_search_season_title);
                                            item_search_season_title.setText(data.season_title);
                                            TextView item_search_season_content = search_item.findViewById(R.id.item_search_season_content);
                                            item_search_season_content.setText(data.season_content);
                                            TextView item_search_rating_null = search_item.findViewById(R.id.item_search_rating_null);
                                            TextView item_search_rating_string = search_item.findViewById(R.id.item_search_rating_string);
                                            item_search_rating_string.setText(String.valueOf(data.media_score));
                                            TextView item_season_badges = search_item.findViewById(R.id.item_season_badges);
                                            if (data.angle_title.equals("")) {
                                                item_season_badges.setVisibility(View.GONE);
                                            } else {
                                                item_season_badges.setVisibility(View.VISIBLE);
                                                item_season_badges.setText(data.angle_title);
                                            }
                                            if (data.media_score == 0) {
                                                item_search_rating_null.setVisibility(View.VISIBLE);
                                                item_search_rating_string.setVisibility(View.INVISIBLE);
                                            } else {
                                                item_search_rating_null.setVisibility(View.INVISIBLE);
                                                item_search_rating_string.setVisibility(View.VISIBLE);
                                            }
                                            RatingBar item_search_rating_start = search_item.findViewById(R.id.item_search_rating_start);
                                            item_search_rating_start.setProgress(
                                                    (int) Math.round(data.media_score)
                                            );
                                            search_item.findViewById(R.id.item_search_go).setOnClickListener(v -> goToSeason(data));
                                            ImageView item_search_season_cover = search_item.findViewById(R.id.item_search_season_cover);
                                            ImageView item_search_season_placeholder = search_item.findViewById(R.id.item_search_season_placeholder);
                                            RequestOptions requestOptions = new RequestOptions()
                                                    .placeholder(R.drawable.pic_doing_v)
                                                    .error(R.drawable.pic_load_failed)
                                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
                                            Glide.with(Search.this)
                                                    .load(data.season_cover)
                                                    .apply(requestOptions)
                                                    .addListener(new RequestListener<Drawable>() {
                                                        @Override
                                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                            return false;
                                                        }

                                                        @Override
                                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                            setAnimateState(false, 400, item_search_season_placeholder, () ->
                                                                    setAnimateState(true, 400, item_search_season_cover, null));
                                                            return false;
                                                        }
                                                    })
                                                    .into(item_search_season_cover);
                                        } else if (data.selection_style.equals("horizontal")) {
                                            search_item = LayoutInflater.from(Search.this).inflate(R.layout.item_search_episode, search_result_list, false);
                                            TextView item_search_episode_title = search_item.findViewById(R.id.item_search_episode_title);
                                            item_search_episode_title.setText(data.episode_title);
                                            TextView item_search_episode_from = search_item.findViewById(R.id.item_search_episode_from);
                                            item_search_episode_from.setText(
                                                    String.format(getString(R.string.text_search_from), data.season_title.toString())
                                            );
                                            TextView item_episode_badges = search_item.findViewById(R.id.item_episode_badges);
                                            if ("".equals(data.episode_badges)) {
                                                item_episode_badges.setVisibility(View.GONE);
                                            } else {
                                                item_episode_badges.setVisibility(View.VISIBLE);
                                                item_episode_badges.setText(data.episode_badges);
                                            }
                                            search_item.findViewById(R.id.item_search_episode_action)
                                                    .setOnClickListener(v -> goToSeason(data));
                                            ImageView item_search_episode_cover = search_item.findViewById(R.id.item_search_episode_cover);
                                            ImageView item_search_episode_placeholder = search_item.findViewById(R.id.item_search_episode_placeholder);
                                            RequestOptions requestOptions = new RequestOptions()
                                                    .placeholder(R.drawable.pic_doing_h)
                                                    .error(R.drawable.pic_load_failed)
                                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
                                            Glide.with(Search.this)
                                                    .load(data.episode_cover)
                                                    .apply(requestOptions)
                                                    .addListener(new RequestListener<Drawable>() {
                                                        @Override
                                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                            return false;
                                                        }

                                                        @Override
                                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                            setAnimateState(false, 400, item_search_episode_placeholder, () ->
                                                                    setAnimateState(true, 400, item_search_episode_cover, null));
                                                            return false;
                                                        }
                                                    })
                                                    .into(item_search_episode_cover);
                                        }
                                        if (search_item != null) {
                                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                                            params.width = getResources().getDisplayMetrics().widthPixels;
                                            params.columnSpec = GridLayout.spec(0);
                                            params.rowSpec = GridLayout.spec(data_index);
                                            search_result_list.addView(search_item, params);
                                        }
                                    }
                                });
                            } else {
                                setAnimateState(true, 300, search_load_state, () ->
                                        search_load_state.setImageResource(R.drawable.pic_null)
                                );
                            }
                        });
                    }
                });
            });
        });
    }

    private void goToSeason(SearchData data) {
        Intent intent = new Intent(Search.this, Season.class);
        intent.putExtra("season_id", data.season_id);
        intent.putExtra("cover_url", data.season_cover);
        intent.putExtra("title", data.season_title.toString());
        startActivity(intent);
    }

    private void getHistory() {
        try {
            FileInputStream fileInputStream = getApplicationContext().openFileInput("history.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String history_content = stringBuilder.toString();
            if (!history_content.equals("")) {
                JSONArray array = new JSONArray(history_content);
                LinearLayout.LayoutParams params_linear = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                for (int array_index = 0; array_index < array.length(); array_index++) {
                    String history_index = array.getString(array_index);
                    View item_search_word = LayoutInflater.from(Search.this).inflate(R.layout.item_search_word, search_history_list, false);
                    TextView item_word_title = item_search_word.findViewById(R.id.item_word_title);
                    item_word_title.setText(history_index);
                    item_word_title.setOnClickListener(v -> onSearch(history_index));
                    search_history_list.addView(item_search_word, params_linear);
                }
                setAnimateState(true, 500, search_history, null);
            }
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                saveExplosion(e, -705);
            }
            search_history.setVisibility(View.GONE);
        } catch (JSONException e) {
            saveExplosion(e, -703);
            search_history.setVisibility(View.GONE);
        }
    }

    private void onAddHistory(String keyword) {
        try {
            JSONArray array_save = new JSONArray();
            array_save.put(keyword);
            try {
                FileInputStream fileInputStream = getApplicationContext().openFileInput("history.json");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String history_content = stringBuilder.toString();
                if (!history_content.equals("")) {
                    JSONArray array = new JSONArray(history_content);
                    for (int array_index = 0; array_index < array.length() && array_index < 10; array_index++) {
                        String history_index = array.getString(array_index);
                        if (!history_index.equals(keyword)) {
                            array_save.put(history_index);
                        }
                    }
                }
            } catch (FileNotFoundException ignore) {
            }
            FileOutputStream fileOutputStream = getApplicationContext().openFileOutput("history.json", MODE_PRIVATE);
            fileOutputStream.write(array_save.toString().getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            saveExplosion(e, -715);
        } catch (JSONException e) {
            saveExplosion(e, -713);
        }
    }

    @Override
    public void onBackPressed() {
        if (is_suggesting) {
            is_suggesting = false;
            search_edit.clearFocus();
        } else {
            stopOnLoadingState();
            finish();
        }
    }

//    @Override
//    protected void onDestroy() {
//        Map<String, Object> season_search = new HashMap<>();
//        season_search.put("count", search_count);
//        MobclickAgent.onEventObject(Search.this, "season_search", season_search);
//        super.onDestroy();
//    }
}

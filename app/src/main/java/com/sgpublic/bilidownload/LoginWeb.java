package com.sgpublic.bilidownload;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.sgpublic.bilidownload.BangumeAPI.LoginHelper;
import com.sgpublic.bilidownload.BangumeAPI.UserManager;
import com.sgpublic.bilidownload.BaseService.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginWeb extends BaseActivity {

    private static final String login_url = "https://passport.bilibili.com/login";
    private static final String login_success = "https://m.bilibili.com/index.html";

    private class InJavaScriptLocalObj {
        @JavascriptInterface
        public void getSource(String html) {
            String[] json_array = html.split(">");
            if (json_array.length > 1) {
                json_content = json_array[1];
            } else {
                json_content = json_array[0];
            }
            json_array = html.split("<");
            json_content = json_array[0]
                    .replace("&amp;", "&");
        }
    }

    private WebView login_web_view;
    private ImageView login_load_state;

    private String web_cookie;
    private String web_user_agent;

    private String json_content;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebSettings webSettings = login_web_view.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web_user_agent = webSettings.getUserAgentString();
        login_web_view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                login_web_view.setVisibility(View.INVISIBLE);
                startOnLoadingState(login_load_state);
                login_load_state.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.equals(login_success)) {
                    login_web_view.setVisibility(View.INVISIBLE);
                    login_load_state.setVisibility(View.VISIBLE);
                    login_web_view.stopLoading();
                    CookieManager manager = CookieManager.getInstance();
                    web_cookie = Objects.requireNonNull(manager.getCookie(login_success));
                    LoginHelper helper = new LoginHelper(LoginWeb.this);
                    helper.loginInWeb(web_cookie, web_user_agent, new LoginHelper.Callback() {
                        @Override
                        public void onFailure(int code, String message, Throwable e) {
                            onToast(LoginWeb.this, R.string.error_login, null, code);
                            login_load_state.setVisibility(View.INVISIBLE);
                            stopOnLoadingState();
                            finish();
                        }

                        @Override
                        public void onResult(String access_key, long mid) {
                            getLoginResult(access_key, mid);
                        }
                    });
                } else {
                    login_web_view.setVisibility(View.VISIBLE);
                    login_load_state.setVisibility(View.INVISIBLE);
                    stopOnLoadingState();
                }
            }
        });
        login_web_view.loadUrl(login_url);
    }

    private void getLoginResult(String access_key, long mid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_key", access_key);
        editor.putLong("mid", mid);
        editor.putLong("expires_in", System.currentTimeMillis() + 2419200000L);
        editor.apply();

        UserManager manager = new UserManager(LoginWeb.this, access_key, mid);
        manager.getInfo(new UserManager.Callback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                onToast(LoginWeb.this, R.string.error_login, message, code);
                stopOnLoadingState();
                saveExplosion(e, code);
                finish();
            }

            @Override
            public void onResult(com.sgpublic.bilidownload.DataHelper.UserData data) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", data.name);
                editor.putString("sign", data.sign);
                editor.putString("face", data.face);
                editor.putInt("sex", data.sex);
                editor.putInt("vip_type", data.vip_type);
                editor.putInt("vip_state", data.vip_state);
                editor.putInt("level", data.level);
                editor.putBoolean("is_login", true);
                editor.apply();
                login_load_state.setVisibility(View.INVISIBLE);
                stopOnLoadingState();
                runOnUiThread(() -> {
                    onToast(LoginWeb.this, R.string.text_login_success);
                    Intent intent = new Intent(LoginWeb.this, Main.class);
                    startActivity(intent);
                });
            }
        });
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();
        setContentView(R.layout.activity_login_web);

        login_load_state = findViewById(R.id.login_load_state);

        CookieManager manager = CookieManager.getInstance();
        if (!"".equals(manager.getCookie(login_url))) {
            manager.removeAllCookies(null);
        }

        login_web_view = findViewById(R.id.login_web_view);

        Toolbar login_web_toolbar = findViewById(R.id.login_web_toolbar);
        setSupportActionBar(login_web_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_login_web);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}

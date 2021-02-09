package com.sgpublic.bilidownload;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.sgpublic.bilidownload.BangumiAPI.LoginHelper;
import com.sgpublic.bilidownload.BangumiAPI.UserManager;
import com.sgpublic.bilidownload.BaseService.BaseActivity;
import com.sgpublic.bilidownload.DataItem.TokenData;

import java.util.Objects;

public class LoginWeb extends BaseActivity {

    private static final String login_url = "https://passport.bilibili.com/login";
    private static final String bilibili_host = "https://m.bilibili.com/index.html";
    private static final String bilibili_passport = "https://passport.bilibili.com/account/security#/home";

    private WebView login_web_view;
    private ImageView login_load_state;

    private String web_cookie;
    private String web_user_agent;

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
                if (url.equals(bilibili_host) || url.equals(bilibili_passport)) {
                    login_web_view.setVisibility(View.INVISIBLE);
                    login_load_state.setVisibility(View.VISIBLE);
                    login_web_view.stopLoading();
                    CookieManager manager = CookieManager.getInstance();
                    web_cookie = Objects.requireNonNull(manager.getCookie(url));
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
                        public void onLimited() {
                        }

                        @Override
                        public void onResult(TokenData token, long mid) {
                            getLoginResult(token, mid);
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

    private void getLoginResult(TokenData token, long mid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_key", token.access_token);
        editor.putLong("mid", mid);
        editor.putLong("expires_in", token.expires_in);
        editor.apply();

        UserManager manager = new UserManager(LoginWeb.this, token.access_token, mid);
        manager.getInfo(new UserManager.Callback() {
            @Override
            public void onFailure(int code, String message, Throwable e) {
                onToast(LoginWeb.this, R.string.error_login, message, code);
                stopOnLoadingState();
                saveExplosion(e, code);
                finish();
            }

            @Override
            public void onResult(com.sgpublic.bilidownload.DataItem.UserData data) {
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

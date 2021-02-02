package com.sgpublic.bilidownload;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.sgpublic.bilidownload.BangumiAPI.LoginHelper;
import com.sgpublic.bilidownload.BangumiAPI.UserManager;
import com.sgpublic.bilidownload.BaseService.BaseActivity;
import com.sgpublic.bilidownload.DataHelper.TokenData;

import static com.sgpublic.bilidownload.BaseService.ActivityController.finishAll;

public class Login extends BaseActivity {

    private TextView login_action;
    private EditText login_username;
    private EditText login_password;
    private ImageView login_banner_left;
    private ImageView login_banner_right;
    private ProgressBar login_doing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_login", false);
        editor.apply();

        LinearLayout login_permission = findViewById(R.id.login_permission);
        LinearLayout login_content = findViewById(R.id.login_content);
        if (getIntent().getIntExtra("grand", 0) == 0) {
            login_permission.setVisibility(View.VISIBLE);
            login_content.setVisibility(View.GONE);
        } else {
            login_permission.setVisibility(View.GONE);
            login_content.setVisibility(View.VISIBLE);
        }
    }

    private void loginAction() {
        setLoadState(true);
        String username = login_username.getText().toString();
        String password = login_password.getText().toString();
        if (username.equals("") || password.equals("")) {
            onToast(Login.this, R.string.text_login_error_empty);
        } else {
            LoginHelper helper = new LoginHelper(Login.this);
            helper.loginInAccount(username, password, new LoginHelper.Callback() {
                @Override
                public void onFailure(int code, String message, Throwable e) {
                    onToast(Login.this, R.string.error_login, message, code);
                    setLoadState(false);
                    saveExplosion(e, code);
                }

                @Override
                public void onLimited() {
                    onToast(Login.this, R.string.error_login_verify);
                    Intent intent = new Intent(Login.this, LoginWeb.class);
                    startActivity(intent);
                }

                @Override
                public void onResult(TokenData token, long mid) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("access_key", token.access_token);
                    editor.putString("refresh_key", token.refresh_token);
                    editor.putLong("mid", mid);
                    editor.putLong("expires_in", token.expires_in);
                    editor.apply();

                    UserManager manager = new UserManager(Login.this, token.access_token, mid);
                    manager.getInfo(new UserManager.Callback() {
                        @Override
                        public void onFailure(int code, String message, Throwable e) {
                            onToast(Login.this, R.string.error_login, message, code);
                            setLoadState(false);
                            saveExplosion(e, code);
                        }

                        @Override
                        public void onResult(com.sgpublic.bilidownload.DataHelper.UserData data) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", data.name);
                            editor.putString("sign", data.sign);
                            editor.putString("face", data.face);
                            editor.putInt("sex", data.sex);
                            editor.putString("vip_label", data.vip_label);
                            editor.putInt("vip_type", data.vip_type);
                            editor.putInt("vip_state", data.vip_state);
                            editor.putInt("level", data.level);
                            editor.putBoolean("is_login", true);
                            editor.apply();
                            setLoadState(false);
                            runOnUiThread(() -> {
                                onToast(Login.this, R.string.text_login_success);
                                Intent intent = new Intent(Login.this, Main.class);
                                startActivity(intent);
                            });
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onUiLoad() {
        super.onUiLoad();

        setContentView(R.layout.activity_login);

        login_banner_left = findViewById(R.id.login_banner_left);
        login_banner_right = findViewById(R.id.login_banner_right);
        login_username = findViewById(R.id.login_username);
        login_doing = findViewById(R.id.login_doing);


        login_password = findViewById(R.id.login_password);
        login_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                int[] image_index;
                if (!editable.toString().equals("")) {
                    image_index = new int[]{R.drawable.pic_login_banner_left_hide, R.drawable.pic_login_banner_right_hide};
                } else {
                    image_index = new int[]{R.drawable.pic_login_banner_left_show, R.drawable.pic_login_banner_right_show};
                }
                login_banner_left.setImageResource(image_index[0]);
                login_banner_right.setImageResource(image_index[1]);
            }
        });

        login_action = findViewById(R.id.login_action);
        login_action.setOnClickListener(v -> Login.this.loginAction());

        findViewById(R.id.login_in_web).setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, LoginWeb.class);
            startActivity(intent);
        });

        findViewById(R.id.login_button_access).setOnClickListener(v -> ActivityCompat.requestPermissions(Login.this, new String[]{
//                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 1));

    }

    private void setLoadState(final boolean is_loading) {
        runOnUiThread(() -> {
            login_username.setEnabled(!is_loading);
            login_password.setEnabled(!is_loading);
            login_action.setEnabled(!is_loading);
            login_doing.setVisibility(is_loading ? View.VISIBLE : View.GONE);
            login_action.setTextColor(is_loading ? Color.GRAY : Login.this.getResources().getColor(R.color.colorAccent));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean is_granted = true;
        for (int grantResult : grantResults) {
            is_granted = is_granted && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (is_granted) {
            findViewById(R.id.login_button_access).setEnabled(false);
            LinearLayout login_permission = findViewById(R.id.login_permission);

            AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setDuration(500);
            animation.setFillAfter(false);
            login_permission.startAnimation(animation);
            login_permission.setVisibility(View.GONE);

            new Handler().postDelayed(() -> {
                LinearLayout login_content = Login.this.findViewById(R.id.login_content);
                AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
                animation1.setDuration(500);
                animation1.setFillAfter(false);
                login_content.startAnimation(animation1);
                login_content.setVisibility(View.VISIBLE);
            }, 500);
        } else {
            Toast.makeText(Login.this, R.string.text_permission_denied, Toast.LENGTH_SHORT).show();
        }
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
                finishAll();
            } else {
                last = now;
                Toast.makeText(this, "请再点击一次退出", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

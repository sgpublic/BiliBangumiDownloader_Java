package com.sgpublic.bilidownload.ui;

import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {
    private static final long DOUBLE_Effective_TIME = 200;
    private static final long DOUBLE_Invalid_TIME = 200;
    private static long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastClickTime < DOUBLE_Effective_TIME) {
            onDoubleClick(v);
        }
        lastClickTime = currentTimeMillis;
    }

    public abstract void onDoubleClick(View v);
}
package com.sgpublic.bilidownload.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class ScrollTextView extends AppCompatTextView {
    public ScrollTextView(Context context) {
        super(context);
        initView();
    }

    public ScrollTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public ScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setSingleLine(true);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
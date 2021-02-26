package com.sgpublic.bilidownload.Widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IntDef;

import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 * Created by huangxy on 2018/8/15.
 */
public class FlowLayout extends ViewGroup {
    private Line mLine = null;
    public static final int DEFAULT_SPACING = 20;
    private SparseArray<View> mViews;
    private int mHorizontalSpacing = DEFAULT_SPACING;
    private int mVerticalSpacing = DEFAULT_SPACING;
    private int mUsedWidth = 0;
    private final List<Line> mLines = new ArrayList<>();
    private int isAlignByCenter = 1;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface AlienState {
        int RIGHT = 0;
        int LEFT = 1;
        int CENTER = 2;

        @IntDef(value = {RIGHT, LEFT, CENTER})
        @interface Val {
        }
    }

    public void setAlignByCenter(@AlienState.Val int isAlignByCenter) {
        this.isAlignByCenter = isAlignByCenter;
        requestLayoutInner();
    }

    private void requestLayoutInner() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    public void setAdapter(List<?> list, int res, ItemView<Object> mItemView) {
        if (list == null) {
            return;
        }
        removeAllViews();
        int layoutPadding = dipToPx(getContext(), 8);
        setHorizontalSpacing(layoutPadding);
        setVerticalSpacing(layoutPadding);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Object item = list.get(i);
            View inflate = LayoutInflater.from(getContext()).inflate(res, null);
            mItemView.getCover(item, new ViewHolder(inflate), inflate, i);
            addView(inflate);
        }
    }

    public abstract static class ItemView<T> {
        abstract void getCover(T item, ViewHolder holder, View inflate, int position);
    }

    class ViewHolder {
        View mConvertView;

        public ViewHolder(View mConvertView) {
            this.mConvertView = mConvertView;
            mViews = new SparseArray<>();
        }

        public <T extends View> T getView(int viewId) {
            View view = mViews.get(viewId);
            if (view == null) {
                view = mConvertView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            try {
                return (T) view;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void setText(int viewId, String text) {
            TextView view = getView(viewId);
            view.setText(text);
        }
    }

    public static int dipToPx(Context ctx, float dip) {
        return (int) TypedValue.applyDimension(1, dip, ctx.getResources().getDisplayMetrics());
    }

    public void setHorizontalSpacing(int spacing) {
        if (mHorizontalSpacing != spacing) {
            mHorizontalSpacing = spacing;
            requestLayoutInner();
        }
    }

    public void setVerticalSpacing(int spacing) {
        if (mVerticalSpacing != spacing) {
            mVerticalSpacing = spacing;
            requestLayoutInner();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight() - getPaddingLeft();
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        restoreLine();// 还原数据，以便重新记录
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(sizeWidth, modeWidth == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeWidth);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(sizeHeight, modeHeight == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeHeight);
            // 测量child
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            if (mLine == null) {
                mLine = new Line();
            }
            int childWidth = child.getMeasuredWidth();
            mUsedWidth += childWidth;// 增加使用的宽度
            if (mUsedWidth <= sizeWidth) {// 使用宽度小于总宽度，该child属于这一行。
                mLine.addView(child);// 添加child
                mUsedWidth += mHorizontalSpacing;// 加上间隔
                if (mUsedWidth >= sizeWidth) {// 加上间隔后如果大于等于总宽度，需要换行
                    if (!newLine()) {
                        break;
                    }
                }
            } else {// 使用宽度大于总宽度。需要换行
                if (mLine.getViewCount() == 0) {// 如果这行一个child都没有，即使占用长度超过了总长度，也要加上去，保证每行都有至少有一个child
                    mLine.addView(child);// 添加child
                    if (!newLine()) {// 换行
                        break;
                    }
                } else {// 如果该行有数据了，就直接换行
                    if (!newLine()) {// 换行
                        break;
                    }
                    // 在新的一行，不管是否超过长度，先加上去，因为这一行一个child都没有，所以必须满足每行至少有一个child
                    mLine.addView(child);
                    mUsedWidth += childWidth + mHorizontalSpacing;
                }
            }
        }

        if (mLine != null && mLine.getViewCount() > 0 && !mLines.contains(mLine)) {
            // 由于前面采用判断长度是否超过最大宽度来决定是否换行，则最后一行可能因为还没达到最大宽度，所以需要验证后加入集合中
            mLines.add(mLine);
        }

        int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int totalHeight = 0;
        final int linesCount = mLines.size();
        for (int i = 0; i < linesCount; i++) {// 加上所有行的高度
            totalHeight += mLines.get(i).mHeight;
        }
        totalHeight += mVerticalSpacing * (linesCount - 1);// 加上所有间隔的高度
        totalHeight += getPaddingTop() + getPaddingBottom();// 加上padding
        // 设置布局的宽高，宽度直接采用父view传递过来的最大宽度，而不用考虑子view是否填满宽度，因为该布局的特性就是填满一行后，再换行
        // 高度根据设置的模式来决定采用所有子View的高度之和还是采用父view传递过来的高度
        setMeasuredDimension(totalWidth, resolveSize(totalHeight, heightMeasureSpec));
    }

    private void restoreLine() {
        mLines.clear();
        mLine = new Line();
        mUsedWidth = 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int left = getPaddingLeft();//获取最初的左上点
            int top = getPaddingTop();
            int count = mLines.size();
            for (int i = 0; i < count; i++) {
                Line line = mLines.get(i);
                line.LayoutView(left, top);//摆放每一行中子View的位置
                top += line.mHeight + mVerticalSpacing;//为下一行的top赋值
            }
        }
    }

    /**
     * 新增加一行
     */
    private boolean newLine() {
        mLines.add(mLine);
        int mMaxLinesCount = Integer.MAX_VALUE;
        if (mLines.size() < mMaxLinesCount) {
            mLine = new Line();
            mUsedWidth = 0;
            return true;
        }
        return false;
    }

    class Line {
        int mWidth = 0;// 该行中所有的子View累加的宽度
        int mHeight = 0;// 该行中所有的子View中高度的那个子View的高度
        List<View> views = new ArrayList<View>();

        public void addView(View view) {// 往该行中添加一个
            views.add(view);
            mWidth += view.getMeasuredWidth();
            int childHeight = view.getMeasuredHeight();
            mHeight = mHeight < childHeight ? childHeight : mHeight;//高度等于一行中最高的View
        }

        public int getViewCount() {
            return views.size();
        }

        //摆放行中子View的位置
        public void LayoutView(int l, int t) {
            int left = l;
            int top = t;
            int count = getViewCount();
            int layoutWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();//行的总宽度
            //剩余的宽度，是除了View和间隙的剩余空间
            int surplusWidth = layoutWidth - mWidth - mHorizontalSpacing * (count - 1);
            if (surplusWidth >= 0) {
                for (int i = 0; i < count; i++) {
                    final View view = views.get(i);
                    int childWidth = view.getMeasuredWidth();
                    int childHeight = view.getMeasuredHeight();
                    //计算出每个View的顶点，是由最高的View和该View高度的差值除以2
                    int topOffset = (int) ((mHeight - childHeight) / 2.0 + 0.5);
                    if (topOffset < 0) {
                        topOffset = 0;
                    }

                    //布局View
                    if (i == 0) {
                        switch (isAlignByCenter) {
                            case AlienState.CENTER:
                                left += surplusWidth / 2;
                                break;
                            case AlienState.RIGHT:
                                left += surplusWidth;
                                break;
                            default:
                                left = 0;
                                break;
                        }
                    }
                    view.layout(left, top + topOffset, left + childWidth, top + topOffset + childHeight);
                    left += childWidth + mVerticalSpacing;//为下一个View的left赋值
                }
            }
        }
    }
}
package com.zy.videocoverselector.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class ThumbView extends FrameLayout {

    private static final int EXTEND_TOUCH_SLOP = 15;

    private Drawable mThumbDrawable;

    private boolean mPressed;

    private int mThumbWidth;
    private int mTickIndex;

    public ThumbView(@NonNull Context context, int thumbWidth, Drawable drawable) {
        super(context);
        mThumbWidth = thumbWidth;
        mThumbDrawable = drawable;
        setBackgroundDrawable(mThumbDrawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(mThumbWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
        mThumbDrawable.setBounds(0, 0, mThumbWidth, getMeasuredHeight());
    }

    public void setThumbWidth(int thumbWidth) {
        mThumbWidth = thumbWidth;
    }

    public int getRangeIndex() {
        return mTickIndex;
    }

    public void setTickIndex(int tickIndex) {
        mTickIndex = tickIndex;
    }

    @Override
    public boolean isPressed() {
        return mPressed;
    }

    @Override
    public void setPressed(boolean pressed) {
        mPressed = pressed;
    }
}

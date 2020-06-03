package com.zy.videocoverselector.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zy.videocoverselector.R;


public class RangeSlider extends ViewGroup {
    private static final int DEFAULT_THUMB_WIDTH = 7;
    private static final int DEFAULT_TICK_START = 0;
    private static final int DEFAULT_TICK_END = 5;
    private static final int DEFAULT_TICK_INTERVAL = 1;
    private static int DEFAULT_MASK_BACKGROUND = 0xA0000000;
    private static int DEFAULT_LINE_COLOR = 0xFFFF584C;
    public static final int TYPE_LEFT = 1;

    @NonNull
    private final ThumbView mThumb;
    private final Paint mBgPaint;
    private final VideoPlayer videoPlayer;
    private final TextureView textureView;
    @Nullable
    private Drawable mIcon;

    private int mTouchSlop;
    private int mOriginalX, mLastX;

    private int mThumbWidth;

    private int mTickStart = DEFAULT_TICK_START;
    private int mTickEnd = DEFAULT_TICK_END;
    private int mTickInterval = DEFAULT_TICK_INTERVAL;
    private int mTickCount = (mTickEnd - mTickStart) / mTickInterval;

    private boolean mIsDragging;

    private OnRangeChangeListener mRangeChangeListener;
    private boolean mComplete;
    private long mStartTime;

    public RangeSlider(@NonNull Context context) {
        this(context, null);
    }

    public RangeSlider(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSlider(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RangeSlider, 0, 0);
        mThumbWidth = array.getDimensionPixelOffset(R.styleable.RangeSlider_thumbWidth, DEFAULT_THUMB_WIDTH);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mBgPaint = new Paint();
        mBgPaint.setColor(array.getColor(R.styleable.RangeSlider_maskColor, DEFAULT_MASK_BACKGROUND));

        mIcon = array.getDrawable(R.styleable.RangeSlider_leftThumbDrawable);
        mThumb = new ThumbView(context, mThumbWidth, mIcon == null ? new ColorDrawable(DEFAULT_LINE_COLOR) : mIcon);
        setTickCount(array.getInteger(R.styleable.RangeSlider_tickCount, DEFAULT_TICK_END));
        setRangeIndex(array.getInteger(R.styleable.RangeSlider_leftThumbIndex, DEFAULT_TICK_START),
                array.getInteger(R.styleable.RangeSlider_rightThumbIndex, mTickCount));
        array.recycle();
        videoPlayer = new VideoPlayer();
        textureView = new TextureView(getContext());
        textureView.setVisibility(GONE);
        videoPlayer.setTextureView(textureView);
        mThumb.addView(textureView);
        addView(mThumb);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mThumb.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int lThumbWidth = mThumb.getMeasuredWidth();
        final int lThumbHeight = mThumb.getMeasuredHeight();
        mThumb.layout(0, 0, lThumbWidth, lThumbHeight);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final float lThumbOffset = mThumb.getX();
        canvas.drawRect(0, 0, lThumbOffset, height, mBgPaint);
        canvas.drawRect(lThumbOffset+mThumbWidth, 0, width, height, mBgPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        moveThumbByIndex(mThumb, mThumb.getRangeIndex());
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        if (!mComplete){
            return false;
        }

        boolean handle = false;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getX();
                int y = (int) event.getY();
                textureView.setVisibility(VISIBLE);
                mIsDragging = false;

                if (!mThumb.isPressed()) {
                    mThumb.setPressed(true);
                    handle = true;
                    if (mRangeChangeListener != null) {
                        mRangeChangeListener.onKeyDown(TYPE_LEFT);
                    }
                    int moveX = (int) (x - (mThumb.getX()+ mThumb.getWidth()/2));
                    getParent().requestDisallowInterceptTouchEvent(true);
                    moveLeftThumbByPixel(moveX);
                    invalidate();
                    if (mRangeChangeListener != null) {
                        mRangeChangeListener.onKeyMove(TYPE_LEFT, mThumb.getRangeIndex(),moveX);
                    }
                }
                mLastX = mOriginalX = x;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsDragging = false;
                mOriginalX = mLastX = 0;
                textureView.setVisibility(GONE);
                getParent().requestDisallowInterceptTouchEvent(false);
                if (mThumb.isPressed()) {
                    releaseLeftThumb();
                    invalidate();
                    handle = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                x = (int) event.getX();

                if (!mIsDragging && Math.abs(x - mOriginalX) > mTouchSlop) {
                    mIsDragging = true;
                }
                if (mIsDragging) {
                    int moveX = x - mLastX;
                    if (mThumb.isPressed()) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        moveLeftThumbByPixel(moveX);
                        handle = true;
                        invalidate();
                        if (mRangeChangeListener != null) {
                            mRangeChangeListener.onKeyMove(TYPE_LEFT, mThumb.getRangeIndex(), moveX);
                        }
                    }
                }

                mLastX = x;
                break;
        }

        return handle;
    }

    private boolean isValidTickCount(int tickCount) {
        return (tickCount > 1);
    }

    private boolean indexOutOfRange(int leftThumbIndex, int rightThumbIndex) {
        return (leftThumbIndex < 0 || leftThumbIndex > mTickCount
                || rightThumbIndex < 0
                || rightThumbIndex > mTickCount);
    }

    private float getRangeLength() {
        int width = getMeasuredWidth();
        if (width < mThumbWidth) {
            return 0;
        }
        return width - mThumbWidth;
    }

    private float getIntervalLength() {
        return getRangeLength() / mTickCount;
    }

    public int getNearestIndex(float x) {
        return Math.round(x / getIntervalLength());
    }

    public int getLeftIndex() {
        return mThumb.getRangeIndex();
    }


    private void notifyRangeChange(int type) {
        if (mRangeChangeListener != null) {
//            mRangeChangeListener.onRangeChange(this, type, mThumb.getRangeIndex(), mRightThumb.getRangeIndex());
        }
    }

    public void setRangeChangeListener(OnRangeChangeListener rangeChangeListener) {
        mRangeChangeListener = rangeChangeListener;
    }

    /**
     * Sets the tick count in the RangeSlider.
     *
     * @param count Integer specifying the number of ticks.
     */
    public void setTickCount(int count) {
        int tickCount = (count - mTickStart) / mTickInterval;
        if (isValidTickCount(tickCount)) {
            mTickEnd = count;
            mTickCount = tickCount;
        } else {
            throw new IllegalArgumentException("tickCount less than 2; invalid tickCount.");
        }
    }

    /**
     * The location of the thumbs according by the supplied index.
     * Numbered from 0 to mTickCount - 1 from the left.
     *
     * @param leftIndex  Integer specifying the index of the left thumb
     * @param rightIndex Integer specifying the index of the right thumb
     */
    public void setRangeIndex(int leftIndex, int rightIndex) {
        if (indexOutOfRange(leftIndex, rightIndex)) {
            throw new IllegalArgumentException(
                    "mThumb index left " + leftIndex + ", or right " + rightIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")");
        } else {
            if (mThumb.getRangeIndex() != leftIndex) {
                mThumb.setTickIndex(leftIndex);
            }
        }
    }

    private boolean moveThumbByIndex(@NonNull ThumbView view, int index) {
        view.setX(index * getIntervalLength());
        if (view.getRangeIndex() != index) {
            view.setTickIndex(index);
            return true;
        }
        return false;
    }

    private void moveLeftThumbByPixel(int pixel) {
        float x = mThumb.getX() + pixel;
        float interval = getIntervalLength();
        float start = mTickStart / mTickInterval * interval;
        float end = mTickEnd / mTickInterval * interval;

        if (x > start && x < end) {
            mThumb.setX(x);
            int index = getNearestIndex(x);
            if (mThumb.getRangeIndex() != index) {
                mThumb.setTickIndex(index);
                notifyRangeChange(TYPE_LEFT);
            }
        }
    }

    private void releaseLeftThumb() {
        mThumb.setPressed(false);
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public void playVideo(final String videoUrl) {
        videoPlayer.reset();
        videoPlayer.setOnStateChangeListener(new VideoPlayer.OnStateChangeListener() {
            @Override
            public void onPrepared() {
                videoPlayer.seekTo(mStartTime);
            }

            @Override
            public void onReset() {
            }

            @Override
            public void onRenderingStart() {
            }

            @Override
            public void onProgressUpdate(float per) {
            }

            @Override
            public void onPause() {
            }

            @Override
            public void onStop() {
            }

            @Override
            public void onComplete() {
            }
        });
        videoPlayer.setDataSource(videoUrl);
        videoPlayer.prepare();
    }


    public void loadComplete(boolean complete) {
        mComplete = complete;
    }

    public void seekTo(long startTime) {
        videoPlayer.seekTo(startTime);
    }

    public interface OnRangeChangeListener {
        void onKeyDown(int type);

        void onKeyMove(int typeLeft, int rangeIndex, int moveX);
    }

}

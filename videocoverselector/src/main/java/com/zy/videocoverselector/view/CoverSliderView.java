package com.zy.videocoverselector.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zy.videocoverselector.ICoverSelector;
import com.zy.videocoverselector.R;
import com.zy.videocoverselector.adapter.SelectCoverAdapter;
import com.zy.videocoverselector.data.VideoInfo;

public class CoverSliderView extends RelativeLayout implements RangeSlider.OnRangeChangeListener {

    private Context mContext;

    private RecyclerView mRecyclerView;
    private RangeSlider mRangeSlider;
    private float mCurrentScroll;
    private int mSingleWidth;
    private int mAllWidth;

    private long mVideoDuration;

    private long mViewMaxDuration;

    private long mStartTime = 0;

    private int mSelectTime;

    private long mVideoStartPos;

    private SelectCoverAdapter mAdapter;

    private ICoverSelector.OnSliderMoveListener mSliderMoveListener;
    private int position;
    private float mViewRate;

    public CoverSliderView(Context context) {
        super(context);

        init(context);
    }

    public CoverSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public CoverSliderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context context) {
        mContext = context;
        inflate(getContext(), R.layout.item_sv_cover_view, this);

        mRangeSlider = (RangeSlider) findViewById(R.id.range_slider);
        mRangeSlider.setRangeChangeListener(this);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mAdapter = new SelectCoverAdapter(mContext);
        mRecyclerView.setAdapter(mAdapter);

        mSingleWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.item_thumb_width);
    }

    /**
     * 设置缩略图个数
     *
     * @param count
     */
    public void setCount(int count) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int width = count * mSingleWidth;
        mAllWidth = width;
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        if (width > screenWidth) {
            width = screenWidth;
        }
        layoutParams.width = width;
        setLayoutParams(layoutParams);
    }

    public void setSliderMoveListener(ICoverSelector.OnSliderMoveListener listener) {
        mSliderMoveListener = listener;
    }

    public void setMediaFileInfo(VideoInfo videoInfo) {
        if (videoInfo == null) {
            return;
        }
        mViewMaxDuration = videoInfo.duration;
        mSelectTime = 0;
        mVideoStartPos = 0;
    }

    public void addBitmap(int index, Bitmap bitmap) {
        mAdapter.add(index, bitmap);
    }

    public void clearAllBitmap() {
        mAdapter.clearAllBitmap();
    }

    @Override
    public void onKeyDown(int type) {
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAdapter != null) {
            mAdapter.clearAllBitmap();
        }
    }


    @Override
    public void onKeyMove(int type, int leftPinIndex, int moveX) {
        mSelectTime = (int) (mViewMaxDuration * leftPinIndex * mViewRate / 100); //ms
        onTimeChangedMove(moveX);
    }

    private void onTimeChangedMove(int moveX) {
        mVideoStartPos = mStartTime + mSelectTime;
        if (mSliderMoveListener != null) {
            mSliderMoveListener.onSliderMove((int) mVideoStartPos, getRangeSlider().getLeftIndex(), moveX);
        }
    }

    private void onTimeChanged() {
        mVideoStartPos = mStartTime + mSelectTime;
    }

    @NonNull
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    onTimeChanged();
                    break;
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    break;
                case RecyclerView.SCROLL_STATE_SETTLING:

                    break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            mCurrentScroll = mCurrentScroll + dx;
            float rate = mCurrentScroll / mAllWidth;
            if (mCurrentScroll + mRecyclerView.getWidth() > mAllWidth) {
                mStartTime = mVideoDuration - mViewMaxDuration;
            } else {
                mStartTime = (int) (rate * mVideoDuration);
            }
        }
    };

    public RangeSlider getRangeSlider() {
        return mRangeSlider;
    }

    public void setVideoInfo(VideoInfo videoInfo) {
        mVideoDuration = videoInfo.duration;
        int durationS = (int) (mVideoDuration / 1000);
        setMediaFileInfo(videoInfo);
        setCount(durationS);
    }

    public void addThumbnail(int index, Bitmap bitmap) {
        this.addBitmap(index, bitmap);
    }

    public void clearThumbnail() {
        this.clearAllBitmap();
    }

    public void setPosition(int position) {
        this.position = position;
        mRangeSlider.setRangeIndex(position, 100);
    }

    public int getPosition() {
        return position;
    }

    public void loadComplete(boolean mComplete) {
        mViewRate = (float) mRecyclerView.getWidth() / mAllWidth;
        mRangeSlider.loadComplete(mComplete);
    }

    public void startScrollBy(int i) {
        mRecyclerView.smoothScrollBy(i, 0);
    }

    public void startScrollTo(int i) {
        mRecyclerView.scrollBy(i, 0);
    }

    public float getmCurrentScroll() {
        return mCurrentScroll;
    }

}

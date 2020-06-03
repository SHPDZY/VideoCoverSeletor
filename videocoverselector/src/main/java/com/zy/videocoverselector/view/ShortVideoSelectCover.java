package com.zy.videocoverselector.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zy.videocoverselector.CoverSelectorManager;
import com.zy.videocoverselector.ICoverSelector;
import com.zy.videocoverselector.R;
import com.zy.videocoverselector.data.VideoInfo;
import com.zy.videocoverselector.utils.LogUtils;
import com.zy.videocoverselector.utils.ScreenUtils;


public class ShortVideoSelectCover extends RelativeLayout {
    private boolean mComplete = false;
    private CoverSliderView mVideoCutLayout;
    private ImageView mImgCover;
    private String mVideoPath = "/storage/emulated/0/Movies/QQ视频_ba17c68784730633f2b679d19823efec1590643488.mp4";
    private long mStartTime;
    private VideoPlayer videoPlayer;
    private TextureView textureView;
    private FrameLayout flVideo;
    private VideoInfo mVideoInfo;

    public ShortVideoSelectCover(Context context) {
        super(context);
        initViews();
    }

    public ShortVideoSelectCover(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ShortVideoSelectCover(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.video_selector_layout, this);
        videoPlayer = new VideoPlayer();
        textureView = new TextureView(getContext());
        videoPlayer.setTextureView(textureView);
        flVideo = findViewById(R.id.flVideo);
        flVideo.addView(textureView);
        mVideoCutLayout = findViewById(R.id.video_cut_layout);
        mImgCover = findViewById(R.id.img_cover);
        mVideoInfo = new VideoInfo();
        mVideoInfo.videoPath = mVideoPath;
        playVideo(mVideoPath);
        mVideoCutLayout.getRangeSlider().playVideo(mVideoPath);
    }

    private void loadVideoInfo(VideoInfo videoInfo) {
        mVideoCutLayout.setVideoInfo(videoInfo);
        mVideoCutLayout.clearThumbnail();
        mVideoCutLayout.setSliderMoveListener(new ICoverSelector.OnSliderMoveListener() {

            @Override
            public void onSliderMove(long startTime, int type, int moveX) {
                LogUtils.error("onSliderMove type:" + type + " moveX:" + moveX);
                mStartTime = startTime;
                videoPlayer.seekTo(startTime);
                mVideoCutLayout.getRangeSlider().seekTo(startTime);
                mImgCover.setVisibility(GONE);
                if (type > 80 && moveX > 0) {
                    mVideoCutLayout.startScrollBy(ScreenUtils.dip2px(150));
                } else if (type < 20 && moveX < 0) {
                    mVideoCutLayout.startScrollBy(-ScreenUtils.dip2px(150));
                }
            }
        });
        CoverSelectorManager.getInstance().setVideoInfo(videoInfo);
        CoverSelectorManager.getInstance().setThumbnailListener(new ICoverSelector.ThumbnailListener() {
            @Override
            public void onThumbnail(int pos, long timeMs, Bitmap bitmap) {
                mComplete = false;
                mVideoCutLayout.addThumbnail(pos, bitmap);
            }

            @Override
            public void loadComplete() {
                mComplete = true;
                mVideoCutLayout.loadComplete(true);
            }
        });
        CoverSelectorManager.getInstance().loadThumbnail(videoInfo);
    }

    public void getCover() {
        if (onGetSampleImageListener != null) {
            onGetSampleImageListener.start();
        }
        CoverSelectorManager.getInstance().setOnFrameAtTimeListener(new ICoverSelector.OnFrameAtTimeListener() {
            @Override
            public void onFrameAtTime(Bitmap bitmap) {
                if (onGetSampleImageListener != null) {
                    onGetSampleImageListener.complete(bitmap);
                }
            }
        });
        CoverSelectorManager.getInstance().getFrameAtTime(mStartTime);
    }

    private void playVideo(final String videoUrl) {
        videoPlayer.reset();
        videoPlayer.setOnStateChangeListener(new VideoPlayer.OnStateChangeListener() {
            @Override
            public void onPrepared() {
                mComplete = false;
                videoPlayer.seekTo(0);
                int duration = videoPlayer.getDuration();
                LogUtils.error("duration :" + duration);
                mVideoInfo.duration = duration;
                loadVideoInfo(mVideoInfo);
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

    private OnGetSampleImageListener onGetSampleImageListener;

    public void setOnGetSampleImageListener(OnGetSampleImageListener onGetSampleImageListener) {
        this.onGetSampleImageListener = onGetSampleImageListener;
    }

    public interface OnGetSampleImageListener {
        void start();

        void complete(Bitmap img);
    }

}

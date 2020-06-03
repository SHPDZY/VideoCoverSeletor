package com.zy.videocoverselector;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.zy.videocoverselector.data.VideoInfo;
import com.zy.videocoverselector.utils.BackgroundTasks;
import com.zy.videocoverselector.utils.LogUtils;

import java.util.concurrent.Executors;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;

public class CoverSelectorManager {

    private static CoverSelectorManager sCoverSelectorManager;
    private MediaMetadataRetriever mmr = null;
    private ICoverSelector.ThumbnailListener mThumbnailListener;
    private ICoverSelector.OnFrameAtTimeListener mOnFrameAtTimeListener;
    private VideoInfo mVideoInfo;

    public static CoverSelectorManager getInstance() {
        if (sCoverSelectorManager == null) {
            synchronized (CoverSelectorManager.class) {
                if (sCoverSelectorManager == null) {
                    sCoverSelectorManager = new CoverSelectorManager();
                }
            }
        }
        return sCoverSelectorManager;
    }

    public CoverSelectorManager() {
        mmr = new MediaMetadataRetriever();
    }

    public CoverSelectorManager setVideoInfo(VideoInfo videoInfo) {
        mVideoInfo = videoInfo;
        mmr.setDataSource(mVideoInfo.videoPath);
        return sCoverSelectorManager;
    }

    public void loadThumbnail(final VideoInfo videoInfo) {
        double durationS = (videoInfo.duration / 1000d);
        final int interval = (int) (videoInfo.duration / durationS);
        int coverNumber = (int) (videoInfo.duration / interval);
        for (int index = 0; index < coverNumber; index++) {
            final int finalIndex = index;
            final int time = interval * finalIndex;
            LogUtils.error("index:" + index + " time:" + time + " interval:" + interval);
            Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = mmr.getFrameAtTime(interval * finalIndex * 1000, OPTION_CLOSEST);
                    BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mThumbnailListener != null) {
                                int size = (int) (videoInfo.duration / interval);
                                mThumbnailListener.onThumbnail(finalIndex, time, bitmap);
                                if (finalIndex == size - 1) {
                                    mThumbnailListener.loadComplete();
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    public void getFrameAtTime(final long startTime) {
        Executors.newSingleThreadScheduledExecutor().submit(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = mmr.getFrameAtTime(startTime * 1000, OPTION_CLOSEST);
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mOnFrameAtTimeListener!=null){
                            mOnFrameAtTimeListener.onFrameAtTime(bitmap);
                        }
                    }
                });
            }
        });
    }

    public void setThumbnailListener(ICoverSelector.ThumbnailListener thumbnailListener) {
        this.mThumbnailListener = thumbnailListener;
    }

    public void setOnFrameAtTimeListener(ICoverSelector.OnFrameAtTimeListener onFrameAtTimeListener) {
        this.mOnFrameAtTimeListener = onFrameAtTimeListener;
    }
}

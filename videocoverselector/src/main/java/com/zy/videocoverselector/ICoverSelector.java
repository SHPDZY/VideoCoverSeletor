package com.zy.videocoverselector;

import android.graphics.Bitmap;

public interface ICoverSelector {


    interface ThumbnailListener {
        void onThumbnail(int pos, long timeMs, final Bitmap bitmap);

        void loadComplete();
    }

    interface OnFrameAtTimeListener{
        void onFrameAtTime(Bitmap bitmap);
    }

    interface OnSliderMoveListener {
        void onSliderMove(long startTime, int type, int moveX);
    }

}

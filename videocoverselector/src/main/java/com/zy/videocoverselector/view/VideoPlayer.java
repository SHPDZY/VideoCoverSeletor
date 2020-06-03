package com.zy.videocoverselector.view;

import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.zy.videocoverselector.utils.LogUtils;

import java.io.IOException;

/**
 * Video Player
 */
public class VideoPlayer {
    private MediaPlayer mediaPlayer;
    private State state = State.IDLE;
    private OnStateChangeListener onStateChangeListener;
    private Handler handler;
    private boolean prePause;
    private TextureView textureView;
    private boolean isPrepared;

    public VideoPlayer() {
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (state == State.PREPAREING) {
                    if (prePause) {
                        state = State.PAUSE;
                        prePause = false;
                    }
//                    else {
//                        start();
//                    }
                    if (onStateChangeListener != null) {
                        onStateChangeListener.onPrepared();
                    }
                }
                isPrepared = true;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                state = State.COMPLETE;
                if (onStateChangeListener != null) {
                    onStateChangeListener.onComplete();
                }
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("mediaplayer错误", "what:" + what + "  extra:" + extra);
                return true;
            }
        });
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    if (onStateChangeListener != null) {
                        onStateChangeListener.onRenderingStart();
                    }
                    refreshProgress();
                }
                return false;
            }
        });
    }

    /**
     * bind TextureView
     */
    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
        if (textureView.isAvailable()) {
            mediaPlayer.setSurface(new Surface(textureView.getSurfaceTexture()));
        } else {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    mediaPlayer.setSurface(new Surface(surface));
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
        }
    }

    public void reset() {
        mediaPlayer.reset();
        state = State.IDLE;
        isPrepared = false;
        if (onStateChangeListener != null) {
            onStateChangeListener.onReset();
            onStateChangeListener = null;
        }
    }

    /**
     * play url
     */
    public void setDataSource(String url) {
        try {
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepare() {
        try {
            isPrepared = false;
            mediaPlayer.prepareAsync();
            state = State.PREPAREING;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (state == State.PREPAREING || state == State.COMPLETE || state == State.PAUSE) {
            mediaPlayer.start();
            state = State.PLAYING;
        }
    }

    public void seekTo(long startTime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(startTime, MediaPlayer.SEEK_CLOSEST);
        } else {
            mediaPlayer.seekTo((int) (startTime / 1000));
        }
    }

    private void refreshProgress() {
        if (state != State.PLAYING) {
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer == null) {
                    return;
                }
                if (onStateChangeListener != null) {
                    onStateChangeListener.onProgressUpdate(mediaPlayer.getCurrentPosition() * 1f / mediaPlayer.getDuration());
                }
                refreshProgress();
            }
        }, 100);
    }

    public void pause() {
        if (state == State.PREPAREING && !prePause) {
            prePause = true;
            if (onStateChangeListener != null) {
                onStateChangeListener.onPause();
            }
        } else if (state == State.PLAYING) {
            mediaPlayer.pause();
            state = State.PAUSE;
            if (onStateChangeListener != null) {
                onStateChangeListener.onPause();
            }
        }
    }

    public boolean startOrPause() {
        if (state == State.PREPAREING || state == State.COMPLETE || state == State.PAUSE) {
            mediaPlayer.start();
            state = State.PLAYING;
            return true;
        } else {
            pause();
            return false;
        }
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public void stop() {
        mediaPlayer.stop();
        state = State.STOP;
        if (onStateChangeListener != null) {
            onStateChangeListener.onStop();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    private float getRateFloat(float video, int device) {
        if (video < device) {
            return device / video;
        } else {
            return video / device;
        }
    }

    public State getState() {
        return state;
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }


    public interface OnStateChangeListener {
        void onPrepared();

        void onReset();

        void onRenderingStart();

        void onProgressUpdate(float per);

        void onPause();

        void onStop();

        void onComplete();
    }

    public enum State {
        /**
         * 空闲
         */
        IDLE,
        /**
         * 准备中
         */
        PREPAREING,
        /**
         * 播放中
         */
        PLAYING,
        /**
         * 暂停中
         */
        PAUSE,
        /**
         * 播放停止
         */
        STOP,
        /**
         * 播放结束
         */
        COMPLETE
    }
}
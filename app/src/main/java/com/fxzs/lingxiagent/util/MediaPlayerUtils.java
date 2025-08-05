package com.fxzs.lingxiagent.util;

import android.media.MediaPlayer;
import android.text.TextUtils;

public class MediaPlayerUtils {

    private static class SingletonHelper {
        private static final MediaPlayerUtils INSTANCE = new MediaPlayerUtils();
    }

    public static MediaPlayerUtils getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private MediaPlayer mPlayer;
    private String path = "";

    // 私有构造函数，防止外部实例化
    MediaPlayerUtils() {}

    public void create() {
        mPlayer = new MediaPlayer();
    }

    public void play(String uri) {
        if (TextUtils.isEmpty(uri)) return;
        path = uri;
        if (mPlayer != null) {
            mPlayer.reset();
            try {
                mPlayer.setDataSource(uri);
                mPlayer.prepare();
                mPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    public void pause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public void stop() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.reset();
        }
    }

    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public String getUri() {
        return path;
    }

    public MediaPlayer getPlayer() {
        return mPlayer;
    }
}
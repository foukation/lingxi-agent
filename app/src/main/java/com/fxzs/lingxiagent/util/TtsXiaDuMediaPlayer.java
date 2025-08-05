package com.fxzs.lingxiagent.util;

import android.media.MediaPlayer;
import android.text.TextUtils;

import java.util.concurrent.LinkedBlockingDeque;


public class TtsXiaDuMediaPlayer {
    private static final String TAG = "TtsXiaDuMediaPlayer";
    private final LinkedBlockingDeque<String> ttsUrls = new LinkedBlockingDeque<>();
    private String currentQid = null;
    private MediaPlayer player;
    private OnPlayerStopListener mOnPlayerStopListener;

    public void stop() {
        currentQid = null;
        ttsUrls.clear();
        if (player != null && player.isPlaying()) {
            player.stop();
            if(mOnPlayerStopListener != null) {
                mOnPlayerStopListener.playerStop();
            }
        }
    }

    public void release() {
        if (player == null)
            return;
        player.setOnCompletionListener(null);
        player.release();
        player = null;
    }

    public void speak(String qid, String ttsUrl) {
        if (!TextUtils.equals(qid, currentQid)) {
            stop();
            currentQid = qid;
        }
        MediaPlayerUtils.getInstance().stop();
        ttsUrls.add(ttsUrl);
        checkToPlay();
    }

    public void setOnPlayerStopListener(OnPlayerStopListener onPlayerStopListener){
        if(onPlayerStopListener != null) {
            this.mOnPlayerStopListener = onPlayerStopListener;
        }
    }

    private void checkToPlay() {
        if (player.isPlaying()) {
            return;
        }
        String url = ttsUrls.poll();
//        Timber.tag(TAG).d("checkToPlay:%s", url);
        if(TextUtils.isEmpty(url)) {
            if(mOnPlayerStopListener != null) {
                mOnPlayerStopListener.playerStop();
            }
            return;
        }
        try {
            player.reset();
            player.setDataSource(url);
            player.prepare();
            player.start();
        } catch (Exception e) {
            // ..
        }
    }


    public static TtsXiaDuMediaPlayer getInstance() {
        return Holder.INSTANCE;
    }

    private TtsXiaDuMediaPlayer() {
        player = new MediaPlayer();
        MediaPlayer.OnCompletionListener completionListener = mp -> checkToPlay();
        player.setOnCompletionListener(completionListener);
    }

    private static class Holder {
        private static final TtsXiaDuMediaPlayer INSTANCE = new TtsXiaDuMediaPlayer();
    }

    public interface OnPlayerStopListener {
        void playerStop();
    }
}

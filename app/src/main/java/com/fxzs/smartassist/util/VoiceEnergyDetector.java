package com.fxzs.smartassist.util;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class VoiceEnergyDetector {
    private static final String TAG = "VoiceEnergyDetector";
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_DURATION_MS = 100; // 100ms 缓冲区
    private static final int SAMPLE_PER_MS = SAMPLE_RATE / 1000;
    private static final int BUFFER_SIZE_SAMPLES = SAMPLE_PER_MS * BUFFER_DURATION_MS;
    private static final int BUFFER_SIZE_BYTES = BUFFER_SIZE_SAMPLES * 2; // 16bit 单声道
    private AudioRecord audioRecord;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private int bufferSize;

    public interface OnVolumeChangedListener {
        void onVolumeChanged(double dB);
    }

    public VoiceEnergyDetector() {
        initAudioRecorder();
    }

    @SuppressLint("MissingPermission")
    private void initAudioRecorder() {
        // 计算有效缓冲区大小
        int minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
        );
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.d(TAG,"Invalid min buffer size");
            return;
        }
        bufferSize = Math.max(minBufferSize, BUFFER_SIZE_BYTES); // 取较大值保证可用性

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
            );
        } catch (IllegalArgumentException e) {
            Log.d("AudioRecord initialization failed %s  ", e.toString());
        }
    }
    public void stopListening() {
        isRecording.set(false);
        try {
            if (audioRecord != null) {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
                audioRecord.release();
                audioRecord = null;
            }
        } catch (Exception e) {
            Log.d("Stop recording failed %s  ", e.toString());
        }
    }


}
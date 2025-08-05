package com.fxzs.lingxiagent.util;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fxzs.smartassist.model.meeting.callback.OnAmplitudeListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 音频录制管理器
 * 负责管理音频录制的启动、暂停、停止以及文件保存
 * 
 * 音频格式针对腾讯云语音识别API优化：
 * - 格式: M4A (AAC编码)
 * - 采样率: 16kHz (腾讯云推荐)
 * - 比特率: 64kbps (平衡质量与文件大小)
 * - 声道: 单声道 (减少文件大小)
 * - 位深度: 16bit (AAC编码器默认)
 * 
 * 参考文档: https://cloud.tencent.com/document/product/1093/37823
 */
public class AudioRecorderManager {
    private static final String TAG = "AudioRecorderManager";
    
    // 音频录制器
    private MediaRecorder mediaRecorder;
    
    // 录音文件路径
    private String audioFilePath;
    
    // 录音状态
    private boolean isRecording = false;
    private boolean isPaused = false;
    
    // 上下文
    private final Context context;
    
    // 音频质量配置 - 针对腾讯云语音识别优化
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int OUTPUT_FORMAT = MediaRecorder.OutputFormat.MPEG_4; // M4A格式
    private static final int AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC;
    private static final int SAMPLE_RATE = 16000; // 腾讯云推荐16kHz采样率
    private static final int BIT_RATE = 64000; // 64kbps，平衡质量和文件大小
    private static final int AUDIO_CHANNELS = 1; // 单声道，减少文件大小
    
    // 录音文件目录
    private static final String AUDIO_DIR_NAME = "MeetingRecordings";

    private Runnable amplitudeRunnable;
    OnAmplitudeListener amplitudeListener;
    private Handler handler = new Handler(Looper.getMainLooper());
    public AudioRecorderManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * 开始录音
     * @param meetingTitle 会议标题，用于生成文件名
     * @return 是否成功开始录音
     */
    public boolean startRecording(String meetingTitle) {
        if (isRecording) {
            Log.w(TAG, "Already recording");
            return false;
        }
        
        try {
            // 创建录音文件
            audioFilePath = createAudioFilePath(meetingTitle);
            
            // 初始化MediaRecorder - 针对腾讯云语音识别优化
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(AUDIO_SOURCE);
            mediaRecorder.setOutputFormat(OUTPUT_FORMAT);
            mediaRecorder.setAudioEncoder(AUDIO_ENCODER);
            mediaRecorder.setAudioSamplingRate(SAMPLE_RATE);
            mediaRecorder.setAudioEncodingBitRate(BIT_RATE);
            mediaRecorder.setAudioChannels(AUDIO_CHANNELS); // 设置为单声道
            mediaRecorder.setOutputFile(audioFilePath);
            
            // 准备并开始录音
            mediaRecorder.prepare();
            mediaRecorder.start();
            
            isRecording = true;
            isPaused = false;

            // 定时获取振幅
            amplitudeRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaRecorder != null) {
                        int amplitude = mediaRecorder.getMaxAmplitude();
                        if (amplitudeListener != null) {
                            amplitudeListener.onAmplitude(amplitude);
                        }
                        handler.postDelayed(this, 100); // 每100ms获取一次
                    }
                }
            };
            handler.post(amplitudeRunnable);
            
            Log.i(TAG, "录音开始 - 腾讯云语音识别优化配置");
            Log.i(TAG, "录音文件路径: " + audioFilePath);
            Log.i(TAG, "音频格式: M4A (AAC编码)");
            Log.i(TAG, "采样率: " + SAMPLE_RATE + "Hz (腾讯云推荐)");
            Log.i(TAG, "比特率: " + BIT_RATE + "bps");
            Log.i(TAG, "声道数: " + AUDIO_CHANNELS + " (单声道)");
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to start recording", e);
            releaseRecorder();
            return false;
        }
    }


    public void setAmplitudeListener(OnAmplitudeListener listener) {
        this.amplitudeListener = listener;
    }
    
    /**
     * 暂停录音 (Android N及以上版本支持)
     * @return 是否成功暂停
     */
    public boolean pauseRecording() {
        if (!isRecording || isPaused) {
            Log.w(TAG, "Not recording or already paused");
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder.pause();
                isPaused = true;
                Log.i(TAG, "Recording paused");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to pause recording", e);
                return false;
            }
        } else {
            Log.w(TAG, "Pause not supported on this Android version");
            return false;
        }
    }
    
    /**
     * 恢复录音 (Android N及以上版本支持)
     * @return 是否成功恢复
     */
    public boolean resumeRecording() {
        if (!isRecording || !isPaused) {
            Log.w(TAG, "Not paused");
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder.resume();
                isPaused = false;
                Log.i(TAG, "Recording resumed");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to resume recording", e);
                return false;
            }
        } else {
            Log.w(TAG, "Resume not supported on this Android version");
            return false;
        }
    }
    
    /**
     * 停止录音并保存文件
     * @return 保存的音频文件路径，失败返回null
     */
    public String stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "Not recording");
            return null;
        }
        
        try {
            mediaRecorder.stop();
            
            // 检查文件是否存在
            File audioFile = new File(audioFilePath);
            if (audioFile.exists() && audioFile.length() > 0) {
                Log.i(TAG, "录音停止并保存成功");
                Log.i(TAG, "文件路径: " + audioFilePath);
                Log.i(TAG, "文件大小: " + audioFile.length() + " bytes (" + (audioFile.length() / 1024) + " KB)");
                Log.i(TAG, "文件名: " + audioFile.getName());
                Log.i(TAG, "保存目录: " + audioFile.getParent());
                return audioFilePath;
            } else {
                Log.e(TAG, "Audio file is empty or doesn't exist");
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop recording", e);
            return null;
        } finally {
            isRecording = false;
            isPaused = false;
            releaseRecorder();
        }
    }
    
    /**
     * 取消录音（不保存文件）
     */
    public void cancelRecording() {
        if (!isRecording) {
            return;
        }
        
        try {
            mediaRecorder.stop();
        } catch (Exception e) {
            Log.e(TAG, "Error stopping recorder during cancel", e);
        }
        
        // 删除录音文件
        if (audioFilePath != null) {
            File audioFile = new File(audioFilePath);
            if (audioFile.exists()) {
                audioFile.delete();
                Log.i(TAG, "Recording cancelled and file deleted");
            }
        }
        
        isRecording = false;
        isPaused = false;
        releaseRecorder();
    }
    
    /**
     * 释放录音器资源
     */
    private void releaseRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing recorder", e);
            }
            mediaRecorder = null;
        }
    }
    
    /**
     * 创建音频文件路径
     * @param meetingTitle 会议标题
     * @return 文件路径
     */
    private String createAudioFilePath(String meetingTitle) {
        // 获取应用私有目录
        File audioDir = new File(context.getExternalFilesDir(null), AUDIO_DIR_NAME);
        if (!audioDir.exists()) {
            audioDir.mkdirs();
            Log.i(TAG, "创建录音目录: " + audioDir.getAbsolutePath());
        }
        
        // 生成文件名：会议标题_时间戳.m4a
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        
        // 清理文件名中的非法字符
        String cleanTitle = meetingTitle.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5\\-_]", "_");
        String fileName = cleanTitle + "_" + timestamp + ".m4a";
        
        return new File(audioDir, fileName).getAbsolutePath();
    }
    
    /**
     * 获取当前录音状态
     * @return 是否正在录音
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * 获取暂停状态
     * @return 是否暂停中
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * 获取当前录音文件路径
     * @return 文件路径
     */
    public String getCurrentAudioPath() {
        return audioFilePath;
    }
    
    /**
     * 获取录音文件保存目录
     * @return 目录路径
     */
    public String getRecordingsDirectory() {
        File audioDir = new File(context.getExternalFilesDir(null), AUDIO_DIR_NAME);
        return audioDir.getAbsolutePath();
    }
    
    /**
     * 获取音频格式配置信息
     * @return 音频格式详细信息
     */
    public String getAudioFormatInfo() {
        return String.format(Locale.getDefault(), 
            "音频格式配置 (腾讯云优化):\n" +
            "格式: M4A (AAC编码)\n" +
            "采样率: %dHz\n" +
            "比特率: %dbps (%dkbps)\n" +
            "声道数: %d (单声道)\n" +
            "音频源: 麦克风",
            SAMPLE_RATE, BIT_RATE, BIT_RATE/1000, AUDIO_CHANNELS);
    }
}
package com.fxzs.lingxiagent.view.common;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fxzs.lingxiagent.R;
import com.fxzs.smartassist.util.ZUtil.SizeUtils;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * 创建者：ZyOng
 * 描述：
 * 创建时间：2025/7/31 下午2:56
 */

public class VoiceRecordView extends FrameLayout {

    public interface RecordListener {
        void onStartRecord();
        void onStopRecord();
        boolean onCancelRecord();
    }

    private TextView mStateTV;
    private ImageView ivBg;
    private int[] locationVoice = new int[2];
    private int bottomPadding = 0;
    private VoiceButton wxVoiceButton;
    private volatile MediaRecorder mRecorder;
    private File voiceFile;
    private RecordListener recordListener;
    private boolean runningObtainDecibelThread = true;
//    private final Handler handler = new Handler();
    private final Handler decibelHandler = new DecibelHandler(this);

    private View view;
    public VoiceRecordView(Context context) {
        super(context);
        init(context);
    }

    public VoiceRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VoiceRecordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        voiceFile =  new File(getContext().getCacheDir(), "temp.3gp");
        if (bottomPadding == 0){
            bottomPadding = SizeUtils.dpToPx(24);
        }
        Log.d("测测ce2 ","高度"+bottomPadding);
        view = LayoutInflater.from(context).inflate(R.layout.dialog_record, this, true);
        mStateTV = view.findViewById(R.id.rc_audio_state_text);
        ivBg = view.findViewById(R.id.iv_bg);
        wxVoiceButton = view.findViewById(R.id.btn_wx_voice);

        mStateTV.setVisibility(View.VISIBLE);
        mStateTV.setText("松手发送,上移取消");
        mStateTV.setTextColor(ContextCompat.getColor(context, R.color.color_A0A0A0));
        ivBg.setImageResource(R.drawable.shape_voice_bg);

        ConstraintLayout flLayout = view.findViewById(R.id.fl_layout);
        flLayout.setPadding(0, 0, 0, bottomPadding);

        setBackgroundColor(Color.TRANSPARENT); // 背景透明
        setVisibility(GONE);
    }

    public void setRecordListener(RecordListener listener) {
        this.recordListener = listener;
    }

    public void setBottomPadding(int px) {
        this.bottomPadding = px;
        if (view == null){
            return;
        }
        Log.d("测测ce ","高度"+px);
        ConstraintLayout flLayout = view.findViewById(R.id.fl_layout);
        flLayout.setPadding(0, 0, 0, bottomPadding);
    }

    public void show() {
//        if (recordListener != null && !isShown()) {
        if (!isShown()) {
            setVisibility(VISIBLE);
            if (recordListener != null) {
                recordListener.onStartRecord();
            }
        }
    }

    public void dismiss() {
        if (isShown()) {
            setVisibility(GONE);
            if (recordListener != null) {
                recordListener.onStopRecord();
            }
        }
    }

    public void switchVoiceStatus(boolean send) {
        if (!send) {
            mStateTV.setText("松开取消");
            mStateTV.setTextColor(ContextCompat.getColor(getContext(), R.color.color_EE3636));
            ivBg.setImageResource(R.drawable.shape_voice_cancel_bg);
        } else {
            mStateTV.setText("松手发送,上移取消");
            mStateTV.setTextColor(ContextCompat.getColor(getContext(), R.color.color_A0A0A0));
            ivBg.setImageResource(R.drawable.shape_voice_bg);
        }
    }

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }



    /**
     * 执行录音操作
     */
    //int num = 0 ;
    public boolean startRecording() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            toast("录音启动失败[" +"缺少 RECORD_AUDIO 权限"+"]");
            return false;
        }
        if (mRecorder != null) {
            mRecorder.reset();
        } else {
            mRecorder = new MediaRecorder();
        }
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.setOutputFile(voiceFile.getAbsolutePath());
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            mRecorder.release();
            mRecorder = null;
            toast("录音启动失败[" + e.getMessage() + "]");
            return false;
        }
        runningObtainDecibelThread = true;
        decibelHandler.sendEmptyMessage(0);
        return true;
    }

    /**
     * 取消录音对话框和停止录音
     */
    public void stopRecording() {
        runningObtainDecibelThread = false;
        decibelHandler.removeCallbacksAndMessages(null);
        switchVoiceStatus(true);
        if (mRecorder != null) {
            try {
                mRecorder.stop();//停止时没有prepare，就会报stop failed
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        this.dismiss();
        if (wxVoiceButton != null) {
            wxVoiceButton.removeAllMessage();
        }
    }



    private static class DecibelHandler extends Handler {
        private final WeakReference<VoiceRecordView> ref;

        public DecibelHandler(VoiceRecordView view) {
            super(Looper.getMainLooper());
            ref = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VoiceRecordView view = ref.get();
            if (view == null || !view.runningObtainDecibelThread) return;

            try {
                if (view.mRecorder != null) {
                    int amp = view.mRecorder.getMaxAmplitude();
                    double db = 20 * Math.log10(amp + 1e-6);
                    view.wxVoiceButton.addVoiceSize((int) db);
                }
            } catch (IllegalStateException ignored) {
            }

            sendEmptyMessageDelayed(0, 500);

        }
    }




    private void toast(String content) {
        GlobalToast.show((Activity)getContext(), content, GlobalToast.Type.ERROR);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopRecording();
        decibelHandler.removeCallbacksAndMessages(null);
    }


}


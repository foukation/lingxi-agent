package com.fxzs.lingxiagent.view.meeting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.chat.callback.RequestCallback;
import com.fxzs.lingxiagent.util.AudioRecorderManager;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.CommonDialog;
import com.fxzs.lingxiagent.view.common.ConfirmDialog;
import com.fxzs.lingxiagent.view.common.LoadingProgressDialog;
import com.fxzs.lingxiagent.viewmodel.meeting.VMRealtimeMeeting;
import android.widget.Toast;
import com.fxzs.lingxiagent.MainActivity;
import com.fxzs.smartassist.model.meeting.callback.OnAmplitudeListener;

public class RealtimeMeetingActivity extends BaseActivity<VMRealtimeMeeting> {
    
    private static final String TAG = "RealtimeMeetingActivity";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1001;
    
    // UI Components
    private ImageView ivBack;
    private ImageView ivMore;
    private LinearLayout layoutNewMeeting;
    private TextView tvMeetingTitle;
    private LinearLayout layoutRecordingContent;
    private FrameLayout layoutMicCircle;
    private ImageView ivMicrophone;
    private LinearLayout layoutWaveAnimation;
    private View waveBar1, waveBar2, waveBar3;
    private FrameLayout btnPauseMeeting;
    private FrameLayout btnStopRecording;
    private TextView tvRecordingTime;
    private TextView tvStatusText;
    
    // Animation variables
    private Animation[] waveAnimations;
    private Handler animationHandler;
    private Runnable waveAnimationRunnable;
    
    // Audio recording
    private AudioRecorderManager audioRecorderManager;
    private long recordingStartTime;
    private Runnable timeUpdateRunnable;
    
    // Intent extras
    private static final String EXTRA_TAB_TYPE = "tab_type";
    private static final String EXTRA_SELECTED_LANGUAGE = "selected_language";
    private static final String EXTRA_MEETING_TITLE = "meeting_title"; // 已废弃：标题固定为"AI实时会议"
    
    private int tabType;
    private String selectedLanguage;
    private String meetingTitle;
    
    // Dialogs
    private LoadingProgressDialog progressDialog;
    private ConfirmDialog newMeetingDialog;
    private ConfirmDialog confirmExitDialog;
    private AnimationDrawable voiceAnimation;
    private OnAmplitudeListener onAmplitudeListener;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_realtime_meeting;
    }
    
    @Override
    protected Class<VMRealtimeMeeting> getViewModelClass() {
        return VMRealtimeMeeting.class;
    }
    
    @Override
    protected void initializeViews() {
        // Initialize UI components
        ivBack = findViewById(R.id.iv_back);
        ivMore = findViewById(R.id.iv_more);
        layoutNewMeeting = findViewById(R.id.layout_new_meeting);
        tvMeetingTitle = findViewById(R.id.tv_meeting_title);
        layoutRecordingContent = findViewById(R.id.layout_recording_content);
        layoutMicCircle = findViewById(R.id.layout_mic_circle);
        ivMicrophone = findViewById(R.id.iv_microphone);
        layoutWaveAnimation = findViewById(R.id.layout_wave_animation);
        waveBar1 = findViewById(R.id.wave_bar_1);
        waveBar2 = findViewById(R.id.wave_bar_2);
        waveBar3 = findViewById(R.id.wave_bar_3);
        btnPauseMeeting = findViewById(R.id.btn_pause_meeting);
        btnStopRecording = findViewById(R.id.btn_stop_recording);
        tvRecordingTime = findViewById(R.id.tv_recording_time);
        tvStatusText = findViewById(R.id.tv_status_text);
        
        // Initialize animation handler
        animationHandler = new Handler();
        initWaveAnimations();
        
        // Set click listeners
        ivBack.setOnClickListener(v -> showConfirmExitDialog());
        layoutNewMeeting.setOnClickListener(v -> showNewMeetingDialog());
        btnPauseMeeting.setOnClickListener(v -> pauseMeeting());
        btnStopRecording.setOnClickListener(v -> endMeetingWithProgress());
        
        // Initialize audio recording
        audioRecorderManager = new AudioRecorderManager(this);
        viewModel.initAudioRecorder(audioRecorderManager);

        onAmplitudeListener =  new OnAmplitudeListener() {
            @Override
            public void onAmplitude(int amplitude) {
                int progress = (int) ((amplitude / 32767.0) * 100);
                ZUtils.print("amplitude = "+amplitude+"progress === "+progress);
                if(progress == 0){
                    ivMicrophone.setBackgroundResource(R.drawable.ic_mic1);
                }else if(progress > 30){
                    ivMicrophone.setBackgroundResource(R.drawable.ic_mic4);
                }else if(progress > 20){
                    ivMicrophone.setBackgroundResource(R.drawable.ic_mic3);
                }else if(progress > 10){
                    ivMicrophone.setBackgroundResource(R.drawable.ic_mic2);
                }

            }
        };
        
        // Check permissions and start recording
        checkAudioPermissionAndStart();
        
        // Get meeting info from intent
        tabType = getIntent().getIntExtra(EXTRA_TAB_TYPE, 0);
        selectedLanguage = getIntent().getStringExtra(EXTRA_SELECTED_LANGUAGE);
        // 标题固定为"AI实时会议"，不从Intent获取
        meetingTitle = "AI实时会议";

        // Set data to ViewModel
        if (selectedLanguage != null) {
            viewModel.setSelectedLanguage(selectedLanguage);
        }
        // 设置固定标题
        tvMeetingTitle.setText(meetingTitle);

        // 设置语音动画
        ivMicrophone.setBackgroundResource(R.drawable.voice_animation);
        voiceAnimation = (AnimationDrawable) ivMicrophone.getBackground();
//        startAnimation();
    }
    
    @Override
    protected void setupDataBinding() {
        Log.d(TAG, "设置数据绑定");
        // Bind ViewModel data
        // 数据绑定由setupObservers处理
    }
    
    @Override
    protected void setupObservers() {
        Log.d(TAG, "设置观察者");
        
        // 观察录音状态
        viewModel.getIsRecording().observe(this, isRecording -> {
            if (isRecording != null) {
                Log.d(TAG, "录音状态变化: " + isRecording);
                updateRecordingUI(isRecording);
                if (viewModel.getIsRecording().getValue() == Boolean.TRUE) {
                    tvStatusText.setText("正在录音...");
                    startTimeUpdate();
                } else {
                    tvStatusText.setText("录音已停止");
                    if (timeUpdateRunnable != null) {
                tvRecordingTime.removeCallbacks(timeUpdateRunnable);
            }
                }
            }
        });
        
        // 观察会议标题 - 标题固定为"AI实时会议"，不响应ViewModel变化
        // viewModel.getMeetingTitle().observe(this, title -> {
        //     if (title != null && !title.isEmpty()) {
        //         tvMeetingTitle.setText(title);
        //     }
        // });
        
        // 观察错误消息
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // 观察进度消息
        viewModel.getProgressMessage().observe(this, message -> {
            if (message != null && progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.setMessage(message);
            }
        });
        
        // 观察识别结果
        viewModel.getRecognitionResult().observe(this, result -> {
            if (result != null) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                
                if (result.isSuccess()) {
                    // 识别成功，跳转到MeetingActivity
                    Intent intent = MeetingActivity.createIntent(
                        RealtimeMeetingActivity.this,
                        result.getMeetingId(),
                        result.getMessage(),
                        tabType
                    );
                    startActivity(intent);
                    finish();
                } else {
                    // 识别失败，显示错误并返回主界面
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(RealtimeMeetingActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }, 2000);
                }
            }
        });
    }
    
    
    private void checkAudioPermissionAndStart() {
        Log.d(TAG, "检查录音权限");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "录音权限未授予，请求权限");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        } else {
            Log.d(TAG, "录音权限已授予，开始录音");
            startRecording();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "录音权限授予成功，开始录音");
                startRecording();
            } else {
                Log.w(TAG, "录音权限被拒绝，退出应用");
                showToast("需要录音权限才能开始会议");
                finish();
            }
        }
    }
    
    private void startRecording() {
        // UI显示标题固定为"AI实时会议"
        String displayTitle = "AI实时会议";
        tvMeetingTitle.setText(displayTitle);

        // 为录音文件生成唯一标题
        String recordingTitle = "AI实时会议_" + System.currentTimeMillis();

        Log.d(TAG, "开始录音 - 会议标题: " + recordingTitle);
        viewModel.startRecording(recordingTitle,onAmplitudeListener);
        recordingStartTime = System.currentTimeMillis();
    }
    
    private void stopRecording() {
        Log.d(TAG, "停止录音");
        viewModel.stopRecording();
    }



    private void startTimeUpdate() {
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewModel.getIsRecording().getValue() == Boolean.TRUE) {
                    long elapsedTime = System.currentTimeMillis() - recordingStartTime;
                    updateRecordingTime(elapsedTime);
                    tvRecordingTime.postDelayed(this, 1000);
                }
            }
        };
        tvRecordingTime.post(timeUpdateRunnable);
    }
    
    private void updateRecordingTime(long elapsedMillis) {
        long seconds = elapsedMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        String timeText;
        if (hours > 0) {
            timeText = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            timeText = String.format("%02d:%02d", minutes, seconds % 60);
        }
        
        tvRecordingTime.setText(timeText);
    }
    
    private void updateRecordingUI(boolean recording) {
        Log.d(TAG, "更新录音UI状态 - recording: " + recording + ", isPaused: " + audioRecorderManager.isPaused());
        // 只在真正停止录音时才隐藏UI，暂停时不改变布局
        if (recording) {
            Log.d(TAG, "UI状态: 显示录音界面，启动动画");
            layoutRecordingContent.setVisibility(View.VISIBLE);
            btnStopRecording.setVisibility(View.VISIBLE);
            startWaveAnimation();
        } else if (!audioRecorderManager.isPaused()) {
            // 只有在不是暂停状态时才隐藏UI
            Log.d(TAG, "UI状态: 隐藏录音界面，停止动画");
            layoutRecordingContent.setVisibility(View.GONE);
            btnStopRecording.setVisibility(View.GONE);
            stopWaveAnimation();
        } else {
            // 暂停状态下只停止波浪动画，不隐藏UI
            Log.d(TAG, "UI状态: 保持录音界面显示，停止动画 (暂停状态)");
            stopWaveAnimation();
        }
    }
    
    private void initWaveAnimations() {
        waveAnimations = new Animation[3];
        
        // Load different scale animations for each wave bar with built-in delays
        waveAnimations[0] = AnimationUtils.loadAnimation(this, R.anim.wave_scale_animation_1);
        waveAnimations[1] = AnimationUtils.loadAnimation(this, R.anim.wave_scale_animation_2);
        waveAnimations[2] = AnimationUtils.loadAnimation(this, R.anim.wave_scale_animation_3);
    }
    
    private void startWaveAnimation() {
        Log.d(TAG, "启动波形动画");
        if (waveBar1 != null && waveBar2 != null && waveBar3 != null) {
            waveBar1.startAnimation(waveAnimations[0]);
            waveBar2.startAnimation(waveAnimations[1]);
            waveBar3.startAnimation(waveAnimations[2]);
            Log.d(TAG, "波形动画已启动 - 3个波形条动画运行中");
        } else {
            Log.w(TAG, "波形动画启动失败 - 波形条控件为空");
        }
    }
    
    private void stopWaveAnimation() {
        Log.d(TAG, "停止波形动画");
        if (waveBar1 != null && waveBar2 != null && waveBar3 != null) {
            waveBar1.clearAnimation();
            waveBar2.clearAnimation();
            waveBar3.clearAnimation();
            Log.d(TAG, "波形动画已停止 - 所有波形条动画已清除");
        } else {
            Log.w(TAG, "波形动画停止失败 - 波形条控件为空");
        }
    }
    
    private void showMoreOptions() {
        showNewMeetingDialog();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Stop recording
        if (viewModel.getIsRecording().getValue() == Boolean.TRUE) {
            stopRecording();
        }
        
        
        // Stop wave animation
        stopWaveAnimation();
        
        // Clean up animation handler
        if (animationHandler != null && waveAnimationRunnable != null) {
            animationHandler.removeCallbacks(waveAnimationRunnable);
        }
        
        // Clean up dialogs
        dismissAllDialogs();
    }
    
    private void dismissAllDialogs() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (newMeetingDialog != null && newMeetingDialog.isShowing()) {
                newMeetingDialog.dismiss();
            }
            if (confirmExitDialog != null && confirmExitDialog.isShowing()) {
                confirmExitDialog.dismiss();
            }
        } catch (Exception e) {
            // 忽略对话框清理异常
        }
    }
    
    @Override
    public void onBackPressed() {
        showConfirmExitDialog();
    }
    
    private void endMeetingWithProgress() {
        Log.d(TAG, "=== 开始结束会议流程 ===");
        
        showProgressDialog();
        
        // 调用ViewModel处理结束会议流程
        viewModel.endMeetingWithProgress();
    }
    
    private void showProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }
        
        progressDialog = new LoadingProgressDialog(this)
                .setMessage("会议生成中...")
                .setCancelable(false);
        progressDialog.show();
        progressDialog.setCancel(new RequestCallback() {
            @Override
            public void callback(Object data) {


                CommonDialog.showConfirmDialog(RealtimeMeetingActivity.this, "将不保存会议内容",
                        "请确认是否退出", "退出",
                        new CommonDialog.OnDialogClickListener() {
                            @Override
                            public void onConfirm() {

                                viewModel.cancelTask();
//                                if (viewModel.getIsRecording().getValue() == Boolean.TRUE || audioRecorderManager.isPaused()) {
//                            showProgressDialog();
                                    stopRecording();
                                    new Handler().postDelayed(() -> {
                                        if (!isFinishing() && !isDestroyed()) {
//                                    hideProgressDialog();
//                                    showToast("会议已保存");
                                            finish(); // 回到主界面
                                        }
                                    }, 500);
//                                }
                            }

                            @Override
                            public void onCancel() {
                                // 用户点击不同意，不做任何操作
                            }
                        });
            }
        });
    }
    
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing() && !isFinishing() && !isDestroyed()) {
            progressDialog.dismiss();
        }
    }
    
    private void updateProgressMessage(String message) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(message);
            Log.d(TAG, "进度提示更新: " + message);
        }
    }
    
    
    private void showNewMeetingDialog() {
        if (newMeetingDialog != null && newMeetingDialog.isShowing()) {
            return;
        }
        
        newMeetingDialog = new ConfirmDialog(this)
                .setTitle("当前会议还未结束，是否新建会议？")
                .setSubtitle("新建后，当前会议自动生成会议记录")
                .setCancelText("继续会议")
                .setConfirmText("新建会议")
                .setOnConfirmDialogListener(new ConfirmDialog.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        if (viewModel.getIsRecording().getValue() == Boolean.TRUE) {
                            // 如果正在录音，显示进度窗口后停止当前会议并退出到主界面
                            showProgressDialog();
                            stopRecording();
                            new Handler().postDelayed(() -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    hideProgressDialog();
                                    showToast("会议已保存");
                                    finish(); // 退出到主界面
                                }
                            }, 2000);
                        } else {
                            // 如果没在录音，直接退出到主界面
                            finish();
                        }
                    }
                    
                    @Override
                    public void onCancel() {
                        // 继续当前会议，什么都不做
                    }
                });
        
        newMeetingDialog.show();
    }
    
    private void showConfirmExitDialog() {
        if (confirmExitDialog != null && confirmExitDialog.isShowing()) {
            return;
        }
        
        confirmExitDialog = new ConfirmDialog(this)
                .setTitle("返回将不保存当前会议内容")
                .setSubtitle("请确认是否退出")
                .setCancelText("取消")
                .setConfirmText("退出")
                .setOnConfirmDialogListener(new ConfirmDialog.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        // 先保存录音文件，然后回到主界面
                        if (viewModel.getIsRecording().getValue() == Boolean.TRUE || audioRecorderManager.isPaused()) {
//                            showProgressDialog();
                            stopRecording();
//                            new Handler().postDelayed(() -> {
//                                if (!isFinishing() && !isDestroyed()) {
//                                    hideProgressDialog();
//                                    showToast("会议已保存");
                                    finish(); // 回到主界面
//                                }
//                            }, 500);
                        } else {
                            finish(); // 直接回到主界面
                        }
                    }
                    
                    @Override
                    public void onCancel() {
                        // 取消退出，什么都不做
                    }
                });
        
        confirmExitDialog.show();
    }
    
    
    private void pauseMeeting() {
        if (viewModel.getIsRecording().getValue() == Boolean.TRUE) {
            Log.d(TAG, "暂停会议");
            // 暂停录音但不保存文件，不退出Activity
            pauseRecording();
            tvStatusText.setText("录音暂停");
            Log.d(TAG, "状态文本更新: 录音暂停");
            showToast("会议已暂停");
            
            // 更新按钮功能为恢复录音，只改变文字
            btnPauseMeeting.setOnClickListener(v -> resumeMeeting());
            updatePauseButtonText(false);
        }
    }
    
    private void resumeMeeting() {
        if (viewModel.getIsRecording().getValue() != Boolean.TRUE && audioRecorderManager.isPaused()) {
            Log.d(TAG, "恢复会议录音");
            // 尝试恢复录音
            boolean success = audioRecorderManager.resumeRecording();
            if (success) {
//                viewModel.startRecording(meetingTitle,onAmplitudeListener);
//                recordingStartTime = System.currentTimeMillis(); // 重新记录时间
                viewModel.resumeRecording(meetingTitle);
                btnPauseMeeting.setOnClickListener(v -> pauseMeeting());
                updatePauseButtonText(true);
            } else {
                Log.w(TAG, "录音恢复失败，开始新的录音");
                // 如果恢复失败，开始新的录音
                if (meetingTitle == null || meetingTitle.isEmpty()) {
                    meetingTitle = "AI实时会议_继续_" + System.currentTimeMillis();
                }
                startRecording();
                btnPauseMeeting.setOnClickListener(v -> pauseMeeting());
                updatePauseButtonText(true);
            }
        }
    }
    
    private void pauseRecording() {
        Log.d(TAG, "暂停录音");
        // 使用AudioRecorderManager的pauseRecording方法
        boolean success = audioRecorderManager.pauseRecording();
        if (success) {
//            viewModel.stopRecording();
            if (timeUpdateRunnable != null) {
                tvRecordingTime.removeCallbacks(timeUpdateRunnable);
            }
            viewModel.setIsRecording(false);
        } else {
            Log.e(TAG, "录音暂停失败，执行完全停止");
            // 如果暂停失败，则完全停止录音
            stopRecording();
        }
    }
    
    private void updatePauseButtonText(boolean isRecording) {
        TextView pauseText = btnPauseMeeting.findViewById(R.id.tv_pause_text);
        if (pauseText == null) {
            // 如果没有找到TextView，创建一个引用
            pauseText = findViewById(R.id.tv_pause_text);
        }
        if (pauseText != null) {
            pauseText.setText(isRecording ? "暂停会议" : "继续会议");
        }
    }
    
    public void showNewMeetingDialogFromMenu() {
        showNewMeetingDialog();
    }


    /**
     * 开始动画
     */
    private void startAnimation() {
        if (voiceAnimation != null && !voiceAnimation.isRunning()) {
            voiceAnimation.start();
        }
    }

    /**
     * 停止动画
     */
    private void stopAnimation() {
        if (voiceAnimation != null && voiceAnimation.isRunning()) {
            voiceAnimation.stop();
        }
    }
    
}
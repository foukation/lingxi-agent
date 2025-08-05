package com.fxzs.lingxiagent.view.common;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.fxzs.lingxiagent.R;
import java.io.IOException;

public class VoiceInputDemoActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1001;
    
    private VoiceInputBox voiceInputBox;
    private Button btnPlayAudio;
    private TextView tvAudioStatus;
    private LinearLayout llChatMessages;
    
    private MediaPlayer mediaPlayer;
    private String lastRecordedAudioPath;
    private boolean isPlaying = false;
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_input_demo);
        
        initViews();
        setupVoiceInputBox();
        setupAudioPlayer();
        
        // 启动时直接显示一个思维链示例
        // showInitialThinkingExample();
    }
    
    private void initViews() {
        voiceInputBox = findViewById(R.id.voice_input_box);
        btnPlayAudio = findViewById(R.id.btn_play_audio);
        tvAudioStatus = findViewById(R.id.tv_audio_status);
        llChatMessages = findViewById(R.id.ll_chat_messages);
    }
    
    private void showInitialThinkingExample() {
        android.util.Log.d("VoiceInputDemoActivity", "=== showInitialThinkingExample() 开始 ===");
        
        // 直接创建一个思维链示例
        ChatMessageView thinkingExample = new ChatMessageView(this);
        thinkingExample.setUserInput("示例：生成一个文档大纲");
        
        android.util.Log.d("VoiceInputDemoActivity", "创建思维链示例");
        thinkingExample.setAIResponseWithThinking(
            "这是一个带有思维链的AI响应示例。点击上方的\"思考过程\"可以查看详细的思维过程。",
            5,
            "这是AI的思考过程：首先分析用户的需求，然后制定解决方案，最后生成合适的回复。"
        );
        
        thinkingExample.setOnRefreshClickListener(() -> {
            Toast.makeText(this, "刷新思维链示例", Toast.LENGTH_SHORT).show();
        });
        
        llChatMessages.addView(thinkingExample);
        android.util.Log.d("VoiceInputDemoActivity", "=== showInitialThinkingExample() 结束 ===");
    }
    
    private void setupVoiceInputBox() {
        voiceInputBox.setOnTextInputListener(new VoiceInputBox.OnTextInputListener() {
            @Override
            public void onTextChanged(String text) {
                // 可以在这里实现实时输入提示等功能
            }
            
            @Override
            public void onSendClicked(String text) {
                // 添加聊天消息到界面
                addChatMessage(text, "这是AI对文本输入的响应");
                
                // 演示不同类型的AI响应，包括思维链
                // demonstrateAIResponses();
            }
        });
        
        voiceInputBox.setOnVoiceInputListener(new VoiceInputBox.OnVoiceInputListener() {
            @Override
            public void onVoiceRecordStart() {
                // 开始录音
            }
            
            @Override
            public void onVoiceRecordStop(String audioFilePath) {
                lastRecordedAudioPath = audioFilePath;
                updateAudioPlayButton();
                // 这里可以调用语音识别API
                processVoiceInput(audioFilePath);
            }
            
            @Override
            public void onVoiceRecordCancel() {
                // 录音已取消
            }
            
            @Override
            public void onPermissionRequired() {
                requestRecordPermission();
            }
        });
    }
    
    
    private void processVoiceInput(String audioFilePath) {
        // 模拟语音识别结果
        handler.postDelayed(() -> {
            String recognizedText = "这是一条通过语音输入的消息";
            
            // 添加聊天消息到界面
            addChatMessage(recognizedText, "这是AI对语音输入的响应，可以包含更多内容...");
            
            // 演示不同类型的AI响应
            demonstrateAIResponses();
        }, 1000);
    }
    
    private void addChatMessage(String userInput, String aiResponse) {
        ChatMessageView chatMessageView = new ChatMessageView(this);
        chatMessageView.setUserInput(userInput);
        chatMessageView.setAIResponseWithThinking(
            aiResponse,
            5,
            "这是AI的思考过程：首先分析用户的需求，然后制定解决方案，最后生成合适的回复。");
        chatMessageView.setOnRefreshClickListener(() -> {
            Toast.makeText(this, "刷新AI响应", Toast.LENGTH_SHORT).show();
        });
        llChatMessages.addView(chatMessageView);
    }
    
    private void demonstrateAIResponses() {
        // 演示1: 思考过程响应
        ChatMessageView thinkingMessage = new ChatMessageView(this);
        thinkingMessage.setUserInput("生成一个文档大纲");
        Toast.makeText(this, "VoiceInputDemoActivity: 开始设置思维链响应", Toast.LENGTH_SHORT).show();
        android.util.Log.d("VoiceInputDemoActivity", "=== 设置思维链响应开始 ===");
        android.util.Log.d("VoiceInputDemoActivity", "思考时间: 3秒");
        android.util.Log.d("VoiceInputDemoActivity", "思维文本长度: " + "用户请求生成文档大纲。基于输入内容分析，需要创建一个结构化的文档框架，包含标题、章节和要点。考虑到用户可能需要编辑和调整，应该提供灵活的编辑功能。生成过程需要考虑内容的逻辑性和完整性，确保大纲能够满足用户的需求。".length());
        thinkingMessage.setAIResponseWithThinking(
            "我们先为你生成了可以自由编辑的大纲，确认无误后，可以点击下方蓝色按钮，进入到生成文档环节。",
            3,
            "用户请求生成文档大纲。基于输入内容分析，需要创建一个结构化的文档框架，包含标题、章节和要点。考虑到用户可能需要编辑和调整，应该提供灵活的编辑功能。生成过程需要考虑内容的逻辑性和完整性，确保大纲能够满足用户的需求。"
        );
        android.util.Log.d("VoiceInputDemoActivity", "=== 设置思维链响应结束 ===");
        thinkingMessage.setOnRefreshClickListener(() -> {
            Toast.makeText(this, "刷新思考过程响应", Toast.LENGTH_SHORT).show();
        });
        llChatMessages.addView(thinkingMessage);
        
        // 演示2: 天气查询响应（带思考过程）
        handler.postDelayed(() -> {
            ChatMessageView weatherMessage = new ChatMessageView(this);
            weatherMessage.setUserInput("今天天气怎么样？");
            weatherMessage.setAIResponseWithThinking(
                "上海今天(3月26日)的天气是晴。最低温度17°C，最高温度31°C，空气质量良，pm2.5指数70，湿度52，东北风1级。\n\n从小时天气来看:\n18:00，晴，27°C，北风1级，湿度47\n19:00，晴，24°C，东南风2级，湿度53",
                2,
                "用户查询\"今天天气\"是在寻求当前日期的天气信息。当前时间为2025年07月03日星期四，因此需要查询2025年07月03日上海市徐汇区龙华街道云视路的天气情况。合理的规划步骤应包括查询用户所在地的天气信息，然后给出答案。"
            );
            weatherMessage.setOnRefreshClickListener(() -> {
                Toast.makeText(this, "刷新天气查询", Toast.LENGTH_SHORT).show();
            });
            llChatMessages.addView(weatherMessage);
        }, 1000);
        
        // 演示3: 话题响应
        handler.postDelayed(() -> {
            ChatMessageView topicMessage = new ChatMessageView(this);
            topicMessage.setUserInput("给我介绍一下淘宝");
            topicMessage.setAIResponseWithTitle(
                "话题1相关内容",
                "淘宝，中国领先的电商平台，成立于2003年，阿里巴巴集团旗下的子公司。作为全球最大的C2C和B2C市场，它连接了消费者、商家和中小企业，推动了中国乃至全球电子商务的发展。通过不断创新，淘宝致力于打造安全、便捷的在线购物环境，倡导\"让天下没有难做的生意\"。"
            );
            topicMessage.setOnRefreshClickListener(() -> {
                Toast.makeText(this, "刷新话题响应", Toast.LENGTH_SHORT).show();
            });
            llChatMessages.addView(topicMessage);
        }, 2000);
        
        // 演示4: 会议摘要响应
        handler.postDelayed(() -> {
            ChatMessageView meetingMessage = new ChatMessageView(this);
            meetingMessage.setUserInput("帮我总结一下刚才的会议");
            meetingMessage.getAIResponseView().setAsMeetingSummary(
                "淘宝，中国领先的电商平台，成立于2003年，阿里巴巴集团旗下的子公司。作为全球最大的C2C和B2C市场，它连接了消费者、商家和中小企业，推动了中国乃至全球电子商务的发展。",
                "1. 公司简介\n2. 培训与发展\n3. 安全与合规\n4. 淘宝公司发展史\n5. 企业文化"
            );
            meetingMessage.setOnRefreshClickListener(() -> {
                Toast.makeText(this, "刷新会议摘要", Toast.LENGTH_SHORT).show();
            });
            llChatMessages.addView(meetingMessage);
        }, 3000);
        
        // 演示5: 简单响应
        handler.postDelayed(() -> {
            ChatMessageView simpleMessage = new ChatMessageView(this);
            simpleMessage.showUserInput(false); // 不显示用户输入
            simpleMessage.getAIResponseView().setAsSimpleResponse(
                "会议内容",
                "淘宝，中国领先的电商平台，成立于2003年，阿里巴巴集团旗下的子公司。作为全球最大的C2C和B2C市场，它连接了消费者、商家和中小企业，推动了中国乃至全球电子商务的发展。通过不断创新，淘宝致力于打造安全、便捷的在线购物环境，倡导\"让天下没有难做的生意\"。"
            );
            simpleMessage.setOnRefreshClickListener(() -> {
                Toast.makeText(this, "刷新简单响应", Toast.LENGTH_SHORT).show();
            });
            llChatMessages.addView(simpleMessage);
        }, 4000);
    }
    
    private void requestRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.RECORD_AUDIO}, 
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "录音权限已授权", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要录音权限才能使用语音输入功能", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void setupAudioPlayer() {
        btnPlayAudio.setOnClickListener(v -> {
            if (isPlaying) {
                stopAudioPlayback();
            } else {
                startAudioPlayback();
            }
        });
    }
    
    private void updateAudioPlayButton() {
        if (lastRecordedAudioPath != null && !lastRecordedAudioPath.isEmpty()) {
            btnPlayAudio.setEnabled(true);
            tvAudioStatus.setText("就绪播放最近录音");
            btnPlayAudio.setText("播放最近录音");
        } else {
            btnPlayAudio.setEnabled(false);
            tvAudioStatus.setText("暂无录音文件");
            btnPlayAudio.setText("播放最近录音");
        }
    }
    
    private void startAudioPlayback() {
        if (lastRecordedAudioPath == null || lastRecordedAudioPath.isEmpty()) {
            Toast.makeText(this, "没有可播放的录音文件", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(lastRecordedAudioPath);
            mediaPlayer.prepare();
            
            mediaPlayer.setOnCompletionListener(mp -> {
                stopAudioPlayback();
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                stopAudioPlayback();
                Toast.makeText(this, "播放音频时出错", Toast.LENGTH_SHORT).show();
                return true;
            });
            
            mediaPlayer.start();
            isPlaying = true;
            btnPlayAudio.setText("停止播放");
            tvAudioStatus.setText("正在播放录音...");
            
        } catch (IOException e) {
            Toast.makeText(this, "无法播放音频文件", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopAudioPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        isPlaying = false;
        btnPlayAudio.setText("播放最近录音");
        tvAudioStatus.setText("播放已停止");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        // 释放所有ChatMessageView的资源
        for (int i = 0; i < llChatMessages.getChildCount(); i++) {
            if (llChatMessages.getChildAt(i) instanceof ChatMessageView) {
                ((ChatMessageView) llChatMessages.getChildAt(i)).release();
            }
        }
    }
}
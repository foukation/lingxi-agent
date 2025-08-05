package com.fxzs.lingxiagent.view.drawing;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.chat.dto.DrawingToChatBean;
import com.fxzs.lingxiagent.model.drawing.dto.AspectRatioDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingStyleDto;
import com.fxzs.lingxiagent.util.SpeechRecognitionUtil;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.view.chat.SuperChatActivity;
import com.fxzs.lingxiagent.view.chat.SuperChatContainActivity;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.view.common.VoiceInputDialog;
import com.fxzs.lingxiagent.viewmodel.drawing.VMDrawing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AI绘画主界面
 */
public class DrawingEditActivity1 extends BaseActivity<VMDrawing> {
    
    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_AUDIO_PERMISSION = 1002;
    private static final int REQUEST_CONTINUE_EDIT = 1003;
    
    private RecyclerView rvStyles;
    private LinearLayout llRatioButtons;
    private EditText etPrompt;
    private ImageView btnVoiceInput;
    private Button btnGenerate;
    private ScrollView svOverlayContent;
    private LinearLayout llConversation;
    private ScrollView svContent;  // 实际的内容滚动视图
    private LinearLayout llContentContainer;  // sv_content内部的LinearLayout容器
    private LinearLayout llBottomFixed;
    private EditText etMessageInput;
    private LinearLayout llBottomCard;
    
    // 输入状态切换相关
    private FrameLayout flInitialState;
    private LinearLayout rlTextInputState;
    private TextView tvHoldToSpeak;
    private ImageView ivVoiceOrKeyboard;
    
    private DrawingStyleAdapter styleAdapter;
    private DrawingImageDto currentImage;
    private SpeechRecognitionUtil speechRecognitionUtil;
    private VoiceInputDialog voiceInputDialog;
    
    // 动态创建的视图引用
    private androidx.cardview.widget.CardView currentProgressCard;
    private ImageView currentBigStar;
    private ImageView currentSmallStar;
    private TextView currentProgressText;
    
    // 参考图片相关
    private LinearLayout cvReferenceImage;
    private ImageView ivReferenceImage;
    private androidx.cardview.widget.CardView cvMinimizedImage;  // 缩小后的图片容器
    private ImageView ivMinimizedImage;  // 缩小后的图片，绝对定位显示
    private String referenceImageUrl;
    private boolean isContinueEditMode = false;  // 是否是继续编辑模式
    private boolean isInitializing = true;  // 是否正在初始化
    
    @Override
    protected int getLayoutResource() {
        // 检查是否是继续编辑模式
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("continue_edit", false)) {
            // 使用没有ScrollView的布局
            return R.layout.activity_drawing_continue_edit;
        }
        // 默认使用原布局
        return R.layout.activity_drawing;
    }
    
    @Override
    protected Class<VMDrawing> getViewModelClass() {
        return VMDrawing.class;
    }
    
    @Override
    protected void initializeViews() {
        boolean isContinueEdit = false;
        Intent intent = getIntent();
        if (intent != null) {
            isContinueEdit = intent.getBooleanExtra("continue_edit", false);
        }

        // 只初始化实际需要的控件
        rvStyles = findViewById(R.id.rv_style_real);
        Log.d("init", "rvStyles=" + rvStyles);
        llRatioButtons = findViewById(R.id.ll_ratio_container_real);
        Log.d("init", "llRatioButtons=" + llRatioButtons);
        etPrompt = findViewById(R.id.et_prompt);
        Log.d("init", "etPrompt=" + etPrompt);
        btnVoiceInput = findViewById(R.id.iv_voice_input_or_send);
        Log.d("init", "btnVoiceInput=" + btnVoiceInput);
//        btnGenerate = findViewById(R.id.btn_generate);
//        Log.d("init", "btnGenerate=" + btnGenerate);
        ivReferenceImage = findViewById(R.id.iv_reference_image);
        Log.d("init", "ivReferenceImage=" + ivReferenceImage);

        if (ivReferenceImage != null) {
            ivReferenceImage.setScaleType(CENTER_CROP);
            if (intent != null) {
                String referenceImageUrl = intent.getStringExtra("reference_image_url");
                if (referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
                    Glide.with(this)
                        .load(referenceImageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .centerCrop() // 强制裁切填满3:4区域
                        .into(ivReferenceImage);
                }
            }
        }

        // 设置返回按钮
        View backBtn = findViewById(R.id.iv_back);
        Log.d("init", "backBtn=" + backBtn);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        // 只在需要的情况下初始化和设置监听
        if (rvStyles != null) setupStyleRecyclerView();
        if (llRatioButtons != null) setupRatioButtons();

        // 输入框相关
        if (etPrompt != null) {
            etPrompt.postDelayed(() -> {
                etPrompt.requestFocus();
                etPrompt.setSelection(etPrompt.getText().length());
                android.view.inputmethod.InputMethodManager imm = 
                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etPrompt, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
                isInitializing = false;
            }, 500);
        }

        // 只在需要时设置星星动画
        setupSparkleAnimation();
        // 只在需要时处理Intent数据
        handleIntentData();
        setupInputStateToggle();
    }
    
    @Override
    protected void setupDataBinding() {
        // 绑定输入框
        DataBindingUtils.bindEditText(etPrompt, viewModel.getPrompt(), this);
        
        // 添加文本变化监听器，用于清除hiddenPrompt和动态切换图标
        etPrompt.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isFirstChange = true;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 当用户开始输入时，如果不是继续编辑模式，清除hiddenPrompt和参考图片
                if (!isContinueEditMode && isFirstChange && count > 0 && currentImage != null) {
                    viewModel.setHiddenPrompt(null);
                    viewModel.clearReferenceImageUrl();
                    referenceImageUrl = null;  // 同时清除本地的参考图片URL
                    isFirstChange = false;
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // 根据输入框内容动态切换图标
                updateVoiceSendIcon(s.toString().trim());
                // 如果用户开始输入，显示当前选择的风格和比例信息
                if (s.length() > 0) {
                    logCurrentSelections();
                }
            }
        });

        // 添加焦点监听器，用于在继续编辑模式下调整图片显示
        etPrompt.setOnFocusChangeListener((v, hasFocus) -> {
            if (isContinueEditMode) {
                Log.d("DrawingActivity", "Input focus changed: " + hasFocus);
                // 当输入框获得焦点时，调整图片显示
                if (hasFocus) {
                    // 延迟调整，确保键盘已经弹起
                    etPrompt.postDelayed(() -> adjustImageForKeyboard(true), 100);
                } else {
                    // 输入框失去焦点时，恢复图片显示
                    adjustImageForKeyboard(false);
                }
            }
        });
        
        // 绑定生成按钮状态 - 移除生成按钮相关代码
        // DataBindingUtils.bindEnabled(btnGenerate, viewModel.getGenerateEnabled(), this);
        
        // 绑定进度显示
        viewModel.getIsGenerating().observe(this, isGenerating -> {
            Log.d("DrawingActivity", "IsGenerating observed: " + isGenerating);
            if (isGenerating) {
                // 由于实际内容在sv_content中，这个控件始终是可见的
                // 只需要添加新的进度卡片
                addProgressCard();
                // 启动星星动画
                setupSparkleAnimation();
            }
        });
        
        // 初始化图标状态
        updateVoiceSendIcon(viewModel.getPrompt().get() != null ? viewModel.getPrompt().get() : "");
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 处理传入的参数
        handleIntent();
        // 设置ViewModel的继续编辑模式
        Intent intent = getIntent();
        boolean isContinueEdit = intent != null && intent.getBooleanExtra("continue_edit", false);
        viewModel.setContinueEditMode(isContinueEdit);
        // 设置软键盘模式：使用adjustResize保持输入框可见
        getWindow().setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
            android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        );

        // 添加键盘监听器
        setupKeyboardListener();
    }
    
    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String prompt = intent.getStringExtra("prompt");
            String style = intent.getStringExtra("style");
            String referenceImageUrl = intent.getStringExtra("reference_image_url");
            boolean isContinueEdit = intent.getBooleanExtra("continue_edit", false);

            Log.d("DrawingActivity", "handleIntent - referenceImageUrl: " + referenceImageUrl);
            Log.d("DrawingActivity", "handleIntent - isContinueEdit: " + isContinueEdit);

            // 设置继续编辑模式和参考图片URL
            if (isContinueEdit) {
                this.isContinueEditMode = true;
                if (referenceImageUrl != null) {
                    this.referenceImageUrl = referenceImageUrl;
                    viewModel.setReferenceImageUrl(referenceImageUrl);
                }
            }

            // 只有在非继续编辑模式下才设置prompt
            if (prompt != null && !prompt.isEmpty() && !isContinueEdit) {
                // 设置提示词
                viewModel.getPrompt().set(prompt);
            }

            if (style != null && !style.isEmpty()) {
                // 设置风格（需要在风格加载完成后选择）
                viewModel.setInitialStyle(style);
            }

            // 注意：参考图片的显示需要在视图初始化后处理，
            // 所以在handleIntentData中处理
        }
    }
    
    @Override
    protected void setupObservers() {
        // 观察风格列表
        viewModel.getStyles().observe(this, styles -> {
            if (styleAdapter != null && styles != null) {
                styleAdapter.setStyles(styles);

                // 同步当前选中的风格到UI
                if (viewModel.getSelectedStyle() != null) {
                    for (int i = 0; i < styles.size(); i++) {
                        if (styles.get(i).getId().equals(viewModel.getSelectedStyle().getId())) {
                            styleAdapter.setSelectedPosition(i);
                            Log.d("DrawingActivity", "Synced selected style to UI: " +
                                              viewModel.getSelectedStyle().getName() + " at position " + i);
                            break;
                        }
                    }
                }

                // 风格加载完成后更新输入框提示
                updateInputHint();
            }
        });
        
        // 观察宽高比列表
        viewModel.getAspectRatios().observe(this, ratios -> {
            updateRatioButtons(ratios);
        });
        
        // 观察生成结果
        viewModel.getGeneratedImage().observe(this, image -> {
            Log.d("DrawingActivity", "GeneratedImage observed: " + (image != null ? image.getImageUrl() : "null"));
            if (image != null && image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                currentImage = image;
                displayResult(image);

                // 生成成功后，重置继续编辑模式和清空关联参数
                if (isContinueEditMode) {
                    Log.d("DrawingActivity", "Continue edit completed, clearing associations");
                    isContinueEditMode = false;
                    viewModel.setContinueEditMode(false);
                    // 清空参考图片URL和隐藏提示词，避免影响后续生成
                    viewModel.clearReferenceImageUrl();
                    viewModel.setHiddenPrompt(null);
                    referenceImageUrl = null;
                }
                Log.d("DrawingActivity", "Image generation completed, reset continue edit mode");
            }
        });
        
        // 观察显示结果状态
        viewModel.getShowResult().observe(this, show -> {
            // 结果卡片现在是动态创建的
        });
        
        // 观察示例数据（如果需要在主界面显示示例）
        viewModel.getSamples().observe(this, samples -> {
            // 可以在这里更新示例UI
        });
        
        // 观察进度
        viewModel.getProgress().observe(this, progress -> {
            // 进度更新现在在addProgressCard中处理
            if (currentProgressText != null) {
                currentProgressText.setText(progress + "%");
            }
        });
    }
    
    // 设置输入状态切换
    private void setupInputStateToggle() {
        // 点击键盘图标切换到文本输入模式
//        ivVoiceOrKeyboard.setOnClickListener(v -> {
//            flInitialState.setVisibility(View.GONE);
//            rlTextInputState.setVisibility(View.VISIBLE);
//            // btnGenerate.setVisibility(View.VISIBLE); // 移除生成按钮
//
//            // 在继续编辑模式下，立即调整图片显示
//            if (isContinueEditMode) {
//                adjustImageForKeyboard(true);
//            }
//
//            // 自动弹出键盘
//            etPrompt.post(() -> {
//                etPrompt.requestFocus();
//                etPrompt.setSelection(etPrompt.getText().length());
//
//                // 禁止ScrollView自动滚动
//                if (findViewById(R.id.sv_content) != null) {
//                    findViewById(R.id.sv_content).setFocusable(false);
//                }
//
//                android.view.inputmethod.InputMethodManager imm =
//                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
//                if (imm != null) {
//                    imm.showSoftInput(etPrompt, android.view.inputmethod.InputMethodManager.SHOW_FORCED);
//                }
//            });
//        });
//
//        // 长按说话功能
//        tvHoldToSpeak.setOnTouchListener((v, event) -> {
//            switch (event.getAction()) {
//                case android.view.MotionEvent.ACTION_DOWN:
//                    // 开始录音
//                    showToast("开始录音");
//                    return true;
//                case android.view.MotionEvent.ACTION_UP:
//                    // 结束录音
//                    showToast("结束录音");
//                    return true;
//            }
//            return false;
//        });
        
        // 语音输入/发送按钮点击事件
        btnVoiceInput.setOnClickListener(v -> {
            String prompt = viewModel.getPrompt().get();
            Log.d("DrawingActivity", "Send button clicked - prompt: " + prompt);
            Log.d("DrawingActivity", "Send button clicked - currentImage: " + (currentImage != null ? "not null" : "null"));
            Log.d("DrawingActivity", "Send button clicked - isContinueEditMode: " + isContinueEditMode);

            // 记录当前选择的风格和比例
            logCurrentSelections();

            // 验证ViewModel中的数据
            verifyViewModelState();

            if (prompt != null && !prompt.isEmpty()) {
                // 有文本输入，执行发送功能
                if (currentImage != null || isContinueEditMode) {
                    // 已经生成过图片或者是继续编辑模式，在当前页面继续对话
                    Log.d("DrawingActivity", "Calling handleContinueConversation");
                    handleContinueConversation(prompt);
                } else {
                    // 第一次生成，跳转到对话页面
                    Log.d("DrawingActivity", "Calling handleFirstGeneration");
                    handleFirstGeneration(prompt);
                }
            } else {
                // 没有文本输入，执行语音输入功能
                Log.d("DrawingActivity", "No prompt text, showing voice input message");
                Toast.makeText(this, "语音输入功能暂未实现", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 处理继续对话（已有图片或继续编辑模式）
     */
    private void handleContinueConversation(String prompt) {
        // 检查是否是继续编辑模式
        if (isContinueEditMode) {
            Log.d("DrawingActivity", "In continue edit mode, preparing to return result");
            // 继续编辑模式：返回结果给调用页面，不在当前页面生成
            Intent resultIntent = new Intent();
            resultIntent.putExtra("edit_prompt", prompt);
            resultIntent.putExtra("reference_image_url", referenceImageUrl);
            resultIntent.putExtra("original_prompt", viewModel.getHiddenPrompt());
            if (viewModel.getSelectedStyle() != null) {
                resultIntent.putExtra("style", viewModel.getSelectedStyle().getName());
            }

            Log.d("DrawingActivity", "Returning result - edit_prompt: " + prompt);
            Log.d("DrawingActivity", "Returning result - reference_image_url: " + referenceImageUrl);
            Log.d("DrawingActivity", "Returning result - original_prompt: " + viewModel.getHiddenPrompt());
            Log.d("DrawingActivity", "Returning result - style: " + (viewModel.getSelectedStyle() != null ? viewModel.getSelectedStyle().getName() : "null"));

            setResult(RESULT_OK, resultIntent);
            finish(); // 关闭继续编辑页面，返回到原对话页面
            return;
        }

        // 普通继续对话模式：在当前页面生成
        // 清除关联，生成全新图片
        viewModel.setHiddenPrompt(null);
        viewModel.clearReferenceImageUrl();
        referenceImageUrl = null;

        // 切换到对话界面状态
        switchToConversationMode();

        // 添加用户消息
        addUserMessage(prompt);

        // 触发生成
        viewModel.generateImage();

        // 清空输入框
        etPrompt.setText("");
    }

    /**
     * 处理首次生成（跳转到对话页面）
     */
    private void handleFirstGeneration(String prompt) {
        Log.d("DrawingActivity", "=== handleFirstGeneration START ===");

        // 在跳转前记录当前状态
        verifyViewModelState();

        etPrompt.setText("");

//        Intent intent = new Intent(this, DrawingChatActivity.class);
//        intent.putExtra("prompt", prompt);
//
//        // 如果有参考图片URL，传递给新页面
//        if (referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
//            intent.putExtra("reference_image_url", referenceImageUrl);
//        }
//
//        // 传递选中的风格信息（包括ID和比例）
//        if (viewModel.getSelectedStyle() != null) {
//            intent.putExtra("style", viewModel.getSelectedStyle().getName());
//            intent.putExtra("style_id", String.valueOf(viewModel.getSelectedStyle().getId()));
//            android.util.Log.d("DrawingActivity", "Passing style: " + viewModel.getSelectedStyle().getName() +
//                              " (ID: " + viewModel.getSelectedStyle().getId() + ")");
//        }
//
//        // 传递选中的比例信息
//        String selectedRatio = viewModel.getSelectedRatio().get();
//        if (selectedRatio != null && !selectedRatio.isEmpty()) {
//            intent.putExtra("ratio", selectedRatio);
//            android.util.Log.d("DrawingActivity", "Passing ratio: " + selectedRatio);
//        }
//
//        android.util.Log.d("DrawingActivity", "Starting DrawingChatActivity...");
//        try {
//            startActivity(intent);
//            android.util.Log.d("DrawingActivity", "DrawingChatActivity started successfully");
//        } catch (Exception e) {
//            android.util.Log.e("DrawingActivity", "Failed to start DrawingChatActivity: " + e.getMessage(), e);
//        }
//
//        android.util.Log.d("DrawingActivity", "=== handleFirstGeneration END ===");


        Intent intent = new Intent(this, SuperChatContainActivity.class);
        intent.putExtra(Constant.INTENT_TYPE, SuperChatContainActivity.TYPE_DRAWING);


        DrawingToChatBean drawingToChatBean = new DrawingToChatBean();
        drawingToChatBean.setPrompt(prompt);

        // 如果有参考图片URL，传递给新页面
        if (referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
            drawingToChatBean.setReference_image_url(referenceImageUrl);
        }

        // 传递选中的风格信息（包括ID和比例）
        if (viewModel.getSelectedStyle() != null) {
            drawingToChatBean.setStyle(viewModel.getSelectedStyle().getName());
            drawingToChatBean.setStyle_id(String.valueOf(viewModel.getSelectedStyle().getId()));
            Log.d("DrawingActivity", "Passing style: " + viewModel.getSelectedStyle().getName() +
                    " (ID: " + viewModel.getSelectedStyle().getId() + ")");
        }

        // 传递选中的比例信息
        String selectedRatio = viewModel.getSelectedRatio().get();
        if (selectedRatio != null && !selectedRatio.isEmpty()) {
            drawingToChatBean.setRatio(selectedRatio);
            Log.d("DrawingActivity", "Passing ratio: " + selectedRatio);
        }



        Log.d("DrawingActivity", "=== handleFirstGeneration END ===");
        intent.putExtra(Constant.INTENT_DATA, drawingToChatBean);
        intent.putExtra(Constant.INTENT_DATA1, viewModel.getSelectedStyle());
        startActivity(intent);
    }

    /**
     * 切换到对话模式界面
     */
    private void switchToConversationMode() {
        // 隐藏底部卡片，显示对话界面
        if (llBottomCard != null) {
            llBottomCard.setVisibility(View.GONE);
        }
        if (svOverlayContent != null) {
            svOverlayContent.setVisibility(View.VISIBLE);
        }
        if (llBottomFixed != null) {
            llBottomFixed.setVisibility(View.VISIBLE);
        }

        // 确保输入框获得焦点
        if (etMessageInput != null) {
            etMessageInput.requestFocus();
        }

        Log.d("DrawingActivity", "Switched to conversation mode");
    }

    // 根据输入框内容动态切换语音/发送图标
    private void updateVoiceSendIcon(String text) {
        if (btnVoiceInput != null) {
            // 如果有文本输入，切换为发送图标状态
            btnVoiceInput.setSelected(!text.isEmpty());
        }
    }
    
    // 设置风格选择RecyclerView
    private void setupStyleRecyclerView() {
        styleAdapter = new DrawingStyleAdapter();
        styleAdapter.setOnStyleClickListener((style, position) -> {
            Log.d("DrawingActivity", "=== STYLE CLICK EVENT ===");
            Log.d("DrawingActivity", "Style clicked: " + style.getName() +
                              " (ID: " + style.getId() + ") at position " + position);
            try {
                if (viewModel != null && style != null) {
                    Log.d("DrawingActivity", "Setting style to ViewModel...");
                    viewModel.setSelectedStyle(style);
                    styleAdapter.setSelectedPosition(position);
                    // 验证设置是否成功
                    DrawingStyleDto actualStyle = viewModel.getSelectedStyle();
                    Log.d("DrawingActivity", "Style set to ViewModel: " +
                                      (actualStyle != null ? actualStyle.getName() + " (ID: " + actualStyle.getId() + ")" : "null"));
                    // 更新输入框提示，体现选中的风格和比例
                    updateInputHint();
                } else {
                    Log.e("DrawingActivity", "ViewModel or style is null!");
                }
            } catch (Exception e) {
                Log.e("DrawingActivity", "Error setting style: " + e.getMessage(), e);
            }
            Log.d("DrawingActivity", "=== STYLE CLICK END ===");
        });

        rvStyles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvStyles.setAdapter(styleAdapter);
    }
    
    // 设置宽高比按钮
    private void setupRatioButtons() {
        // 宽高比按钮在XML中定义，这里设置点击事件
        int[] buttonIds = {R.id.tv_ratio_9_16_real, R.id.tv_ratio_16_9_real, R.id.tv_ratio_4_3_real, R.id.tv_ratio_2_3_real, R.id.tv_ratio_1_1_real};
        int[] containerIds = {R.id.ll_ratio_9_16_real, R.id.ll_ratio_16_9_real, R.id.ll_ratio_4_3_real, R.id.ll_ratio_2_3_real, R.id.ll_ratio_1_1_real};
        String[] ratios = {"9:16", "16:9", "4:3", "2:3", "1:1"};

        for (int i = 0; i < buttonIds.length; i++) {
            LinearLayout container = findViewById(containerIds[i]);
            String ratio = ratios[i];

            // 验证容器是否找到
            if (container == null) {
                Log.e("DrawingActivity", "Container not found for ratio: " + ratio + " (ID: " + containerIds[i] + ")");
                continue;
            }

            Log.d("DrawingActivity", "Setting click listener for ratio: " + ratio);
            container.setOnClickListener(v -> {
                Log.d("DrawingActivity", "=== RATIO CLICK EVENT ===");
                Log.d("DrawingActivity", "Ratio clicked: " + ratio);
                try {
                    if (viewModel != null && viewModel.getSelectedRatio() != null) {
                        Log.d("DrawingActivity", "Setting ratio to ViewModel...");
                        viewModel.getSelectedRatio().set(ratio);
                        // 验证设置是否成功
                        String actualRatio = viewModel.getSelectedRatio().get();
                        Log.d("DrawingActivity", "Ratio set to ViewModel: " + actualRatio);
                        updateRatioButtonStates(ratio);
                        // 更新输入框提示，体现选中的风格和比例
                        updateInputHint();
                        // 立即记录当前选择状态
                        logCurrentSelections();
                    } else {
                        Log.e("DrawingActivity", "ViewModel or selectedRatio is null!");
                    }
                } catch (Exception e) {
                    Log.e("DrawingActivity", "Error setting ratio: " + e.getMessage(), e);
                }
                Log.d("DrawingActivity", "=== RATIO CLICK END ===");
            });
        }

        // 设置默认选中9:16
        viewModel.getSelectedRatio().set("9:16");
        updateRatioButtonStates("9:16");
        // 初始化时也要更新输入框提示
        updateInputHint();

        // 延迟记录初始选择状态，确保所有数据都已加载
        etPrompt.postDelayed(() -> {
            Log.d("DrawingActivity", "=== Initial Setup Complete ===");
            verifyControlsInitialization();
            logCurrentSelections();
        }, 1000);
    }
    
    // 更新宽高比按钮
    private void updateRatioButtons(List<AspectRatioDto> ratios) {
        // 可以根据后端返回的数据动态更新按钮
    }
    
    // 更新宽高比按钮状态
    private void updateRatioButtonStates(String selectedRatio) {
        int[] buttonIds = {R.id.tv_ratio_9_16_real, R.id.tv_ratio_16_9_real, R.id.tv_ratio_4_3_real, R.id.tv_ratio_2_3_real, R.id.tv_ratio_1_1_real};
        int[] containerIds = {R.id.ll_ratio_9_16_real, R.id.ll_ratio_16_9_real, R.id.ll_ratio_4_3_real, R.id.ll_ratio_2_3_real, R.id.ll_ratio_1_1_real};
        String[] ratios = {"9:16", "16:9", "4:3", "2:3", "1:1"};

        for (int i = 0; i < buttonIds.length; i++) {
            TextView button = findViewById(buttonIds[i]);
            LinearLayout container = findViewById(containerIds[i]);
            boolean isSelected = ratios[i].equals(selectedRatio);
            button.setSelected(isSelected);
            container.setSelected(isSelected);
        }
    }

    // 更新输入框提示，体现选中的风格和比例
    private void updateInputHint() {
        if (etPrompt == null) return;

        StringBuilder hintBuilder = new StringBuilder("发消息...");

        // 添加风格信息
        if (viewModel.getSelectedStyle() != null) {
            hintBuilder.append(" [风格: ").append(viewModel.getSelectedStyle().getName()).append("]");
        }

        // 添加比例信息
        String selectedRatio = viewModel.getSelectedRatio().get();
        if (selectedRatio != null && !selectedRatio.isEmpty()) {
            hintBuilder.append(" [比例: ").append(selectedRatio).append("]");
        }

        etPrompt.setHint(hintBuilder.toString());
    }

    // 记录当前选择的风格和比例信息
    private void logCurrentSelections() {
        Log.d("DrawingActivity", "=== Current Selections ===");

        // 记录风格选择
        if (viewModel.getSelectedStyle() != null) {
            Log.d("DrawingActivity", "Selected Style: " + viewModel.getSelectedStyle().getName() +
                              " (ID: " + viewModel.getSelectedStyle().getId() + ")");
        } else {
            Log.w("DrawingActivity", "No style selected!");
        }

        // 记录比例选择
        String selectedRatio = viewModel.getSelectedRatio().get();
        if (selectedRatio != null && !selectedRatio.isEmpty()) {
            Log.d("DrawingActivity", "Selected Ratio: " + selectedRatio);

            // 计算并显示实际的宽高
            int width = 512;
            int height = calculateHeightFromRatio(selectedRatio, width);
            Log.d("DrawingActivity", "Calculated dimensions: " + width + "x" + height);
        } else {
            Log.w("DrawingActivity", "No ratio selected!");
        }

        // 记录表单验证状态
        Log.d("DrawingActivity", "Generate enabled: " + viewModel.getGenerateEnabled().get());

        Log.d("DrawingActivity", "========================");
    }

    // 根据比例字符串和固定宽度计算高度（与ViewModel中的方法保持一致）
    private int calculateHeightFromRatio(String ratioStr, int width) {
        if (ratioStr == null || ratioStr.isEmpty()) {
            return width; // 默认1:1
        }

        try {
            String[] parts = ratioStr.split(":");
            if (parts.length == 2) {
                double widthRatio = Double.parseDouble(parts[0]);
                double heightRatio = Double.parseDouble(parts[1]);
                return (int) Math.round(width * heightRatio / widthRatio);
            }
        } catch (NumberFormatException e) {
            Log.e("DrawingActivity", "Error parsing ratio: " + ratioStr, e);
        }

        return width; // 默认1:1
    }

    // 验证ViewModel状态
    private void verifyViewModelState() {
        Log.d("DrawingActivity", "=== Pre-Generation Verification ===");

        // 检查ViewModel是否为空
        if (viewModel == null) {
            Log.e("DrawingActivity", "ViewModel is null!");
            return;
        }

        // 检查风格选择
        DrawingStyleDto selectedStyle = viewModel.getSelectedStyle();
        if (selectedStyle != null) {
            Log.d("DrawingActivity", "ViewModel selectedStyle: " +
                              selectedStyle.getName() + " (ID: " + selectedStyle.getId() + ")");
        } else {
            Log.w("DrawingActivity", "ViewModel selectedStyle is null!");
        }

        // 检查比例选择
        String selectedRatio = viewModel.getSelectedRatio().get();
        if (selectedRatio != null && !selectedRatio.isEmpty()) {
            Log.d("DrawingActivity", "ViewModel selectedRatio: " + selectedRatio);
            // 计算预期的尺寸
            int expectedHeight = calculateHeightFromRatio(selectedRatio, 512);
            Log.d("DrawingActivity", "Expected dimensions: 512x" + expectedHeight);
        } else {
            Log.w("DrawingActivity", "ViewModel selectedRatio is null or empty!");
        }

        // 检查提示词
        String prompt = viewModel.getPrompt().get();
        Log.d("DrawingActivity", "ViewModel prompt: " + prompt);

        // 检查生成状态
        boolean generateEnabled = viewModel.getGenerateEnabled().get();
        Log.d("DrawingActivity", "Generate enabled: " + generateEnabled);

        Log.d("DrawingActivity", "===============================");
    }

    // 验证控件初始化状态
    private void verifyControlsInitialization() {
        Log.d("DrawingActivity", "=== Controls Verification ===");

        // 检查ViewModel
        Log.d("DrawingActivity", "ViewModel: " + (viewModel != null ? "OK" : "NULL"));

        // 检查风格适配器
        Log.d("DrawingActivity", "StyleAdapter: " + (styleAdapter != null ? "OK" : "NULL"));

        // 检查比例按钮
        int[] containerIds = {R.id.ll_ratio_9_16_real, R.id.ll_ratio_16_9_real, R.id.ll_ratio_4_3_real, R.id.ll_ratio_2_3_real, R.id.ll_ratio_1_1_real};
        String[] ratios = {"9:16", "16:9", "4:3", "2:3", "1:1"};

        for (int i = 0; i < containerIds.length; i++) {
            LinearLayout container = findViewById(containerIds[i]);
            Log.d("DrawingActivity", "Ratio container " + ratios[i] + ": " + (container != null ? "OK" : "NULL"));
        }

        // 检查RecyclerView
        Log.d("DrawingActivity", "RecyclerView: " + (rvStyles != null ? "OK" : "NULL"));

        Log.d("DrawingActivity", "============================");
    }
    
    // 显示结果
    private void displayResult(DrawingImageDto image) {
        // 先预加载图片，加载完成后再替换进度卡片
        Glide.with(this)
            .load(image.getImageUrl())
            .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model,
                                            com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                    // 加载失败时也要移除进度卡片并显示错误
                    runOnUiThread(() -> {
                        removeProgressCard();
                        Toast.makeText(DrawingEditActivity1.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                    });
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model,
                                               com.bumptech.glide.request.target.Target<Drawable> target,
                                               com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                    // 图片加载完成后，移除进度卡片并显示图片
                    runOnUiThread(() -> {
                        removeProgressCard();
                        addAiReplyText();
                        addResultImage(image);
                        
                        // 在输入框中保留原始提示词，方便用户修改
                        if (etMessageInput != null && !etMessageInput.isFocused()) {
                            etMessageInput.setHint("发消息...");
                        }
                        
                        // 滚动到底部显示最新内容
                        if (!isInitializing) {
                            svOverlayContent.post(() -> svOverlayContent.fullScroll(View.FOCUS_DOWN));
                        }
                    });
                    return false;
                }
            })
            .preload();
    }
    
    // 移除进度卡片的辅助方法
    private void removeProgressCard() {
        if (currentProgressCard != null && currentProgressCard.getParent() != null) {
            View parent = (View) currentProgressCard.getParent();
            if (parent.getParent() == llContentContainer) {
                llContentContainer.removeView(parent);
            }
            currentProgressCard = null;
        }
    }
    
    // 添加AI回复文本
    private void addAiReplyText() {
        // 不再添加固定的文本，直接显示生成的图片
        // 如果需要显示接口返回的文本，可以从currentImage中获取
    }
    
    
    // 添加结果图片
    private void addResultImage(DrawingImageDto image) {
        // 创建CardView容器
        androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dpToPx(200), dpToPx(356));
        cardParams.bottomMargin = dpToPx(16);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(16));
        cardView.setCardElevation(0);
        
        // 创建FrameLayout
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        
        // 创建ImageView
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(CENTER_CROP);
        
        // 加载图片（移除过渡效果，因为图片已经预加载完成）
        Glide.with(this)
            .load(image.getImageUrl())
            .error(R.drawable.ic_image_placeholder)
            .into(imageView);
        
        // 设置点击事件
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(this, DrawingImageViewerActivity.class);
            intent.putExtra("image_url", image.getImageUrl());
            // 如果image对象没有prompt，使用当前输入的prompt
            String prompt = image.getPrompt();
            if (prompt == null || prompt.isEmpty()) {
                prompt = viewModel.getPrompt().get();
            }
            intent.putExtra("prompt", prompt);
            startActivity(intent);
        });
        
        // 创建底部按钮区域
        LinearLayout buttonLayout = createButtonLayout();
        
        frameLayout.addView(imageView);
        frameLayout.addView(buttonLayout);
        cardView.addView(frameLayout);
        if (llContentContainer != null) {
            llContentContainer.addView(cardView);
        }
        
        // 滚动到底部
        if (!isInitializing && svContent != null) {
            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    // 创建底部按钮布局
    private LinearLayout createButtonLayout() {
        LinearLayout buttonLayout = new LinearLayout(this);
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = android.view.Gravity.BOTTOM;
        buttonLayout.setLayoutParams(buttonParams);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setBackgroundResource(R.drawable.gradient_black_transparent);
        buttonLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        
        // 下载按钮
        LinearLayout downloadBtn = createActionButton("下载", R.drawable.ic_download, v -> downloadImage());
        
        // 分隔视图
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(8), 0));
        
        // 继续编辑按钮
        LinearLayout editBtn = createActionButton("继续编辑", 0, v -> onContinueEditClick());
        
        buttonLayout.addView(downloadBtn);
        buttonLayout.addView(divider);
        buttonLayout.addView(editBtn);
        
        return buttonLayout;
    }
    
    // 创建操作按钮
    private LinearLayout createActionButton(String text, int iconRes, View.OnClickListener listener) {
        LinearLayout button = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        button.setLayoutParams(params);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(android.view.Gravity.CENTER);
        button.setBackgroundResource(R.drawable.bg_button_rounded_white_alpha);
        button.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        button.setOnClickListener(listener);
        
        if (iconRes != 0) {
            ImageView icon = new ImageView(this);
            icon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16)));
            icon.setImageResource(iconRes);
            icon.setColorFilter(getResources().getColor(R.color.white));
            LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
            iconParams.rightMargin = dpToPx(4);
            icon.setLayoutParams(iconParams);
            button.addView(icon);
        }
        
        TextView tvText = new TextView(this);
        tvText.setText(text);
        tvText.setTextSize(12);
        tvText.setTextColor(getResources().getColor(R.color.white));
        button.addView(tvText);
        
        return button;
    }
    
    // 语音输入
    private void onVoiceInputClick() {
        if (checkAudioPermission()) {
            startVoiceInput();
        } else {
            requestAudioPermission();
        }
    }
    
    // 检查录音权限
    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }
    
    // 请求录音权限
    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_AUDIO_PERMISSION);
    }
    
    // 开始语音输入
    private void startVoiceInput() {
        try {
            if (speechRecognitionUtil == null) {
                speechRecognitionUtil = new SpeechRecognitionUtil(this);
                speechRecognitionUtil.setOnSpeechRecognitionListener(new SpeechRecognitionUtil.OnSpeechRecognitionListener() {
                @Override
                public void onResult(String text) {
                    if (voiceInputDialog != null) {
                        voiceInputDialog.showResult(text);
                        // 延迟关闭对话框并填充文本
                        etPrompt.postDelayed(() -> {
                            if (voiceInputDialog != null && voiceInputDialog.isShowing()) {
                                voiceInputDialog.dismiss();
                            }
                            // 将识别结果追加到输入框
                            String currentText = viewModel.getPrompt().get();
                            if (currentText != null && !currentText.isEmpty()) {
                                viewModel.getPrompt().set(currentText + " " + text);
                            } else {
                                viewModel.getPrompt().set(text);
                            }
                        }, 1000);
                    }
                }
                
                @Override
                public void onError(String errorMessage) {
                    if (voiceInputDialog != null) {
                        voiceInputDialog.showError(errorMessage);
                        etPrompt.postDelayed(() -> {
                            if (voiceInputDialog != null && voiceInputDialog.isShowing()) {
                                voiceInputDialog.dismiss();
                            }
                        }, 2000);
                    }
                }
                
                @Override
                public void onReadyForSpeech() {
                    if (voiceInputDialog != null) {
                        voiceInputDialog.showReady();
                    }
                }
                
                @Override
                public void onBeginningOfSpeech() {
                    if (voiceInputDialog != null) {
                        voiceInputDialog.showListening();
                    }
                }
                
                @Override
                public void onEndOfSpeech() {
                    // 语音输入结束
                }
                
                @Override
                public void onPartialResult(String partialText) {
                    if (voiceInputDialog != null) {
                        voiceInputDialog.showPartialResult(partialText);
                    }
                }
            });
        }
        
        // 显示语音输入对话框
        if (voiceInputDialog == null) {
            voiceInputDialog = new VoiceInputDialog(this);
            voiceInputDialog.setOnVoiceInputListener(() -> {
                if (speechRecognitionUtil != null) {
                    speechRecognitionUtil.cancelListening();
                }
            });
        }
        
        voiceInputDialog.show();
        if (speechRecognitionUtil != null) {
            speechRecognitionUtil.startListening();
        } else {
            Toast.makeText(this, "语音识别初始化失败，请重试", Toast.LENGTH_SHORT).show();
            if (voiceInputDialog != null && voiceInputDialog.isShowing()) {
                voiceInputDialog.dismiss();
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "语音输入启动失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (voiceInputDialog != null && voiceInputDialog.isShowing()) {
                voiceInputDialog.dismiss();
            }
        }
    }
    
    // 继续编辑
    private void onContinueEditClick() {
        if (currentImage != null) {
            // 跳转到继续编辑页面，让用户输入修改要求
            Intent intent = new Intent(this, DrawingEditActivity1.class);
            intent.putExtra("continue_edit", true);  // 标记为继续编辑模式
            intent.putExtra("reference_image_url", currentImage.getImageUrl());  // 传递参考图片URL
            intent.putExtra("original_prompt", currentImage.getPrompt()); // 传递原始提示词
            if (viewModel.getSelectedStyle() != null) {
                intent.putExtra("style", viewModel.getSelectedStyle().getName()); // 传递当前风格名称
            }

            // 启动继续编辑页面，等待结果返回
            startActivityForResult(intent, REQUEST_CONTINUE_EDIT);
        }
    }

    /**
     * 添加继续编辑的提示消息
     */
    private void addContinueEditMessage(DrawingImageDto image) {
        // 创建继续编辑提示布局
        LinearLayout editHintLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = dpToPx(16);
        editHintLayout.setLayoutParams(layoutParams);
        editHintLayout.setOrientation(LinearLayout.VERTICAL);
        editHintLayout.setGravity(android.view.Gravity.CENTER);
        editHintLayout.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

        // 设置背景（使用现有的渐变背景）
        editHintLayout.setBackgroundResource(R.drawable.bg_user_prompt_gradient);

        // 添加提示文字
        TextView tvHint = new TextView(this);
        tvHint.setText("基于以下图片继续编辑：");
        tvHint.setTextSize(14);
        tvHint.setTextColor(getResources().getColor(android.R.color.white));
        tvHint.setGravity(android.view.Gravity.CENTER);

        // 创建图片容器
        androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dpToPx(120), dpToPx(120));
        cardParams.topMargin = dpToPx(8);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(8));
        cardView.setCardElevation(dpToPx(2));

        ImageView ivRef = new ImageView(this);
        ivRef.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        ivRef.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // 加载图片
        Glide.with(this)
            .load(image.getImageUrl())
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_error)
            .into(ivRef);

        cardView.addView(ivRef);
        editHintLayout.addView(tvHint);
        editHintLayout.addView(cardView);

        // 添加到内容容器
        if (llContentContainer != null) {
            llContentContainer.addView(editHintLayout);
        }

        Log.d("DrawingActivity", "Added continue edit message with reference image");
    }

    // 下载图片
    private void downloadImage() {
        if (currentImage == null || currentImage.getImageUrl() == null) {
            Toast.makeText(this, "没有可下载的图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上不需要存储权限
            performDownload();
        } else {
            // Android 9及以下需要存储权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                performDownload();
            }
        }
    }
    
    // 执行下载
    private void performDownload() {
        Toast.makeText(this, "正在下载图片...", Toast.LENGTH_SHORT).show();
        
        Glide.with(this)
            .asBitmap()
            .load(currentImage.getImageUrl())
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    saveImageToGallery(resource);
                }
                
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }
                
                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    Toast.makeText(DrawingEditActivity1.this, "图片下载失败", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    // 保存图片到相册
    private void saveImageToGallery(Bitmap bitmap) {
        String fileName = "IYA_Drawing_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上使用MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.DESCRIPTION, "通通助手AI绘画");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/IYAProject");
            
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (FileOutputStream fos = (FileOutputStream) getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Android 9及以下使用传统方式
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "IYAProject");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            
            File imageFile = new File(storageDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                
                // 通知系统相册更新
                MediaStore.Images.Media.insertImage(getContentResolver(), imageFile.getAbsolutePath(), fileName, "通通助手AI绘画");
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
                
                Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("DrawingActivity", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == REQUEST_CONTINUE_EDIT) {
            Log.d("DrawingActivity", "REQUEST_CONTINUE_EDIT received");
            if (resultCode == RESULT_OK) {
                Log.d("DrawingActivity", "RESULT_OK received");
                if (data != null) {
                    Log.d("DrawingActivity", "Intent data is not null");
                    // 从继续编辑页面返回，获取用户输入的编辑要求
                    String editPrompt = data.getStringExtra("edit_prompt");
                    String referenceImageUrl = data.getStringExtra("reference_image_url");
                    String originalPrompt = data.getStringExtra("original_prompt");
                    String style = data.getStringExtra("style");

                    Log.d("DrawingActivity", "Received data - editPrompt: " + editPrompt);
                    Log.d("DrawingActivity", "Received data - referenceImageUrl: " + referenceImageUrl);
                    Log.d("DrawingActivity", "Received data - originalPrompt: " + originalPrompt);
                    Log.d("DrawingActivity", "Received data - style: " + style);

                    if (editPrompt != null && !editPrompt.isEmpty()) {
                        // 在当前对话页面中继续编辑
                        handleContinueEditInConversation(editPrompt, referenceImageUrl, originalPrompt, style);
                    } else {
                        Log.w("DrawingActivity", "editPrompt is null or empty");
                    }
                } else {
                    Log.w("DrawingActivity", "Intent data is null");
                }
            } else {
                Log.w("DrawingActivity", "Result code is not RESULT_OK: " + resultCode);
            }
        } else {
            Log.d("DrawingActivity", "Not REQUEST_CONTINUE_EDIT: " + requestCode);
        }
    }

    /**
     * 在对话页面中处理继续编辑
     */
    private void handleContinueEditInConversation(String editPrompt, String referenceImageUrl, String originalPrompt, String style) {
        Log.d("DrawingActivity", "handleContinueEditInConversation called");
        Log.d("DrawingActivity", "editPrompt: " + editPrompt);
        Log.d("DrawingActivity", "referenceImageUrl: " + referenceImageUrl);
        Log.d("DrawingActivity", "originalPrompt: " + originalPrompt);

        // 设置继续编辑模式
        isContinueEditMode = true;
        viewModel.setContinueEditMode(true);

        // 设置参考图片和原始提示词
        if (referenceImageUrl != null) {
            viewModel.setReferenceImageUrl(referenceImageUrl);
            this.referenceImageUrl = referenceImageUrl;
        }
        if (originalPrompt != null) {
            viewModel.setHiddenPrompt(originalPrompt);
        }

        // 切换到对话模式界面
        switchToConversationMode();

        // 创建参考图片对象用于显示
        DrawingImageDto referenceImage = new DrawingImageDto();
        referenceImage.setImageUrl(referenceImageUrl);
        referenceImage.setPrompt(originalPrompt);

        // 添加继续编辑的提示消息（显示参考图片）
        addContinueEditMessage(referenceImage);

        // 添加用户的编辑要求消息
        addUserMessage(editPrompt);

        // 设置新的prompt并触发生成
        viewModel.getPrompt().set(editPrompt);

        Log.d("DrawingActivity", "About to call generateImage()");
        viewModel.generateImage();

        Log.d("DrawingActivity", "Continue edit in conversation completed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performDownload();
            } else {
                Toast.makeText(this, "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授权成功后，不直接调用startVoiceInput()
                // 而是提示用户可以再次点击语音按钮
                Toast.makeText(this, "语音权限已授权，请再次点击语音按钮", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要录音权限才能使用语音输入", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // 发送消息的统一方法
    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (!text.isEmpty()) {
            // 立即清除输入框显示
            etMessageInput.setText("");

            // 根据是否是继续编辑模式决定是否保持关联
            if (isContinueEditMode) {
                // 继续编辑模式：保持对原图的关联
                // 不清除hiddenPrompt和referenceImageUrl，确保基于原图进行编辑
                Log.d("DrawingActivity", "SendMessage in continue edit mode: keeping associations");
            } else {
                // 普通对话模式：清除关联，生成全新图片
                viewModel.setHiddenPrompt(null);
                viewModel.clearReferenceImageUrl();
                Log.d("DrawingActivity", "SendMessage in normal mode: cleared associations");
            }

            // 设置新的prompt
            viewModel.getPrompt().set(text);

            // 添加用户消息到对话界面
            addUserMessage(text);

            // 触发生成
            viewModel.generateImage();
        }
    }
    
    // 添加参考图片到对话界面
    private void addReferenceImageToConversation() {
        if (referenceImageUrl == null || referenceImageUrl.isEmpty()) {
            return;
        }
        
        // 创建参考图片的消息视图
        LinearLayout referenceLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = dpToPx(16);
        referenceLayout.setLayoutParams(layoutParams);
        referenceLayout.setOrientation(LinearLayout.VERTICAL);
        
        // 添加提示文字
        TextView tvHint = new TextView(this);
        tvHint.setText("基于以下图片继续创作：");
        tvHint.setTextSize(14);
        tvHint.setTextColor(getResources().getColor(R.color.text_secondary));
        tvHint.setPadding(0, 0, 0, dpToPx(8));
        
        // 创建图片容器
        androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dpToPx(160), dpToPx(160));
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(8));
        cardView.setCardElevation(0);
        
        ImageView ivRef = new ImageView(this);
        ivRef.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        ivRef.setScaleType(CENTER_CROP);
        
        // 加载图片
        Glide.with(this)
            .load(referenceImageUrl)
            .into(ivRef);
        
        cardView.addView(ivRef);
        referenceLayout.addView(tvHint);
        referenceLayout.addView(cardView);
        if (llContentContainer != null) {
            llContentContainer.addView(referenceLayout);
        }
    }
    
    // 处理Intent传递的数据
    private void handleIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        String prompt = intent.getStringExtra("prompt");
        String style = intent.getStringExtra("style");
        String ratio = intent.getStringExtra("ratio");
        String referenceImageUrl = intent.getStringExtra("reference_image_url");
        boolean isContinueEditMode = intent.getBooleanExtra("continue_edit", false);

        Log.d("init", "handleIntentData - prompt: " + prompt);
        Log.d("init", "handleIntentData - style: " + style);
        Log.d("init", "handleIntentData - ratio: " + ratio);
        Log.d("init", "handleIntentData - referenceImageUrl: " + referenceImageUrl);
        Log.d("init", "handleIntentData - isContinueEditMode: " + isContinueEditMode);

        // 只处理继续编辑模式下实际存在的控件
        if (isContinueEditMode) {
            // 设置图片（已在initializeViews中处理）
            // 设置输入框
            if (etPrompt != null) {
                etPrompt.setText("");
                etPrompt.setHint("请输入您的修改要求");
            }
            // 设置风格
            if (style != null && !style.isEmpty() && viewModel != null) {
                viewModel.setInitialStyle(style);
            }
            // 设置比例
            if (ratio != null && !ratio.isEmpty() && viewModel != null && viewModel.getSelectedRatio() != null) {
                viewModel.getSelectedRatio().set(ratio);
            }
            // 参考图片URL已在initializeViews中处理
            return;
        }

        // 非继续编辑模式下，保留原有逻辑（如有需要）
        // ...（可根据实际需求补充）...
    }
    
    // 设置星星闪烁动画
    private void setupSparkleAnimation() {
        if (currentBigStar == null || currentSmallStar == null) return;
        
        // 使用属性动画实现两个星星的位置交换
        animateStarSwap();
    }
    
    private void animateStarSwap() {
        if (currentBigStar == null || currentSmallStar == null) return;
        
        // 获取两个星星的初始位置
        float bigStarX = currentBigStar.getTranslationX();
        float bigStarY = currentBigStar.getTranslationY();
        float smallStarX = currentSmallStar.getTranslationX();
        float smallStarY = currentSmallStar.getTranslationY();
        
        // 创建位置交换动画
        android.animation.AnimatorSet swapAnimation = new android.animation.AnimatorSet();
        
        // 大星星移动到小星星位置
        android.animation.ObjectAnimator bigStarMoveX = android.animation.ObjectAnimator.ofFloat(
                currentBigStar, "translationX", bigStarX, smallStarX);
        android.animation.ObjectAnimator bigStarMoveY = android.animation.ObjectAnimator.ofFloat(
                currentBigStar, "translationY", bigStarY, smallStarY);
        
        // 小星星移动到大星星位置
        android.animation.ObjectAnimator smallStarMoveX = android.animation.ObjectAnimator.ofFloat(
                currentSmallStar, "translationX", smallStarX, bigStarX);
        android.animation.ObjectAnimator smallStarMoveY = android.animation.ObjectAnimator.ofFloat(
                currentSmallStar, "translationY", smallStarY, bigStarY);
        
        // 添加旋转动画
        android.animation.ObjectAnimator bigStarRotate = android.animation.ObjectAnimator.ofFloat(
                currentBigStar, "rotation", 0f, 180f);
        android.animation.ObjectAnimator smallStarRotate = android.animation.ObjectAnimator.ofFloat(
                currentSmallStar, "rotation", 0f, -180f);
        
        // 设置动画时长和插值器
        swapAnimation.setDuration(800);
        swapAnimation.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        
        // 同时播放所有动画
        swapAnimation.playTogether(bigStarMoveX, bigStarMoveY, smallStarMoveX, smallStarMoveY, 
                bigStarRotate, smallStarRotate);
        
        // 动画结束后延迟2秒再次执行
        swapAnimation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // 延迟2秒后再次执行动画
                if (currentBigStar != null) {
                    currentBigStar.postDelayed(() -> {
                        if (viewModel != null && viewModel.getIsGenerating().get()) {
                            animateStarSwap();
                        }
                    }, 2000);
                }
            }
        });
        
        swapAnimation.start();
        
        // 同时添加持续的闪烁效果
        addSparkleEffect();
    }
    
    private void addSparkleEffect() {
        if (currentBigStar == null || currentSmallStar == null) return;
        
        // 大星星闪烁
        AlphaAnimation bigStarAlpha = new AlphaAnimation(0.6f, 1.0f);
        bigStarAlpha.setDuration(1000);
        bigStarAlpha.setRepeatCount(Animation.INFINITE);
        bigStarAlpha.setRepeatMode(Animation.REVERSE);
        currentBigStar.startAnimation(bigStarAlpha);
        
        // 小星星闪烁（不同频率）
        AlphaAnimation smallStarAlpha = new AlphaAnimation(0.5f, 1.0f);
        smallStarAlpha.setDuration(750);
        smallStarAlpha.setRepeatCount(Animation.INFINITE);
        smallStarAlpha.setRepeatMode(Animation.REVERSE);
        currentSmallStar.startAnimation(smallStarAlpha);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放语音识别资源
        if (speechRecognitionUtil != null) {
            speechRecognitionUtil.destroy();
            speechRecognitionUtil = null;
        }
        if (voiceInputDialog != null && voiceInputDialog.isShowing()) {
            voiceInputDialog.dismiss();
        }
    }
    
    // 分享图片
    private void shareImage() {
        if (currentImage == null || currentImage.getImageUrl() == null) {
            Toast.makeText(this, "没有可分享的图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "正在准备分享...", Toast.LENGTH_SHORT).show();
        
        // 下载图片并分享
        Glide.with(this)
            .asBitmap()
            .load(currentImage.getImageUrl())
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    shareImageBitmap(resource);
                }
                
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }
                
                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    Toast.makeText(DrawingEditActivity1.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    // 添加用户消息到对话界面
    private void addUserMessage(String message) {
        // 创建新的用户消息视图
        LinearLayout userMessageLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = android.view.Gravity.END;
        layoutParams.bottomMargin = dpToPx(16);
        userMessageLayout.setLayoutParams(layoutParams);
        userMessageLayout.setOrientation(LinearLayout.HORIZONTAL);
        userMessageLayout.setBackgroundResource(R.drawable.bg_user_prompt_gradient);
        userMessageLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        
        // 在布局参数中设置最大宽度
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.setMarginEnd(dpToPx(16));
        layoutParams.setMarginStart(dpToPx(80)); // 限制最大宽度
        userMessageLayout.setLayoutParams(layoutParams);
        
        TextView tvMessage = new TextView(this);
        tvMessage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvMessage.setText(message);
        tvMessage.setTextColor(getResources().getColor(android.R.color.white));
        tvMessage.setTextSize(16);
        tvMessage.setLineHeight(dpToPx(24));
        
        userMessageLayout.addView(tvMessage);
        if (llContentContainer != null) {
            llContentContainer.addView(userMessageLayout);
        }
        
        // 滚动到底部
        if (!isInitializing && svContent != null) {
            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    // 添加AI消息到对话界面
    private void addAiMessage(String message) {
        // 创建新的AI消息视图 - 使用渐变蓝底白字样式
        LinearLayout aiMessageLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = android.view.Gravity.START;
        layoutParams.bottomMargin = dpToPx(16);
        layoutParams.setMarginEnd(dpToPx(80)); // 限制最大宽度
        aiMessageLayout.setLayoutParams(layoutParams);
        aiMessageLayout.setOrientation(LinearLayout.HORIZONTAL);
        aiMessageLayout.setBackgroundResource(R.drawable.bg_user_prompt_gradient);
        aiMessageLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        
        TextView tvMessage = new TextView(this);
        tvMessage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvMessage.setText(message);
        tvMessage.setTextColor(getResources().getColor(android.R.color.white));
        tvMessage.setTextSize(16);
        tvMessage.setLineHeight(dpToPx(24));
        
        aiMessageLayout.addView(tvMessage);
        if (llContentContainer != null) {
            llContentContainer.addView(aiMessageLayout);
        }
        
        // 滚动到底部
        if (!isInitializing && svContent != null) {
            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    // 添加进度卡片
    private void addProgressCard() {
        // 确保有容器可以添加内容
        if (llContentContainer == null) {
            Log.e("DrawingActivity", "llContentContainer is null, cannot add progress card");
            return;
        }
        
        // 创建一个包装布局，确保卡片左对齐
        LinearLayout wrapperLayout = new LinearLayout(this);
        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wrapperParams.bottomMargin = dpToPx(16);
        wrapperLayout.setLayoutParams(wrapperParams);
        wrapperLayout.setOrientation(LinearLayout.HORIZONTAL);
        // 不设置gravity，让子视图保持自己的对齐方式
        
        // 创建CardView容器
        currentProgressCard = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dpToPx(200), dpToPx(356));
        currentProgressCard.setLayoutParams(cardParams);
        currentProgressCard.setRadius(dpToPx(16));
        currentProgressCard.setCardElevation(0);
        
        // 创建进度布局
        LinearLayout progressLayout = new LinearLayout(this);
        progressLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        progressLayout.setOrientation(LinearLayout.VERTICAL);
        progressLayout.setGravity(android.view.Gravity.CENTER);
        progressLayout.setBackgroundResource(R.drawable.bg_gradient_purple_pink);
        
        // 创建星星容器
        FrameLayout starContainer = new FrameLayout(this);
        LinearLayout.LayoutParams starParams = new LinearLayout.LayoutParams(dpToPx(80), dpToPx(60));
        starParams.bottomMargin = dpToPx(20);
        starContainer.setLayoutParams(starParams);
        
        // 大星星
        currentBigStar = new ImageView(this);
        FrameLayout.LayoutParams bigStarParams = new FrameLayout.LayoutParams(dpToPx(36), dpToPx(36));
        bigStarParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
        currentBigStar.setLayoutParams(bigStarParams);
        currentBigStar.setImageResource(R.drawable.ic_star_white);
        
        // 小星星
        currentSmallStar = new ImageView(this);
        FrameLayout.LayoutParams smallStarParams = new FrameLayout.LayoutParams(dpToPx(28), dpToPx(28));
        smallStarParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        currentSmallStar.setLayoutParams(smallStarParams);
        currentSmallStar.setImageResource(R.drawable.ic_star_white);
        
        starContainer.addView(currentBigStar);
        starContainer.addView(currentSmallStar);
        
        // 进度文本
        currentProgressText = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        currentProgressText.setLayoutParams(textParams);
        currentProgressText.setText("0%");
        currentProgressText.setTextColor(getResources().getColor(R.color.white));
        currentProgressText.setTextSize(36);
        currentProgressText.setTypeface(null, android.graphics.Typeface.BOLD);
        currentProgressText.setGravity(android.view.Gravity.CENTER);
        
        progressLayout.addView(starContainer);
        progressLayout.addView(currentProgressText);
        currentProgressCard.addView(progressLayout);
        
        // 将卡片添加到包装布局中
        wrapperLayout.addView(currentProgressCard);
        // 将包装布局添加到实际的内容容器中
        llContentContainer.addView(wrapperLayout);
        
        // 绑定进度更新
        viewModel.getProgress().observe(this, progress -> {
            if (currentProgressText != null && progress != null) {
                currentProgressText.setText(progress + "%");
            }
        });
        
        // 滚动到底部
        if (!isInitializing && svContent != null) {
            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    // dp转px
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    
    
    // 分享位图
    private void shareImageBitmap(Bitmap bitmap) {
        try {
            // 保存图片到缓存目录
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "shared_image.jpg");
            FileOutputStream stream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();
            
            // 获取图片URI
            Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", imageFile);
            
            if (contentUri != null) {
                // 创建分享Intent
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                
                // 添加文字描述
                String shareText = "看看我用通通助手AI创作的画作";
                if (currentImage.getPrompt() != null && !currentImage.getPrompt().isEmpty()) {
                    shareText += "：" + currentImage.getPrompt();
                }
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                
                startActivity(Intent.createChooser(shareIntent, "分享图片到"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 根据图片实际比例调整参考图片尺寸
     */
    private void adjustReferenceImageSize(ImageView imageView, int imageWidth, int imageHeight) {
        if (imageView == null || imageWidth <= 0 || imageHeight <= 0) return;

        // 计算图片比例
        float aspectRatio = (float) imageWidth / imageHeight;

        // 设置最大尺寸限制（dp转px）
        float density = getResources().getDisplayMetrics().density;
        int maxWidth = (int) (200 * density); // 200dp
        int maxHeight = (int) (280 * density); // 280dp
        int minWidth = (int) (120 * density); // 120dp

        // 根据比例计算合适的尺寸
        final int targetWidth, targetHeight;

        if (aspectRatio > 1) {
            // 横图：宽度优先
            int tempWidth = Math.min(maxWidth, Math.max(minWidth, maxWidth));
            int tempHeight = (int) (tempWidth / aspectRatio);
            if (tempHeight > maxHeight) {
                tempHeight = maxHeight;
                tempWidth = (int) (tempHeight * aspectRatio);
            }
            targetWidth = tempWidth;
            targetHeight = tempHeight;
        } else {
            // 竖图：高度优先
            int tempHeight = Math.min(maxHeight, (int) (minWidth / aspectRatio));
            int tempWidth = (int) (tempHeight * aspectRatio);
            if (tempWidth > maxWidth) {
                tempWidth = maxWidth;
                tempHeight = (int) (tempWidth / aspectRatio);
            }
            targetWidth = tempWidth;
            targetHeight = tempHeight;
        }

        // 在主线程中更新UI
        runOnUiThread(() -> {
            ViewGroup.LayoutParams imageParams = imageView.getLayoutParams();
            if (imageParams != null) {
                imageParams.width = targetWidth;
                imageParams.height = targetHeight;
                imageView.setLayoutParams(imageParams);

                Log.d("DrawingActivity", "Adjusted reference image size: " + targetWidth + "x" + targetHeight +
                    " (aspect ratio: " + aspectRatio + ", original: " + imageWidth + "x" + imageHeight + ")");
            }
        });
    }

    private void setupKeyboardListener() {
        // 获取根视图
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                private int previousHeight = 0;

                @Override
                public void onGlobalLayout() {
                    android.graphics.Rect rect = new android.graphics.Rect();
                    rootView.getWindowVisibleDisplayFrame(rect);
                    int currentHeight = rect.height();

                    if (previousHeight == 0) {
                        previousHeight = currentHeight;
                        return;
                    }

                    // 计算高度差
                    int heightDiff = previousHeight - currentHeight;

                    // 如果高度差大于200dp，认为键盘弹起
                    int threshold = (int) (200 * getResources().getDisplayMetrics().density);

                    if (heightDiff > threshold) {
                        // 键盘弹起，调整输入框位置
                        adjustInputContainerForKeyboard(true, heightDiff);
                        adjustImageCardForKeyboard(true);
                    } else if (heightDiff < -threshold) {
                        // 键盘收起，恢复输入框位置
                        adjustInputContainerForKeyboard(false, 0);
                        adjustImageCardForKeyboard(false);
                    }

                    previousHeight = currentHeight;
                }
            });
        }
    }

    private void adjustInputContainerForKeyboard(boolean keyboardVisible, int keyboardHeight) {
        View inputContainer = findViewById(R.id.ll_input_container);
        EditText editText = findViewById(R.id.et_prompt);

        // 在继续编辑模式下，同时调整图片显示
        if (isContinueEditMode) {
            adjustImageForKeyboard(keyboardVisible);
        }

        if (inputContainer != null) {
            ViewGroup.LayoutParams layoutParams = inputContainer.getLayoutParams();

            // 检查布局参数类型
            if (layoutParams instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) layoutParams;

                if (keyboardVisible) {
                    // 键盘弹起时，设置输入框为自适应高度，最大180dp
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    int marginBottom = (int) (20 * getResources().getDisplayMetrics().density);
                    params.bottomMargin = marginBottom;
                } else {
                    // 键盘收起时，恢复固定高度54dp
                    int fixedHeight = (int) (54 * getResources().getDisplayMetrics().density);
                    params.height = fixedHeight;
                    int marginBottom = (int) (20 * getResources().getDisplayMetrics().density);
                    params.bottomMargin = marginBottom;
                }

                inputContainer.setLayoutParams(params);
            } else if (layoutParams instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) layoutParams;

                if (keyboardVisible) {
                    // 键盘弹起时，设置输入框为自适应高度，最大180dp
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    int marginBottom = (int) (20 * getResources().getDisplayMetrics().density);
                    params.bottomMargin = marginBottom;
                } else {
                    // 键盘收起时，恢复固定高度54dp
                    int fixedHeight = (int) (54 * getResources().getDisplayMetrics().density);
                    params.height = fixedHeight;
                    int marginBottom = (int) (20 * getResources().getDisplayMetrics().density);
                    params.bottomMargin = marginBottom;
                }

                inputContainer.setLayoutParams(params);
            }
        }

        // 调整EditText的最大高度
        if (editText != null) {
            if (keyboardVisible) {
                // 键盘弹起时，设置EditText为多行输入，最大高度180dp
                int maxHeight = (int) (180 * getResources().getDisplayMetrics().density);
                editText.setMaxHeight(maxHeight);
                editText.setMaxLines(Integer.MAX_VALUE);
                editText.setSingleLine(false);
                editText.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
            } else {
                // 键盘收起时，恢复单行输入
                int singleLineHeight = (int) (54 * getResources().getDisplayMetrics().density);
                editText.setMaxHeight(singleLineHeight);
                editText.setMaxLines(1);
                editText.setSingleLine(true);
                editText.setGravity(android.view.Gravity.CENTER_VERTICAL);
            }
        }

        // 对于继续编辑页面，还需要调整ScrollView的padding来确保内容可见
        View referenceImage = findViewById(R.id.cv_reference_image);
        if (referenceImage != null && keyboardVisible) {
            // 为引用图片容器添加额外的底部padding，确保图片完全可见
            int extraPadding = (int) (150 * getResources().getDisplayMetrics().density);
            referenceImage.setPadding(
                referenceImage.getPaddingLeft(),
                referenceImage.getPaddingTop(),
                referenceImage.getPaddingRight(),
                extraPadding
            );
        } else if (referenceImage != null && !keyboardVisible) {
            // 键盘收起时，恢复原始padding
            int originalPadding = (int) (12 * getResources().getDisplayMetrics().density);
            referenceImage.setPadding(
                originalPadding,
                originalPadding,
                originalPadding,
                originalPadding
            );
        }
    }

    /**
     * 在继续编辑模式下，根据键盘状态调整图片显示
     * @param keyboardVisible 键盘是否可见（即风格和比例选择区域是否显示）
     */
    private void adjustImageForKeyboard(boolean keyboardVisible) {
        Log.d("DrawingActivity", "Adjusting image for keyboard: " + keyboardVisible);

        if (keyboardVisible) {
            // 键盘弹起时：完全隐藏ScrollView中的原图片，显示绝对定位的缩小图片
            if (cvReferenceImage != null) {
                cvReferenceImage.setVisibility(View.GONE);
                Log.d("DrawingActivity", "Hidden original reference image");
            }

            // 同时隐藏ScrollView中动态添加的图片CardView
            hideOriginalImageInScrollView();

            // 显示缩小的图片
            if (referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
                showMinimizedImage(referenceImageUrl);
            }
        } else {
            // 键盘收起时：显示ScrollView中的原图片，隐藏绝对定位的缩小图片
            if (cvReferenceImage != null) {
                cvReferenceImage.setVisibility(View.VISIBLE);
                Log.d("DrawingActivity", "Shown original reference image");
            }

            // 同时显示ScrollView中动态添加的图片CardView
            showOriginalImageInScrollView();

            // 隐藏缩小的图片
            hideMinimizedImage();
        }
    }

    /**
     * 隐藏ScrollView中动态添加的原图片CardView
     */
    private void hideOriginalImageInScrollView() {
        if (llContentContainer != null) {
            for (int i = 0; i < llContentContainer.getChildCount(); i++) {
                View child = llContentContainer.getChildAt(i);
                if (child instanceof androidx.cardview.widget.CardView) {
                    androidx.cardview.widget.CardView cardView = (androidx.cardview.widget.CardView) child;
                    // 检查CardView中是否包含ImageView（即图片卡片）
                    if (cardView.getChildCount() > 0 && cardView.getChildAt(0) instanceof ImageView) {
                        cardView.setVisibility(View.GONE);
                        Log.d("DrawingActivity", "Hidden original image CardView in ScrollView");
                        break;
                    }
                }
            }
        }
    }

    /**
     * 显示ScrollView中动态添加的原图片CardView
     */
    private void showOriginalImageInScrollView() {
        if (llContentContainer != null) {
            for (int i = 0; i < llContentContainer.getChildCount(); i++) {
                View child = llContentContainer.getChildAt(i);
                if (child instanceof androidx.cardview.widget.CardView) {
                    androidx.cardview.widget.CardView cardView = (androidx.cardview.widget.CardView) child;
                    // 检查CardView中是否包含ImageView（即图片卡片）
                    if (cardView.getChildCount() > 0 && cardView.getChildAt(0) instanceof ImageView) {
                        cardView.setVisibility(View.VISIBLE);
                        Log.d("DrawingActivity", "Shown original image CardView in ScrollView");
                        break;
                    }
                }
            }
        }
    }



    /**
     * 显示缩小后的图片在绝对定位区域，根据原图比例自适应
     */
    private void showMinimizedImage(String imageUrl) {
        if (cvMinimizedImage == null || ivMinimizedImage == null || imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        // 先加载图片到缩小的ImageView
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource,
                                          @Nullable Transition<? super Drawable> transition) {
                    // 获取图片的实际尺寸
                    int imageWidth = resource.getIntrinsicWidth();
                    int imageHeight = resource.getIntrinsicHeight();

                    if (imageWidth > 0 && imageHeight > 0) {
                        // 计算图片的宽高比
                        float aspectRatio = (float) imageWidth / imageHeight;

                        // 使用post确保布局已经完成测量
                        cvMinimizedImage.post(() -> {
                            // 获取CardView的实际高度（减去margin和padding）
                            int actualHeight = cvMinimizedImage.getHeight();
                            if (actualHeight > 0) {
                                // 根据原图比例计算宽度
                                int calculatedWidth = (int) (actualHeight * aspectRatio);

                                // 设置CardView的布局参数
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cvMinimizedImage.getLayoutParams();
                                if (params != null) {
                                    params.width = calculatedWidth;
                                    cvMinimizedImage.setLayoutParams(params);

                                    Log.d("DrawingActivity", "Adjusted minimized image CardView size: " +
                                                      calculatedWidth + "x" + actualHeight +
                                                      " (original ratio: " + aspectRatio + ")");
                                }
                            }
                        });
                    }

                    // 设置图片到ImageView
                    ivMinimizedImage.setImageDrawable(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    // 清理资源
                }
            });

        // 显示缩小的图片CardView
        cvMinimizedImage.setVisibility(View.VISIBLE);

        Log.d("DrawingActivity", "Showing minimized image with original aspect ratio: " + imageUrl);
    }

    /**
     * 隐藏缩小后的图片
     */
    private void hideMinimizedImage() {
        if (cvMinimizedImage != null) {
            cvMinimizedImage.setVisibility(View.GONE);
            Log.d("DrawingActivity", "Hiding minimized image");
        }
    }

    /**
     * 键盘弹起时让图片CardView覆盖顶部导航栏，收起时恢复，并保持3:4比例缩放（以屏幕高度为基准）
     */
    private void adjustImageCardForKeyboard(boolean keyboardVisible) {
        View cardView = findViewById(R.id.cv_reference_image);
        if (cardView == null) return;

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
        if (keyboardVisible) {
            // 移除 layout_below，添加 alignParentTop，实现覆盖导航栏
//            params.removeRule(RelativeLayout.BELOW);
//            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            params.topMargin = 0;

            // 最大宽度限制为100dp，保持3:4比例
            int maxWidth = dpToPx(150);
            int width = maxWidth;
            int height = (int) (width * 4f / 3f);
            params.width = width;
            params.height = height;
        } else {
            // 恢复原始宽高
//            params.addRule(RelativeLayout.BELOW, R.id.rl_top_bar);
//            params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.topMargin = dpToPx(30);
            params.width = dpToPx(240);
            params.height = dpToPx(320);
        }
        cardView.setLayoutParams(params);
    }

}
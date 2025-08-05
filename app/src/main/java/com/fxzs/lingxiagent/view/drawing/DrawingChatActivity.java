package com.fxzs.lingxiagent.view.drawing;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.MainActivity;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingStyleDto;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.viewmodel.drawing.VMDrawing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AI绘画对话界面 - 专门用于显示生成过程和结果
 */
public class DrawingChatActivity extends BaseActivity<VMDrawing> {
    
    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_CONTINUE_EDIT = 1003;
    
    private ScrollView svContent;
    private LinearLayout llContentContainer;
    private LinearLayout llBottomFixed;
    private EditText etMessageInput;
    private ImageView btnVoiceInputOrSend;
    
    private DrawingImageDto currentImage;
    private String referenceImageUrl;
    private boolean isInitializing = true;
    
    // 动态创建的视图引用
    private View currentProgressCard;
    private ImageView currentBigStar;
    private ImageView currentSmallStar;
    private TextView currentProgressText;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_drawing_conversation;
    }
    
    @Override
    protected Class<VMDrawing> getViewModelClass() {
        return VMDrawing.class;
    }
    
    @Override
    protected void initializeViews() {
        android.util.Log.d("DrawingChatActivity", "=== initializeViews START ===");
        // 初始化控件
        svContent = findViewById(R.id.sv_content);
        if (svContent != null && svContent.getChildCount() > 0 && svContent.getChildAt(0) instanceof LinearLayout) {
            llContentContainer = (LinearLayout) svContent.getChildAt(0);
        }
        
        llBottomFixed = findViewById(R.id.ll_bottom_fixed);
        etMessageInput = findViewById(R.id.et_message_input);
        btnVoiceInputOrSend = findViewById(R.id.iv_voice_input_or_send);
        
        // 设置返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        
        // 设置底部输入框的发送功能
        etMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
        
        // 设置语音输入/发送按钮点击事件
        btnVoiceInputOrSend.setOnClickListener(v -> {
            String text = etMessageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                // 有文本输入，执行发送功能
                sendMessage();
            } else {
                // 没有文本输入，执行语音输入功能
                // TODO: 实现语音输入功能
                Toast.makeText(this, "语音输入功能暂未实现", Toast.LENGTH_SHORT).show();
            }
        });
        
        
        // 处理Intent传递的数据
        handleIntentData();
        
        isInitializing = false;
    }
    
    @Override
    protected void setupDataBinding() {
        // 添加文本变化监听器，用于动态切换图标
        etMessageInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                // 根据输入框内容动态切换图标
                updateVoiceSendIcon(s.toString().trim());
            }
        });
        
        // 绑定进度显示
        viewModel.getIsGenerating().observe(this, isGenerating -> {
            if (isGenerating) {
                addProgressBubble();
                setupSparkleAnimation();
            }
        });
        
        // 初始化图标状态
        updateVoiceSendIcon("");
    }
    
    @Override
    protected void setupObservers() {
        // 观察生成结果
        viewModel.getGeneratedImage().observe(this, image -> {
            android.util.Log.d("DrawingChatActivity", "Received image: " + (image != null ? image.getImageUrl() : "null"));
            if (image != null && image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                currentImage = image;
                displayResult(image);

                // 如果是继续编辑模式生成完成，清空关联参数避免影响后续生成
                if (viewModel.isContinueEditMode()) {
                    android.util.Log.d("DrawingChatActivity", "Continue edit completed, clearing associations");
                    viewModel.setContinueEditMode(false);
                    viewModel.clearReferenceImageUrl();
                    viewModel.setHiddenPrompt(null);
                }
            }
        });
        
        // 观察进度
        viewModel.getProgress().observe(this, progress -> {
            android.util.Log.d("DrawingChatActivity", "Progress: " + progress);
            if (currentProgressText != null) {
                currentProgressText.setText(progress + "%");
            }
        });
        
        // 观察生成状态
        viewModel.getIsGenerating().observe(this, isGenerating -> {
            android.util.Log.d("DrawingChatActivity", "IsGenerating: " + isGenerating);
        });
    }
    
    // 处理Intent传递的数据
    private void handleIntentData() {
        android.util.Log.d("DrawingChatActivity", "=== handleIntentData START ===");
        Intent intent = getIntent();
        if (intent != null) {
            // 首先检查是否从历史记录进入
            Long sessionId = intent.getLongExtra("sessionId", 0);
            DrawingSessionDto sessionDetail =
                (DrawingSessionDto) intent.getSerializableExtra("sessionDetail");
            
            if (sessionDetail != null && sessionDetail.getAiImageList() != null) {
                // 从历史记录进入，显示会话详情
                displaySessionHistory(sessionDetail);
                return;
            }
            
            // 正常的生成流程
            String prompt = intent.getStringExtra("prompt");
            String style = intent.getStringExtra("style");
            String styleId = intent.getStringExtra("style_id");
            String ratio = intent.getStringExtra("ratio");
            String referenceImageUrl = intent.getStringExtra("reference_image_url");

            android.util.Log.d("DrawingChatActivity", "Intent data - prompt: " + prompt + ", style: " + style +
                              ", styleId: " + styleId + ", ratio: " + ratio + ", referenceImageUrl: " + referenceImageUrl);

            if (prompt != null && !prompt.isEmpty()) {
                // 添加用户消息
                addUserMessage(prompt);

                // 不再单独添加AI回复文字，将在气泡中一起显示

                // 设置ViewModel的prompt
                viewModel.getPrompt().set(prompt);
                android.util.Log.d("DrawingChatActivity", "Set prompt to ViewModel: " + prompt);

                // 设置传递过来的比例，如果没有则使用默认9:16
                String selectedRatio = (ratio != null && !ratio.isEmpty()) ? ratio : "9:16";
                viewModel.getSelectedRatio().set(selectedRatio);
                android.util.Log.d("DrawingChatActivity", "Set ratio to ViewModel: " + selectedRatio);

                // 设置传递过来的风格
                if (style != null && !style.isEmpty() && styleId != null && !styleId.isEmpty()) {
                    // 需要等待风格列表加载完成后再设置选中的风格
                    setupStyleFromIntent(style, styleId);
                }

                // 如果有参考图片URL，传递给ViewModel
                if (referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
                    this.referenceImageUrl = referenceImageUrl;
                    viewModel.setReferenceImageUrl(referenceImageUrl);
                    android.util.Log.d("DrawingChatActivity", "Set reference image URL: " + referenceImageUrl);
                }

                // 延迟一小段时间再开始生成，确保ViewModel初始化完成
                etMessageInput.postDelayed(() -> {
                    android.util.Log.d("DrawingChatActivity", "Starting image generation...");
                    viewModel.generateImage();
                }, 500);
            }
        }
    }
    
    // 显示结果
    private void displayResult(DrawingImageDto image) {
        // 先预加载图片，加载完成后再替换进度卡片
        Glide.with(this)
            .load(image.getImageUrl())
            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, 
                        com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                    runOnUiThread(() -> {
                        removeProgressCard();
                        Toast.makeText(DrawingChatActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                    });
                    return false;
                }

                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, 
                        com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                        com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                    runOnUiThread(() -> {
                        removeProgressCard();
                        addResultImage(image);
                        
                        // 在输入框中设置提示
                        if (etMessageInput != null && !etMessageInput.isFocused()) {
                            etMessageInput.setHint("发消息...");
                        }
                        
                        // 滚动到底部显示最新内容
                        if (!isInitializing) {
                            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
                        }
                    });
                    return false;
                }
            })
            .preload();
    }
    
    // 移除进度气泡的辅助方法
    private void removeProgressCard() {
        if (currentProgressCard != null && currentProgressCard.getParent() != null) {
            // 新的气泡样式直接在容器中，不需要额外的包装层
            if (currentProgressCard.getParent() == llContentContainer) {
                llContentContainer.removeView(currentProgressCard);
            }
            currentProgressCard = null;
            currentProgressText = null;
        }
    }
    
    // 添加结果图片 - 使用气泡样式
    private void addResultImage(DrawingImageDto image) {
        // 使用新的气泡样式布局
        View bubbleView = getLayoutInflater().inflate(R.layout.item_ai_image_bubble, null);

        // 获取控件引用
        TextView tvMessage = bubbleView.findViewById(R.id.tv_message);
        ImageView ivGeneratedImage = bubbleView.findViewById(R.id.iv_generated_image);
        TextView tvImageSize = bubbleView.findViewById(R.id.tv_image_size);
        androidx.cardview.widget.CardView cvImageContainer = bubbleView.findViewById(R.id.cv_image_container);
        ImageView llOverlayDownload = bubbleView.findViewById(R.id.ll_overlay_download);
        TextView llOverlayEdit = bubbleView.findViewById(R.id.ll_overlay_edit);
        ImageView ivCopy = bubbleView.findViewById(R.id.iv_copy);
        ImageView ivRegenerate = bubbleView.findViewById(R.id.iv_regenerate);

        // 设置图片尺寸标签
        if (image.getWidth() != null && image.getHeight() != null) {
            tvImageSize.setText(image.getWidth() + " × " + image.getHeight());
        } else {
            tvImageSize.setText("512 × 768"); // 默认尺寸
        }

        // 加载图片并根据比例调整尺寸
        Glide.with(this)
            .asBitmap()
            .load(image.getImageUrl())
            .error(R.drawable.ic_image_placeholder)
            .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                @Override
                public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource,
                        @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                    // 设置图片
                    ivGeneratedImage.setImageBitmap(resource);

                    // 根据图片比例调整CardView尺寸
                    adjustImageContainerSize(cvImageContainer, resource.getWidth(), resource.getHeight());
                }

                @Override
                public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {
                    ivGeneratedImage.setImageDrawable(placeholder);
                }

                @Override
                public void onLoadFailed(@androidx.annotation.Nullable android.graphics.drawable.Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    ivGeneratedImage.setImageDrawable(errorDrawable);
                }
            });

        // 设置图片点击事件
        ivGeneratedImage.setOnClickListener(v -> {
            Intent intent = new Intent(this, DrawingImageViewerActivity.class);
            intent.putExtra("image_url", image.getImageUrl());
            String prompt = image.getPrompt();
            if (prompt == null || prompt.isEmpty()) {
                prompt = viewModel.getPrompt().get();
            }
            intent.putExtra("prompt", prompt);
            startActivity(intent);
        });

        // 设置悬浮按钮点击事件
        llOverlayDownload.setOnClickListener(v -> downloadImage());
        llOverlayEdit.setOnClickListener(v -> onContinueEditClick());

        // 设置底部按钮点击事件
        ivCopy.setOnClickListener(v -> copyImageToClipboard(image));
        ivRegenerate.setOnClickListener(v -> regenerateImage(image));

        // 添加到容器
        if (llContentContainer != null) {
            llContentContainer.addView(bubbleView);
        }
        
        // 滚动到底部
        if (!isInitializing && svContent != null) {
            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    // 创建底部按钮布局 (已废弃，使用新的气泡布局)
    @Deprecated
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
    
    // 创建操作按钮 (已废弃，使用新的气泡布局)
    @Deprecated
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
    
    // 继续编辑
    private void onContinueEditClick() {
        if (currentImage != null) {
            Intent intent = new Intent(this, DrawingActivity.class);
            intent.putExtra("continue_edit", true);
            intent.putExtra("reference_image_url", currentImage.getImageUrl());
            intent.putExtra("original_prompt", currentImage.getPrompt());
            if (viewModel.getSelectedStyle() != null) {
                intent.putExtra("style", viewModel.getSelectedStyle().getName());
            }
            // 保证回到当前session
            intent.putExtra("from_chat", true);

            android.util.Log.d("DrawingChatActivity", "Starting continue edit activity for result");
            // 使用startActivityForResult等待返回结果
            startActivityForResult(intent, REQUEST_CONTINUE_EDIT);
        }
    }

    // 重新生成图片 - 使用上一次的请求参数
    private void regenerateImage(DrawingImageDto image) {
        if (image == null || image.getPrompt() == null) {
            Toast.makeText(this, "无法获取原始参数", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("DrawingChatActivity", "Regenerating image with prompt: " + image.getPrompt());

        // 清除继续编辑模式的关联参数，确保生成全新图片
        viewModel.setContinueEditMode(false);
        viewModel.clearReferenceImageUrl();
        viewModel.setHiddenPrompt(null);

        // 添加用户消息显示重新生成的提示词
        addUserMessage("重新生成：" + image.getPrompt());

        // 设置原始提示词并重新生成
        viewModel.getPrompt().set(image.getPrompt());

        // 触发生成
        viewModel.generateImage();

        Toast.makeText(this, "正在重新生成图片...", Toast.LENGTH_SHORT).show();
    }
    
    // 下载图片
    private void downloadImage() {
        if (currentImage == null || currentImage.getImageUrl() == null) {
            Toast.makeText(this, "没有可下载的图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            performDownload();
        } else {
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
                    Toast.makeText(DrawingChatActivity.this, "图片下载失败", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    // 保存图片到相册
    private void saveImageToGallery(Bitmap bitmap) {
        String fileName = "IYA_Drawing_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "IYAProject");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            
            File imageFile = new File(storageDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                
                MediaStore.Images.Media.insertImage(getContentResolver(), imageFile.getAbsolutePath(), fileName, "通通助手AI绘画");
                sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
                
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

        android.util.Log.d("DrawingChatActivity", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == REQUEST_CONTINUE_EDIT && resultCode == RESULT_OK && data != null) {
            // 从继续编辑页面返回，获取用户输入的编辑要求
            String editPrompt = data.getStringExtra("edit_prompt");
            String referenceImageUrl = data.getStringExtra("reference_image_url");
            String originalPrompt = data.getStringExtra("original_prompt");
            String style = data.getStringExtra("style");

            android.util.Log.d("DrawingChatActivity", "Received continue edit result:");
            android.util.Log.d("DrawingChatActivity", "editPrompt: " + editPrompt);
            android.util.Log.d("DrawingChatActivity", "referenceImageUrl: " + referenceImageUrl);
            android.util.Log.d("DrawingChatActivity", "originalPrompt: " + originalPrompt);
            android.util.Log.d("DrawingChatActivity", "style: " + style);

            if (editPrompt != null && !editPrompt.isEmpty()) {
                // 在当前对话页面中继续编辑
                handleContinueEditInConversation(editPrompt, referenceImageUrl, originalPrompt, style);
            }
        }
    }

    /**
     * 在对话页面中处理继续编辑
     */
    private void handleContinueEditInConversation(String editPrompt, String referenceImageUrl, String originalPrompt, String style) {
        android.util.Log.d("DrawingChatActivity", "handleContinueEditInConversation called");

        // 设置继续编辑模式
        viewModel.setContinueEditMode(true);

        // 设置参考图片和原始提示词
        if (referenceImageUrl != null) {
            viewModel.setReferenceImageUrl(referenceImageUrl);
        }
        if (originalPrompt != null) {
            viewModel.setHiddenPrompt(originalPrompt);
        }

        // 添加用户的编辑要求消息
        addUserMessage(editPrompt);

        // 不再单独添加AI回复文字，将在气泡中一起显示

        // 设置新的prompt并触发生成
        viewModel.getPrompt().set(editPrompt);

        android.util.Log.d("DrawingChatActivity", "About to call generateImage()");
        viewModel.generateImage();

        android.util.Log.d("DrawingChatActivity", "Continue edit in conversation completed");
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
        }
    }
    
    // 根据输入框内容动态切换语音/发送图标
    private void updateVoiceSendIcon(String text) {
        if (btnVoiceInputOrSend != null) {
            // 如果有文本输入，切换为发送图标状态
            btnVoiceInputOrSend.setSelected(!text.isEmpty());
        }
    }
    
    // 发送消息的统一方法
    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (!text.isEmpty()) {
            etMessageInput.setText("");

            // 普通对话模式：清空之前的关联参数，确保生成全新图片
            android.util.Log.d("DrawingChatActivity", "SendMessage in normal mode: clearing associations");
            viewModel.setContinueEditMode(false);
            viewModel.clearReferenceImageUrl();
            viewModel.setHiddenPrompt(null);

            // 添加新的用户消息
            addUserMessage(text);

            // 不再单独添加AI回复文字，将在气泡中一起显示

            // 更新提示词并重新生成
            viewModel.getPrompt().set(text);

            // 注意：这里不再设置referenceImageUrl，确保生成全新图片

            viewModel.generateImage();
        }
    }
    
    // 添加用户消息到对话界面
    private void addUserMessage(String message) {
        LinearLayout userMessageLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = android.view.Gravity.END;
        layoutParams.bottomMargin = dpToPx(16);
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.setMarginEnd(dpToPx(16));
        layoutParams.setMarginStart(dpToPx(20));
        userMessageLayout.setLayoutParams(layoutParams);
        userMessageLayout.setOrientation(LinearLayout.HORIZONTAL);
        userMessageLayout.setBackgroundResource(R.drawable.bg_user_prompt_gradient);
        userMessageLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        
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
        
        if (!isInitializing && svContent != null) {
            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    // 添加AI消息到对话界面
    private void addAiMessage(String message) {
        LinearLayout aiMessageLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = android.view.Gravity.START;
        layoutParams.bottomMargin = dpToPx(16);
        layoutParams.setMarginEnd(dpToPx(20));
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
        
        if (!isInitializing && svContent != null) {
            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    // 添加进度气泡
    private void addProgressBubble() {
        // 使用新的气泡样式布局
        View bubbleView = getLayoutInflater().inflate(R.layout.item_ai_image_bubble, null);

        // 获取控件引用
        TextView tvMessage = bubbleView.findViewById(R.id.tv_message);
        ImageView ivGeneratedImage = bubbleView.findViewById(R.id.iv_generated_image);
        TextView tvImageSize = bubbleView.findViewById(R.id.tv_image_size);
        ProgressBar pbLoading = bubbleView.findViewById(R.id.pb_loading);
        LinearLayout llActions = bubbleView.findViewById(R.id.ll_actions);
        ImageView llOverlayDownload = bubbleView.findViewById(R.id.ll_overlay_download);
        TextView llOverlayEdit = bubbleView.findViewById(R.id.ll_overlay_edit);

        // 设置消息文字
        tvMessage.setText("以下是为你生成的图片：");

        // 隐藏图片尺寸标签、进度条、悬浮按钮和操作按钮
        tvImageSize.setVisibility(View.GONE);
        pbLoading.setVisibility(View.GONE);
        llOverlayDownload.setVisibility(View.GONE);
        llOverlayEdit.setVisibility(View.GONE);
        llActions.setVisibility(View.GONE);

        // 设置图片为9:16比例的占位图，使用渐变背景
        ivGeneratedImage.setBackgroundResource(R.drawable.bg_gradient_purple_pink);
        ivGeneratedImage.setScaleType(ImageView.ScaleType.CENTER);

        // 图片高度已在布局文件中设置为9:16比例

        // 获取图片容器的FrameLayout
        FrameLayout frameLayout = null;
        if (ivGeneratedImage.getParent() instanceof FrameLayout) {
            frameLayout = (FrameLayout) ivGeneratedImage.getParent();
        }

        if (frameLayout != null) {
            // 创建星星容器
            FrameLayout starContainer = new FrameLayout(this);
            FrameLayout.LayoutParams starContainerParams = new FrameLayout.LayoutParams(dpToPx(80), dpToPx(60));
            starContainerParams.gravity = android.view.Gravity.CENTER;
            starContainerParams.bottomMargin = dpToPx(60); // 在进度文字上方
            starContainer.setLayoutParams(starContainerParams);

            // 创建大星星
            currentBigStar = new ImageView(this);
            FrameLayout.LayoutParams bigStarParams = new FrameLayout.LayoutParams(dpToPx(36), dpToPx(36));
            bigStarParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
            currentBigStar.setLayoutParams(bigStarParams);
            currentBigStar.setImageResource(R.drawable.ic_star_white);

            // 创建小星星
            currentSmallStar = new ImageView(this);
            FrameLayout.LayoutParams smallStarParams = new FrameLayout.LayoutParams(dpToPx(28), dpToPx(28));
            smallStarParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
            currentSmallStar.setLayoutParams(smallStarParams);
            currentSmallStar.setImageResource(R.drawable.ic_star_white);

            starContainer.addView(currentBigStar);
            starContainer.addView(currentSmallStar);

            // 创建进度文字
            currentProgressText = new TextView(this);
            currentProgressText.setText("0%");
            currentProgressText.setTextColor(getResources().getColor(R.color.white));
            currentProgressText.setTextSize(36);
            currentProgressText.setTypeface(null, android.graphics.Typeface.BOLD);
            currentProgressText.setGravity(android.view.Gravity.CENTER);

            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            textParams.gravity = android.view.Gravity.CENTER;
            currentProgressText.setLayoutParams(textParams);

            // 添加到容器
            frameLayout.addView(starContainer);
            frameLayout.addView(currentProgressText);
        }

        // 保存引用以便更新进度
        currentProgressCard = bubbleView;

        // 添加到容器
        if (llContentContainer != null) {
            llContentContainer.addView(bubbleView);
        }

        // 滚动到底部
        if (!isInitializing && svContent != null) {
            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
        }
    }

    // 添加进度卡片 (已废弃，使用新的气泡样式)
    @Deprecated
    private void addProgressCard() {
        if (llContentContainer == null) {
            android.util.Log.e("DrawingChatActivity", "llContentContainer is null, cannot add progress card");
            return;
        }
        
        LinearLayout wrapperLayout = new LinearLayout(this);
        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wrapperParams.bottomMargin = dpToPx(16);
        wrapperLayout.setLayoutParams(wrapperParams);
        wrapperLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        androidx.cardview.widget.CardView progressCardView = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dpToPx(200), dpToPx(356));
        progressCardView.setLayoutParams(cardParams);
        progressCardView.setRadius(dpToPx(16));
        progressCardView.setCardElevation(0);
        currentProgressCard = progressCardView;
        
        LinearLayout progressLayout = new LinearLayout(this);
        progressLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        progressLayout.setOrientation(LinearLayout.VERTICAL);
        progressLayout.setGravity(android.view.Gravity.CENTER);
        progressLayout.setBackgroundResource(R.drawable.bg_gradient_purple_pink);
        
        FrameLayout starContainer = new FrameLayout(this);
        LinearLayout.LayoutParams starParams = new LinearLayout.LayoutParams(dpToPx(80), dpToPx(60));
        starParams.bottomMargin = dpToPx(20);
        starContainer.setLayoutParams(starParams);
        
        currentBigStar = new ImageView(this);
        FrameLayout.LayoutParams bigStarParams = new FrameLayout.LayoutParams(dpToPx(36), dpToPx(36));
        bigStarParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
        currentBigStar.setLayoutParams(bigStarParams);
        currentBigStar.setImageResource(R.drawable.ic_star_white);
        
        currentSmallStar = new ImageView(this);
        FrameLayout.LayoutParams smallStarParams = new FrameLayout.LayoutParams(dpToPx(28), dpToPx(28));
        smallStarParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        currentSmallStar.setLayoutParams(smallStarParams);
        currentSmallStar.setImageResource(R.drawable.ic_star_white);
        
        starContainer.addView(currentBigStar);
        starContainer.addView(currentSmallStar);
        
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
        progressCardView.addView(progressLayout);

        wrapperLayout.addView(progressCardView);
        llContentContainer.addView(wrapperLayout);
        
        viewModel.getProgress().observe(this, progress -> {
            if (currentProgressText != null && progress != null) {
                currentProgressText.setText(progress + "%");
            }
        });
        
        if (!isInitializing && svContent != null) {
            svContent.post(() -> svContent.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    // 设置星星闪烁动画
    private void setupSparkleAnimation() {
        if (currentBigStar == null || currentSmallStar == null) return;
        
        animateStarSwap();
    }
    
    private void animateStarSwap() {
        if (currentBigStar == null || currentSmallStar == null) return;
        
        float bigStarX = currentBigStar.getTranslationX();
        float bigStarY = currentBigStar.getTranslationY();
        float smallStarX = currentSmallStar.getTranslationX();
        float smallStarY = currentSmallStar.getTranslationY();
        
        android.animation.AnimatorSet swapAnimation = new android.animation.AnimatorSet();
        
        android.animation.ObjectAnimator bigStarMoveX = android.animation.ObjectAnimator.ofFloat(
                currentBigStar, "translationX", bigStarX, smallStarX);
        android.animation.ObjectAnimator bigStarMoveY = android.animation.ObjectAnimator.ofFloat(
                currentBigStar, "translationY", bigStarY, smallStarY);
        
        android.animation.ObjectAnimator smallStarMoveX = android.animation.ObjectAnimator.ofFloat(
                currentSmallStar, "translationX", smallStarX, bigStarX);
        android.animation.ObjectAnimator smallStarMoveY = android.animation.ObjectAnimator.ofFloat(
                currentSmallStar, "translationY", smallStarY, bigStarY);
        
        android.animation.ObjectAnimator bigStarRotate = android.animation.ObjectAnimator.ofFloat(
                currentBigStar, "rotation", 0f, 180f);
        android.animation.ObjectAnimator smallStarRotate = android.animation.ObjectAnimator.ofFloat(
                currentSmallStar, "rotation", 0f, -180f);
        
        swapAnimation.setDuration(800);
        swapAnimation.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        
        swapAnimation.playTogether(bigStarMoveX, bigStarMoveY, smallStarMoveX, smallStarMoveY, 
                bigStarRotate, smallStarRotate);
        
        swapAnimation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
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
        addSparkleEffect();
    }
    
    private void addSparkleEffect() {
        if (currentBigStar == null || currentSmallStar == null) return;
        
        android.view.animation.AlphaAnimation bigStarAlpha = new android.view.animation.AlphaAnimation(0.6f, 1.0f);
        bigStarAlpha.setDuration(1000);
        bigStarAlpha.setRepeatCount(android.view.animation.Animation.INFINITE);
        bigStarAlpha.setRepeatMode(android.view.animation.Animation.REVERSE);
        currentBigStar.startAnimation(bigStarAlpha);
        
        android.view.animation.AlphaAnimation smallStarAlpha = new android.view.animation.AlphaAnimation(0.5f, 1.0f);
        smallStarAlpha.setDuration(750);
        smallStarAlpha.setRepeatCount(android.view.animation.Animation.INFINITE);
        smallStarAlpha.setRepeatMode(android.view.animation.Animation.REVERSE);
        currentSmallStar.startAnimation(smallStarAlpha);
    }
    
    @Override
    public void onBackPressed() {
        // 系统返回按钮跳转到主界面
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
    
    // 复制图片到剪贴板
    private void copyImageToClipboard(DrawingImageDto image) {
        if (image == null || image.getImageUrl() == null) {
            Toast.makeText(this, "图片信息不完整", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "正在复制图片...", Toast.LENGTH_SHORT).show();

        // 下载图片并复制到剪贴板
        Glide.with(this)
            .asBitmap()
            .load(image.getImageUrl())
            .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                @Override
                public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource,
                        @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                    // 复制图片到剪贴板
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                    // 将Bitmap转换为Uri
                    try {
                        // 创建临时文件
                        java.io.File cachePath = new java.io.File(getCacheDir(), "images");
                        cachePath.mkdirs();
                        java.io.File imageFile = new java.io.File(cachePath, "copied_image.png");

                        // 保存Bitmap到文件
                        java.io.FileOutputStream stream = new java.io.FileOutputStream(imageFile);
                        resource.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();

                        // 创建Uri
                        android.net.Uri imageUri = androidx.core.content.FileProvider.getUriForFile(
                            DrawingChatActivity.this,
                            getPackageName() + ".fileprovider",
                            imageFile
                        );

                        // 复制到剪贴板
                        android.content.ClipData clip = android.content.ClipData.newUri(getContentResolver(), "图片", imageUri);
                        clipboard.setPrimaryClip(clip);

                        Toast.makeText(DrawingChatActivity.this, "图片已复制到剪贴板", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        android.util.Log.e("DrawingChatActivity", "复制图片失败", e);
                        Toast.makeText(DrawingChatActivity.this, "复制图片失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {
                }

                @Override
                public void onLoadFailed(@androidx.annotation.Nullable android.graphics.drawable.Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    Toast.makeText(DrawingChatActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                }
            });
    }

    // dp转px
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    // 显示会话历史内容
    private void displaySessionHistory(DrawingSessionDto sessionDetail) {
        // 显示所有历史图片和对话
        if (sessionDetail.getAiImageList() != null && !sessionDetail.getAiImageList().isEmpty()) {
            for (DrawingImageDto imageDto : sessionDetail.getAiImageList()) {
                // 显示每个图片的提示词作为用户消息
                if (imageDto.getPrompt() != null && !imageDto.getPrompt().isEmpty()) {
                    addUserMessage(imageDto.getPrompt());
                }
                
                // 不再单独添加AI回复文字，将在气泡中一起显示
                
                // 设置当前图片，以便支持继续编辑功能
                currentImage = imageDto;
                // 直接添加结果图片，不再调用displayResult避免重复预加载
                addResultImage(imageDto);
            }
            
            // 如果有历史图片，获取最后一张图片的URL作为参考图片
            if (!sessionDetail.getAiImageList().isEmpty()) {
                DrawingImageDto lastImage =
                    sessionDetail.getAiImageList().get(sessionDetail.getAiImageList().size() - 1);
                if (lastImage != null && lastImage.getImageUrl() != null) {
                    this.referenceImageUrl = lastImage.getImageUrl();
                    viewModel.setReferenceImageUrl(referenceImageUrl);
                }
            }
        }
        
        // 保持底部输入框可见，支持继续编辑
        if (llBottomFixed != null) {
            llBottomFixed.setVisibility(View.VISIBLE);
        }
        
        // 设置输入框提示文字
        if (etMessageInput != null) {
            etMessageInput.setHint("继续编辑这个会话...");
        }
    }

    // 根据Intent传递的风格信息设置选中的风格
    private void setupStyleFromIntent(String styleName, String styleId) {
        android.util.Log.d("DrawingChatActivity", "Setting up style from intent: " + styleName + " (ID: " + styleId + ")");

        // 观察风格列表，等待加载完成后设置选中的风格
        viewModel.getStyles().observe(this, styles -> {
            if (styles != null && !styles.isEmpty()) {
                android.util.Log.d("DrawingChatActivity", "Styles loaded, searching for style ID: " + styleId);

                // 查找匹配的风格
                for (DrawingStyleDto style : styles) {
                    android.util.Log.d("DrawingChatActivity", "Comparing style ID: " + style.getId() + " with target: " + styleId);
                    if (String.valueOf(style.getId()).equals(styleId)) {
                        android.util.Log.d("DrawingChatActivity", "Found matching style: " + style.getName() + " (ID: " + style.getId() + ")");
                        viewModel.setSelectedStyle(style);
                        return;
                    }
                }

                android.util.Log.w("DrawingChatActivity", "Style not found with ID: " + styleId + ", using default");
                // 如果没找到，使用第一个风格作为默认
                if (!styles.isEmpty()) {
                    viewModel.setSelectedStyle(styles.get(0));
                }
            }
        });
    }

    /**
     * 根据图片实际比例调整图片容器尺寸
     */
    private void adjustImageContainerSize(androidx.cardview.widget.CardView cardView, int imageWidth, int imageHeight) {
        if (cardView == null || imageWidth <= 0 || imageHeight <= 0) return;

        // 计算图片比例
        float aspectRatio = (float) imageWidth / imageHeight;

        // 设置最大尺寸限制（dp转px）
        float density = getResources().getDisplayMetrics().density;
        int maxWidth = (int) (250 * density); // 250dp
        int maxHeight = (int) (350 * density); // 350dp
        int minWidth = (int) (150 * density); // 150dp

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
            android.view.ViewGroup.LayoutParams cardParams = cardView.getLayoutParams();
            if (cardParams != null) {
                cardParams.width = targetWidth;
                cardParams.height = targetHeight;
                cardView.setLayoutParams(cardParams);

                android.util.Log.d("DrawingChat", "Adjusted image container size: " + targetWidth + "x" + targetHeight +
                    " (aspect ratio: " + aspectRatio + ", original: " + imageWidth + "x" + imageHeight + ")");
            }
        });
    }
}
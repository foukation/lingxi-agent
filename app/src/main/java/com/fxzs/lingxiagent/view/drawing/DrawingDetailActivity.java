package com.fxzs.lingxiagent.view.drawing;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.drawing.VMDrawingDetail;

/**
 * AI绘画图片详情页
 * 展示图片详细信息，提供保存、分享、继续编辑、做同款等功能
 */
public class DrawingDetailActivity extends BaseActivity<VMDrawingDetail> {
    
    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    
    private ImageView ivFullImage;
    private TextView tvPrompt;
    private TextView btnCreateSimilar;
    private View progressLoading;
    
    private DrawingImageDto currentImage;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_drawing_detail;
    }
    
    @Override
    protected Class<VMDrawingDetail> getViewModelClass() {
        return VMDrawingDetail.class;
    }
    
    @Override
    protected void initializeViews() {
        // 初始化控件
        ivFullImage = findViewById(R.id.iv_full_image);
        tvPrompt = findViewById(R.id.tv_prompt);
        btnCreateSimilar = findViewById(R.id.btn_create_similar);
        progressLoading = findViewById(R.id.progress_loading);
        
        // 设置返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        
        // 设置点击事件
        btnCreateSimilar.setOnClickListener(v -> createSimilar());
        
        // 获取传递的数据
        Intent intent = getIntent();
        String sampleId = intent.getStringExtra("sample_id");
        String imageUrl = intent.getStringExtra("image_url");
        String prompt = intent.getStringExtra("prompt");
        String style = intent.getStringExtra("style");
        int width = intent.getIntExtra("width", 0);
        int height = intent.getIntExtra("height", 0);
        
        android.util.Log.d("DrawingDetailActivity", "sampleId: " + sampleId);
        android.util.Log.d("DrawingDetailActivity", "imageUrl: " + imageUrl);
        android.util.Log.d("DrawingDetailActivity", "prompt: " + prompt);
        android.util.Log.d("DrawingDetailActivity", "style: " + style);
        android.util.Log.d("DrawingDetailActivity", "width: " + width + ", height: " + height);
        
        if (imageUrl != null) {
            // 直接显示传递的图片信息，不需要再调用API
            android.util.Log.d("DrawingDetailActivity", "Displaying image directly from intent data");
            DrawingImageDto imageDto = new DrawingImageDto();
            if (sampleId != null) {
                try {
                    imageDto.setId(Long.parseLong(sampleId));
                } catch (NumberFormatException e) {
                    android.util.Log.e("DrawingDetailActivity", "Invalid sample ID: " + sampleId);
                }
            }
            imageDto.setImageUrl(imageUrl);
            imageDto.setPrompt(prompt);
            imageDto.setStyle(style);
            if (width > 0) imageDto.setWidth(width);
            if (height > 0) imageDto.setHeight(height);
            currentImage = imageDto;
            displayImage(imageDto);
            // 使用viewModel的字段更新，确保数据绑定生效
            viewModel.getPromptText().set(prompt != null ? prompt : "");
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    protected void setupDataBinding() {
        // 绑定提示词
        DataBindingUtils.bindTextView(tvPrompt, viewModel.getPromptText(), this);
        
        // 绑定加载状态
        DataBindingUtils.bindVisibility(progressLoading, viewModel.getIsLoading(), this);
    }
    
    @Override
    protected void setupObservers() {
        // 观察图片详情
        viewModel.getImageDetail().observe(this, image -> {
            if (image != null) {
                currentImage = image;
                displayImage(image);
            }
        });
        
        // 观察删除结果
    }
    
    private void displayImage(DrawingImageDto image) {
        // 加载图片
        android.util.Log.d("DrawingDetailActivity", "displayImage - URL: " + image.getImageUrl());
        android.util.Log.d("DrawingDetailActivity", "displayImage - Prompt: " + image.getPrompt());
        
        Glide.with(this)
                .load(image.getImageUrl())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(ivFullImage);
    }
    
    
    private void createSimilar() {
        if (currentImage != null) {
            Intent intent = new Intent(this, DrawingActivity.class);
            // 做同款第一次带上prompt
            intent.putExtra("prompt", currentImage.getPrompt());
            intent.putExtra("style", currentImage.getStyle());
            // 传递当前图片URL作为参考图片
            if (currentImage.getImageUrl() != null && !currentImage.getImageUrl().isEmpty()) {
                intent.putExtra("reference_image_url", currentImage.getImageUrl());
            }
            startActivity(intent);
        }
    }
    
}
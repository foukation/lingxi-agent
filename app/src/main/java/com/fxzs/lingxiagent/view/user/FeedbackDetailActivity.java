package com.fxzs.lingxiagent.view.user;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.user.VMFeedbackDetail;
import com.fxzs.lingxiagent.view.drawing.DrawingImageViewerActivity;

public class FeedbackDetailActivity extends BaseActivity<VMFeedbackDetail> {
    
    private ImageView ivBack;
    private TextView tvContent;
    private TextView tvContact;
    private TextView tvTime;
    private TextView tvStatus;
    private TextView tvReplyContent;
    private TextView tvReplyTime;
    
    private LinearLayout layoutImages;
    private LinearLayout layoutContact;
    private LinearLayout layoutReply;
    private RecyclerView rvImages;
    
    private FeedbackDetailImageAdapter imageAdapter;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_feedback_detail;
    }
    
    @Override
    protected Class<VMFeedbackDetail> getViewModelClass() {
        return VMFeedbackDetail.class;
    }
    
    @Override
    protected void initializeViews() {
        // 初始化控件
        ivBack = findViewById(R.id.iv_back);
        tvContent = findViewById(R.id.tv_content);
        tvContact = findViewById(R.id.tv_contact);
        tvTime = findViewById(R.id.tv_time);
        tvStatus = findViewById(R.id.tv_status);
        tvReplyContent = findViewById(R.id.tv_reply_content);
        tvReplyTime = findViewById(R.id.tv_reply_time);
        
        layoutImages = findViewById(R.id.layout_images);
        layoutContact = findViewById(R.id.layout_contact);
        layoutReply = findViewById(R.id.layout_reply);
        rvImages = findViewById(R.id.rv_images);
        
        // 设置图片列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvImages.setLayoutManager(layoutManager);
        imageAdapter = new FeedbackDetailImageAdapter(this);
        imageAdapter.setOnImageClickListener((imageUrl, position) -> {
            // 打开图片预览界面
            openImageViewer(imageUrl);
        });
        rvImages.setAdapter(imageAdapter);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        
        // 获取传入的反馈ID
        Long feedbackId = getIntent().getLongExtra("feedback_id", -1);
        if (feedbackId != -1) {
            viewModel.setFeedbackId(feedbackId);
        } else {
            showToast("反馈ID无效");
            finish();
        }
    }
    
    @Override
    protected void setupDataBinding() {
        // 绑定文本
        DataBindingUtils.bindTextView(tvContent, viewModel.getContent(), this);
        DataBindingUtils.bindTextView(tvContact, viewModel.getContact(), this);
        DataBindingUtils.bindTextView(tvTime, viewModel.getTime(), this);
        DataBindingUtils.bindTextView(tvStatus, viewModel.getStatus(), this);
        DataBindingUtils.bindTextView(tvReplyContent, viewModel.getReplyContent(), this);
        DataBindingUtils.bindTextView(tvReplyTime, viewModel.getReplyTime(), this);
        
        // 绑定可见性
        DataBindingUtils.bindVisibility(layoutImages, viewModel.getHasImages(), this);
        DataBindingUtils.bindVisibility(layoutContact, viewModel.getHasContact(), this);
        DataBindingUtils.bindVisibility(layoutReply, viewModel.getHasReply(), this);
    }
    
    @Override
    protected void setupObservers() {
        // 观察图片列表
        viewModel.getImageUrls().observe(this, imageUrls -> {
            if (imageAdapter != null) {
                imageAdapter.setImageUrls(imageUrls);
            }
        });
    }

    /**
     * 打开图片预览界面
     * @param imageUrl 图片URL
     */
    private void openImageViewer(String imageUrl) {
        Intent intent = new Intent(this, DrawingImageViewerActivity.class);
        intent.putExtra("image_url", imageUrl);
        intent.putExtra("prompt", ""); // 反馈图片没有prompt，传空字符串
        intent.putExtra("hide_bottom_bar", true); // 隐藏底部操作栏
        startActivity(intent);
    }
}
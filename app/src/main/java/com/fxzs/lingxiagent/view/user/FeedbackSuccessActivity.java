package com.fxzs.lingxiagent.view.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fxzs.lingxiagent.R;

public class FeedbackSuccessActivity extends AppCompatActivity {
    
    private ImageView ivClose;
    private TextView tvHistory;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_success);
        
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        ivClose = findViewById(R.id.iv_close);
        tvHistory = findViewById(R.id.tv_history);
    }
    
    private void setupClickListeners() {
        // 关闭按钮 - 关闭当前页面和反馈页面，返回到之前的页面
        ivClose.setOnClickListener(v -> {
            // 设置结果，让FeedbackActivity也关闭
            setResult(RESULT_OK);
            finish();
        });
        
        // 反馈历史 - 跳转到反馈历史页面
        tvHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, FeedbackHistoryActivity.class);
            startActivity(intent);
            // 关闭成功页面
            finish();
        });
    }
    
    @Override
    public void onBackPressed() {
        // 返回键也要设置结果
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
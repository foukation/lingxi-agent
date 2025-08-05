package com.fxzs.lingxiagent.view.user;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.user.VMChangeMobile;

public class ChangeMobileActivity extends BaseActivity<VMChangeMobile> {
    
    private ImageView ivBack;
    private EditText etMobile;
    private EditText etCode;
    private Button btnSendCode;
    private Button btnConfirm;
    private TextView tvCurrentMobile;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_change_mobile;
    }
    
    @Override
    protected Class<VMChangeMobile> getViewModelClass() {
        return VMChangeMobile.class;
    }
    
    @Override
    protected void initializeViews() {
        ivBack = findViewById(R.id.iv_back);
        etMobile = findViewById(R.id.et_mobile);
        etCode = findViewById(R.id.et_code);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnConfirm = findViewById(R.id.btn_confirm);
        tvCurrentMobile = findViewById(R.id.tv_current_mobile);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        btnSendCode.setOnClickListener(v -> viewModel.sendVerificationCode());
        btnConfirm.setOnClickListener(v -> viewModel.changeMobile());
    }
    
    @Override
    protected void setupDataBinding() {
        // 双向绑定输入框
        DataBindingUtils.bindEditText(etMobile, viewModel.getMobile(), this);
        DataBindingUtils.bindEditText(etCode, viewModel.getVerificationCode(), this);
        
        // 绑定按钮状态
        DataBindingUtils.bindEnabled(btnSendCode, viewModel.getSendCodeEnabled(), this);
        DataBindingUtils.bindEnabled(btnConfirm, viewModel.getConfirmEnabled(), this);
        
        // 绑定按钮文本
        DataBindingUtils.bindTextView(btnSendCode, viewModel.getSendCodeText(), this);
        
        // 绑定当前手机号显示
        DataBindingUtils.bindTextView(tvCurrentMobile, viewModel.getCurrentMobileText(), this);
    }
    
    @Override
    protected void setupObservers() {
        // 观察修改成功事件
        viewModel.getChangeSuccess().observe(this, success -> {
            if (success != null && success) {
                showToast("手机号修改成功");
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
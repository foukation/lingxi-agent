package com.fxzs.lingxiagent.view.user;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.fxzs.lingxiagent.MainActivity;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.user.VMChangePassword;

public class ChangePasswordActivity extends BaseActivity<VMChangePassword> {
    
    private ImageView ivBack;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnConfirm;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_change_password;
    }
    
    @Override
    protected Class<VMChangePassword> getViewModelClass() {
        return VMChangePassword.class;
    }
    
    @Override
    protected void initializeViews() {
        ivBack = findViewById(R.id.iv_back);
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnConfirm = findViewById(R.id.btn_confirm);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> viewModel.changePassword());
    }
    
    @Override
    protected void setupDataBinding() {
        // 双向绑定密码输入框
        DataBindingUtils.bindEditText(etOldPassword, viewModel.getOldPassword(), this);
        DataBindingUtils.bindEditText(etNewPassword, viewModel.getNewPassword(), this);
        DataBindingUtils.bindEditText(etConfirmPassword, viewModel.getConfirmPassword(), this);
        
        // 绑定确认按钮状态
        DataBindingUtils.bindEnabled(btnConfirm, viewModel.getConfirmEnabled(), this);
    }

    @Override
    protected void setupObservers() {
        // 观察修改成功事件
        viewModel.getChangeSuccess().observe(this, success -> {
            if (success != null && success) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("selected_tab", 0); // 选中对话Tab
                startActivity(intent);

                finish();
            }
        });
    }
}
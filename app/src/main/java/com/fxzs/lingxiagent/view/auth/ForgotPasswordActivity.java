package com.fxzs.lingxiagent.view.auth;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.auth.VMForgotPassword;

public class ForgotPasswordActivity extends BaseActivity<VMForgotPassword> {
    private ImageView ivBack;
    private EditText etPhone;
    private EditText etVerification;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private TextView tvGetCode;
    private ImageView ivTogglePassword;
    private ImageView ivToggleConfirmPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_forgot_password;
    }

    @Override
    protected Class<VMForgotPassword> getViewModelClass() {
        return VMForgotPassword.class;
    }

    @Override
    protected void initializeViews() {
        ivBack = findViewById(R.id.iv_back);
        etPhone = findViewById(R.id.et_phone);
        etVerification = findViewById(R.id.et_verification);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvGetCode = findViewById(R.id.tv_get_code);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);
        ivToggleConfirmPassword = findViewById(R.id.iv_toggle_confirm_password);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progressBar);

        ivBack.setOnClickListener(v -> finish());
        tvGetCode.setOnClickListener(v -> viewModel.sendVerificationCode());
        ivTogglePassword.setOnClickListener(v -> viewModel.togglePasswordVisibility());
        ivToggleConfirmPassword.setOnClickListener(v -> viewModel.toggleConfirmPasswordVisibility());
        btnResetPassword.setOnClickListener(v -> viewModel.performPasswordReset());
    }

    @Override
    protected void setupDataBinding() {
        DataBindingUtils.bindEditText(etPhone, viewModel.getPhone(), this);
        DataBindingUtils.bindEditText(etVerification, viewModel.getVerificationCode(), this);
        DataBindingUtils.bindEditText(etPassword, viewModel.getNewPassword(), this);
        DataBindingUtils.bindEditText(etConfirmPassword, viewModel.getConfirmPassword(), this);
        DataBindingUtils.bindTextView(tvGetCode, viewModel.getCountdownText(), this);
    }

    @Override
    protected void setupObservers() {
        // 密码可见性
        viewModel.getPasswordVisible().observe(this, visible -> {
            if (visible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivTogglePassword.setSelected(true);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivTogglePassword.setSelected(false);
            }
            etPassword.setSelection(etPassword.length());
        });
        viewModel.getConfirmPasswordVisible().observe(this, visible -> {
            if (visible) {
                etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivToggleConfirmPassword.setSelected(true);
            } else {
                etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivToggleConfirmPassword.setSelected(false);
            }
            etConfirmPassword.setSelection(etConfirmPassword.length());
        });
        // 按钮高亮/变灰联动，enabled+alpha
        viewModel.getNextEnabled().observe(this, enabled -> {
            btnResetPassword.setEnabled(enabled != null && enabled);
            btnResetPassword.setAlpha(enabled != null && enabled ? 1.0f : 0.4f);
        });
        // 重置成功关闭页面
        viewModel.getResetResult().observe(this, success -> {
            if (success != null && success) {
                finish();
            }
        });
    }

    @Override
    protected void handleLoadingState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
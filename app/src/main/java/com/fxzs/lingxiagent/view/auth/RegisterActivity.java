package com.fxzs.lingxiagent.view.auth;

import android.content.Intent;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.fxzs.lingxiagent.model.auth.dto.LoginResponse;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.view.common.GlobalToast;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.CommonDialog;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.view.common.WebViewActivity;
import com.fxzs.lingxiagent.MainActivity;
import com.fxzs.lingxiagent.viewmodel.auth.VMRegister;

public class RegisterActivity extends BaseActivity<VMRegister> {
    
    // 顶部栏
    private ImageView ivBack;
    private TextView tvRegisterLink;
    
    // 标签切换
    private TextView tvVerificationTab;
    private TextView tvPasswordTab;
    private View indicatorLine;
    private View layoutVerification;
    private View layoutPassword;
    
    // 输入框
    private EditText etPhone;
    private EditText etPhoneVerification;
    private EditText etVerification;
    private EditText etPassword;
    private TextView tvGetCode;
    private ImageView ivTogglePassword;
    private TextView tvForgetPassword;
    private TextView tvRemainFailCount;

    // 底部
    private Button btnLogin;
    private CheckBox cbAgreement;
    private TextView tvAgreement;
    private ProgressBar progressBar;
    
    // 当前登录模式
    private boolean isPasswordMode = true;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_register;
    }
    
    @Override
    protected Class<VMRegister> getViewModelClass() {
        return VMRegister.class;
    }
    
    @Override
    protected void initializeViews() {
        // 顶部栏
        ivBack = findViewById(R.id.iv_back);
        tvRegisterLink = findViewById(R.id.tv_register_link);
        
        // 标签切换
        tvVerificationTab = findViewById(R.id.tv_verification_tab);
        tvPasswordTab = findViewById(R.id.tv_password_tab);
        indicatorLine = findViewById(R.id.indicator_line);
        layoutVerification = findViewById(R.id.layout_verification);
        layoutPassword = findViewById(R.id.layout_password);
        
        // 输入框
        etPhone = findViewById(R.id.et_phone);
        etPhoneVerification = findViewById(R.id.et_phone_verification);
        etVerification = findViewById(R.id.et_verification);
        etPassword = findViewById(R.id.et_password);
        tvGetCode = findViewById(R.id.tv_get_code);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);
        tvForgetPassword = findViewById(R.id.tv_forget_password);
        tvRemainFailCount = findViewById(R.id.tv_remain_fail_count);

        // 底部
        btnLogin = findViewById(R.id.btn_login);
        cbAgreement = findViewById(R.id.cb_agreement);
        TextView tvUserAgreement = findViewById(R.id.tv_user_agreement);
        TextView tvPrivacyPolicy = findViewById(R.id.tv_privacy_policy);
        progressBar = findViewById(R.id.progressBar);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        tvRegisterLink.setOnClickListener(v -> {
            // 跳转到新的注册界面
            Intent intent = new Intent(this, RegisterNewActivity.class);
            startActivity(intent);
        });
        tvGetCode.setOnClickListener(v -> {
            String phone = isPasswordMode ? etPhone.getText().toString().trim() : etPhoneVerification.getText().toString().trim();
            if (phone.length() != 11) {
                GlobalToast.show(this, "手机号码不合法", GlobalToast.Type.ERROR);
                return;
            }
            viewModel.sendVerificationCode(Constants.SCENE_LOGIN);
        });
        ivTogglePassword.setOnClickListener(v -> viewModel.togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> performLogin());
        
        // 标签切换点击事件
        tvVerificationTab.setOnClickListener(v -> switchToVerificationMode());
        tvPasswordTab.setOnClickListener(v -> switchToPasswordMode());
        
        // 忘记密码点击事件
        if (tvForgetPassword != null) {
            tvForgetPassword.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
        
        // 协议点击事件
        tvUserAgreement.setOnClickListener(v -> {
            WebViewActivity.start(this, Constants.USER_AGREEMENT_URL, "用户协议");
        });

        tvPrivacyPolicy.setOnClickListener(v -> {
            WebViewActivity.start(this, Constants.PRIVACY_POLICY_URL, "隐私政策");
        });
        
        // 默认显示密码登录模式
        switchToPasswordMode();
    }
    
    @Override
    protected void setupDataBinding() {
        // 双向绑定输入框 - 账号密码模式的手机号
        DataBindingUtils.bindEditText(etPhone, viewModel.getPhone(), this);
        // 验证码模式的手机号
        DataBindingUtils.bindEditText(etPhoneVerification, viewModel.getPhone(), this);
        DataBindingUtils.bindEditText(etVerification, viewModel.getVerificationCode(), this);
        DataBindingUtils.bindEditText(etPassword, viewModel.getPassword(), this);
        
        // 绑定按钮状态
        DataBindingUtils.bindEnabled(btnLogin, viewModel.getRegisterEnabled(), this);
        DataBindingUtils.bindEnabled(tvGetCode, viewModel.getCanGetCode(), this);
        
        // 绑定文本显示
        DataBindingUtils.bindTextView(tvGetCode, viewModel.getCountdownText(), this);
        
        // 绑定复选框
        cbAgreement.setOnCheckedChangeListener((buttonView, isChecked) -> 
            viewModel.getAgreementChecked().set(isChecked));
        
        // 绑定密码可见性
        viewModel.getPasswordVisible().observe(this, visible -> {
            if (visible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivTogglePassword.setImageResource(android.R.drawable.ic_secure);
            }
            // 保持光标在末尾
            etPassword.setSelection(etPassword.length());
        });
    }
    
    @Override
    protected void setupObservers() {
        // 观察发送验证码的结果
        viewModel.getSendSmsResult().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (null != success && success) {
                    tvRemainFailCount.setVisibility(View.INVISIBLE);
                }
            }
        });
        // 添加对registerEnabled的观察，用于调试
        viewModel.getRegisterEnabled().observeForever(enabled -> {
            android.util.Log.d("RegisterActivity", "registerEnabled changed to: " + enabled);
        });
        
        // 观察验证码登录结果
        viewModel.getLoginBySmsResult().observe(this, new Observer<LoginResponse>() {
            @Override
            public void onChanged(LoginResponse loginResponse) {
                if (null != loginResponse) {
                    String token = loginResponse.getToken();
                    if (null != token) {
                        // 登录成功，直接跳转到主界面
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        int remainFailCount = loginResponse.getRemainFailCount();
                        tvRemainFailCount.setVisibility(View.VISIBLE);
                        tvRemainFailCount.setText("验证码不正确，剩余验证" + remainFailCount + "次！");
                    }
                }
            }
        });

        // 观察注册结果
        viewModel.getRegisterResult().observe(this, success -> {
            if (success != null && success) {
                // 登录成功，直接跳转到主界面
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
    
    @Override
    protected void handleLoadingState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
    
    /**
     * 切换到验证码登录模式
     */
    private void switchToVerificationMode() {
        isPasswordMode = false;
        viewModel.setLoginMode(false);
        
        // 更新标签样式
        tvVerificationTab.setTextColor(getColor(R.color.login_text_primary));
        tvVerificationTab.setTypeface(null, android.graphics.Typeface.BOLD);
        tvPasswordTab.setTextColor(getColor(android.R.color.darker_gray));
        tvPasswordTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        // 移动指示线到验证码下方
        // 使用post确保视图已经布局完成
        tvVerificationTab.post(() -> {
            int tabX = tvVerificationTab.getLeft();
            int tabWidth = tvVerificationTab.getWidth();
            int indicatorWidth = indicatorLine.getWidth();
            // 计算使指示线居中的x坐标
            int centerX = tabX + (tabWidth - indicatorWidth) / 2;
            
            indicatorLine.animate()
                .x(centerX)
                .setDuration(200)
                .start();
        });
        
        // 切换布局
        layoutVerification.setVisibility(View.VISIBLE);
        layoutPassword.setVisibility(View.GONE);
        
        // 清空密码输入
        viewModel.getPassword().set("");
    }
    
    /**
     * 切换到账号密码登录模式
     */
    private void switchToPasswordMode() {
        isPasswordMode = true;
        viewModel.setLoginMode(true);
        
        // 更新标签样式
        tvPasswordTab.setTextColor(getColor(R.color.login_text_primary));
        tvPasswordTab.setTypeface(null, android.graphics.Typeface.BOLD);
        tvVerificationTab.setTextColor(getColor(android.R.color.darker_gray));
        tvVerificationTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        // 移动指示线到账号密码下方
        // 使用post确保视图已经布局完成
        tvPasswordTab.post(() -> {
            int tabX = tvPasswordTab.getLeft();
            int tabWidth = tvPasswordTab.getWidth();
            int indicatorWidth = indicatorLine.getWidth();
            // 计算使指示线居中的x坐标
            int centerX = tabX + (tabWidth - indicatorWidth) / 2;
            
            indicatorLine.animate()
                .x(centerX)
                .setDuration(200)
                .start();
        });
        
        // 切换布局
        layoutPassword.setVisibility(View.VISIBLE);
        layoutVerification.setVisibility(View.GONE);
        
        // 清空验证码输入
        viewModel.getVerificationCode().set("");
    }
    
    /**
     * 执行登录操作
     */
    private void performLogin() {
        // 检查是否同意协议
        if (!cbAgreement.isChecked()) {
            // 使用协议确认弹窗（带有可点击的链接）
            CommonDialog.showAgreementDialog(this,
                new CommonDialog.OnDialogClickListener() {
                    @Override
                    public void onConfirm() {
                        // 用户点击同意，自动勾选协议并继续登录
                        cbAgreement.setChecked(true);
                        performLogin();
                    }

                    @Override
                    public void onCancel() {
                        // 用户点击不同意，不做任何操作
                    }
                });
            return;
        }

        // 如果是密码模式，进行密码校验
        if (isPasswordMode) {
            String passwordError = viewModel.getPasswordValidationError();
            if (passwordError != null) {
                GlobalToast.show(this, passwordError, GlobalToast.Type.ERROR);
                return;
            }
            viewModel.loginByPassword();
        } else {
            viewModel.loginBySms();
        }
    }
}
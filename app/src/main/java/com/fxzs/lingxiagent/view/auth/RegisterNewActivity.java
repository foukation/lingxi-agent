package com.fxzs.lingxiagent.view.auth;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.CommonDialog;
import com.fxzs.lingxiagent.view.common.ConfirmDialog;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.MainActivity;
import com.fxzs.lingxiagent.view.common.GlobalToast;
import com.fxzs.lingxiagent.viewmodel.auth.VMRegister;
import com.fxzs.lingxiagent.view.common.WebViewActivity;

public class RegisterNewActivity extends BaseActivity<VMRegister> {
    
    // UI组件
    private ImageView ivBack;
    private EditText etPhone;
    private EditText etVerification;
    private EditText etPassword;
    private TextView tvGetCode;
    private ImageView ivTogglePassword;
    private Button btnRegister;
    private CheckBox cbAgreement;
    private TextView tvAgreement;
    private ProgressBar progressBar;
    
    // 密码是否可见
    private boolean isPasswordVisible = false;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_register_new;
    }
    
    @Override
    protected Class<VMRegister> getViewModelClass() {
        return VMRegister.class;
    }
    
    @Override
    protected void initializeViews() {
        // 初始化视图
        ivBack = findViewById(R.id.iv_back);
        etPhone = findViewById(R.id.et_phone);
        etVerification = findViewById(R.id.et_verification);
        etPassword = findViewById(R.id.et_password);
        tvGetCode = findViewById(R.id.tv_get_code);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);
        btnRegister = findViewById(R.id.btn_register);
        cbAgreement = findViewById(R.id.cb_agreement);
        tvAgreement = findViewById(R.id.tv_agreement);
        progressBar = findViewById(R.id.progressBar);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        
        tvGetCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.sendVerificationCode(Constants.SCENE_REGISTER);
        });
        
        ivTogglePassword.setOnClickListener(v -> {
            viewModel.togglePasswordVisibility();
        });
        
        btnRegister.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String code = etVerification.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (phone.isEmpty() || code.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 密码校验
            String passwordError = ((VMRegister) viewModel).getPasswordValidationError();
            if (passwordError != null) {
                GlobalToast.show(this, passwordError,
                   GlobalToast.Type.ERROR);
                return;
            }
            
            if (!cbAgreement.isChecked()) {
                // 使用协议确认弹窗（带有可点击的链接）
                CommonDialog.showAgreementDialog(this,
                    new CommonDialog.OnDialogClickListener() {
                        @Override
                        public void onConfirm() {
                            // 用户点击同意，自动勾选协议并继续注册
                            cbAgreement.setChecked(true);
                            viewModel.performRegister();
                        }

                        @Override
                        public void onCancel() {
                            // 用户点击不同意，不做任何操作
                        }
                    });
                return;
            }
            
            viewModel.performRegister();
        });
        
        // 添加输入监听，实时更新按钮状态
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateRegisterButtonState();
            }
        };
        
        etPhone.addTextChangedListener(textWatcher);
        etVerification.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        findViewById(R.id.tv_yidong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewActivity.start(RegisterNewActivity.this, "https://mobile-web.jmkjsh.com/user_contract.html", "服务协议");
            }
        });
        findViewById(R.id.tv_user_agreement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewActivity.start(RegisterNewActivity.this, "https://mobile-web.jmkjsh.com/user_contract.html", "使用协议");
            }
        });
        findViewById(R.id.tv_privacy_policy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewActivity.start(RegisterNewActivity.this, "https://mobile-web.jmkjsh.com/privacy.html", "隐私政策");
            }
        });
    }
    
    @Override
    protected void setupDataBinding() {
        // 设置双向数据绑定
        DataBindingUtils.bindEditText(etPhone, viewModel.getPhone(), this);
        DataBindingUtils.bindEditText(etVerification, viewModel.getVerificationCode(), this);
        DataBindingUtils.bindEditText(etPassword, viewModel.getPassword(), this);
        DataBindingUtils.bindCheckBox(cbAgreement, viewModel.getAgreementChecked(), this);
        DataBindingUtils.bindButtonEnabled(btnRegister, viewModel.getRegisterEnabled(), this);
    }
    
    @Override
    protected void setupObservers() {
        // 观察加载状态
        viewModel.getLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnRegister.setEnabled(!isLoading);
        });

        // 观察验证码倒计时文本
        viewModel.getCountdownText().observe(this, text -> {
            tvGetCode.setText(text);
        });
        
        // 观察是否可以获取验证码
        viewModel.getCanGetCode().observe(this, canGet -> {
            tvGetCode.setEnabled(canGet);
        });
        
        // 观察密码可见性
        viewModel.getPasswordVisible().observe(this, visible -> {
            ivTogglePassword.setSelected(visible);
            if (visible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.getText().length());
        });
        
        // 观察注册结果
        viewModel.getRegisterResult().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                // 跳转到主界面
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
    
    private void updateRegisterButtonState() {
        String phone = etPhone.getText().toString().trim();
        String code = etVerification.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        boolean isEnabled = !phone.isEmpty() && !code.isEmpty() && !password.isEmpty();
        btnRegister.setEnabled(isEnabled);
        btnRegister.setAlpha(isEnabled ? 1.0f : 0.4f);
    }
}
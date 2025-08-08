package com.fxzs.lingxiagent.view.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.util.ZUtils;

public class ForgetPwdActivity extends AppCompatActivity {
    
    private ImageView ivBack;
    private EditText etPhone;
    private EditText etVerifyCode;
    private TextView btnGetCode;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnResetPassword;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ZUtils.setSystem(ForgetPwdActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_pwd);
        
        initViews();
        setListeners();
    }
    
    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etPhone = findViewById(R.id.et_phone);
        etVerifyCode = findViewById(R.id.et_verification);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnGetCode = findViewById(R.id.tv_get_code);
        btnResetPassword = findViewById(R.id.btn_reset_password);
    }
    
    private void setListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = etPhone.getText().toString().trim();
                if (phone.isEmpty()) {
                    Toast.makeText(ForgetPwdActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                // TODO: 发送验证码逻辑
                Toast.makeText(ForgetPwdActivity.this, "验证码已发送，5分钟内有效", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    // TODO: 重置密码逻辑
                    Toast.makeText(ForgetPwdActivity.this, "密码重置成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
    
    private boolean validateInput() {
        String phone = etPhone.getText().toString().trim();
        String code = etVerifyCode.getText().toString().trim();
        String newPwd = etNewPassword.getText().toString().trim();
        String confirmPwd = etConfirmPassword.getText().toString().trim();
        
        if (phone.isEmpty()) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (code.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (newPwd.isEmpty()) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (confirmPwd.isEmpty()) {
            Toast.makeText(this, "请确认新密码", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (!newPwd.equals(confirmPwd)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
}
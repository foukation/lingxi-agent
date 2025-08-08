package com.fxzs.lingxiagent.viewmodel.auth;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.fxzs.lingxiagent.model.auth.dto.LoginResponse;
import com.fxzs.lingxiagent.model.auth.repository.AuthRepository;
import com.fxzs.lingxiagent.model.auth.repository.AuthRepositoryImpl;
import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.common.ObservableField;
import java.util.concurrent.TimeUnit;

public class VMRegister extends BaseViewModel {
    
    // 双向绑定字段
    private final ObservableField<String> phone = new ObservableField<>("");
    private final ObservableField<String> verificationCode = new ObservableField<>("");
    private final ObservableField<String> password = new ObservableField<>("");
    private final ObservableField<Boolean> registerEnabled = new ObservableField<>(false);
    private final ObservableField<Boolean> agreementChecked = new ObservableField<>(false);
    private final ObservableField<Boolean> passwordVisible = new ObservableField<>(false);
    private final ObservableField<String> countdownText = new ObservableField<>("获取验证码");
    private final ObservableField<Boolean> canGetCode = new ObservableField<>(true);
    private final ObservableField<Boolean> isPasswordMode = new ObservableField<>(false);
    
    // 业务状态
    private final MutableLiveData<Boolean> registerResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sendSmsResult = new MutableLiveData<>();
    private final MutableLiveData<LoginResponse> loginBySmsResult = new MutableLiveData<>();

    private int countdownSeconds = 0;
    private final AuthRepository authRepository;
    
    public VMRegister(@NonNull Application application) {
        super(application);
        // 初始化Repository
        authRepository = new AuthRepositoryImpl();
        
        // 设置字段监听
        phone.observeForever(value -> validateForm());
        verificationCode.observeForever(value -> validateForm());
        password.observeForever(value -> validateForm());
    }
    
    // Getters
    public ObservableField<String> getPhone() {
        return phone;
    }
    
    public ObservableField<String> getVerificationCode() {
        return verificationCode;
    }
    
    public ObservableField<String> getPassword() {
        return password;
    }
    
    public ObservableField<Boolean> getRegisterEnabled() {
        return registerEnabled;
    }
    
    public ObservableField<Boolean> getAgreementChecked() {
        return agreementChecked;
    }
    
    public ObservableField<Boolean> getPasswordVisible() {
        return passwordVisible;
    }
    
    public ObservableField<String> getCountdownText() {
        return countdownText;
    }
    
    public ObservableField<Boolean> getCanGetCode() {
        return canGetCode;
    }
    
    public ObservableField<Boolean> getIsPasswordMode() {
        return isPasswordMode;
    }
    
    public MutableLiveData<Boolean> getRegisterResult() {
        return registerResult;
    }

    public MutableLiveData<Boolean> getSendSmsResult() {
        return sendSmsResult;
    }

    public MutableLiveData<LoginResponse> getLoginBySmsResult() {
        return loginBySmsResult;
    }

    // 验证码登录
    public void loginBySms() {
        setLoading(true);
        String phoneNumber = phone.get();
        String code = verificationCode.get();
        // 仅使用验证码登录
        authRepository.loginBySms(phoneNumber, code).observeForever(new Observer<LoginResponse>() {
            @Override
            public void onChanged(LoginResponse response) {
                setLoading(false);
                if (response != null && !TextUtils.isEmpty(response.getToken())) {
                    loginBySmsResult.postValue(response);
                    setSuccess("登录成功");
                } else {
                    loginBySmsResult.postValue(response);
                    setError(response.getMessage());
                }
            }
        });
    }

    // 账号密码登录
    public void loginByPassword() {
        setLoading(true);
        
        String phoneNumber = phone.get();
        String pwd = password.get();
        authRepository.loginByPassword(phoneNumber, pwd).observeForever(new Observer<LoginResponse>() {
            @Override
            public void onChanged(LoginResponse response) {
                setLoading(false);
                if (response != null) {
                    registerResult.postValue(true);
                    setSuccess("登录成功");
                } else {
                    setError("登录失败，请检查账号或密码是否正确");
                }
            }
        });
    }

    // 使用验证码+密码进行注册
    public void performRegister() {
        if (!registerEnabled.get()) {
            return;
        }
        
        setLoading(true);
        
        String phoneNumber = phone.get();
        String code = verificationCode.get();
        String pwd = password.get();
        authRepository.register(phoneNumber, code, pwd).observeForever(new Observer<LoginResponse>() {
            @Override
            public void onChanged(LoginResponse response) {
                setLoading(false);
                if (response != null) {
                    registerResult.postValue(true);
                    setSuccess("注册成功");
                } else {
                    setError("注册失败，请检查信息是否正确");
                }
            }
        });
    }
    
    public void performOneClickLogin(String loginToken) {
        setLoading(true);

        authRepository.oneClickLogin(loginToken).observeForever(new Observer<LoginResponse>() {
            @Override
            public void onChanged(LoginResponse response) {
                setLoading(false);
                if (response != null) {
                    registerResult.postValue(true);
                    setSuccess("登录成功");
                } else {
                    setError("一键登录失败，请尝试其他登录方式");
                }
            }
        });
    }
    
    public void setLoginMode(boolean passwordMode) {
        isPasswordMode.set(passwordMode);
        validateForm();
    }
    
    public void sendVerificationCode(int scene) {
        if (!canGetCode.get() || !isPhoneValid()) {
            return;
        }
        
        setLoading(true);
        
        String phoneNumber = phone.get();
        authRepository.sendSmsCode(phoneNumber, scene).observeForever(new Observer<BaseResponse<Boolean>>() {
            @Override
            public void onChanged(BaseResponse<Boolean> response) {
                setLoading(false);

                if (null != response) {
                    if (response.isSuccess()) {
                        setSuccess("验证码已发送，5分钟内有效");
                        sendSmsResult.setValue(true);
                        startCountdown();
                    } else {
                        String msg = response.getMsg();
                        setError(msg.isEmpty() ? "发送验证码失败，请稍后再试" : msg);
                    }
                }
            }
        });
    }
    
    public void togglePasswordVisibility() {
        passwordVisible.set(!passwordVisible.get());
    }
    
    // 私有方法
    private void validateForm() {
        boolean phoneValid = isPhoneValid();
        
        if (isPasswordMode.get()) {
            // 密码登录模式
            boolean passwordValid = isPasswordValid();
            registerEnabled.set(phoneValid && passwordValid);
        } else {
            // 验证码登录模式
            boolean codeValid = verificationCode.isNotEmpty() && verificationCode.get().length() >= Constants.VERIFICATION_CODE_LEN;
            registerEnabled.set(phoneValid && codeValid);
        }
    }
    
    private boolean isPhoneValid() {
        String phoneNumber = phone.get();
        return phoneNumber != null && phoneNumber.matches("^1[3-9]\\d{9}$");
    }
    
    private boolean isPasswordValid() {
        String pass = password.get();
        if (pass == null || pass.isEmpty()) {
            return false;
        }

        // 长度校验：至少6位
        if (pass.length() < 6) {
            return false;
        }

        // 最大长度限制：不超过20位
        if (pass.length() > 20) {
            return false;
        }

        // 字符校验：只允许字母、数字、特殊字符
        if (!pass.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]+$")) {
            return false;
        }

        // 复杂度校验：至少包含大小写字母各一位和特殊字符
        boolean hasUpperCase = pass.matches(".*[A-Z].*");
        boolean hasLowerCase = pass.matches(".*[a-z].*");
        boolean hasDigit = pass.matches(".*\\d.*");
        boolean hasSpecialChar = pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`].*");

        return hasUpperCase && hasLowerCase && hasSpecialChar;
    }

    /**
     * 获取密码校验错误信息
     * @return 错误信息，如果密码有效则返回null
     */
    public String getPasswordValidationError() {
        String pass = password.get();
        if (pass == null || pass.isEmpty()) {
            return "请输入密码";
        }

        // 长度校验
        if (pass.length() < 6) {
            return "密码长度至少6位";
        }

        if (pass.length() > 20) {
            return "密码长度不能超过20位";
        }

        // 字符校验
        if (!pass.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]+$")) {
            return "密码只能包含字母、数字和常用特殊字符";
        }

        // 复杂度校验
        boolean hasUpperCase = pass.matches(".*[A-Z].*");
        boolean hasLowerCase = pass.matches(".*[a-z].*");
        boolean hasDigit = pass.matches(".*\\d.*");
        boolean hasSpecialChar = pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`].*");

        if (!hasUpperCase) {
            return "密码必须包含至少一个大写字母";
        }

        if (!hasLowerCase) {
            return "密码必须包含至少一个小写字母";
        }

        if (!hasSpecialChar) {
            return "密码必须包含至少一个特殊字符";
        }

        return null; // 密码有效
    }

    private void startCountdown() {
        canGetCode.set(false);
        countdownSeconds = Constants.SMS_COUNTDOWN;
        
        new Thread(() -> {
            while (countdownSeconds > 0) {
                countdownText.postValue(countdownSeconds + "s后重新获取");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countdownSeconds--;
            }
            countdownText.postValue("获取验证码");
            canGetCode.postValue(true);
        }).start();
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        phone.removeObserver(value -> validateForm());
        verificationCode.removeObserver(value -> validateForm());
        password.removeObserver(value -> validateForm());
    }
}
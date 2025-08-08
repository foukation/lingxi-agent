package com.fxzs.lingxiagent.viewmodel.auth;

import android.app.Application;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.fxzs.lingxiagent.model.auth.repository.AuthRepository;
import com.fxzs.lingxiagent.model.auth.repository.AuthRepositoryImpl;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.common.BaseResponse;

public class VMForgotPassword extends BaseViewModel {
    
    // 双向绑定字段
    private final ObservableField<String> phone = new ObservableField<>("");
    private final ObservableField<String> verificationCode = new ObservableField<>("");
    private final ObservableField<String> newPassword = new ObservableField<>("");
    private final ObservableField<Boolean> passwordVisible = new ObservableField<>(false);
    private final ObservableField<Boolean> nextEnabled = new ObservableField<>(false);
    private final ObservableField<Boolean> canGetCode = new ObservableField<>(true);
    private final ObservableField<String> countdownText = new ObservableField<>("获取验证码");
    private final ObservableField<String> confirmPassword = new ObservableField<>("");
    private final ObservableField<Boolean> confirmPasswordVisible = new ObservableField<>(false);
    
    // 业务状态
    private final MutableLiveData<Boolean> resetResult = new MutableLiveData<>();
    
    // 仓库
    private final AuthRepository authRepository;
    
    // 倒计时器
    private CountDownTimer countDownTimer;
    
    public VMForgotPassword(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepositoryImpl();
        
        // 设置字段联动逻辑
        phone.observeForever(this::validateForm);
        verificationCode.observeForever(this::validateForm);
        newPassword.observeForever(this::validateForm);
        confirmPassword.observeForever(this::validateForm);
    }
    
    // Getter方法
    public ObservableField<String> getPhone() {
        return phone;
    }
    
    public ObservableField<String> getVerificationCode() {
        return verificationCode;
    }
    
    public ObservableField<String> getNewPassword() {
        return newPassword;
    }
    
    public ObservableField<Boolean> getPasswordVisible() {
        return passwordVisible;
    }
    
    public ObservableField<Boolean> getNextEnabled() {
        return nextEnabled;
    }
    
    public ObservableField<Boolean> getCanGetCode() {
        return canGetCode;
    }
    
    public ObservableField<String> getCountdownText() {
        return countdownText;
    }
    
    public ObservableField<String> getConfirmPassword() { return confirmPassword; }
    public ObservableField<Boolean> getConfirmPasswordVisible() { return confirmPasswordVisible; }
    
    public MutableLiveData<Boolean> getResetResult() {
        return resetResult;
    }
    
    /**
     * 发送验证码
     */
    public void sendVerificationCode() {
        String phoneNumber = phone.get();
        if (phoneNumber == null || phoneNumber.length() != 11) {
            setError("请输入正确的手机号");
            return;
        }
        
        setLoading(true);
        authRepository.sendSmsCode(phoneNumber, Constants.SCENE_RESET_PWD)
                .observeForever(new Observer<BaseResponse<Boolean>>() {
                    @Override
                    public void onChanged(BaseResponse<Boolean> response) {
                        setLoading(false);
                        if (response != null && response.isSuccess()) {
                            setSuccess("验证码已发送，5分钟内有效");
                            startCountdown();
                        } else {
                            String errorMsg = "验证码发送失败";
                            if (null != response) {
                                errorMsg = response.getMsg();
                            }
                            setError(errorMsg);
                        }
                    }
                });
    }
    
    /**
     * 切换密码可见性
     */
    public void togglePasswordVisibility() {
        passwordVisible.set(!passwordVisible.get());
    }

    /**
     * 切换确认密码可见性
     */
    public void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible.set(!confirmPasswordVisible.get());
    }
    
    /**
     * 执行密码重置
     */
    public void performPasswordReset() {
        if (!nextEnabled.get()) {
            return;
        }
        
        String phoneNumber = phone.get();
        String code = verificationCode.get();
        String password = newPassword.get();
        String confirmPwd = confirmPassword.get();

        // 密码校验
        String passwordError = getPasswordValidationError(password);
        if (passwordError != null) {
            setError(passwordError);
            return;
        }

        if (!password.equals(confirmPwd)) {
            setError("两次输入的密码不一致");
            return;
        }
        
        setLoading(true);
        authRepository.resetPassword(phoneNumber, code, password).observeForever(new Observer<BaseResponse<Boolean>>() {
            @Override
            public void onChanged(BaseResponse<Boolean> resp) {
                setLoading(false);
                if (resp != null && resp.getCode() == 0 && Boolean.TRUE.equals(resp.getData())) {
                    resetResult.postValue(true);
                    setSuccess("密码重置成功");
                } else {
                    setError(resp != null ? resp.getMsg() : "重置失败");
                }
            }
        });
    }
    
    /**
     * 获取密码校验错误信息
     * @param password 密码
     * @return 错误信息，如果密码有效则返回null
     */
    private String getPasswordValidationError(String password) {
        if (password == null || password.isEmpty()) {
            return "请输入密码";
        }

        // 长度校验
        if (password.length() < 6) {
            return "密码长度至少6位";
        }

        if (password.length() > 20) {
            return "密码长度不能超过20位";
        }

        // 字符校验
        if (!password.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]+$")) {
            return "密码只能包含字母、数字和常用特殊字符";
        }

        // 复杂度校验
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`].*");

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

    /**
     * 表单验证
     */
    private void validateForm(String value) {
        boolean phoneValid = phone.isNotEmpty() && phone.get().length() == 11;
        boolean codeValid = verificationCode.isNotEmpty() && verificationCode.get().length() >= 4;
        boolean passwordValid = newPassword.isNotEmpty() && getPasswordValidationError(newPassword.get()) == null;
        boolean confirmValid = confirmPassword.isNotEmpty() && confirmPassword.get().equals(newPassword.get());
        nextEnabled.set(phoneValid && codeValid && passwordValid && confirmValid);
    }
    
    /**
     * 开始倒计时
     */
    private void startCountdown() {
        canGetCode.set(false);
        
        countDownTimer = new CountDownTimer(Constants.SMS_COUNTDOWN * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                countdownText.set("重新发送 " + seconds + "s");
            }
            
            @Override
            public void onFinish() {
                canGetCode.set(true);
                countdownText.set("获取验证码");
            }
        }.start();
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理观察者
        phone.removeObserver(this::validateForm);
        verificationCode.removeObserver(this::validateForm);
        newPassword.removeObserver(this::validateForm);
        confirmPassword.removeObserver(this::validateForm);
        
        // 取消倒计时
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
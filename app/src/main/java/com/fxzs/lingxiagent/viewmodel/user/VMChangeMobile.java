package com.fxzs.lingxiagent.viewmodel.user;

import android.app.Application;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.fxzs.lingxiagent.model.auth.repository.AuthRepository;
import com.fxzs.lingxiagent.model.auth.repository.AuthRepositoryImpl;
import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.user.UserUtil;
import com.fxzs.lingxiagent.model.user.dto.UpdateMobileReqDto;
import com.fxzs.lingxiagent.model.user.repository.UserRepository;
import com.fxzs.lingxiagent.model.user.repository.UserRepositoryImpl;
import com.fxzs.lingxiagent.util.AesUtil;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;

public class VMChangeMobile extends BaseViewModel {
    
    // 双向绑定字段
    private final ObservableField<String> mobile = new ObservableField<>("");
    private final ObservableField<String> verificationCode = new ObservableField<>("");
    private final ObservableField<Boolean> sendCodeEnabled = new ObservableField<>(false);
    private final ObservableField<Boolean> confirmEnabled = new ObservableField<>(false);
    private final ObservableField<String> sendCodeText = new ObservableField<>("获取验证码");
    private final ObservableField<String> currentMobileText = new ObservableField<>("当前手机号：");
    
    // 业务状态
    private final MutableLiveData<Boolean> changeSuccess = new MutableLiveData<>();
    
    // Repository
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    // 倒计时器
    private CountDownTimer countDownTimer;
    private boolean isCountingDown = false;
    
    public VMChangeMobile(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepositoryImpl();
        userRepository = new UserRepositoryImpl();
        
        // 监听手机号变化，更新发送验证码按钮状态
        mobile.observeForever(this::validateMobile);
        
        // 监听验证码变化，更新确认按钮状态
        verificationCode.observeForever(value -> validateForm());
        
        // 加载当前手机号
        loadCurrentMobile();
    }
    
    // Getters
    public ObservableField<String> getMobile() {
        return mobile;
    }
    
    public ObservableField<String> getVerificationCode() {
        return verificationCode;
    }
    
    public ObservableField<Boolean> getSendCodeEnabled() {
        return sendCodeEnabled;
    }
    
    public ObservableField<Boolean> getConfirmEnabled() {
        return confirmEnabled;
    }
    
    public ObservableField<String> getSendCodeText() {
        return sendCodeText;
    }
    
    public ObservableField<String> getCurrentMobileText() {
        return currentMobileText;
    }
    
    public MutableLiveData<Boolean> getChangeSuccess() {
        return changeSuccess;
    }
    
    // 业务方法
    public void sendVerificationCode() {
        String phoneNumber = mobile.get();
        if (!isValidMobile(phoneNumber) || isCountingDown) {
            return;
        }
        
        setLoading(true);
        
        // 修改手机号场景，scene为2
        authRepository.sendSmsCode(phoneNumber, Constants.SCENE_CHANGE_PHONE).observeForever(new Observer<BaseResponse<Boolean>>() {
            @Override
            public void onChanged(BaseResponse<Boolean> response) {
                setLoading(false);
                if (response.isSuccess()) {
                    setSuccess("验证码已发送，5分钟内有效");
                    // 开始倒计时
                    startCountdown();
                } else {
                    String msg = response.getMsg();
                    setError(msg.isEmpty() ? "发送验证码失败，请稍后再试" : msg);
                }
            }
        });
    }
    
    public void changeMobile() {
        String oldMobile = SharedPreferencesUtil.getUserPhone();
        String newMobile = mobile.get();
        String encryptedMobile = AesUtil.encrypt(newMobile, Constants.KEY_ALIAS);
        String code = verificationCode.get();
        
        if (!validateInput(newMobile, code)) {
            return;
        }
        
        setLoading(true);

        UpdateMobileReqDto updateMobileReq = new UpdateMobileReqDto();
        updateMobileReq.setOldMobile(oldMobile);
        updateMobileReq.setMobile(encryptedMobile);
        updateMobileReq.setCode(AesUtil.encrypt(code, Constants.KEY_ALIAS));
        
        userRepository.updateMobile(updateMobileReq, new UserRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                setLoading(false);
                SharedPreferencesUtil.saveUserPhone(encryptedMobile);
                setSuccess("手机号修改成功");
                changeSuccess.postValue(true);
            }
            
            @Override
            public void onError(String error) {
                setError("修改失败: " + error);
            }
        });
    }
    
    // 私有方法
    private void loadCurrentMobile() {
        String phone = SharedPreferencesUtil.getUserPhone();
        currentMobileText.postValue("当前手机号：" + UserUtil.formatPhone(phone));
    }
    
    private void validateMobile(String value) {
        boolean isValid = isValidMobile(value) && !isCountingDown;
        sendCodeEnabled.set(isValid);
        validateForm();
    }
    
    private void validateForm() {
        String phoneNumber = mobile.get();
        String code = verificationCode.get();
        
        boolean isValid = isValidMobile(phoneNumber) && 
                         code != null && code.length() == 6;
        
        confirmEnabled.set(isValid);
    }
    
    private boolean validateInput(String newMobile, String code) {
        if (!isValidMobile(newMobile)) {
            setError("请输入正确的手机号");
            return false;
        }
        
        if (code == null || code.length() != 6) {
            setError("请输入6位验证码");
            return false;
        }
        
        return true;
    }
    
    private boolean isValidMobile(String mobile) {
        return mobile != null && mobile.matches("^1[3-9]\\d{9}$");
    }
    
    private String formatPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    private void startCountdown() {
        isCountingDown = true;
        sendCodeEnabled.set(false);
        
        countDownTimer = new CountDownTimer(Constants.SMS_COUNTDOWN * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                sendCodeText.postValue(seconds + "秒后重试");
            }
            
            @Override
            public void onFinish() {
                isCountingDown = false;
                sendCodeText.postValue("获取验证码");
                validateMobile(mobile.get());
            }
        }.start();
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        mobile.removeObserver(this::validateMobile);
        verificationCode.removeObserver(value -> validateForm());
    }
}
package com.fxzs.lingxiagent.viewmodel.user;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.auth.repository.AuthRepository;
import com.fxzs.lingxiagent.model.auth.repository.AuthRepositoryImpl;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.user.dto.UpdatePasswordReqDto;
import com.fxzs.lingxiagent.model.user.repository.UserRepository;
import com.fxzs.lingxiagent.model.user.repository.UserRepositoryImpl;
import com.fxzs.lingxiagent.util.AesUtil;
public class VMChangePassword extends BaseViewModel {
    
    // 双向绑定字段
    private final ObservableField<String> oldPassword = new ObservableField<>("");
    private final ObservableField<String> newPassword = new ObservableField<>("");
    private final ObservableField<String> confirmPassword = new ObservableField<>("");
    private final ObservableField<Boolean> confirmEnabled = new ObservableField<>(false);
    
    // 业务状态
    private final MutableLiveData<Boolean> changeSuccess = new MutableLiveData<>();
    
    // Repository
    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    public VMChangePassword(@NonNull Application application) {
        super(application);
        userRepository = new UserRepositoryImpl();
        authRepository = new AuthRepositoryImpl();

        // 监听密码变化，更新确认按钮状态
        oldPassword.observeForever(value -> validateForm());
        newPassword.observeForever(value -> validateForm());
        confirmPassword.observeForever(value -> validateForm());
    }
    
    // Getters
    public ObservableField<String> getOldPassword() {
        return oldPassword;
    }
    
    public ObservableField<String> getNewPassword() {
        return newPassword;
    }
    
    public ObservableField<String> getConfirmPassword() {
        return confirmPassword;
    }
    
    public ObservableField<Boolean> getConfirmEnabled() {
        return confirmEnabled;
    }
    
    public MutableLiveData<Boolean> getChangeSuccess() {
        return changeSuccess;
    }
    
    // 业务方法
    public void changePassword() {
        String oldPwd = oldPassword.get();
        String newPwd = newPassword.get();
        String confirmPwd = confirmPassword.get();
        
        // 验证输入
        if (!validatePassword(oldPwd, newPwd, confirmPwd)) {
            return;
        }
        
        setLoading(true);

        UpdatePasswordReqDto updatePasswordReq = new UpdatePasswordReqDto();
        updatePasswordReq.setOldPassword(AesUtil.encrypt(oldPwd, Constants.KEY_ALIAS));
        updatePasswordReq.setPassword(AesUtil.encrypt(newPwd, Constants.KEY_ALIAS));

        userRepository.updatePassword(updatePasswordReq, new UserRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                setLoading(false);
                authRepository.logout();
                setSuccess("密码修改成功，请重新登录");
                changeSuccess.postValue(true);
            }
            
            @Override
            public void onError(String error) {
                setError("修改失败: " + error);
            }
        });
    }
    
    // 私有方法
    private void validateForm() {
        String oldPwd = oldPassword.get();
        String newPwd = newPassword.get();
        String confirmPwd = confirmPassword.get();
        
        boolean isValid = oldPwd != null && !oldPwd.isEmpty() &&
                newPwd != null && !newPwd.isEmpty() &&
                confirmPwd != null && !confirmPwd.isEmpty();
        
        confirmEnabled.set(isValid);
    }
    
    private boolean validatePassword(String oldPwd, String newPwd, String confirmPwd) {
        if (oldPwd == null || oldPwd.isEmpty()) {
            setError("请输入原密码");
            return false;
        }
        
        if (newPwd == null || newPwd.length() < 8 || newPwd.length() > 20) {
            setError("新密码长度必须在8-20个字符之间");
            return false;
        }
        
        // 检查大小写字母和特殊字符
        boolean hasUpperCase = newPwd.matches(".*[A-Z].*");
        boolean hasLowerCase = newPwd.matches(".*[a-z].*");
        boolean hasSpecialChar = newPwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`].*");

        if (!hasUpperCase) {
            setError("新密码必须包含至少一个大写字母");
            return false;
        }

        if (!hasLowerCase) {
            setError("新密码必须包含至少一个小写字母");
            return false;
        }

        if (!hasSpecialChar) {
            setError("新密码必须包含至少一个特殊字符");
            return false;
        }
        
        if (!newPwd.equals(confirmPwd)) {
            setError("两次输入的密码不一致");
            return false;
        }
        
        if (oldPwd.equals(newPwd)) {
            setError("新密码不能与原密码相同");
            return false;
        }
        
        return true;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        oldPassword.removeObserver(value -> validateForm());
        newPassword.removeObserver(value -> validateForm());
        confirmPassword.removeObserver(value -> validateForm());
    }
}
package com.fxzs.lingxiagent.viewmodel.user;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.user.UserUtil;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.model.user.repository.UserRepository;
import com.fxzs.lingxiagent.model.user.repository.UserRepositoryImpl;
import com.fxzs.lingxiagent.model.user.dto.UserDto;


public class VMUserProfile extends BaseViewModel {
    // 双向绑定字段
    private final ObservableField<String> username = new ObservableField<>("");
    private final ObservableField<String> phone = new ObservableField<>("");
    private final ObservableField<String> point = new ObservableField<>("0");        // 积分
    private final ObservableField<String> experience = new ObservableField<>("0");    // 经验值
    private final ObservableField<String> level = new ObservableField<>("LV1");       // 会员等级
    private final ObservableField<String> mark = new ObservableField<>("");           // 个性签名
    private final MutableLiveData<String> avatarUrl = new MutableLiveData<>();
    
    // 业务状态
    private final MutableLiveData<Integer> navigationTarget = new MutableLiveData<>();
    
    // Repository
    private final UserRepository userRepository;
    
    // 导航目标常量
    public static final int NAV_SETTINGS = 1;
    public static final int NAV_ABOUT = 2;
    public static final int NAV_HELP = 3;
    
    public VMUserProfile(@NonNull Application application) {
        super(application);
        userRepository = new UserRepositoryImpl();
        // 加载用户信息
        loadUserProfile();
    }
    
    // Getters
    public ObservableField<String> getUsername() {
        return username;
    }
    
    public ObservableField<String> getPhone() {
        return phone;
    }
    
    public MutableLiveData<String> getAvatarUrl() {
        return avatarUrl;
    }
    
    public ObservableField<String> getPoint() {
        return point;
    }
    
    public ObservableField<String> getExperience() {
        return experience;
    }
    
    public ObservableField<String> getLevel() {
        return level;
    }
    
    public ObservableField<String> getMark() {
        return mark;
    }
    
    public MutableLiveData<Integer> getNavigationTarget() {
        return navigationTarget;
    }
    
    // 业务方法
    public void navigateToSettings() {
        navigationTarget.setValue(NAV_SETTINGS);
    }
    
    public void navigateToAbout() {
        navigationTarget.setValue(NAV_ABOUT);
    }
    
    public void navigateToHelp() {
        navigationTarget.setValue(NAV_HELP);
    }
    
    public void clearNavigationTarget() {
        navigationTarget.setValue(null);
    }
    
    public void loadUserProfile() {
        setLoading(true);
        
        userRepository.getUserProfile(new UserRepository.Callback<UserDto>() {
            @Override
            public void onSuccess(UserDto user) {
                username.postValue(user.getNickname() != null ? user.getNickname() : "用户" + user.getId());
                phone.postValue(UserUtil.formatPhone(user.getMobile()));
                
                // 更新新字段
                point.postValue(user.getPoint() != null ? user.getPoint().toString() : "0");
                experience.postValue(user.getExperience() != null ? user.getExperience().toString() : "0");
                level.postValue(user.getLevel() != null ? "LV" + user.getLevel() : "LV1");
                mark.postValue(user.getMark() != null ? user.getMark() : "这个人很懒，什么都没留下");

                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    avatarUrl.postValue(user.getAvatar());
                }

                setLoading(false);
            }
            
            @Override
            public void onError(String error) {
                setError("获取用户信息失败: " + error);
            }
        });
    }
}
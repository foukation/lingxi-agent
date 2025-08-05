package com.fxzs.lingxiagent.viewmodel.user;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.user.dto.AppVersionResponse;
import com.fxzs.lingxiagent.model.user.repository.AppVersionRepository;
import com.fxzs.lingxiagent.model.user.repository.AppVersionRepositoryImpl;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VMAboutApp extends BaseViewModel {
    
    // Repository
    private final AppVersionRepository versionRepository;
    
    // 双向绑定字段
    private final ObservableField<String> versionText = new ObservableField<>("");
    
    // 业务状态
    private final MutableLiveData<Boolean> versionEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> learnMoreEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> versionUpdateEvent = new MutableLiveData<>();
    private final MutableLiveData<AppVersionResponse> versionInfo = new MutableLiveData<>();
    
    public VMAboutApp(@NonNull Application application) {
        super(application);
        this.versionRepository = new AppVersionRepositoryImpl();
    }
    
    // Getters
    public ObservableField<String> getVersionText() {
        return versionText;
    }
    
    public MutableLiveData<Boolean> getVersionEvent() {
        return versionEvent;
    }
    
    public MutableLiveData<Boolean> getLearnMoreEvent() {
        return learnMoreEvent;
    }
    
    public MutableLiveData<Boolean> getVersionUpdateEvent() {
        return versionUpdateEvent;
    }
    
    public MutableLiveData<AppVersionResponse> getVersionInfo() {
        return versionInfo;
    }
    
    // 业务方法
    public void setVersionDisplay(String version) {
        versionText.set(version);
    }
    
    public void onVersionClicked() {
        versionEvent.setValue(true);
    }
    
    public void onLearnMoreClicked() {
        learnMoreEvent.setValue(true);
    }
    
    public void onVersionUpdateClicked() {
        versionUpdateEvent.setValue(true);
    }
    
    // 获取版本信息
    public void fetchVersionInfo() {
        setLoading(true);
        
        versionRepository.getLatestVersion().enqueue(new Callback<BaseResponse<AppVersionResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<AppVersionResponse>> call, 
                                 Response<BaseResponse<AppVersionResponse>> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<AppVersionResponse> baseResponse = response.body();
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        versionInfo.postValue(baseResponse.getData());
//                        setSuccess("获取版本信息成功");
                    } else {
                        setError(baseResponse.getMsg() != null ? baseResponse.getMsg() : "获取版本信息失败");
                    }
                } else {
                    setError("网络请求失败");
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<AppVersionResponse>> call, Throwable t) {
                setLoading(false);
                setError("网络异常: " + t.getMessage());
            }
        });
    }
    
    // 检查版本更新
    public void checkVersionUpdate(String currentVersion) {
        setLoading(true);
        
        versionRepository.checkUpdate(currentVersion).enqueue(new Callback<BaseResponse<AppVersionResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<AppVersionResponse>> call, 
                                 Response<BaseResponse<AppVersionResponse>> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<AppVersionResponse> baseResponse = response.body();
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        AppVersionResponse versionData = baseResponse.getData();
                        versionInfo.postValue(versionData);
                        
                        // 如果需要更新，不再触发更新事件，避免无限循环
                        if (versionData.getNeedUpdate() != null && versionData.getNeedUpdate()) {
                            // 不再设置 versionUpdateEvent，而是直接通过 versionInfo 触发更新对话框
                        } else {
                            setSuccess("已是最新版本");
                        }
                    } else {
                        setError(baseResponse.getMsg() != null ? baseResponse.getMsg() : "检查更新失败");
                    }
                } else {
                    setError("网络请求失败");
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<AppVersionResponse>> call, Throwable t) {
                setLoading(false);
                setError("网络异常: " + t.getMessage());
            }
        });
    }
}
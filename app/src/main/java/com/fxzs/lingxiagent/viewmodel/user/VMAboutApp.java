package com.fxzs.lingxiagent.viewmodel.user;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.user.UserUtil;
import com.fxzs.lingxiagent.model.user.dto.AppVersionResponse;
import com.fxzs.lingxiagent.model.user.repository.UserRepository;
import com.fxzs.lingxiagent.model.user.repository.UserRepositoryImpl;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VMAboutApp extends BaseViewModel {
    private static final String TAG = "VMAboutApp";
    // Repository
    private final UserRepository userRepository;

    // 双向绑定字段
    private final ObservableField<String> versionText = new ObservableField<>("");
    
    // 业务状态
    private final MutableLiveData<AppVersionResponse> versionInfo = new MutableLiveData<>();
    
    public VMAboutApp(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepositoryImpl();
    }
    
    // Getters
    public ObservableField<String> getVersionText() {
        return versionText;
    }
    
    public MutableLiveData<AppVersionResponse> getVersionInfo() {
        return versionInfo;
    }
    
    // 业务方法
    public void setVersionDisplay(String version) {
        versionText.set(version);
    }

    // 获取版本信息
    public void fetchAppUpgradeInfo(Context context) {
        setLoading(true);
        // 请求body参数
        Map<String, String> params = new HashMap<>();
        params.put("brand", Build.BRAND);
        params.put("model", Build.MODEL);
        params.put("os", "android");
        params.put("osVersion", Build.VERSION.RELEASE);
        params.put("androidId", UserUtil.getAndroidId(context));
        params.put("versionCode", String.valueOf(UserUtil.getAppVersionCode(context)));
        params.put("versionName", UserUtil.getAppVersionName(context));
        params.put("packageName", context.getPackageName());

        userRepository.checkAppUpgrade(params).enqueue(new Callback<BaseResponse<AppVersionResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<AppVersionResponse>> call, Response<BaseResponse<AppVersionResponse>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<AppVersionResponse> baseResponse = response.body();
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        Log.i(TAG, "获取版本信息成功");
                        versionInfo.postValue(baseResponse.getData());
                    } else {
                        Log.e(TAG, baseResponse.getMsg() != null ? baseResponse.getMsg() : "获取版本信息失败");
                    }
                } else {
                    Log.e(TAG, "网络请求失败");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<AppVersionResponse>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "网络异常: " + t.getMessage());
            }
        });
    }
}
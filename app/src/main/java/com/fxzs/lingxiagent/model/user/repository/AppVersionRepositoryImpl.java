package com.fxzs.lingxiagent.model.user.repository;

import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.network.RetrofitClient;
import com.fxzs.lingxiagent.model.user.api.AppVersionApiService;
import com.fxzs.lingxiagent.model.user.dto.AppVersionResponse;

import retrofit2.Call;

public class AppVersionRepositoryImpl implements AppVersionRepository {
    
    private final AppVersionApiService apiService;
    
    // 固定的token值
    private static final String VERSION_TOKEN = "b2ad5b7483cb4386b0dd3cfc711c01e4";
    
    public AppVersionRepositoryImpl() {
        this.apiService = RetrofitClient.getInstance().createService(AppVersionApiService.class);
    }
    
    @Override
    public Call<BaseResponse<AppVersionResponse>> getLatestVersion() {
        return apiService.getLatestVersion(VERSION_TOKEN);
    }
    
    @Override
    public Call<BaseResponse<AppVersionResponse>> checkUpdate(String currentVersion) {
        return apiService.checkUpdate(currentVersion, VERSION_TOKEN);
    }
}
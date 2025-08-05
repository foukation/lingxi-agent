package com.fxzs.lingxiagent.model.user.repository;

import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.user.dto.AppVersionResponse;

import retrofit2.Call;

public interface AppVersionRepository {
    /**
     * 获取最新版本信息
     * @return 版本信息响应
     */
    Call<BaseResponse<AppVersionResponse>> getLatestVersion();
    
    /**
     * 检查版本更新
     * @param currentVersion 当前版本号
     * @return 版本信息响应
     */
    Call<BaseResponse<AppVersionResponse>> checkUpdate(String currentVersion);
}
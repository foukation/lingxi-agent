package com.fxzs.lingxiagent.model.user.api;

import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.user.dto.AppVersionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface AppVersionApiService {
    
    /**
     * 获取最新版本信息
     * @param token 访问令牌
     * @return 版本信息响应
     */
    @GET("app-api/app-api/ai/apk-version/get-latest")
    Call<BaseResponse<AppVersionResponse>> getLatestVersion(
        @Header("token") String token
    );
    
    /**
     * 检查版本更新
     * @param currentVersion 当前版本号
     * @param token 访问令牌
     * @return 版本信息响应
     */
    @GET("app-api/app-api/ai/apk-version/check-update")
    Call<BaseResponse<AppVersionResponse>> checkUpdate(
        @Query("currentVersion") String currentVersion,
        @Header("token") String token
    );
}
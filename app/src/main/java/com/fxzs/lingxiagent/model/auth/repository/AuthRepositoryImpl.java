package com.fxzs.lingxiagent.model.auth.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.auth.api.AuthApiService;
import com.fxzs.lingxiagent.model.auth.dto.LoginRequest;
import com.fxzs.lingxiagent.model.auth.dto.LoginResponse;
import com.fxzs.lingxiagent.model.auth.dto.OneClickLoginRequest;
import com.fxzs.lingxiagent.model.auth.dto.RegisterRequest;
import com.fxzs.lingxiagent.model.auth.dto.SendSmsRequest;
import com.fxzs.lingxiagent.model.auth.dto.SmsLoginRequest;
import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.network.RetrofitClient;
import com.fxzs.lingxiagent.util.AesUtil;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.model.user.dto.ResetPasswordReqDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 认证仓库实现
 */
public class AuthRepositoryImpl implements AuthRepository {
    
    private static final String TAG = "AuthRepository";
    private final AuthApiService authApiService;
    
    public AuthRepositoryImpl() {
        this.authApiService = RetrofitClient.getInstance().getAuthApiService();
    }
    
    @Override
    public LiveData<LoginResponse> loginByPassword(String mobile, String password) {
        MutableLiveData<LoginResponse> result = new MutableLiveData<>();

        String phoneNum = AesUtil.encrypt(mobile, Constants.KEY_ALIAS);
        String pwd = AesUtil.encrypt(password, Constants.KEY_ALIAS);
        LoginRequest request = LoginRequest.passwordLogin(phoneNum, pwd);
        authApiService.loginByPassword(request).enqueue(new Callback<BaseResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<LoginResponse>> call, 
                                 Response<BaseResponse<LoginResponse>> response) {
                LoginResponse loginResponse = null;
                BaseResponse<LoginResponse> baseResponse = response.body();
                if (null != baseResponse) {
                    loginResponse = baseResponse.getData();
                }
                if (response.isSuccessful() && null != baseResponse) {
                    if (baseResponse.isSuccess() && null != loginResponse) {
                        // 保存登录信息
                        SharedPreferencesUtil.saveLoginInfo(loginResponse);
                        result.postValue(loginResponse);
                    } else {
                        Log.e(TAG, "Login failed: " + baseResponse);
                        result.postValue(loginResponse);
                    }
                } else {
                    result.postValue(loginResponse);
                    Log.e(TAG, "Login request failed: " + response);
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<LoginResponse>> call, Throwable t) {
                result.postValue(null);
                Log.e(TAG, "Login error", t);
            }
        });
        
        return result;
    }
    
    @Override
    public LiveData<LoginResponse> loginBySms(String mobile, String code) {
        return loginBySms(mobile, code, null);
    }
    
    @Override
    public LiveData<LoginResponse> loginBySms(String mobile, String code, String password) {
        MutableLiveData<LoginResponse> result = new MutableLiveData<>();
        
        Log.d(TAG, "=== 开始验证码登录 ===");
        Log.d(TAG, "手机号: " + mobile);
        Log.d(TAG, "验证码: " + code);
        Log.d(TAG, "密码: " + (password != null ? "已提供" : "未提供"));
        
        SmsLoginRequest request = new SmsLoginRequest(mobile, code, password);
        authApiService.loginBySms(request).enqueue(new Callback<BaseResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<LoginResponse>> call, 
                                 Response<BaseResponse<LoginResponse>> response) {
                Log.d(TAG, "=== 验证码登录响应 ===");
                Log.d(TAG, "响应码: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<LoginResponse> baseResponse = response.body();
                    Log.d(TAG, "响应成功状态: " + baseResponse.isSuccess());
                    Log.d(TAG, "响应消息: " + baseResponse.getMessage());
                    Log.d(TAG, "响应码: " + baseResponse.getCode());

                    LoginResponse loginResponse = baseResponse.getData();
                    if (baseResponse.isSuccess() && null != loginResponse) {
                        // 打印获取到的Token信息
                        Log.d(TAG, "=== 登录成功，Token信息 ===");
                        Log.d(TAG, "AccessToken: " + loginResponse.getAccessToken());
                        Log.d(TAG, "RefreshToken: " + loginResponse.getRefreshToken());
                        Log.d(TAG, "ExpiresTime: " + loginResponse.getExpiresTime());
                        Log.d(TAG, "UserId: " + loginResponse.getUserId());
                        if (loginResponse.getUser() != null) {
                            Log.d(TAG, "用户昵称: " + loginResponse.getUser().getNickname());
                            Log.d(TAG, "用户手机: " + loginResponse.getUser().getMobile());
                        }
                        
                        // 保存登录信息
                        SharedPreferencesUtil.saveLoginInfo(loginResponse);
                        Log.d(TAG, "Token已保存到SharedPreferences");
                        
                        // 验证保存是否成功
                        String savedToken = SharedPreferencesUtil.getAccessToken();
                        Log.d(TAG, "从SharedPreferences读取的Token: " + savedToken);
                        
                        result.postValue(loginResponse);
                    } else {
                        loginResponse.setMessage(baseResponse.getMessage());
                        result.postValue(loginResponse);
                        Log.e(TAG, "SMS login failed: " + baseResponse.getMessage());
                    }
                } else {
                    result.postValue(null);
                    Log.e(TAG, "SMS login request failed: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "错误响应: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "读取错误响应失败", e);
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<LoginResponse>> call, Throwable t) {
                result.postValue(null);
                Log.e(TAG, "SMS login error", t);
                Log.e(TAG, "错误详情: " + t.getMessage());
            }
        });
        
        return result;
    }
    
    @Override
    public LiveData<BaseResponse<Boolean>> sendSmsCode(String mobile, int scene) {
        MutableLiveData<BaseResponse<Boolean>> result = new MutableLiveData<>();
        
        Log.d(TAG, "=== 发送验证码请求 ===");
        Log.d(TAG, "手机号: " + mobile);
        Log.d(TAG, "场景: " + scene + " (1=登录, 4=重置密码)");
        
        SendSmsRequest request = new SendSmsRequest(mobile, scene);
        authApiService.sendSmsCode(request).enqueue(new Callback<BaseResponse<Boolean>>() {
            @Override
            public void onResponse(Call<BaseResponse<Boolean>> call, 
                                 Response<BaseResponse<Boolean>> response) {
                Log.d(TAG, "=== 发送验证码响应 ===");
                Log.d(TAG, "响应码: " + response.code());
                BaseResponse<Boolean> baseResponse = response.body();
                Log.d(TAG, "响应状态码: " + baseResponse.getCode());
                    Log.d(TAG, "响应成功状态: " + baseResponse.isSuccess());
                    Log.d(TAG, "响应消息: " + baseResponse.getMessage());
                    Log.d(TAG, "响应数据: " + baseResponse.getData());
                if (response.errorBody() != null) {
                    try {
                        Log.e(TAG, "错误响应: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "读取错误响应失败", e);
                    }
                }
                result.postValue(baseResponse);
            }
            
            @Override
            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
                result.postValue(null);
                Log.e(TAG, "Send SMS error", t);
                Log.e(TAG, "错误详情: " + t.getMessage());
            }
        });
        
        return result;
    }
    
    @Override
    public LiveData<LoginResponse> register(String mobile, String code, String password) {
        MutableLiveData<LoginResponse> result = new MutableLiveData<>();

        RegisterRequest request = new RegisterRequest(mobile, code, password);
        authApiService.register(request).enqueue(new Callback<BaseResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<LoginResponse>> call, 
                                 Response<BaseResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<LoginResponse> baseResponse = response.body();
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        LoginResponse loginResponse = baseResponse.getData();
                        // 保存登录信息
                        SharedPreferencesUtil.saveLoginInfo(loginResponse);
                        result.postValue(loginResponse);
                    } else {
                        result.postValue(null);
                        Log.e(TAG, "Register failed: " + baseResponse.getMessage());
                    }
                } else {
                    result.postValue(null);
                    Log.e(TAG, "Register request failed: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<LoginResponse>> call, Throwable t) {
                result.postValue(null);
                Log.e(TAG, "Register error", t);
            }
        });
        
        return result;
    }
    
    @Override
    public LiveData<LoginResponse> refreshToken(String refreshToken) {
        MutableLiveData<LoginResponse> result = new MutableLiveData<>();
        
        authApiService.refreshToken(refreshToken).enqueue(new Callback<BaseResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<LoginResponse>> call, 
                                 Response<BaseResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<LoginResponse> baseResponse = response.body();
                    if (baseResponse.isSuccess()) {
                        LoginResponse loginResponse = baseResponse.getData();
                        // 更新Token
                        SharedPreferencesUtil.updateToken(loginResponse.getAccessToken());
                        result.postValue(loginResponse);
                    } else {
                        result.postValue(null);
                        Log.e(TAG, "Refresh token failed: " + baseResponse.getMsg());
                    }
                } else {
                    result.postValue(null);
                    Log.e(TAG, "Refresh token request failed: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<LoginResponse>> call, Throwable t) {
                result.postValue(null);
                Log.e(TAG, "Refresh token error", t);
            }
        });
        
        return result;
    }
    
    @Override
    public LiveData<Boolean> logout() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        authApiService.logout().enqueue(new Callback<BaseResponse<Boolean>>() {
            @Override
            public void onResponse(Call<BaseResponse<Boolean>> call,
                                 Response<BaseResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = response.body().getData();
                    if (success) {
                        // 清除本地登录信息
                        SharedPreferencesUtil.clearLoginInfo();
                    }
                    result.postValue(success);
                } else {
                    result.postValue(false);
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
                // 即使请求失败也清除本地信息
                SharedPreferencesUtil.clearLoginInfo();
                result.postValue(false);
                Log.e(TAG, "Logout error", t);
            }
        });
        
        return result;
    }

    @Override
    public LiveData<BaseResponse<Boolean>> resetPassword(String mobile, String code, String password) {
        MutableLiveData<BaseResponse<Boolean>> result = new MutableLiveData<>();
        ResetPasswordReqDto req = new ResetPasswordReqDto();
        req.setMobile(AesUtil.encrypt(mobile, Constants.KEY_ALIAS));
        req.setCode(AesUtil.encrypt(code, Constants.KEY_ALIAS));
        req.setPassword(AesUtil.encrypt(password, Constants.KEY_ALIAS));
        authApiService.resetPassword(req).enqueue(new Callback<BaseResponse<Boolean>>() {
            @Override
            public void onResponse(Call<BaseResponse<Boolean>> call, Response<BaseResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(response.body());
                } else {
                    BaseResponse<Boolean> error = new BaseResponse<>();
                    error.setCode(-1);
                    error.setMessage("网络异常，请重试");
                    result.postValue(error);
                }
            }
            @Override
            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
                BaseResponse<Boolean> error = new BaseResponse<>();
                error.setCode(-1);
                error.setMessage("网络异常，请重试");
                result.postValue(error);
            }
        });
        return result;
    }
    
    @Override
    public LiveData<LoginResponse> oneClickLogin(String loginToken) {
        MutableLiveData<LoginResponse> result = new MutableLiveData<>();
        
        Log.d(TAG, "=== 开始一键登录 ===");
        Log.d(TAG, "LoginToken: " + loginToken);
        
        OneClickLoginRequest request = new OneClickLoginRequest(loginToken);
        authApiService.oneClickLogin(request).enqueue(new Callback<BaseResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<LoginResponse>> call, 
                                 Response<BaseResponse<LoginResponse>> response) {
                Log.d(TAG, "=== 一键登录响应 ===");
                Log.d(TAG, "响应码: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<LoginResponse> baseResponse = response.body();
                    Log.d(TAG, "响应成功状态: " + baseResponse.isSuccess());
                    Log.d(TAG, "响应消息: " + baseResponse.getMessage());
                    
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        LoginResponse loginResponse = baseResponse.getData();
                        
                        // 打印获取到的Token信息
                        Log.d(TAG, "=== 一键登录成功 ===");
                        Log.d(TAG, "AccessToken: " + loginResponse.getAccessToken());
                        Log.d(TAG, "RefreshToken: " + loginResponse.getRefreshToken());
                        Log.d(TAG, "ExpiresTime: " + loginResponse.getExpiresTime());
                        Log.d(TAG, "UserId: " + loginResponse.getUserId());
                        
                        // 保存登录信息
                        SharedPreferencesUtil.saveLoginInfo(loginResponse);
                        result.postValue(loginResponse);
                    } else {
                        LoginResponse errorResponse = new LoginResponse();
                        errorResponse.setMessage(baseResponse.getMessage());
                        result.postValue(errorResponse);
                        Log.e(TAG, "One click login failed: " + baseResponse.getMessage());
                    }
                } else {
                    LoginResponse errorResponse = new LoginResponse();
                    errorResponse.setMessage("一键登录失败");
                    result.postValue(errorResponse);
                    Log.e(TAG, "One click login request failed: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<LoginResponse>> call, Throwable t) {
                LoginResponse errorResponse = new LoginResponse();
                errorResponse.setMessage("网络错误");
                result.postValue(errorResponse);
                Log.e(TAG, "One click login error", t);
            }
        });
        
        return result;
    }
}
package com.fxzs.lingxiagent.model.user.repository;

import android.text.TextUtils;
import android.util.Log;

import com.fxzs.lingxiagent.model.auth.api.AuthApiService;
import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.user.api.UserApiService;
import com.fxzs.lingxiagent.model.user.dto.FeedbackDto;
import com.fxzs.lingxiagent.model.user.dto.FeedbackReqDto;
import com.fxzs.lingxiagent.model.user.dto.UpdateMobileReqDto;
import com.fxzs.lingxiagent.model.user.dto.UpdatePasswordReqDto;
import com.fxzs.lingxiagent.model.user.dto.UserDto;
import com.fxzs.lingxiagent.model.user.dto.UserUpdateReqDto;
import com.fxzs.lingxiagent.model.network.RetrofitClient;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class UserRepositoryImpl implements UserRepository {
    private static final String TAG = "UserRepository";
    private final UserApiService userApiService;
    private final AuthApiService authApiService;

    public UserRepositoryImpl() {
        userApiService = RetrofitClient.getInstance().createService(UserApiService.class);
        authApiService = RetrofitClient.getInstance().createService(AuthApiService.class);
    }
    
    @Override
    public void getUserProfile(Callback<UserDto> callback) {
        userApiService.getUserProfile().enqueue(new retrofit2.Callback<BaseResponse<UserDto>>() {
            @Override
            public void onResponse(Call<BaseResponse<UserDto>> call, Response<BaseResponse<UserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<UserDto> baseResponse = response.body();
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        UserDto userDto = baseResponse.getData();
                        // 保存登录信息
                        SharedPreferencesUtil.saveUserInfo(userDto);
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg());
                    }
                } else {
                    callback.onError("获取用户信息失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<UserDto>> call, Throwable t) {
                Log.e(TAG, "getUserProfile failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void updateUserProfile(UserUpdateReqDto userUpdate, Callback<Boolean> callback) {
        Map<String, Object> params = new HashMap<>();
        if (userUpdate.getNickname() != null) {
            params.put("nickname", userUpdate.getNickname());
        }
        if (userUpdate.getAvatar() != null) {
            params.put("avatar", userUpdate.getAvatar());
        }
        if (userUpdate.getSex() != null) {
            params.put("sex", userUpdate.getSex());
        }
        if (userUpdate.getMark() != null) {
            params.put("mark", userUpdate.getMark());
        }
        
        userApiService.updateUserProfile(params).enqueue(new retrofit2.Callback<BaseResponse<Boolean>>() {
            @Override
            public void onResponse(Call<BaseResponse<Boolean>> call, Response<BaseResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<Boolean> baseResponse = response.body();
                    if (baseResponse.isSuccess()) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError(baseResponse.getMsg());
                    }
                } else {
                    callback.onError("更新失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "updateUserProfile failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    // 上传头像图片
    @Override
    public void uploadAvatar(String imagePath, Callback<String> callback) {
        File file = new File(imagePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatarFile", file.getName(), requestFile);

        userApiService.updateAvatar(body).enqueue(new retrofit2.Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<String> baseResponse = response.body();
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg());
                    }
                } else {
                    callback.onError("更新头像失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                Log.e(TAG, "uploadAvatar failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void submitFeedback(FeedbackReqDto feedbackReq, Callback<Boolean> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", feedbackReq.getType());
        // 添加标题字段
        params.put("title", feedbackReq.getTitle() != null ? feedbackReq.getTitle() : "用户反馈");
        params.put("content", feedbackReq.getContent());
        String contact = feedbackReq.getContact();
        if (!TextUtils.isEmpty(contact)) {
            params.put("contact", contact);
        }
        String imageUrls = feedbackReq.getImageUrls();
        if (!TextUtils.isEmpty(imageUrls)) {
            params.put("images", imageUrls);
        }
        String logUrl = feedbackReq.getLogUrl();;
        if (!TextUtils.isEmpty(logUrl)) {
            params.put("logUrl", logUrl);
        }
        params.put("appVersion", feedbackReq.getAppVersion());
        params.put("os", feedbackReq.getOs());
        params.put("osVersion", feedbackReq.getOsVersion());
        params.put("brand", feedbackReq.getBrand());
        params.put("model", feedbackReq.getModel());

        userApiService.submitFeedback(params).enqueue(new retrofit2.Callback<BaseResponse<Long>>() {
            @Override
            public void onResponse(Call<BaseResponse<Long>> call, Response<BaseResponse<Long>> response) {
                Log.d(TAG, "submitFeedback response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<Long> baseResponse = response.body();
                    Log.d(TAG, "submitFeedback response success: " + baseResponse.isSuccess() + ", msg: " + baseResponse.getMsg() + ", data: " + baseResponse.getData());
                    if (baseResponse.isSuccess()) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError(baseResponse.getMsg());
                    }
                } else {
                    String errorMsg = "提交失败: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Log.e(TAG, "submitFeedback error: " + errorMsg);
                    callback.onError(errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<Long>> call, Throwable t) {
                Log.e(TAG, "submitFeedback failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void getFeedbackHistory(int page, int size, Callback<List<FeedbackDto>> callback) {
        userApiService.getFeedbackList(page, size).enqueue(new retrofit2.Callback<BaseResponse<List<FeedbackDto>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<FeedbackDto>>> call, Response<BaseResponse<List<FeedbackDto>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<List<FeedbackDto>> baseResponse = response.body();
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg());
                    }
                } else {
                    callback.onError("获取反馈历史失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<List<FeedbackDto>>> call, Throwable t) {
                Log.e(TAG, "getFeedbackHistory failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    @Override
    public void getFeedbackDetail(Long id, Callback<FeedbackDto> callback) {
        userApiService.getFeedbackDetail(id).enqueue(new retrofit2.Callback<BaseResponse<FeedbackDto>>() {
            @Override
            public void onResponse(Call<BaseResponse<FeedbackDto>> call, Response<BaseResponse<FeedbackDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<FeedbackDto> baseResponse = response.body();
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg());
                    }
                } else {
                    callback.onError("获取反馈详情失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<FeedbackDto>> call, Throwable t) {
                Log.e(TAG, "getFeedbackDetail failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    @Override
    public void updatePassword(UpdatePasswordReqDto updatePasswordReq, Callback<Boolean> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("oldPassword", updatePasswordReq.getOldPassword());
        params.put("password", updatePasswordReq.getPassword());
        params.put("code", updatePasswordReq.getCode());
        
        authApiService.updatePassword(params).enqueue(new retrofit2.Callback<BaseResponse<Boolean>>() {
            @Override
            public void onResponse(Call<BaseResponse<Boolean>> call, Response<BaseResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<Boolean> baseResponse = response.body();
                    if (baseResponse.isSuccess()) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError(baseResponse.getMsg());
                    }
                } else {
                    callback.onError("修改密码失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "updatePassword failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void updateMobile(UpdateMobileReqDto updateMobileReq, Callback<Boolean> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("oldMobile", updateMobileReq.getOldMobile());
        params.put("mobile", updateMobileReq.getMobile());
        params.put("code", updateMobileReq.getCode());
        
        userApiService.updateMobile(params).enqueue(new retrofit2.Callback<BaseResponse<Boolean>>() {
            @Override
            public void onResponse(Call<BaseResponse<Boolean>> call, Response<BaseResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<Boolean> baseResponse = response.body();
                    if (baseResponse.isSuccess()) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError(baseResponse.getMsg());
                    }
                } else {
                    callback.onError("修改手机号失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "updateMobile failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
    
//    @Override
//    public void resetPassword(ResetPasswordReqDto resetPasswordReq, Callback<Boolean> callback) {
//        Map<String, Object> params = new HashMap<>();
//        params.put("mobile", resetPasswordReq.getMobile());
//        params.put("code", resetPasswordReq.getCode());
//        params.put("password", resetPasswordReq.getPassword());
//
//        userApiService.resetPassword(params).enqueue(new retrofit2.Callback<BaseResponse<Boolean>>() {
//            @Override
//            public void onResponse(Call<BaseResponse<Boolean>> call, Response<BaseResponse<Boolean>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    BaseResponse<Boolean> baseResponse = response.body();
//                    if (baseResponse.isSuccess()) {
//                        callback.onSuccess(true);
//                    } else {
//                        callback.onError(baseResponse.getMsg());
//                    }
//                } else {
//                    callback.onError("重置密码失败: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
//                Log.e(TAG, "resetPassword failed", t);
//                callback.onError("网络错误: " + t.getMessage());
//            }
//        });
//    }
    
//    @Override
//    public void logout(Callback<Boolean> callback) {
//        userApiService.logout().enqueue(new retrofit2.Callback<BaseResponse<Boolean>>() {
//            @Override
//            public void onResponse(Call<BaseResponse<Boolean>> call, Response<BaseResponse<Boolean>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    BaseResponse<Boolean> baseResponse = response.body();
//                    if (baseResponse.isSuccess()) {
//                        callback.onSuccess(true);
//                    } else {
//                        callback.onError(baseResponse.getMsg());
//                    }
//                } else {
//                    callback.onError("退出登录失败: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
//                Log.e(TAG, "logout failed", t);
//                callback.onError("网络错误: " + t.getMessage());
//            }
//        });
//    }
    
//    @Override
//    public void sendSmsCode(String mobile, int scene, Callback<Boolean> callback) {
//        SendSmsRequest request = new SendSmsRequest(mobile, scene);
//        authApiService.sendSmsCode(request).enqueue(new retrofit2.Callback<BaseResponse<Boolean>>() {
//            @Override
//            public void onResponse(Call<BaseResponse<Boolean>> call, Response<BaseResponse<Boolean>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    BaseResponse<Boolean> baseResponse = response.body();
//                    if (baseResponse.isSuccess()) {
//                        callback.onSuccess(true);
//                    } else {
//                        callback.onError(baseResponse.getMsg());
//                    }
//                } else {
//                    callback.onError("发送验证码失败: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
//                Log.e(TAG, "sendSmsCode failed", t);
//                callback.onError("网络错误: " + t.getMessage());
//            }
//        });
//    }
    
    @Override
    public void uploadFile(MultipartBody.Part file, Callback<String> callback) {
        userApiService.uploadFile(file).enqueue(new retrofit2.Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<String> baseResponse = response.body();
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg());
                    }
                } else {
                    callback.onError("上传失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                Log.e(TAG, "uploadFile failed", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
}
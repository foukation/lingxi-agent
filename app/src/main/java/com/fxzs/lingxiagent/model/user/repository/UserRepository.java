package com.fxzs.lingxiagent.model.user.repository;

import com.fxzs.lingxiagent.model.user.dto.FeedbackDto;
import com.fxzs.lingxiagent.model.user.dto.FeedbackReqDto;
import com.fxzs.lingxiagent.model.user.dto.ResetPasswordReqDto;
import com.fxzs.lingxiagent.model.user.dto.UpdateMobileReqDto;
import com.fxzs.lingxiagent.model.user.dto.UpdatePasswordReqDto;
import com.fxzs.lingxiagent.model.user.dto.UserDto;
import com.fxzs.lingxiagent.model.user.dto.UserUpdateReqDto;

import java.util.List;

import okhttp3.MultipartBody;

public interface UserRepository {
    
    interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    // 获取用户信息
    void getUserProfile(Callback<UserDto> callback);
    
    // 更新用户信息
    void updateUserProfile(UserUpdateReqDto userUpdate, Callback<Boolean> callback);
    
    // 修改密码
    void updatePassword(UpdatePasswordReqDto updatePasswordReq, Callback<Boolean> callback);
    
    // 修改手机号
    void updateMobile(UpdateMobileReqDto updateMobileReq, Callback<Boolean> callback);
    
    // 重置密码
//    void resetPassword(ResetPasswordReqDto resetPasswordReq, Callback<Boolean> callback);
    
    // 退出登录
//    void logout(Callback<Boolean> callback);
    
    // 发送验证码
//    void sendSmsCode(String mobile, int scene, Callback<Boolean> callback);
    
    // 上传头像
    void uploadAvatar(String imagePath, Callback<String> callback);
    
    // 上传文件
    void uploadFile(MultipartBody.Part file, Callback<String> callback);
    
    // 提交反馈
    void submitFeedback(FeedbackReqDto feedbackReq, Callback<Boolean> callback);
    
    // 获取反馈历史
    void getFeedbackHistory(int page, int size, Callback<List<FeedbackDto>> callback);

    // 获取反馈详情
    void getFeedbackDetail(Long id, Callback<FeedbackDto> callback);

    // 注销账号
//    void deleteAccount(Callback<Boolean> callback);
}
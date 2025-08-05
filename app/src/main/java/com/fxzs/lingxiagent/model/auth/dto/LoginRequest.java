package com.fxzs.lingxiagent.model.auth.dto;

import com.google.gson.annotations.SerializedName;

/**
 * 登录请求参数
 */
public class LoginRequest {
    
    @SerializedName("mobile")
    private String mobile;
    
    @SerializedName("password")
    private String password;
    
    
    @SerializedName("socialType")
    private Integer socialType;
    
    @SerializedName("socialCode")
    private String socialCode;
    
    @SerializedName("socialState")
    private String socialState;
    
    // 密码登录构造器
    public static LoginRequest passwordLogin(String mobile, String password) {
        LoginRequest request = new LoginRequest();
        request.mobile = mobile;
        request.password = password;
        return request;
    }
    
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    
    public Integer getSocialType() {
        return socialType;
    }
    
    public void setSocialType(Integer socialType) {
        this.socialType = socialType;
    }
    
    public String getSocialCode() {
        return socialCode;
    }
    
    public void setSocialCode(String socialCode) {
        this.socialCode = socialCode;
    }
    
    public String getSocialState() {
        return socialState;
    }
    
    public void setSocialState(String socialState) {
        this.socialState = socialState;
    }
}
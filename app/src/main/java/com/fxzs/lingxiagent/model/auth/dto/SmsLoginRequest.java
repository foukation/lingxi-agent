package com.fxzs.lingxiagent.model.auth.dto;

import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.util.AesUtil;
import com.google.gson.annotations.SerializedName;

/**
 * 短信验证码登录请求参数
 */
public class SmsLoginRequest {
    
    @SerializedName("mobile")
    private String mobile;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("password")
    private String password;
    
    public SmsLoginRequest() {
    }
    
    public SmsLoginRequest(String mobile, String code) {
        this.mobile = mobile;
        this.code = code;
        this.password = null;
    }
    
    public SmsLoginRequest(String mobile, String code, String password) {
        this.mobile =  AesUtil.encrypt(mobile, Constants.KEY_ALIAS);
        this.code = AesUtil.encrypt(code, Constants.KEY_ALIAS);
        this.password = password;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
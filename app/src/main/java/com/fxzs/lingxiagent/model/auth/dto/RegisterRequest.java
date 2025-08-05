package com.fxzs.lingxiagent.model.auth.dto;

import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.util.AesUtil;
import com.google.gson.annotations.SerializedName;

/**
 * 注册请求参数
 */
public class RegisterRequest {
    
    @SerializedName("mobile")
    private String mobile;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("nickname")
    private String nickname;
    
    public RegisterRequest() {
    }
    
    public RegisterRequest(String mobile, String code, String password) {
        this.mobile = AesUtil.encrypt(mobile, Constants.KEY_ALIAS);
        this.code = AesUtil.encrypt(code, Constants.KEY_ALIAS);
        this.password = AesUtil.encrypt(password, Constants.KEY_ALIAS);
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
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
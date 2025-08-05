package com.fxzs.lingxiagent.model.user.dto;

public class ResetPasswordReqDto {
    private String mobile;
    private String password;
    private String code;
    
    // Getters and Setters
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
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
}
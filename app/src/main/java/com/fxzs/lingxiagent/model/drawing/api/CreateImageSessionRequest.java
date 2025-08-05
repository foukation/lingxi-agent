package com.fxzs.lingxiagent.model.drawing.api;

import java.io.Serializable;

/**
 * 创建图片会话请求
 */
public class CreateImageSessionRequest implements Serializable {
    
    private String name; // 会话名称（使用prompt作为名称）
    
    public CreateImageSessionRequest() {
    }
    
    public CreateImageSessionRequest(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
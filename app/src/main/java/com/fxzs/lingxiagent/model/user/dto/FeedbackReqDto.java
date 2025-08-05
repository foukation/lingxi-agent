package com.fxzs.lingxiagent.model.user.dto;

public class FeedbackReqDto {
    private String title;       // 反馈标题
    private String type;        // 反馈类型：bug、feature、other
    private String content;     // 反馈内容
    private String contact;     // 联系方式（可选）
    private String imageUrls;   // 图片链接，多个用逗号分隔（可选）
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public String getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }
}
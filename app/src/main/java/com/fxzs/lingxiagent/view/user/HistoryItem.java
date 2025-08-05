package com.fxzs.lingxiagent.view.user;

public class HistoryItem {
    public static final int TYPE_DATE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_LOADING = 2;
    
    private int type;
    private String title;
    private String subtitle;
    private int avatarResId;
    private String imageUrl;  // 添加网络图片URL支持
    private Long sessionId;   // 添加会话ID支持
    private Long conversationId; // 添加对话ID支持（智能体对话）
    private Integer modelType;   // 添加模型类型支持
    private Long modelId;   //模型id
    private String model;   //模型
    private String modelName;   //模型名字
    private Long meetingId;      // 添加会议ID支持
    private Integer meetingType; // 添加会议类型支持
    
    public HistoryItem(int type, String title, String subtitle, int avatarResId) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.avatarResId = avatarResId;
    }
    
    // 新增构造函数，支持网络图片URL
    public HistoryItem(int type, String title, String subtitle, String imageUrl) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public int getAvatarResId() {
        return avatarResId;
    }
    
    public void setAvatarResId(int avatarResId) {
        this.avatarResId = avatarResId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getModelType() {
        return modelType;
    }

    public void setModelType(Integer modelType) {
        this.modelType = modelType;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public Integer getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(Integer meetingType) {
        this.meetingType = meetingType;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
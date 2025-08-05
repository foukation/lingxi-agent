package com.fxzs.lingxiagent.model.drawing.dto;

/**
 * AI绘画对话消息DTO
 */
public class DrawingMessageDto {
    private String id;
    private boolean isUserMessage;
    private String text;
    private String imageUrl;
    private boolean isGenerating;
    private int progress;
    private long timestamp;
    
    // 构造函数
    public DrawingMessageDto() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // 创建用户消息
    public static DrawingMessageDto createUserMessage(String text) {
        DrawingMessageDto message = new DrawingMessageDto();
        message.setUserMessage(true);
        message.setText(text);
        return message;
    }
    
    // 创建AI消息
    public static DrawingMessageDto createAiMessage(String text) {
        DrawingMessageDto message = new DrawingMessageDto();
        message.setUserMessage(false);
        message.setText(text);
        return message;
    }
    
    // 创建生成中的消息
    public static DrawingMessageDto createGeneratingMessage() {
        DrawingMessageDto message = new DrawingMessageDto();
        message.setUserMessage(false);
        message.setGenerating(true);
        message.setProgress(0);
        return message;
    }
    
    // 创建图片消息
    public static DrawingMessageDto createImageMessage(String imageUrl) {
        DrawingMessageDto message = new DrawingMessageDto();
        message.setUserMessage(false);
        message.setImageUrl(imageUrl);
        message.setGenerating(false);
        return message;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public boolean isUserMessage() {
        return isUserMessage;
    }
    
    public void setUserMessage(boolean userMessage) {
        isUserMessage = userMessage;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public boolean isGenerating() {
        return isGenerating;
    }
    
    public void setGenerating(boolean generating) {
        isGenerating = generating;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
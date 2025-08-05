package com.fxzs.lingxiagent.model.drawing.dto;

/**
 * 绘画画廊项目数据类
 */
public class DrawingGalleryItem {
    private String imageUrl;
    private String prompt;
    private String style;
    private String actionText;
    
    public DrawingGalleryItem() {
    }
    
    public DrawingGalleryItem(String imageUrl, String prompt, String style, String actionText) {
        this.imageUrl = imageUrl;
        this.prompt = prompt;
        this.style = style;
        this.actionText = actionText;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
    
    public String getActionText() {
        return actionText;
    }
    
    public void setActionText(String actionText) {
        this.actionText = actionText;
    }
}
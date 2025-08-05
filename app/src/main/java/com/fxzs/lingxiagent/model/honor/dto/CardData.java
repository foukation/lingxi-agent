package com.fxzs.lingxiagent.model.honor.dto;

public class CardData {
    private String templateId;
    private String serviceId;
    private String content;
    private String type;

    public CardData(String templateId, String serviceId, String content, String type) {
        this.templateId = templateId;
        this.serviceId = serviceId;
        this.content = content;
        this.type = type;
    }

    // Getters
    public String getTemplateId() { return templateId; }
    public String getServiceId() { return serviceId; }
    public String getContent() { return content; }
    public String getType() { return type; }

    // Setters
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public void setContent(String content) { this.content = content; }
    public void setType(String type) { this.type = type; }
}


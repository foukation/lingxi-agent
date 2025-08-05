package com.fxzs.lingxiagent.network.ZNet.bean;

import java.util.List;

public class ChatContentRequest {
    private String title;
    private List<ChatContent> contents;

    public ChatContentRequest(String title, List<ChatContent> contents) {
        this.title = title;
        this.contents = contents;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ChatContent> getContents() {
        return contents;
    }

    public void setContents(List<ChatContent> contents) {
        this.contents = contents;
    }
}
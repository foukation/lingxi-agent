package com.fxzs.lingxiagent.model.chat.callback;


import com.fxzs.lingxiagent.model.chat.dto.ChatMessage;

public interface MsgActionCallback {
    void refresh(String content);
    void msgClick();
    void continueDrawing(ChatMessage message);
    void regenerateDrawing(ChatMessage message);
    void downloadDrawing(ChatMessage message);
    void viewDrawing(ChatMessage message);
}

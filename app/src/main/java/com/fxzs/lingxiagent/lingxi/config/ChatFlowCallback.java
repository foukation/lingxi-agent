package com.fxzs.lingxiagent.lingxi.config;

import com.fxzs.lingxiagent.lingxi.lingxi_conversation.LocalModule;

public interface ChatFlowCallback {
    void receive(LocalModule curModel, Boolean isBreak, String content);
    void end();
}

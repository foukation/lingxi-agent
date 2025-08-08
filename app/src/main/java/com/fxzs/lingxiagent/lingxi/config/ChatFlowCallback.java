package com.fxzs.lingxiagent.lingxi.config;

import com.fxzs.lingxiagent.lingxi.lingxi_conversation.LocalModule;

import java.util.ArrayList;

public interface ChatFlowCallback {
    void receive(LocalModule curModel, Boolean isBreak, String content);
    void receive(LocalModule curModel, Boolean isBreak, ArrayList<String> imageList);
    void end();
}

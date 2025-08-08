package com.fxzs.lingxiagent.lingxi.config;

import com.fxzs.lingxiagent.lingxi.lingxi_conversation.AdapterType;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.LocalModule;

import java.util.ArrayList;

public interface ChatFlowCallback {
    void receive(AdapterType type, Boolean isBreak, String content);
    void receive(ArrayList<String> imageList);
    void end();
}

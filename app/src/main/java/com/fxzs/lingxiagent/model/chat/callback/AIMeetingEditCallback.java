package com.fxzs.lingxiagent.model.chat.callback;


import com.fxzs.lingxiagent.model.chat.dto.OptionModel;

public interface AIMeetingEditCallback {
    void send(String content);
    void close();
    void voice();
    void keyboard();
    void pressDown();
    void pressUp(boolean isInArea);

    default void voiceMove(boolean status){};
}

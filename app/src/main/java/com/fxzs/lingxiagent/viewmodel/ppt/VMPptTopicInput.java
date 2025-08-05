package com.fxzs.lingxiagent.viewmodel.ppt;

import android.app.Application;

import androidx.annotation.NonNull;

import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.common.BaseViewModel;

public class VMPptTopicInput extends BaseViewModel {
    
    private final ObservableField<String> topicText = new ObservableField<>("");
    private final ObservableField<Boolean> sendButtonEnabled = new ObservableField<>(false);
    
    public VMPptTopicInput(@NonNull Application application) {
        super(application);
        
        topicText.observeForever(text -> {
            boolean isValid = text != null && !text.trim().isEmpty() && text.length() <= 40;
            sendButtonEnabled.set(isValid);
        });
    }
    
    public ObservableField<String> getTopicText() {
        return topicText;
    }
    
    public ObservableField<Boolean> getSendButtonEnabled() {
        return sendButtonEnabled;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        topicText.removeObserver(text -> {});
    }
}
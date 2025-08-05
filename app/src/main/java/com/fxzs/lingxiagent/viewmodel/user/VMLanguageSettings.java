package com.fxzs.lingxiagent.viewmodel.user;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.model.chat.repository.ChatRepository;
import com.fxzs.lingxiagent.model.chat.repository.ChatRepositoryImpl;
import com.fxzs.lingxiagent.util.GlobalSettings;

import java.util.LinkedHashMap;
import java.util.Map;

public class VMLanguageSettings extends BaseViewModel {
    
    private final MutableLiveData<Boolean> languageChanged = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String>> languageList = new MutableLiveData<>();
    
    // Repository
    private final ChatRepository chatRepository;
    
    // 语言映射表
    private Map<String, String> languageMap = new LinkedHashMap<>();
    
    public VMLanguageSettings(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepositoryImpl();
        // 加载语言列表
        loadLanguageList();
    }
    
    public MutableLiveData<Boolean> getLanguageChanged() {
        return languageChanged;
    }
    
    public MutableLiveData<Map<String, String>> getLanguageList() {
        return languageList;
    }
    
    public void selectLanguage(String languageCode) {
        // 保存语言设置
        SharedPreferencesUtil.saveLanguage(languageCode);
        
        // 获取语言名称
        String languageName = languageMap.get(languageCode);
        if (languageName != null) {
            // 保存到全局设置
            GlobalSettings.getInstance().setSelectedLanguage(languageCode, languageName);
        }
        
        // 通知语言已更改
        languageChanged.setValue(true);
    }
    
    public String getCurrentLanguage() {
        // 获取当前语言设置
        String languageCode = GlobalSettings.getInstance().getSelectedLanguageCode();
        if (languageCode == null) {
            languageCode = SharedPreferencesUtil.getLanguage();
        }
        return languageCode;
    }
    
    public Map<String, String> getLanguageMap() {
        return languageMap;
    }
    
    private void loadLanguageList() {
        setLoading(true);
        chatRepository.getEngineModelType(new ChatRepository.Callback<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> data) {
                setLoading(false);
                if (data != null && !data.isEmpty()) {
                    languageMap = data;
                    languageList.setValue(data);
                } else {
                    // 如果接口返回为空，使用默认值
                    Map<String, String> defaultMap = new LinkedHashMap<>();
                    defaultMap.put("zh_CN", "普通话");
                    defaultMap.put("en", "英语");
                    defaultMap.put("zh_HK", "粤语");
                    defaultMap.put("zh_CN_en", "普方英");
                    languageMap = defaultMap;
                    languageList.setValue(defaultMap);
                }
            }
            
            @Override
            public void onError(String error) {
                setLoading(false);
                setError("加载语言列表失败: " + error);
                // 使用默认值
                Map<String, String> defaultMap = new LinkedHashMap<>();
                defaultMap.put("zh_CN", "普通话");
                defaultMap.put("en", "英语");
                defaultMap.put("zh_HK", "粤语");
                defaultMap.put("zh_CN_en", "普方英");
                languageMap = defaultMap;
                languageList.setValue(defaultMap);
            }
        });
    }
}
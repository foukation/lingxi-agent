package com.fxzs.lingxiagent.util;

import android.content.Context;

/**
 * 全局设置管理类
 * 用于管理应用中的全局设置，如选中的模型和语言
 */
public class GlobalSettings {
    
    private static GlobalSettings instance;
    
    // 当前选中的模型
    private String selectedModelCode;
    private String selectedModelName;
    
    // 当前选中的语音识别语言
    private String selectedLanguageCode;
    private String selectedLanguageName;
    
    private GlobalSettings() {
        // 私有构造函数
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized GlobalSettings getInstance() {
        if (instance == null) {
            instance = new GlobalSettings();
        }
        return instance;
    }
    
    /**
     * 初始化全局设置（从SharedPreferences加载）
     */
    public void init(Context context) {
        // 加载模型设置
        selectedModelCode = SharedPreferencesUtil.getSelectedModel(context);
        selectedModelName = SharedPreferencesUtil.getModelDisplayName(context);
        
        // 加载语言设置
        selectedLanguageCode = SharedPreferencesUtil.getLanguage(context);
        // 语言名称需要从API获取后更新
    }
    
    /**
     * 设置选中的模型
     */
    public void setSelectedModel(Context context, String modelCode, String modelName) {
        this.selectedModelCode = modelCode;
        this.selectedModelName = modelName;
        // 保存到SharedPreferences
        SharedPreferencesUtil.setSelectedModel(context, modelCode);
    }
    
    /**
     * 设置选中的语言
     */
    public void setSelectedLanguage(String languageCode, String languageName) {
        this.selectedLanguageCode = languageCode;
        this.selectedLanguageName = languageName;
        // 保存到SharedPreferences
        SharedPreferencesUtil.saveLanguage(languageCode);
    }
    
    /**
     * 获取选中的模型代码
     */
    public String getSelectedModelCode() {
        return selectedModelCode;
    }
    
    /**
     * 获取选中的模型名称
     */
    public String getSelectedModelName() {
        return selectedModelName;
    }
    
    /**
     * 获取选中的语言代码
     */
    public String getSelectedLanguageCode() {
        return selectedLanguageCode;
    }
    
    /**
     * 获取选中的语言名称
     */
    public String getSelectedLanguageName() {
        return selectedLanguageName;
    }
    
    /**
     * 更新语言名称（从API获取后调用）
     */
    public void updateLanguageName(String languageName) {
        this.selectedLanguageName = languageName;
    }
}
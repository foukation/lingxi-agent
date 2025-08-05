package com.fxzs.lingxiagent.viewmodel.ppt;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.view.ppt.PptTemplateSelectionActivity.PptTemplate;
import com.fxzs.lingxiagent.model.common.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class VMPptTemplateSelection extends BaseViewModel {
    
    private final MutableLiveData<List<PptTemplate>> templateList = new MutableLiveData<>(new ArrayList<>());
    private final ObservableField<String> selectedTemplateId = new ObservableField<>("");
    private final ObservableField<String> selectedColor = new ObservableField<>("rainbow");
    private final ObservableField<String> selectedStyle = new ObservableField<>("推荐");
    private final ObservableField<Boolean> generateButtonEnabled = new ObservableField<>(false);
    
    public VMPptTemplateSelection(@NonNull Application application) {
        super(application);
        
        selectedTemplateId.observeForever(id -> {
            generateButtonEnabled.set(id != null && !id.isEmpty());
        });
    }
    
    public void loadTemplates() {
        setLoading(true);
        
        // Simulate loading templates
        List<PptTemplate> templates = new ArrayList<>();
        templates.add(new PptTemplate("1", "运营年终工作总结", ""));
        templates.add(new PptTemplate("2", "工程项目进度汇报", ""));
        templates.add(new PptTemplate("3", "年度工作回顾", ""));
        templates.add(new PptTemplate("4", "企业品牌介绍", ""));
        templates.add(new PptTemplate("5", "功能简介", ""));
        templates.add(new PptTemplate("6", "名片内联官网", ""));
        templates.add(new PptTemplate("7", "多身份切换", ""));
        templates.add(new PptTemplate("8", "多种分享模式及物料", ""));
        
        templateList.postValue(templates);
        setLoading(false);
    }
    
    public void refreshTemplates() {
        loadTemplates();
    }
    
    public void selectTemplate(String templateId) {
        selectedTemplateId.set(templateId);
    }
    
    public void setSelectedColor(String color) {
        selectedColor.set(color);
    }
    
    public void setSelectedStyle(String style) {
        selectedStyle.set(style);
    }
    
    public MutableLiveData<PptGenerationResult> generatePpt(String topic) {
        MutableLiveData<PptGenerationResult> result = new MutableLiveData<>();
        
        setLoading(true);
        
        // Simulate PPT generation
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                result.postValue(new PptGenerationResult(true, "ppt_123"));
            } catch (InterruptedException e) {
                result.postValue(new PptGenerationResult(false, null));
            } finally {
                setLoading(false);
            }
        }).start();
        
        return result;
    }
    
    public MutableLiveData<List<PptTemplate>> getTemplateList() {
        return templateList;
    }
    
    public ObservableField<String> getSelectedTemplateId() {
        return selectedTemplateId;
    }
    
    public ObservableField<String> getSelectedColor() {
        return selectedColor;
    }
    
    public ObservableField<String> getSelectedStyle() {
        return selectedStyle;
    }
    
    public ObservableField<Boolean> getGenerateButtonEnabled() {
        return generateButtonEnabled;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        selectedTemplateId.removeObserver(id -> {});
    }
    
    public static class PptGenerationResult {
        private final boolean success;
        private final String pptId;
        
        public PptGenerationResult(boolean success, String pptId) {
            this.success = success;
            this.pptId = pptId;
        }
        
        public boolean isSuccess() { return success; }
        public String getPptId() { return pptId; }
    }
}
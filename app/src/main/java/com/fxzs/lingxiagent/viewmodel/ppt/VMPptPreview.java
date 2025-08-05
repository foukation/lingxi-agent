package com.fxzs.lingxiagent.viewmodel.ppt;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.view.ppt.PptPreviewActivity.PptSlide;
import com.fxzs.lingxiagent.model.common.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class VMPptPreview extends BaseViewModel {
    
    private final ObservableField<String> pptTitle = new ObservableField<>("");
    private final MutableLiveData<List<PptSlide>> slides = new MutableLiveData<>(new ArrayList<>());
    
    public VMPptPreview(@NonNull Application application) {
        super(application);
    }
    
    public void loadPptData(String pptId) {
        setLoading(true);
        
        // Simulate loading PPT data
        pptTitle.set("运营年终工作总结");
        
        List<PptSlide> slideList = new ArrayList<>();
        
        // Cover slide
        slideList.add(new PptSlide(
            "运营年终工作总结",
            "2024年度汇报",
            PptSlide.SlideType.COVER,
            false
        ));
        
        // Section slide
        slideList.add(new PptSlide(
            "01 年度工作回顾",
            null,
            PptSlide.SlideType.SECTION,
            false
        ));
        
        // Content slide with chart
        slideList.add(new PptSlide(
            "业绩数据展示",
            "全年完成销售额增长20%，客户满意度提升30%",
            PptSlide.SlideType.CONTENT,
            true
        ));
        
        // Content slide
        slideList.add(new PptSlide(
            "团队建设与培训",
            "01\n• 组织新员工培训5场\n• 完成技能提升培训10次\n• 团队建设活动8次\n• 员工满意度达到95%",
            PptSlide.SlideType.CONTENT,
            false
        ));
        
        slides.postValue(slideList);
        setLoading(false);
    }
    
    public ObservableField<String> getPptTitle() {
        return pptTitle;
    }
    
    public MutableLiveData<List<PptSlide>> getSlides() {
        return slides;
    }
}
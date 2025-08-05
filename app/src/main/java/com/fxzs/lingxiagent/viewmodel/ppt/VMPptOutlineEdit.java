package com.fxzs.lingxiagent.viewmodel.ppt;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.view.ppt.PptOutlineEditActivity.OutlineItem;
import com.fxzs.lingxiagent.model.common.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class VMPptOutlineEdit extends BaseViewModel {
    
    private final MutableLiveData<List<OutlineItem>> outlineItems = new MutableLiveData<>(new ArrayList<>());
    private final ObservableField<Boolean> isGenerating = new ObservableField<>(false);
    
    public VMPptOutlineEdit(@NonNull Application application) {
        super(application);
    }
    
    public void loadOutline(String pptId) {
        setLoading(true);
        
        // Simulate loading outline
        List<OutlineItem> items = new ArrayList<>();
        items.add(new OutlineItem("公司简介", 
            "淘宝网是亚太地区较大的网络零售、商圈，由阿里巴巴集团在2003年5月创立。" +
            "淘宝网是中国深受欢迎的网购零售平台，拥有近5亿的注册用户数，每天有超过6000万的固定访客。"));
        
        items.add(new OutlineItem("培训与发展", 
            "我们重视员工的成长和发展：\n" +
            "• 新员工入职培训\n" +
            "• 定期技能提升培训\n" +
            "• 管理能力培养计划\n" +
            "• 跨部门轮岗机会"));
        
        items.add(new OutlineItem("员工福利", 
            "完善的福利体系：\n" +
            "• 五险一金\n" +
            "• 带薪年假\n" +
            "• 节日福利\n" +
            "• 员工体检"));
        
        items.add(new OutlineItem("企业文化", ""));
        items.add(new OutlineItem("未来展望", ""));
        
        outlineItems.postValue(items);
        setLoading(false);
        
        // Simulate content generation
        simulateContentGeneration();
    }
    
    private void simulateContentGeneration() {
        isGenerating.set(true);
        
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                
                List<OutlineItem> items = outlineItems.getValue();
                if (items != null && items.size() > 3) {
                    items.get(3).setContent(
                        "企业文化核心价值观：\n" +
                        "• 客户第一\n" +
                        "• 团队合作\n" +
                        "• 拥抱变化\n" +
                        "• 诚信\n" +
                        "• 激情\n" +
                        "• 敬业"
                    );
                    outlineItems.postValue(items);
                }
                
                Thread.sleep(2000);
                
                if (items != null && items.size() > 4) {
                    items.get(4).setContent(
                        "面向未来，我们将：\n" +
                        "• 持续创新，引领行业发展\n" +
                        "• 深化国际化战略\n" +
                        "• 加强技术研发投入\n" +
                        "• 提升用户体验\n" +
                        "• 承担更多社会责任"
                    );
                    outlineItems.postValue(items);
                }
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                isGenerating.postValue(false);
            }
        }).start();
    }
    
    public void regenerateOutline() {
        // Simulate regenerating outline
        loadOutline("");
    }
    
    public void updateOutlineItem(int position, OutlineItem item) {
        List<OutlineItem> items = outlineItems.getValue();
        if (items != null && position >= 0 && position < items.size()) {
            items.set(position, item);
            outlineItems.postValue(items);
        }
    }
    
    public void stopGenerating() {
        isGenerating.set(false);
    }
    
    public MutableLiveData<List<OutlineItem>> getOutlineItems() {
        return outlineItems;
    }
    
    public ObservableField<Boolean> getIsGenerating() {
        return isGenerating;
    }
}
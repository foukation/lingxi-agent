package com.fxzs.lingxiagent.viewmodel.drawing;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingGalleryItem;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSampleDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingCategoryDto;
import com.fxzs.lingxiagent.model.drawing.api.PageResult;
import com.fxzs.lingxiagent.model.drawing.api.SampleListRequest;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepository;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * AI绘画示例画廊ViewModel
 */
public class VMDrawingGallery extends BaseViewModel {
    
    // 双向绑定字段
    private final ObservableField<String> promptHint = new ObservableField<>("描述你想要创作的内容...");
    
    // 业务状态
    private final MutableLiveData<List<DrawingGalleryItem>> galleryItems = new MutableLiveData<>();
    private final MutableLiveData<DrawingGalleryItem> selectedItem = new MutableLiveData<>();
    private final MutableLiveData<List<DrawingSampleDto>> samples = new MutableLiveData<>();
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("selected");
    private final MutableLiveData<List<DrawingCategoryDto>> categories = new MutableLiveData<>();
    private final DrawingRepository repository;
    
    public VMDrawingGallery(@NonNull Application application) {
        super(application);
        repository = DrawingRepositoryImpl.getInstance();
        // 初始时加载精选分类的数据
        loadSamplesByCategory("selected");
    }
    
    // Getters
    public ObservableField<String> getPromptHint() {
        return promptHint;
    }
    
    public MutableLiveData<List<DrawingGalleryItem>> getGalleryItems() {
        return galleryItems;
    }
    
    public MutableLiveData<DrawingGalleryItem> getSelectedItem() {
        return selectedItem;
    }
    
    public MutableLiveData<List<DrawingSampleDto>> getSamples() {
        return samples;
    }
    
    public MutableLiveData<String> getSelectedCategory() {
        return selectedCategory;
    }
    
    public MutableLiveData<List<DrawingCategoryDto>> getCategories() {
        return categories;
    }
    
    /**
     * 设置选中的分类
     */
    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
        // 当分类改变时，加载对应的示例数据
        loadSamplesByCategory(category);
    }
    
    
    /**
     * 选择画廊项目
     */
    public void selectItem(DrawingGalleryItem item) {
        selectedItem.setValue(item);
    }
    
    /**
     * 加载分类列表
     */
    private void loadCategories() {
        repository.getCategoryList().observeForever(result -> {
            if (result.isSuccess() && result.getData() != null) {
                List<DrawingCategoryDto> categoryList = result.getData();
                categories.postValue(categoryList);
                android.util.Log.d("VMDrawingGallery", "Categories loaded: " + categoryList.size());
                for (DrawingCategoryDto cat : categoryList) {
                    android.util.Log.d("VMDrawingGallery", "Category: " + cat.getName() + " (ID: " + cat.getId() + ")");
                }
                // 分类加载完成后，如果当前有选中的分类，重新加载样本数据
                String currentCategory = selectedCategory.getValue();
                if (currentCategory != null) {
                    loadSamplesByCategory(currentCategory);
                }
            } else {
                android.util.Log.e("VMDrawingGallery", "Failed to load categories");
            }
        });
    }
    
    
    /**
     * 按分类加载示例
     */
    public void loadSamplesByCategory(String category) {
        setLoading(true);
        
        android.util.Log.d("VMDrawingGallery", "Loading samples for category: " + category);
        
        // 根据分类名称获取对应的分类ID
        Long categoryId = getCategoryIdByName(category);
        
        // 创建请求参数
        SampleListRequest request = new SampleListRequest();
        request.setCatId(categoryId);
        request.setPageNo(1);
        request.setPageSize(100); // 可根据需要调整
        
        // 使用 getSampleList API
        repository.getSampleList(request).observeForever(result -> {
            setLoading(false);
            
            if (result.isSuccess() && result.getData() != null) {
                PageResult<DrawingSampleDto> pageResult = result.getData();
                List<DrawingSampleDto> sampleList = pageResult.getList();
                
                android.util.Log.d("VMDrawingGallery", "=== getSampleList Response ===");
                android.util.Log.d("VMDrawingGallery", "Category: " + category + ", CategoryId: " + categoryId);
                android.util.Log.d("VMDrawingGallery", "Total samples returned: " + (sampleList != null ? sampleList.size() : 0));
                
                if (sampleList != null && !sampleList.isEmpty()) {
                    // 不需要客户端过滤，API已经返回了对应分类的数据
                    List<DrawingGalleryItem> items = new ArrayList<>();
                    
                    for (DrawingSampleDto sample : sampleList) {
                        // 打印每个样本的详细信息
                        android.util.Log.d("VMDrawingGallery", "Sample Detail:");
                        android.util.Log.d("VMDrawingGallery", "  - ID: " + sample.getId() + " <-- 服务器返回的ID");
                        android.util.Log.d("VMDrawingGallery", "  - Prompt: " + sample.getPrompt());
                        android.util.Log.d("VMDrawingGallery", "  - ImageUrl: " + sample.getImageUrl());
                        android.util.Log.d("VMDrawingGallery", "  - CatId: " + sample.getCatId());
                        android.util.Log.d("VMDrawingGallery", "  - CatName: " + sample.getCatName());
                        android.util.Log.d("VMDrawingGallery", "  - StyleId: " + sample.getStyleId());
                        android.util.Log.d("VMDrawingGallery", "  - StyleName: " + sample.getStyleName());
                        
                        // 直接添加所有返回的样本
                        DrawingGalleryItem galleryItem = new DrawingGalleryItem(
                            sample.getImageUrl(),
                            sample.getPrompt(),
                            sample.getStyleName() != null ? sample.getStyleName() : "默认",
                            "做同款"
                        );
                        android.util.Log.d("VMDrawingGallery", "Creating DrawingGalleryItem with imageUrl: " + galleryItem.getImageUrl());
                        items.add(galleryItem);
                    }
                    
                    android.util.Log.d("VMDrawingGallery", "Total samples for category '" + category + "': " + sampleList.size());
                    samples.postValue(sampleList);
                    galleryItems.postValue(items);
                } else {
                    android.util.Log.d("VMDrawingGallery", "No data returned from API");
                    samples.postValue(new ArrayList<>());
                    galleryItems.postValue(new ArrayList<>());
                }
            } else {
                android.util.Log.e("VMDrawingGallery", "API Error: " + result.getError());
                setError(result.getError() != null ? result.getError() : "获取示例失败");
            }
        });
    }
    
    /**
     * 根据分类名称获取分类ID
     */
    private Long getCategoryIdByName(String category) {
        // 根据实际的API返回的分类ID进行映射
        switch (category) {
            case "selected":
                return 1L;  // 精选 (ID: 1)
            case "portrait":
                return 2L;  // 人像摄影 (ID: 2)
            case "art":
                return 3L;  // 艺术 (ID: 3)
            case "chinese_comic":
                return 4L;  // 国风漫画 (ID: 4)
            case "anime":
                return 5L;  // 动漫 (ID: 5)
            default:
                return 1L;  // 默认返回精选
        }
    }
    
    /**
     * 映射内部分类标识到实际分类名称
     */
    private String mapCategoryName(String category) {
        switch (category) {
            case "selected":
                return "精选";
            case "portrait":
                return "人像摄影";
            case "art":
                return "艺术";
            case "chinese_comic":
                return "国风漫画";
            case "anime":
                return "动漫";
            default:
                return "精选";
        }
    }
    
    /**
     * 匹配分类名称
     */
    private boolean matchesCategoryName(String catName, String category) {
        if (catName == null || category == null) return false;
        
        switch (category) {
            case "portrait":
                return catName.contains("人像") || catName.contains("摄影");
            case "art":
                return catName.contains("艺术");
            case "chinese_comic":
                return catName.contains("国风") || catName.contains("漫画");
            case "anime":
                return catName.contains("二次元") || catName.contains("动漫");
            default:
                return false;
        }
    }
    
    /**
     * 选择示例
     */
    public void selectSample(DrawingSampleDto sample) {
        if (sample != null) {
            DrawingGalleryItem item = new DrawingGalleryItem(
                sample.getImageUrl(),
                sample.getPrompt(),
                sample.getStyleName() != null ? sample.getStyleName() : "默认",
                "做同款"
            );
            selectedItem.setValue(item);
        }
    }
}
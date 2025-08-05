package com.fxzs.lingxiagent.viewmodel.drawing;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.drawing.api.GenerateImageRequest;
import com.fxzs.lingxiagent.model.drawing.dto.AspectRatioDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingStyleDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSampleDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.model.drawing.api.PageResult;
import com.fxzs.lingxiagent.model.drawing.api.SampleListRequest;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepository;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepositoryImpl;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;

/**
 * AI绘画主界面ViewModel
 */
public class VMDrawing extends BaseViewModel {
    
    // 双向绑定字段
    private final ObservableField<String> prompt = new ObservableField<>("");
    private final ObservableField<String> selectedRatio = new ObservableField<>("1:1");
    private final ObservableField<Boolean> generateEnabled = new ObservableField<>(false);
    private final ObservableField<Boolean> isGenerating = new ObservableField<>(false);
    private final ObservableField<Integer> progress = new ObservableField<>(0);
    private final ObservableField<String> progressText = new ObservableField<>("");
    
    // 业务状态
    private final MutableLiveData<List<DrawingStyleDto>> styles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<AspectRatioDto>> aspectRatios = new MutableLiveData<>();
    private final MutableLiveData<DrawingImageDto> generatedImage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showResult = new MutableLiveData<>(false);
    private final MutableLiveData<List<DrawingSampleDto>> samples = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<DrawingSessionDto> currentSession = new MutableLiveData<>();
    
    private DrawingStyleDto selectedStyle = null;
    private final DrawingRepository repository;
    private Timer progressTimer;
    private Long currentSessionId;
    private Long currentTaskId;
    private String initialStyle = null;
    private String referenceImageUrl = null; // 参考图片URL
    private String hiddenPrompt = null; // 继续编辑模式下的隐藏prompt，用于关联但不显示
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isCreatingSession = false; // 是否正在创建会话
    private boolean pendingGeneration = false; // 是否有待处理的生成请求
    private boolean isContinueEditMode = false; // 是否是继续编辑模式
    
    public VMDrawing(@NonNull Application application) {
        super(application);
        repository = DrawingRepositoryImpl.getInstance();
        
        // 监听输入变化
        prompt.observeForever(this::validateForm);
        // 监听比例选择变化
        selectedRatio.observeForever(this::validateForm);
        
        // 初始化数据
        initAspectRatios();
        loadStyles();
        
        // 暂时不在初始化时创建会话，等用户输入prompt后再创建
    }
    
    // Getters
    public ObservableField<String> getPrompt() {
        return prompt;
    }
    
    public DrawingStyleDto getSelectedStyle() {
        return selectedStyle;
    }
    
    public void setSelectedStyle(DrawingStyleDto style) {
        // 记录调用栈，找出是谁在重置风格
        android.util.Log.d("VMDrawing", "setSelectedStyle called with: " + (style != null ? style.getName() + " (ID: " + style.getId() + ")" : "null"));
//        android.util.Log.d("VMDrawing", "Call stack: " + android.util.Log.getStackTraceString(new Throwable()));

        this.selectedStyle = style;
        android.util.Log.d("VMDrawing", "Style selected: " + (style != null ? style.getName() + " (ID: " + style.getId() + ")" : "null"));
        android.util.Log.d("VMDrawing", "selectedStyle object updated: " + (this.selectedStyle != null ? this.selectedStyle.getName() + " (ID: " + this.selectedStyle.getId() + ")" : "null"));
        validateForm(prompt.get());
    }
    
    public ObservableField<String> getSelectedRatio() {
        return selectedRatio;
    }
    
    public ObservableField<Boolean> getGenerateEnabled() {
        return generateEnabled;
    }
    
    public ObservableField<Boolean> getIsGenerating() {
        return isGenerating;
    }
    
    public ObservableField<Integer> getProgress() {
        return progress;
    }
    
    public ObservableField<String> getProgressText() {
        return progressText;
    }
    
    public MutableLiveData<List<DrawingStyleDto>> getStyles() {
        return styles;
    }
    
    public MutableLiveData<List<AspectRatioDto>> getAspectRatios() {
        return aspectRatios;
    }
    
    public MutableLiveData<DrawingImageDto> getGeneratedImage() {
        return generatedImage;
    }
    
    public MutableLiveData<Boolean> getShowResult() {
        return showResult;
    }
    
    public MutableLiveData<List<DrawingSampleDto>> getSamples() {
        return samples;
    }
    
    public MutableLiveData<DrawingSessionDto> getCurrentSession() {
        return currentSession;
    }
    
    // 初始化宽高比选项（注意：这些数据仅用于显示，实际计算使用512作为基准宽度）
    private void initAspectRatios() {
        List<AspectRatioDto> ratios = new ArrayList<>();
        // 基准宽度512，根据比例计算高度
        ratios.add(new AspectRatioDto("9:16", "9:16", 512, 910, false));  // 512 * 16/9 ≈ 910
        ratios.add(new AspectRatioDto("16:9", "16:9", 512, 288, false));  // 512 * 9/16 = 288
        ratios.add(new AspectRatioDto("4:3", "4:3", 512, 384, false));    // 512 * 3/4 = 384
        ratios.add(new AspectRatioDto("2:3", "2:3", 512, 768, false));    // 512 * 3/2 = 768
        ratios.add(new AspectRatioDto("1:1", "1:1", 512, 512, true));     // 512 * 1/1 = 512，默认
        aspectRatios.postValue(ratios);

        android.util.Log.d("VMDrawing", "Initialized aspect ratios with base width 512");
    }
    
    // 加载风格列表
    private void loadStyles() {
        setLoading(true);
        
        repository.getStyles().observeForever(result -> {
            setLoading(false);
            if (result.isSuccess() && result.getData() != null) {
                List<DrawingStyleDto> styleList = result.getData();
                styles.postValue(styleList);
                
                // 应用初始风格（如果有）
                if (initialStyle != null) {
                    for (DrawingStyleDto style : styleList) {
                        if (style.getName().equals(initialStyle)) {
                            setSelectedStyle(style);
                            initialStyle = null; // 清空，避免重复设置
                            return;
                        }
                    }
                }
                
                // 如果没有初始风格或找不到，使用默认第一个（但不覆盖用户已选择的风格）
                if (!styleList.isEmpty() && selectedStyle == null) {
                    setSelectedStyle(styleList.get(0));
                    android.util.Log.d("VMDrawing", "Set default style: " + styleList.get(0).getName());
                } else if (selectedStyle != null) {
                    android.util.Log.d("VMDrawing", "Keeping existing selected style: " + selectedStyle.getName() + " (ID: " + selectedStyle.getId() + ")");
                }
            } else {
                // 如果获取失败，使用默认风格
                List<DrawingStyleDto> mockStyles = createMockStyles();
                styles.postValue(mockStyles);
                
                // 应用初始风格（如果有）
                if (initialStyle != null) {
                    for (DrawingStyleDto style : mockStyles) {
                        if (style.getName().equals(initialStyle)) {
                            setSelectedStyle(style);
                            initialStyle = null;
                            setError(result.getError() != null ? result.getError() : "获取风格列表失败");
                            return;
                        }
                    }
                }
                
                if (!mockStyles.isEmpty() && selectedStyle == null) {
                    setSelectedStyle(mockStyles.get(0));
                    android.util.Log.d("VMDrawing", "Set default mock style: " + mockStyles.get(0).getName());
                } else if (selectedStyle != null) {
                    android.util.Log.d("VMDrawing", "Keeping existing selected style (mock): " + selectedStyle.getName() + " (ID: " + selectedStyle.getId() + ")");
                }
                setError(result.getError() != null ? result.getError() : "获取风格列表失败");
            }
        });
    }
    
    // 创建模拟风格数据
    private List<DrawingStyleDto> createMockStyles() {
        List<DrawingStyleDto> list = new ArrayList<>();
        
        DrawingStyleDto style1 = new DrawingStyleDto();
        style1.setId(1L);
        style1.setName("写实");
        style1.setPrompt("realistic, photorealistic");
        style1.setIconUrl("https://example.com/style/realistic.jpg");
        list.add(style1);
        
        DrawingStyleDto style2 = new DrawingStyleDto();
        style2.setId(2L);
        style2.setName("数字艺术写实");
        style2.setPrompt("digital art, realistic style");
        style2.setIconUrl("https://example.com/style/digital_art.jpg");
        list.add(style2);
        
        DrawingStyleDto style3 = new DrawingStyleDto();
        style3.setId(3L);
        style3.setName("古风仙侠");
        style3.setPrompt("ancient chinese style, fantasy, xianxia");
        style3.setIconUrl("https://example.com/style/ancient.jpg");
        list.add(style3);
        
        DrawingStyleDto style4 = new DrawingStyleDto();
        style4.setId(4L);
        style4.setName("机甲风");
        style4.setPrompt("mecha, sci-fi, mechanical");
        style4.setIconUrl("https://example.com/style/mecha.jpg");
        list.add(style4);
        
        DrawingStyleDto style5 = new DrawingStyleDto();
        style5.setId(5L);
        style5.setName("数码漫画（二次元）");
        style5.setPrompt("anime, manga style, 2D");
        style5.setIconUrl("https://example.com/style/anime.jpg");
        list.add(style5);
        
        DrawingStyleDto style6 = new DrawingStyleDto();
        style6.setId(6L);
        style6.setName("乙女漫画（厚涂）");
        style6.setPrompt("shoujo manga, thick painting style");
        style6.setIconUrl("https://example.com/style/shoujo.jpg");
        list.add(style6);
        
        return list;
    }
    
    // 表单验证
    private void validateForm(String value) {
        boolean hasPrompt = prompt.get() != null && !prompt.get().trim().isEmpty();
        boolean hasStyle = selectedStyle != null;
        boolean hasRatio = selectedRatio.get() != null && !selectedRatio.get().trim().isEmpty();
        generateEnabled.set(hasPrompt && hasStyle && hasRatio && !isGenerating.get());

        android.util.Log.d("VMDrawing", "validateForm - hasPrompt: " + hasPrompt +
                          ", hasStyle: " + hasStyle + ", hasRatio: " + hasRatio +
                          ", generateEnabled: " + generateEnabled.get());
    }
    
    // 生成图片
    public void generateImage() {
        android.util.Log.d("VMDrawing", "=== generateImage called ===");
        android.util.Log.d("VMDrawing", "Call stack: " + android.util.Log.getStackTraceString(new Throwable()));
        android.util.Log.d("VMDrawing", "generateEnabled: " + generateEnabled.get());
        android.util.Log.d("VMDrawing", "isGenerating: " + isGenerating.get());
        android.util.Log.d("VMDrawing", "prompt: " + prompt.get());
        android.util.Log.d("VMDrawing", "selectedStyle: " + (selectedStyle != null ? selectedStyle.getName() + " (ID: " + selectedStyle.getId() + ")" : "null"));
        android.util.Log.d("VMDrawing", "selectedRatio: " + selectedRatio.get());
        android.util.Log.d("VMDrawing", "referenceImageUrl: " + referenceImageUrl);
        android.util.Log.d("VMDrawing", "isContinueEditMode: " + isContinueEditMode);
        
        if (!generateEnabled.get() || isGenerating.get()) {
            android.util.Log.w("VMDrawing", "Generation blocked: generateEnabled=" + generateEnabled.get() + ", isGenerating=" + isGenerating.get());
            return;
        }
        
        // 准备请求参数
        GenerateImageRequest request = new GenerateImageRequest();
        
        // 组合提示词
        String fullPrompt = prompt.get().trim();
        android.util.Log.d("VMDrawing", "Original prompt: " + fullPrompt);

        // 如果有hiddenPrompt（继续编辑模式），则将其与用户输入组合
        if (hiddenPrompt != null && !hiddenPrompt.isEmpty()) {
            android.util.Log.d("VMDrawing", "Hidden prompt: " + hiddenPrompt);
            // 继续编辑模式：组合隐藏的prompt和新输入
            if (!fullPrompt.isEmpty()) {
                fullPrompt = hiddenPrompt + ", " + fullPrompt;
            } else {
                fullPrompt = hiddenPrompt;
            }
            android.util.Log.d("VMDrawing", "Combined with hidden prompt: " + fullPrompt);
        }

        // 如果有参考图片（做同款/继续编辑），只使用用户输入的提示词，不追加风格描述
        // 因为用户通常只想修改局部内容（如"头发换成红色"）
        if (referenceImageUrl == null || referenceImageUrl.isEmpty()) {
            // 只有在没有参考图片时，才追加风格提示词
            if (selectedStyle != null && selectedStyle.getPrompt() != null) {
                android.util.Log.d("VMDrawing", "Adding style prompt: " + selectedStyle.getPrompt());
                fullPrompt += ", " + selectedStyle.getPrompt();
                android.util.Log.d("VMDrawing", "Final prompt with style: " + fullPrompt);
            }
        }
        request.setPrompt(fullPrompt);
        
        // 设置宽高：宽度固定512，高度根据比例计算
        String selectedRatioStr = selectedRatio.get();
        android.util.Log.d("VMDrawing", "Getting selected ratio: " + selectedRatioStr);
        int width = 512;
        int height = calculateHeightFromRatio(selectedRatioStr, width);
        request.setWidth(width);
        request.setHeight(height);

        android.util.Log.d("VMDrawing", "Calculated dimensions - ratio: " + selectedRatioStr +
                          ", width: " + width + ", height: " + height);
        
        // 设置风格ID
        if (selectedStyle != null) {
            request.setStyleId(selectedStyle.getId());
            android.util.Log.d("VMDrawing", "Setting style: " + selectedStyle.getName() + " (ID: " + selectedStyle.getId() + ")");
        } else {
            android.util.Log.w("VMDrawing", "No style selected!");
        }

        // 添加调试日志
        android.util.Log.d("VMDrawing", "Final request - prompt: " + request.getPrompt());
        android.util.Log.d("VMDrawing", "Final request - width: " + request.getWidth() + ", height: " + request.getHeight());
        android.util.Log.d("VMDrawing", "Final request - styleId: " + request.getStyleId());
        
        // 设置会话ID
        if (currentSessionId == null && !isCreatingSession) {
            // 如果没有会话，先创建会话
            android.util.Log.d("VMDrawing", "Creating session before generating image");
            pendingGeneration = true; // 标记有待处理的生成请求
            createNewSession(prompt.get().trim());
            
            // 等待会话创建完成后再生成图片
            isGenerating.set(false);
            return;
        } else if (currentSessionId == null && isCreatingSession) {
            // 如果正在创建会话，提示等待
            android.util.Log.d("VMDrawing", "Waiting for session creation");
            isGenerating.set(false);
            setError("会话创建中，请稍后重试");
            return;
        } else {
            // 有会话ID，设置到请求中
            request.setSessionId(currentSessionId);
            android.util.Log.d("VMDrawing", "Using sessionId: " + currentSessionId);
        }
        
        // 设置参考图片URL
        if (isContinueEditMode && referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
            request.setImagUrls(new String[]{referenceImageUrl});
            android.util.Log.d("VMDrawing", "Setting reference image URL: " + referenceImageUrl);
        }
        
        // 开始生成
        isGenerating.set(true);
        progress.set(0);
        progressText.set("正在生成中...");
        showResult.postValue(false);
        
        // 启动进度模拟
        startProgressTimer();
        
        // 使用异步接口生成图片
        repository.generateImage(request).observeForever(result -> {
            if (result.isSuccess() && result.getData() != null) {
                // 异步接口返回任务ID
                currentTaskId = result.getData().getId();
                android.util.Log.d("VMDrawing", "Task ID received: " + currentTaskId);
                
                // 开始轮询任务状态
                startPollingTaskStatus();
            } else {
                stopProgressTimer();
                mainHandler.post(() -> {
                    isGenerating.set(false);
                    progress.set(0);
                });
                setError(result.getError() != null ? result.getError() : "生成失败");
                
                // 生成失败也清除参考图片URL
                clearReferenceImageUrl();
            }
        });
    }
    
    // 查找选中的宽高比
    private AspectRatioDto findSelectedRatio() {
        List<AspectRatioDto> ratios = aspectRatios.getValue();
        if (ratios == null) return null;

        String selected = selectedRatio.get();
        for (AspectRatioDto ratio : ratios) {
            if (ratio.getRatio().equals(selected)) {
                return ratio;
            }
        }
        return null;
    }

    // 根据比例字符串和固定宽度计算高度
    private int calculateHeightFromRatio(String ratioStr, int width) {
        if (ratioStr == null || ratioStr.isEmpty()) {
            return width; // 默认1:1
        }

        try {
            String[] parts = ratioStr.split(":");
            if (parts.length == 2) {
                double widthRatio = Double.parseDouble(parts[0]);
                double heightRatio = Double.parseDouble(parts[1]);
                int height = (int) Math.round(width * heightRatio / widthRatio);
                android.util.Log.d("VMDrawing", "Ratio calculation - " + ratioStr +
                                  " with width " + width + " = height " + height);
                return height;
            }
        } catch (NumberFormatException e) {
            android.util.Log.e("VMDrawing", "Error parsing ratio: " + ratioStr, e);
        }

        return width; // 默认1:1
    }
    
    // 启动进度定时器
    private void startProgressTimer() {
        stopProgressTimer();
        // 由于现在有真实的进度值，不再需要模拟进度
        // 只保留定时器用于初始阶段的提示
        progressTimer = new Timer();
        progressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 如果5秒后还没有收到进度更新，显示初始提示
                mainHandler.post(() -> {
                    if (progress.get() == 0) {
                        progressText.set("正在初始化...");
                    }
                });
            }
        }, 5000); // 5秒后执行一次
    }
    
    // 停止进度定时器
    private void stopProgressTimer() {
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer = null;
        }
    }
    
    // 创建模拟结果
    private DrawingImageDto createMockResult(GenerateImageRequest request) {
        DrawingImageDto result = new DrawingImageDto();
        result.setId(System.currentTimeMillis());
        result.setPrompt(request.getPrompt());
        result.setWidth(request.getWidth());
        result.setHeight(request.getHeight());
        result.setStatus(1); // 成功
        result.setCreateTime("2024-01-07 16:00:00");
        
        // 使用默认图片URL
        result.setImageUrl("https://via.placeholder.com/768x768/95E1D3/FFFFFF?text=AI+Art");
        result.setThumbnailUrl(result.getImageUrl());
        
        return result;
    }
    
    // 重新生成
    public void regenerate() {
        generateImage();
    }
    
    // 做同款
    public void useAsTemplate(DrawingImageDto image) {
        if (image != null) {
            prompt.set(image.getPrompt());
            // 设置参考图片URL，使生成的图片与原图相关
            if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                setReferenceImageUrl(image.getImageUrl());
            }
            
            // 设置图片尺寸，保持与原图相同的大小
            if (image.getWidth() != null && image.getHeight() != null) {
                // 计算宽高比
                double ratio = (double) image.getWidth() / image.getHeight();
                String ratioStr;
                
                // 判断常见的宽高比
                if (Math.abs(ratio - 9.0/16.0) < 0.01) {
                    ratioStr = "9:16";
                } else if (Math.abs(ratio - 16.0/9.0) < 0.01) {
                    ratioStr = "16:9";
                } else if (Math.abs(ratio - 4.0/3.0) < 0.01) {
                    ratioStr = "4:3";
                } else if (Math.abs(ratio - 2.0/3.0) < 0.01) {
                    ratioStr = "2:3";
                } else if (Math.abs(ratio - 1.0) < 0.01) {
                    ratioStr = "1:1";
                } else {
                    // 如果不是预设的比例，使用最接近的比例，但保持原始尺寸
                    ratioStr = image.getWidth() + ":" + image.getHeight();
                }
                
                // 设置宽高比和具体尺寸
                setAspectRatio(ratioStr, image.getWidth(), image.getHeight());
                android.util.Log.d("VMDrawing", "Set aspect ratio from template: " + ratioStr + 
                    ", width: " + image.getWidth() + ", height: " + image.getHeight());
            }
            
            // TODO: 根据图片信息恢复风格选择
        }
    }
    
    // 开始轮询任务状态
    private void startPollingTaskStatus() {
        if (currentTaskId == null) {
            android.util.Log.e("VMDrawing", "Cannot start polling: currentTaskId is null");
            return;
        }
        
        android.util.Log.d("VMDrawing", "Starting polling for task ID: " + currentTaskId);
        
        Timer pollingTimer = new Timer();
        final int[] pollCount = {0}; // 轮询次数计数器
        final int maxPolls = 60; // 最多轮询60次（2分钟）
        
        pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                pollCount[0]++;
                android.util.Log.d("VMDrawing", "Polling attempt " + pollCount[0] + " for task ID: " + currentTaskId);
                
                // 超时检查
                if (pollCount[0] > maxPolls) {
                    android.util.Log.e("VMDrawing", "Polling timeout after " + maxPolls + " attempts");
                    pollingTimer.cancel();
                    stopProgressTimer();
                    
                    mainHandler.post(() -> {
                        isGenerating.set(false);
                        setError("图片生成超时，请重试");
                        clearReferenceImageUrl();
                    });
                    return;
                }
                
                // 在主线程中执行
                mainHandler.post(() -> {
                    DrawingImageDto queryDto = new DrawingImageDto();
                    queryDto.setId(currentTaskId);
                    // 设置会话ID用于查询
                    if (currentSessionId != null) {
                        queryDto.setSessionId(currentSessionId);
                    }
                    
                    repository.getImageDetail(queryDto).observeForever(result -> {
                        android.util.Log.d("VMDrawing", "Polling result received for task ID: " + currentTaskId);
                        
                        if (result.isSuccess() && result.getData() != null) {
                            DrawingImageDto image = result.getData();
                            Integer status = image.getStatus();
                            android.util.Log.d("VMDrawing", "Task status: " + status + 
                                ", imageUrl: " + image.getImageUrl());
                            
                            // 状态码：10=进行中, 20=已完成, 30=失败
                            if (status != null && status == 20) {
                                // 状态20表示已完成
                                android.util.Log.d("VMDrawing", "Task completed successfully");
                                android.util.Log.d("VMDrawing", "Image data - imageUrl: " + image.getImageUrl());
                                android.util.Log.d("VMDrawing", "Image data - sampleUrl: " + image.getSampleUrl());
                                android.util.Log.d("VMDrawing", "Image data - id: " + image.getId());
                                android.util.Log.d("VMDrawing", "Image data - prompt: " + image.getPrompt());
                                
                                pollingTimer.cancel();
                                stopProgressTimer();
                                
                                // 检查是否有图片URL（getImageUrl()方法会自动返回imageUrl或sampleUrl）
                                String finalImageUrl = image.getImageUrl();
                                if (finalImageUrl != null && !finalImageUrl.isEmpty()) {
                                    android.util.Log.d("VMDrawing", "Using image URL: " + finalImageUrl);
                                    generatedImage.postValue(image);
                                    isGenerating.set(false);
                                    progress.set(100);
                                    progressText.set("生成完成");
                                    showResult.postValue(true);
                                    setSuccess("图片生成成功");
                                } else {
                                    // 状态是完成但没有图片URL
                                    android.util.Log.e("VMDrawing", "No image URL found in completed task");
                                    isGenerating.set(false);
                                    setError("图片生成失败：未返回图片地址");
                                }
                                
                                // 清除参考图片URL
                                clearReferenceImageUrl();
                            } else if (status != null && status == 30) {
                                // 状态30表示失败
                                android.util.Log.e("VMDrawing", "Task failed");
                                pollingTimer.cancel();
                                stopProgressTimer();
                                
                                isGenerating.set(false);
                                String errorMsg = image.getErrorMsg() != null ? image.getErrorMsg() : "图片生成失败";
                                setError(errorMsg);
                                
                                // 生成失败也清除参考图片URL
                                clearReferenceImageUrl();
                            } else if (status != null && status == 10) {
                                // 状态10表示进行中，继续轮询
                                android.util.Log.d("VMDrawing", "Task still processing");
                                
                                // 更新进度显示（由于没有具体进度值，使用递增显示）
                                int currentProgress = progress.get();
                                if (currentProgress < 90) {
                                    // 缓慢增加进度，但不超过90%
                                    progress.set(currentProgress + 5);
                                    progressText.set("生成中 " + (currentProgress + 5) + "%");
                                }
                            } else {
                                // 未知状态
                                android.util.Log.w("VMDrawing", "Unknown task status: " + status);
                                progressText.set("正在生成中...");
                            }
                        } else {
                            android.util.Log.e("VMDrawing", "Polling failed: " + 
                                (result.getError() != null ? result.getError() : "No data returned"));
                            
                            // 如果查询失败次数过多，停止轮询
                            if (pollCount[0] > 10 && !result.isSuccess()) {
                                pollingTimer.cancel();
                                stopProgressTimer();
                                
                                isGenerating.set(false);
                                setError("查询图片状态失败，请重试");
                                clearReferenceImageUrl();
                            }
                        }
                    });
                });
            }
        }, 1000, 2000); // 1秒后开始，每2秒轮询一次
    }
    
    /**
     * 设置宽高比信息（从比例选择页面传入）
     */
    public void setAspectRatio(String ratio, int width, int height) {
        selectedRatio.set(ratio);
        
        // 更新比例列表中对应的选项
        List<AspectRatioDto> ratios = aspectRatios.getValue();
        if (ratios != null) {
            boolean found = false;
            for (AspectRatioDto aspectRatio : ratios) {
                if (aspectRatio.getRatio().equals(ratio)) {
                    aspectRatio.setWidth(width);
                    aspectRatio.setHeight(height);
                    found = true;
                    break;
                }
            }
            
            // 如果不是预设的比例，添加一个自定义比例
            if (!found) {
                AspectRatioDto customRatio = new AspectRatioDto(ratio, ratio, width, height, false);
                ratios.add(customRatio);
                aspectRatios.postValue(ratios);
                android.util.Log.d("VMDrawing", "Added custom aspect ratio: " + ratio + " (" + width + "x" + height + ")");
            }
        }
    }
    
    /**
     * 设置初始风格（从画廊页面传入）
     */
    public void setInitialStyle(String styleName) {
        this.initialStyle = styleName;
    }
    
    /**
     * 设置参考图片URL（用于做同款功能）
     */
    public void setReferenceImageUrl(String imageUrl) {
        this.referenceImageUrl = imageUrl;
        android.util.Log.d("VMDrawing", "Reference image URL set: " + imageUrl);
    }
    
    /**
     * 清除参考图片URL
     */
    public void clearReferenceImageUrl() {
        this.referenceImageUrl = null;
    }
    
    /**
     * 设置隐藏的prompt（用于继续编辑模式）
     */
    public void setHiddenPrompt(String prompt) {
        this.hiddenPrompt = prompt;
        android.util.Log.d("VMDrawing", "Hidden prompt set: " + prompt);
    }
    
    /**
     * 获取隐藏的prompt
     */
    public String getHiddenPrompt() {
        return hiddenPrompt;
    }
    
    /**
     * 设置是否为继续编辑模式
     */
    public void setContinueEditMode(boolean isContinueEditMode) {
        this.isContinueEditMode = isContinueEditMode;
    }

    /**
     * 获取是否为继续编辑模式
     */
    public boolean isContinueEditMode() {
        return isContinueEditMode;
    }
    
    // 创建新会话
    private void createNewSession(String sessionName) {
        // 如果已经在创建会话，则不重复创建
        if (isCreatingSession) {
            android.util.Log.d("VMDrawing", "Session creation already in progress");
            return;
        }
        
        // 如果已经有会话ID，则不需要创建
        if (currentSessionId != null) {
            android.util.Log.d("VMDrawing", "Session already exists with ID: " + currentSessionId);
            return;
        }
        
        isCreatingSession = true;
        android.util.Log.d("VMDrawing", "Creating new image session with name: " + sessionName);
        
        repository.createImageSession(sessionName).observeForever(result -> {
            isCreatingSession = false;
            
            if (result.isSuccess() && result.getData() != null) {
                currentSessionId = result.getData();
                android.util.Log.d("VMDrawing", "New session created with ID: " + currentSessionId);
                
                // 创建会话DTO
                DrawingSessionDto session = new DrawingSessionDto();
                session.setId(currentSessionId);
                session.setName("绘画会话 " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
                currentSession.postValue(session);
                
                // 会话创建成功，清除错误信息
                clearError();
                
                // 如果有待处理的生成请求，自动触发生成
                if (pendingGeneration) {
                    pendingGeneration = false;
                    android.util.Log.d("VMDrawing", "Session created, auto-triggering pending image generation");
                    generateImage();
                }
            } else {
                android.util.Log.e("VMDrawing", "Failed to create session: " + 
                    (result.getError() != null ? result.getError() : "Unknown error"));
                setError("创建会话失败，请重试");
            }
        });
    }
    
    // 加载绘画示例
    public void loadSamples(Long categoryId) {
        SampleListRequest request = new SampleListRequest();
        request.setCatId(categoryId);
        request.setPageNo(1);
        request.setPageSize(20);
        
        repository.getSampleList(request).observeForever(result -> {
            if (result.isSuccess() && result.getData() != null) {
                PageResult<DrawingSampleDto> pageResult = result.getData();
                if (pageResult.getRecords() != null) {
                    samples.postValue(pageResult.getRecords());
                }
            } else {
                setError(result.getError() != null ? result.getError() : "获取示例失败");
            }
        });
    }
    
    // 使用示例作为模板
    public void useSampleAsTemplate(DrawingSampleDto sample) {
        if (sample != null) {
            prompt.set(sample.getPrompt());
            // 设置对应的风格
            if (sample.getStyleId() != null) {
                List<DrawingStyleDto> styleList = styles.getValue();
                if (styleList != null) {
                    for (DrawingStyleDto style : styleList) {
                        if (style.getId().equals(sample.getStyleId())) {
                            setSelectedStyle(style);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    // 更新会话名称
    public void updateSessionName(String name) {
        DrawingSessionDto session = currentSession.getValue();
        if (session != null) {
            session.setName(name);
            repository.updateSession(session).observeForever(result -> {
                if (result.isSuccess()) {
                    // 更新成功
                } else {
                    setError("更新会话失败");
                }
            });
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        stopProgressTimer();
        prompt.removeObserver(this::validateForm);
        selectedRatio.removeObserver(this::validateForm);
    }
}
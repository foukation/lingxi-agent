package com.fxzs.lingxiagent.viewmodel.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.chat.dto.ConversationHistoryDto;
import com.fxzs.lingxiagent.model.chat.dto.ConversationHistoryListDto;
import com.fxzs.lingxiagent.model.chat.repository.ChatRepository;
import com.fxzs.lingxiagent.model.chat.repository.ChatRepositoryImpl;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepository;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepositoryImpl;
import com.fxzs.lingxiagent.model.meeting.dto.MeetingHistoryDto;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepository;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepositoryImpl;

import com.fxzs.lingxiagent.network.ZNet.ApiResponse;
import com.fxzs.lingxiagent.network.ZNet.HttpRequest;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.view.common.BaseViewModel;
import com.fxzs.lingxiagent.view.user.HistoryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 历史记录ViewModel
 * 管理所有类型的历史记录数据
 */
public class VMHistory extends BaseViewModel {
    
    // 历史记录类型常量
    public static final int TAB_CHAT = 0;
    public static final int TAB_AGENT = 1;
    public static final int TAB_DRAWING = 2;
    public static final int TAB_MEETING = 3;
    public static final int TAB_PPT = 4;
    public static final int TAB_TRANSLATE = 5;
    
    // Repository
    private final ChatRepository chatRepository;
    private final DrawingRepository drawingRepository;
    private final MeetingRepository meetingRepository;
    
    // LiveData
    private final MutableLiveData<Integer> currentTabIndex = new MutableLiveData<>(TAB_CHAT);
    private final MutableLiveData<List<HistoryItem>> historyItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);
    
    // 分页参数
    private static final int PAGE_SIZE = 20;
    private int currentPage = 1;
    private boolean hasMoreData = true;
    
    public VMHistory() {
        chatRepository = new ChatRepositoryImpl();
        drawingRepository = DrawingRepositoryImpl.getInstance();
        meetingRepository = new MeetingRepositoryImpl();
        // 不在构造函数中自动加载数据，等待Fragment调用selectTab时再加载
    }
    
    // Getters
    public LiveData<Integer> getCurrentTabIndex() {
        return currentTabIndex;
    }
    
    public LiveData<List<HistoryItem>> getHistoryItems() {
        return historyItems;
    }
    
    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }
    
    public boolean hasMoreData() {
        return hasMoreData;
    }
    
    /**
     * 切换Tab
     */
    public void selectTab(int tabIndex) {
        android.util.Log.d("VMHistory", "selectTab: " + tabIndex);

        // 检查是否是相同Tab且已经有数据
        if (currentTabIndex.getValue() != null && currentTabIndex.getValue() == tabIndex) {
            List<HistoryItem> currentItems = historyItems.getValue();
            if (currentItems != null && !currentItems.isEmpty()) {
                android.util.Log.d("VMHistory", "相同Tab且已有数据，不重复加载");
                return; // 相同Tab且已有数据不重复加载
            }
        }

        currentTabIndex.setValue(tabIndex);
        refreshHistory();
    }
    
    /**
     * 刷新历史记录
     */
    public void refreshHistory() {
        currentPage = 1;
        hasMoreData = true;
        loadHistory(true);
    }
    
    /**
     * 加载更多历史记录
     */
    public void loadMoreHistory() {
        if (!hasMoreData || Boolean.TRUE.equals(isLoading.getValue())) {
            return;
        }
        
        // 显示加载指示器
        showLoadingIndicator();
        loadHistory(false);
    }
    
    /**
     * 加载历史记录
     */
    private void loadHistory(boolean isRefresh) {
        Integer tabIndex = currentTabIndex.getValue();
        if (tabIndex == null) return;
        
        if (isRefresh) {
            isRefreshing.setValue(true);
        } else {
            setLoading(true);
        }
        
        android.util.Log.d("VMHistory", "loadHistory for tab: " + tabIndex + ", isRefresh: " + isRefresh);

        switch (tabIndex) {
            case TAB_CHAT:
                android.util.Log.d("VMHistory", "加载对话历史");
                loadChatHistory(8, isRefresh); // modelType = 8 表示对话模型
                break;
            case TAB_AGENT:
                android.util.Log.d("VMHistory", "加载智能体历史");
                loadChatHistory(1, isRefresh); // modelType = 1 表示智能体模型
                break;
            case TAB_DRAWING:
                android.util.Log.d("VMHistory", "加载绘画历史");
                loadDrawingHistory(isRefresh);
                break;
            case TAB_MEETING:
                android.util.Log.d("VMHistory", "加载会议历史");
                loadMeetingHistory(isRefresh);
                break;
            case TAB_PPT:
            case TAB_TRANSLATE:
                android.util.Log.d("VMHistory", "加载PPT/翻译历史（暂未实现）");
                // TODO: 实现PPT和翻译历史记录
                handleEmptyResult(isRefresh);
                break;
            default:
                android.util.Log.w("VMHistory", "未知的Tab类型: " + tabIndex);
                handleEmptyResult(isRefresh);
                break;
        }
    }
    
    /**
     * 加载对话历史记录
     */
    private void loadChatHistory(int modelType, boolean isRefresh) {
        Map<String, Object> params = createBaseParams();
        
        chatRepository.getConversationHistoryList(modelType, params, new ChatRepository.Callback<ConversationHistoryListDto>() {
            @Override
            public void onSuccess(ConversationHistoryListDto data) {
                List<HistoryItem> items = convertConversationToHistoryItems(data.getList());
                handleSuccessResult(items, isRefresh);
            }
            
            @Override
            public void onError(String error) {
                handleErrorResult(error, isRefresh);
            }
        });
    }
    
    /**
     * 加载绘画历史记录
     */
    public void loadDrawingHistory(boolean isRefresh) {
        try {
            Map<String, Object> params = createBaseParams();

            drawingRepository.getSessionList(params).observeForever(result -> {
                try {
                    if (result != null && result.isSuccess() && result.getData() != null) {
                        List<DrawingImageDto> drawingImages = result.getData().getRecords();
                        if (drawingImages != null) {
                            List<HistoryItem> items = convertDrawingToHistoryItems(drawingImages);
                            handleSuccessResult(items, isRefresh);
                        } else {
                            handleSuccessResult(new ArrayList<>(), isRefresh);
                        }
                    } else {
                        String errorMsg = result != null ? result.getError() : "加载绘画历史失败";
                        handleErrorResult(errorMsg, isRefresh);
                    }
                } catch (Exception e) {
                    handleErrorResult("处理绘画历史数据时出错: " + e.getMessage(), isRefresh);
                }
            });
        } catch (Exception e) {
            handleErrorResult("加载绘画历史时出错: " + e.getMessage(), isRefresh);
        }
    }
    
    /**
     * 加载会议历史记录
     */
    public void loadMeetingHistory(boolean isRefresh) {
        Map<String, Object> params = createBaseParams();
        params.put("sort", 1);
        params.put("keyword", "");

        meetingRepository.getMeetingHistoryList(params).observeForever(result -> {
            if (result != null && result.isSuccess() && result.getData() != null) {
                List<HistoryItem> items = convertMeetingToHistoryItems(result.getData().getList());
                handleSuccessResult(items, isRefresh);
            } else {
                handleErrorResult(result != null ? result.getError() : "加载会议历史失败", isRefresh);
            }
        });
    }
    
    /**
     * 创建基础请求参数
     */
    private Map<String, Object> createBaseParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", currentPage);
        params.put("pageSize", PAGE_SIZE);
        
        long userId = SharedPreferencesUtil.getUserId();
        if (userId > 0) {
            params.put("userId", userId);
        }
        
        return params;
    }
    
    /**
     * 处理成功结果
     */
    private void handleSuccessResult(List<HistoryItem> newItems, boolean isRefresh) {
        List<HistoryItem> currentItems = historyItems.getValue();
        if (currentItems == null) {
            currentItems = new ArrayList<>();
        }
        
        // 移除加载指示器（如果存在）
        removeLoadingIndicator(currentItems);
        
        if (isRefresh) {
            currentItems.clear();
            currentPage = 1;
        }
        
        // 合并新数据，避免重复的日期头
        if (!isRefresh && !currentItems.isEmpty() && !newItems.isEmpty()) {
            // 如果当前列表最后一项是日期头，且新数据第一项也是相同的日期头，则移除新数据的日期头
            HistoryItem lastCurrentItem = currentItems.get(currentItems.size() - 1);
            HistoryItem firstNewItem = newItems.get(0);
            
            if (lastCurrentItem.getType() == HistoryItem.TYPE_DATE_HEADER && 
                firstNewItem.getType() == HistoryItem.TYPE_DATE_HEADER &&
                lastCurrentItem.getTitle().equals(firstNewItem.getTitle())) {
                newItems.remove(0);
            }
        }
        
        currentItems.addAll(newItems);
        
        // 更新分页状态 - 根据实际返回的数据项数量判断是否还有更多数据
        int actualDataCount = 0;
        for (HistoryItem item : newItems) {
            if (item.getType() == HistoryItem.TYPE_ITEM) {
                actualDataCount++;
            }
        }
        hasMoreData = actualDataCount >= PAGE_SIZE;
        
        if (!isRefresh) {
            currentPage++;
        }
        
        android.util.Log.d("VMHistory", "处理成功结果 - 新增数据项: " + actualDataCount + 
                          ", 是否还有更多: " + hasMoreData + ", 当前页: " + currentPage);
        
        historyItems.setValue(currentItems);
        setLoading(false);
        isRefreshing.setValue(false);
    }
    
    /**
     * 处理错误结果
     */
    private void handleErrorResult(String error, boolean isRefresh) {
        // 移除加载指示器（如果存在）
        if (!isRefresh) {
            List<HistoryItem> currentItems = historyItems.getValue();
            if (currentItems != null) {
                removeLoadingIndicator(currentItems);
                historyItems.setValue(currentItems);
            }
        }
        
        setError(error);
        setLoading(false);
        isRefreshing.setValue(false);
    }
    
    /**
     * 处理空结果
     */
    private void handleEmptyResult(boolean isRefresh) {
        if (isRefresh) {
            historyItems.setValue(new ArrayList<>());
        }
        setLoading(false);
        isRefreshing.setValue(false);
    }
    
    // 数据转换方法（从原Fragment中迁移）
    private List<HistoryItem> convertConversationToHistoryItems(List<ConversationHistoryDto> conversations) {
        List<HistoryItem> items = new ArrayList<>();
        if (conversations == null || conversations.isEmpty()) {
            return items;
        }

        String lastDate = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日", Locale.getDefault());

        for (ConversationHistoryDto conversation : conversations) {
            // 添加日期分组头
            String date = dateFormat.format(new Date(conversation.getCreateTime()));
            if (!date.equals(lastDate)) {
                items.add(new HistoryItem(HistoryItem.TYPE_DATE_HEADER, date, null, 0));
                lastDate = date;
            }

            // 构建显示标题
            String title = conversation.getTitle();
            if (title == null || title.trim().isEmpty()) {
                title = "对话记录";
            }

            // 获取头像信息
            String avatarUrl = conversation.getIconUrl();
            HistoryItem historyItem;

            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                // 使用网络头像URL
                historyItem = new HistoryItem(HistoryItem.TYPE_ITEM, title, "", avatarUrl);
            } else {
                // 使用默认头像
                historyItem = new HistoryItem(HistoryItem.TYPE_ITEM, title, "", com.fxzs.lingxiagent.R.drawable.ic_app_logo);
            }

            historyItem.setConversationId(conversation.getId());
            historyItem.setModelType(conversation.getModelType());
            historyItem.setModelId(conversation.getModelId());
            historyItem.setModelName(conversation.getModelName());
            historyItem.setModel(conversation.getModel());
            items.add(historyItem);
        }

        return items;
    }

    private List<HistoryItem> convertDrawingToHistoryItems(List<DrawingImageDto> drawingImages) {
        List<HistoryItem> items = new ArrayList<>();
        if (drawingImages == null || drawingImages.isEmpty()) {
            return items;
        }

        String lastDate = "";

        try {
            for (DrawingImageDto image : drawingImages) {
                if (image == null) continue;

                // 添加日期分组头
                String date = getDateFromDrawingTime(image.getCreateTime());
                if (!date.equals(lastDate)) {
                    items.add(new HistoryItem(HistoryItem.TYPE_DATE_HEADER, date, null, 0));
                    lastDate = date;
                }

                // 构建显示标题
                String title = image.getPrompt();
                if (title == null || title.trim().isEmpty()) {
                    title = "AI绘画";
                }
                if (title.length() > 20) {
                    title = title.substring(0, 20) + "...";
                }

                // 创建HistoryItem，使用图片URL构造函数
                String imageUrl = null;
                if (image.getThumbnailUrl() != null && !image.getThumbnailUrl().trim().isEmpty()) {
                    imageUrl = image.getThumbnailUrl();
                } else if (image.getImageUrl() != null && !image.getImageUrl().trim().isEmpty()) {
                    imageUrl = image.getImageUrl();
                }

                HistoryItem historyItem = new HistoryItem(HistoryItem.TYPE_ITEM, title, "", imageUrl);
                historyItem.setSessionId(image.getSessionId());
                items.add(historyItem);
            }
        } catch (Exception e) {
            // 如果转换过程中出错，返回已转换的部分
            android.util.Log.e("VMHistory", "转换绘画历史数据时出错: " + e.getMessage());
        }

        return items;
    }

    /**
     * 从绘画时间字符串中提取日期
     */
    private String getDateFromDrawingTime(String timeStr) {
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM月dd日", Locale.getDefault());

        if (timeStr == null || timeStr.trim().isEmpty()) {
            return outputFormat.format(new Date());
        }

        try {
            // 尝试解析不同的时间格式
            Date date = null;

            if (timeStr.contains("-") && timeStr.contains(":")) {
                // 格式：2024-01-07 16:00:00 或 2024-01-07T16:00:00
                String cleanTimeStr = timeStr.replace("T", " ");
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                date = inputFormat.parse(cleanTimeStr);
            } else if (timeStr.contains("-") && !timeStr.contains(":")) {
                // 格式：2024-01-07
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                date = inputFormat.parse(timeStr);
            } else if (timeStr.matches("\\d{13}")) {
                // 13位时间戳（毫秒）
                date = new Date(Long.parseLong(timeStr));
            } else if (timeStr.matches("\\d{10}")) {
                // 10位时间戳（秒）
                date = new Date(Long.parseLong(timeStr) * 1000);
            } else {
                // 其他格式，使用当前时间
                android.util.Log.w("VMHistory", "无法解析时间格式: " + timeStr);
                date = new Date();
            }

            return outputFormat.format(date);

        } catch (Exception e) {
            // 解析失败，使用当前日期
            android.util.Log.e("VMHistory", "解析时间失败: " + timeStr + ", 错误: " + e.getMessage());
            return outputFormat.format(new Date());
        }
    }

    private List<HistoryItem> convertMeetingToHistoryItems(List<MeetingHistoryDto> meetings) {
        List<HistoryItem> items = new ArrayList<>();
        if (meetings == null || meetings.isEmpty()) {
            return items;
        }

        String lastDate = "";

        for (MeetingHistoryDto meeting : meetings) {
            // 使用会议名称中的时间戳或当前时间作为日期
            String date = getDateFromMeetingName(meeting.getName());
            if (!date.equals(lastDate)) {
                items.add(new HistoryItem(HistoryItem.TYPE_DATE_HEADER, date, null, 0));
                lastDate = date;
            }

            // 构建显示标题
            String title = meeting.getName();
            if (title == null || title.trim().isEmpty()) {
                title = "会议记录";
            }

            // 创建HistoryItem
            HistoryItem historyItem = new HistoryItem(HistoryItem.TYPE_ITEM, title, "", com.fxzs.lingxiagent.R.drawable.ic_nav_meeting);
            historyItem.setMeetingId(meeting.getId());
            historyItem.setMeetingType(meeting.getType());
            items.add(historyItem);
        }

        return items;
    }

    /**
     * 从会议名称中提取日期
     */
    private String getDateFromMeetingName(String meetingName) {
        if (meetingName == null) {
            return new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(new Date());
        }

        // 尝试从会议名称中提取时间戳
        try {
            // 假设会议名称格式包含时间戳，如 "会议_20231201_143000"
            if (meetingName.contains("_")) {
                String[] parts = meetingName.split("_");
                for (String part : parts) {
                    if (part.length() == 8 && part.matches("\\d{8}")) {
                        // 解析日期格式 YYYYMMDD
                        String year = part.substring(0, 4);
                        String month = part.substring(4, 6);
                        String day = part.substring(6, 8);
                        return month + "月" + day + "日";
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败，使用当前日期
        }

        return new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(new Date());
    }

    public void updateName(HistoryItem item,String inputText) {
        int currentTab = currentTabIndex.getValue();
        String id = "";
        if (currentTab == VMHistory.TAB_CHAT && item.getConversationId() != null) {
            // 对话tab，跳转到对话页面
            id = item.getConversationId()+"";
            int modelType = item.getModelType();
            long modeId = item.getModelId();
        } else if (currentTab == VMHistory.TAB_AGENT && item.getConversationId() != null) {
            // 智能体tab，跳转到对话页面
            id = item.getConversationId()+"";
            int modelType = item.getModelType();
            long modeId = item.getModelId();
        } else if (currentTab == VMHistory.TAB_DRAWING && item.getSessionId() != null) {
            // AI绘画tab，调用详情接口并跳转
            id = item.getSessionId()+"";
        } else if (currentTab == VMHistory.TAB_MEETING && item.getMeetingId() != null) {
            // 会议tab，跳转到会议详情页面
            id = item.getMeetingId()+"";
        }
        if(currentTab == VMHistory.TAB_DRAWING){//绘画
            updateSessionName(id,inputText);
        }else  if(currentTab == VMHistory.TAB_MEETING){//会议
            updateMeetingName(id,inputText);
        }else {
            updateConversationName(id,inputText);
        }
    }

    // 更新会话名称
    public void updateConversationName(String id,String name) {

        HttpRequest request = new HttpRequest();
        request.updateMyEditName(id, name, new Observer<ApiResponse<Boolean>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ApiResponse<Boolean> booleanApiResponse) {

//                    GlobalToast.show(getActivity(),"删除成功",GlobalToast.Type.SUCCESS);
                setMessage("更新成功");
                refreshHistory();
            }


            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
    // 更新会话名称
    public void updateSessionName(String id,String name) {
        DrawingRepository repository = DrawingRepositoryImpl.getInstance();
        DrawingSessionDto session = new DrawingSessionDto();
            session.setId(Long.valueOf(id));
            session.setName(name);
            repository.updateSession(session).observeForever(result -> {
                if (result.isSuccess()) {
                    // 更新成功
                    setMessage("更新成功");
                    refreshHistory();
                } else {
                    setError("更新会话失败");
                }
            });

    }
    // 更新会议
    public void updateMeetingName(String id,String name) {
        MeetingRepository repository = new MeetingRepositoryImpl();
        repository.updateMeetingName(Integer.valueOf(id), name)
                .observeForever(updateResult -> {
                    if (updateResult != null) {
                        if (updateResult.isSuccess()) {
                            setMessage("更新成功");
                            refreshHistory();
                        } else {
                        }
                    }
                });

    }
    
    /**
     * 显示加载指示器
     */
    private void showLoadingIndicator() {
        List<HistoryItem> currentItems = historyItems.getValue();
        if (currentItems == null) {
            currentItems = new ArrayList<>();
        }
        
        // 检查是否已经有加载指示器
        if (!currentItems.isEmpty() && 
            currentItems.get(currentItems.size() - 1).getType() == HistoryItem.TYPE_LOADING) {
            return; // 已经有加载指示器，不重复添加
        }
        
        // 添加加载指示器
        HistoryItem loadingItem = new HistoryItem(HistoryItem.TYPE_LOADING, "", "", 0);
        currentItems.add(loadingItem);
        historyItems.setValue(currentItems);
    }
    
    /**
     * 移除加载指示器
     */
    private void removeLoadingIndicator(List<HistoryItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        // 从列表末尾开始查找并移除加载指示器
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i).getType() == HistoryItem.TYPE_LOADING) {
                items.remove(i);
                break; // 只移除一个加载指示器
            }
        }
    }
}

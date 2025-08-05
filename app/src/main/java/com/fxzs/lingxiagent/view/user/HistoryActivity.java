package com.fxzs.lingxiagent.view.user;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.model.drawing.api.PageResult;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.model.meeting.dto.MeetingDto;
import com.fxzs.lingxiagent.view.chat.SuperChatActivity;
import com.fxzs.lingxiagent.view.chat.SuperChatContainActivity;
import com.fxzs.lingxiagent.view.common.LoadingProgressDialog;
import com.fxzs.lingxiagent.view.meeting.MeetingActivity;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepository;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepositoryImpl;
import com.fxzs.lingxiagent.model.meeting.dto.MeetingHistoryDto;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepository;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepositoryImpl;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    private View viewGradientBg;
    private View viewWhiteBg;
    private View viewHandle;
    private TextView tvTitle;
    private ImageView ivClose;
    private HorizontalScrollView scrollViewTabs;
    private LinearLayout llTabs;
    private TextView tvTabChat;
    private TextView tvTabAgent;
    private TextView tvTabDrawing;
    private TextView tvTabMeeting;
    private TextView tvTabPPT;
    private TextView tvTabTranslate;
    private View viewTabIndicator;
    private RecyclerView rvHistory;
    
    private HistoryAdapter historyAdapter;
    private int currentTabIndex = 1; // Default to "智能体" tab
    
    // Repository for drawing API
    private DrawingRepository drawingRepository;

    // Repository for meeting API
    private MeetingRepository meetingRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        // Initialize repositories
        drawingRepository = DrawingRepositoryImpl.getInstance();
        meetingRepository = new MeetingRepositoryImpl();
        
        initViews();
        setupListeners();
        setupRecyclerView();
        loadHistoryData();
    }
    
    private void initViews() {
        viewGradientBg = findViewById(R.id.viewGradientBg);
        viewWhiteBg = findViewById(R.id.viewWhiteBg);
        viewHandle = findViewById(R.id.viewHandle);
        tvTitle = findViewById(R.id.tvTitle);
        ivClose = findViewById(R.id.ivClose);
        scrollViewTabs = findViewById(R.id.scrollViewTabs);
        llTabs = findViewById(R.id.llTabs);
        tvTabChat = findViewById(R.id.tvTabChat);
        tvTabAgent = findViewById(R.id.tvTabAgent);
        tvTabDrawing = findViewById(R.id.tvTabDrawing);
        tvTabMeeting = findViewById(R.id.tvTabMeeting);
        tvTabPPT = findViewById(R.id.tvTabPPT);
        tvTabTranslate = findViewById(R.id.tvTabTranslate);
        viewTabIndicator = findViewById(R.id.viewTabIndicator);
        rvHistory = findViewById(R.id.rvHistory);
    }
    
    private void setupListeners() {
        ivClose.setOnClickListener(v -> finish());
        
        tvTabChat.setOnClickListener(v -> selectTab(0));
        tvTabAgent.setOnClickListener(v -> selectTab(1));
        tvTabDrawing.setOnClickListener(v -> selectTab(2));
        tvTabMeeting.setOnClickListener(v -> selectTab(3));
        tvTabPPT.setOnClickListener(v -> selectTab(4));
        tvTabTranslate.setOnClickListener(v -> selectTab(5));
    }
    
    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
        
        // 设置点击监听器
        historyAdapter.setOnItemClickListener(item -> {
            if (currentTabIndex == 2 && item.getSessionId() != null) {
                // AI绘画tab，调用详情接口并跳转
                loadDrawingSessionDetail(item.getSessionId());
            } else if (currentTabIndex == 3 && item.getMeetingId() != null) {
                // 会议tab，跳转到会议详情页面
                jumpToMeetingDetail(item.getMeetingId(), item.getMeetingType());
            }
        });

        // 设置更多操作监听器
        historyAdapter.setOnMoreActionListener((anchor, item, actionType) -> {
            if (actionType == 0) { // 查看详情
                handleViewDetail(item);
            } else if (actionType == 1) { // 重命名
                // 弹出输入框，确认后重命名
                android.widget.EditText editText = new android.widget.EditText(this);
                editText.setText(item.getTitle());
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("重命名")
                    .setView(editText)
                    .setPositiveButton("确定", (dialog, which) -> {
                        String newName = editText.getText().toString().trim();
                        // TODO: 调用重命名接口
                        android.widget.Toast.makeText(this, "重命名为: " + newName, android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            } else if (actionType == 2) { // 删除
                // 弹出确认对话框
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("删除确认")
                    .setMessage("确定要删除这条记录吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // TODO: 调用删除接口
                        android.widget.Toast.makeText(this, "已删除", android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            }
        });
    }

    /**
     * 处理查看详情操作
     */
    private void handleViewDetail(HistoryItem item) {
        if (currentTabIndex == 2 && item.getSessionId() != null) {
            // AI绘画tab，调用详情接口并跳转
            loadDrawingSessionDetail(item.getSessionId());
        } else if (currentTabIndex == 3 && item.getMeetingId() != null) {
            // 会议tab，跳转到会议详情页面
            jumpToMeetingDetail(item.getMeetingId(), item.getMeetingType());
        } else {
            android.widget.Toast.makeText(this, "暂不支持查看此类型详情", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void selectTab(int index) {
        currentTabIndex = index;
        
        // Reset all tabs to normal state
        tvTabChat.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvTabChat.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabAgent.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvTabAgent.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabDrawing.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvTabDrawing.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabMeeting.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvTabMeeting.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabPPT.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvTabPPT.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvTabTranslate.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        tvTabTranslate.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        // Set selected tab
        TextView selectedTab = null;
        switch (index) {
            case 0:
                selectedTab = tvTabChat;
                break;
            case 1:
                selectedTab = tvTabAgent;
                break;
            case 2:
                selectedTab = tvTabDrawing;
                break;
            case 3:
                selectedTab = tvTabMeeting;
                break;
            case 4:
                selectedTab = tvTabPPT;
                break;
            case 5:
                selectedTab = tvTabTranslate;
                break;
        }
        
        if (selectedTab != null) {
            selectedTab.setTextColor(ContextCompat.getColor(this, R.color.text_black));
            selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);
            
            // First, remove indicator from its current parent (if any)
            if (viewTabIndicator.getParent() != null) {
                ((ViewGroup) viewTabIndicator.getParent()).removeView(viewTabIndicator);
            }
            
            // Move indicator
            ViewGroup parent = (ViewGroup) selectedTab.getParent();
            if (parent instanceof FrameLayout) {
                // Tab is already wrapped in FrameLayout
                parent.addView(viewTabIndicator);
            } else if (parent instanceof LinearLayout) {
                // Tab is directly in LinearLayout, need to wrap it
                LinearLayout linearParent = (LinearLayout) parent;
                int tabIndex = linearParent.indexOfChild(selectedTab);
                
                // Remove the TextView from LinearLayout
                linearParent.removeView(selectedTab);
                
                // Create a new FrameLayout
                FrameLayout frameLayout = new FrameLayout(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                if (tabIndex > 0) {
                    layoutParams.setMarginStart((int) (8 * getResources().getDisplayMetrics().density));
                }
                frameLayout.setLayoutParams(layoutParams);
                
                // Add TextView and indicator to FrameLayout
                frameLayout.addView(selectedTab);
                frameLayout.addView(viewTabIndicator);
                
                // Add FrameLayout back to LinearLayout at the same position
                linearParent.addView(frameLayout, tabIndex);
            }
        }
        
        // Load data for selected tab
        loadHistoryData();
    }
    
    private void loadHistoryData() {
        List<HistoryItem> items = new ArrayList<>();
        
        if (currentTabIndex == 1) { // Agent tab
            items.add(new HistoryItem(HistoryItem.TYPE_DATE_HEADER, "2025年04月16日", null, 0));
            items.add(new HistoryItem(HistoryItem.TYPE_ITEM, "瘦身小天使", null, R.drawable.ic_avatar_fitness));
            items.add(new HistoryItem(HistoryItem.TYPE_DATE_HEADER, "2025年04月15日", null, 0));
            items.add(new HistoryItem(HistoryItem.TYPE_ITEM, "千变女友", null, R.drawable.ic_avatar_girlfriend));
            items.add(new HistoryItem(HistoryItem.TYPE_ITEM, "AI 写作", null, R.drawable.ic_avatar_ai_writer));
            historyAdapter.setIsDrawingTab(false);
            historyAdapter.setItems(items);
        } else if (currentTabIndex == 2) { // AI绘画 tab
            // 设置为绘画tab模式
            historyAdapter.setIsDrawingTab(true);
            // 调用API获取绘画历史
            loadDrawingHistory();
        } else if (currentTabIndex == 3) { // 会议 tab
            // 设置为普通tab模式
            historyAdapter.setIsDrawingTab(false);
            // 调用API获取会议历史
            loadMeetingHistory();
        } else {
            // 其他tab暂时显示空数据
            historyAdapter.setIsDrawingTab(false);
            historyAdapter.setItems(items);
        }
    }
    
    private void loadDrawingHistory() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 20);
        
        // 添加用户ID参数
        long userId = SharedPreferencesUtil.getUserId();
        if (userId > 0) {
            params.put("userId", userId);
        }
        
        // 添加模型类型参数，用于筛选绘画相关的对话
        params.put("modelType", 8);  // 8 表示绘画模型类型
        
        drawingRepository.getSessionList(params).observeForever(result -> {
            if (result.isSuccess() && result.getData() != null) {
                PageResult<DrawingImageDto> pageResult = result.getData();
                List<DrawingImageDto> drawingImages = pageResult.getRecords();
                
                // 转换为HistoryItem并按日期分组
                List<HistoryItem> items = convertDrawingToHistoryItems(drawingImages);
                historyAdapter.setItems(items);
            } else {
                // 加载失败，显示空列表
                historyAdapter.setItems(new ArrayList<>());
            }
        });
    }
    
    private List<HistoryItem> convertDrawingToHistoryItems(List<DrawingImageDto> drawingImages) {
        List<HistoryItem> items = new ArrayList<>();
        String lastDate = "";
        
        for (DrawingImageDto image : drawingImages) {
            String date = getDateFromTime(image.getCreateTime());
            if (!date.equals(lastDate)) {
                // 添加日期分组头
                items.add(new HistoryItem(HistoryItem.TYPE_DATE_HEADER, date, null, 0));
                lastDate = date;
            }
            
            // 添加图片项，使用提示词作为标题
            String prompt = image.getPrompt();
            if (prompt != null && prompt.length() > 30) {
                prompt = prompt.substring(0, 30) + "...";
            }
            String imageUrl = image.getThumbnailUrl() != null ? image.getThumbnailUrl() : image.getImageUrl();
            HistoryItem historyItem = new HistoryItem(HistoryItem.TYPE_ITEM, prompt, null, imageUrl);
            historyItem.setSessionId(image.getSessionId());
            items.add(historyItem);
        }
        
        return items;
    }

    /**
     * 加载会议历史记录
     */
    private void loadMeetingHistory() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 20);
        params.put("type", 3);  // 会议类型
        params.put("sort", 1);  // 排序方式
        params.put("keyword", "");  // 关键词搜索

        // 添加用户ID参数
        long userId = SharedPreferencesUtil.getUserId();
        if (userId > 0) {
            params.put("userId", userId);
        }

        android.util.Log.d("HistoryActivity", "开始加载会议历史，用户ID: " + userId);

        meetingRepository.getMeetingHistoryList(params).observeForever(result -> {
            if (result != null) {
                if (result.isSuccess() && result.getData() != null) {
                    android.util.Log.d("HistoryActivity", "会议历史加载成功，数量: " +
                        (result.getData().getList() != null ? result.getData().getList().size() : 0));

                    // 转换为HistoryItem并按日期分组
                    List<HistoryItem> items = convertMeetingToHistoryItems(result.getData().getList());
                    historyAdapter.setItems(items);
                } else {
                    android.util.Log.e("HistoryActivity", "会议历史加载失败: " + result.getError());

                    // 加载失败，显示空列表
                    historyAdapter.setItems(new ArrayList<>());

                    // 可选：显示错误提示
                    android.widget.Toast.makeText(this,
                        "加载会议历史失败: " + result.getError(),
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 将会议数据转换为HistoryItem
     */
    private List<HistoryItem> convertMeetingToHistoryItems(List<MeetingHistoryDto> meetings) {
        List<HistoryItem> items = new ArrayList<>();
        String lastDate = "";

        for (MeetingHistoryDto meeting : meetings) {
            String date = getDateFromTime(meeting.getCreateTime());
            if (!date.equals(lastDate)) {
                // 添加日期分组头
                items.add(new HistoryItem(HistoryItem.TYPE_DATE_HEADER, date, null, 0));
                lastDate = date;
            }

            // 添加会议项
            String title = meeting.getName();
            if (title == null || title.trim().isEmpty()) {
                title = "会议记录";
            }

            HistoryItem historyItem = new HistoryItem(HistoryItem.TYPE_ITEM, title, null, R.drawable.ic_nav_meeting);
            historyItem.setMeetingId(meeting.getId());
            historyItem.setMeetingType(meeting.getType());
            items.add(historyItem);
        }

        return items;
    }
    
    private String getDateFromTime(String timeStr) {
        if (timeStr == null) return "";
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(timeStr);
            
            Calendar today = Calendar.getInstance();
            Calendar targetDay = Calendar.getInstance();
            targetDay.setTime(date);
            
            // 判断是否是今天
            if (isSameDay(today, targetDay)) {
                return "今天";
            }
            
            // 判断是否是昨天
            today.add(Calendar.DAY_OF_YEAR, -1);
            if (isSameDay(today, targetDay)) {
                return "昨天";
            }
            
            // 其他日期
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
            return outputFormat.format(date);
            
        } catch (Exception e) {
            return "";
        }
    }
    
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    private void loadDrawingSessionDetail(Long sessionId) {
        // 显示加载框
        LoadingProgressDialog loadingDialog =
            new LoadingProgressDialog(this)
                .setMessage("加载中...")
                .setCancelable(false);
        loadingDialog.show();
        
        // 调用API获取会话详情
        drawingRepository.getSessionDetailById(sessionId).observeForever(result -> {
            loadingDialog.dismiss();
            
            if (result.isSuccess() && result.getData() != null) {
                DrawingSessionDto sessionDetail = result.getData();
                // 跳转到SuperChatContainActivity
                android.content.Intent intent = new android.content.Intent(this,
                        SuperChatContainActivity.class);
                intent.putExtra("sessionId", sessionId);
                intent.putExtra("sessionDetail", sessionDetail);
                startActivity(intent);
            } else {
                // 显示错误提示
                android.widget.Toast.makeText(this, 
                    "获取详情失败：" + (result.getError() != null ? result.getError() : "未知错误"), 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 跳转到会议详情页面
     */
    private void jumpToMeetingDetail(Long meetingId, Integer meetingType) {
        android.util.Log.d("HistoryActivity", "跳转到会议详情，meetingId: " + meetingId + ", meetingType: " + meetingType);

        // 显示加载提示
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("正在加载会议详情...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // 先查询会议详情获取转写内容
            meetingRepository.getMeetingDetail(meetingId.toString()).observeForever(result -> {
                // 隐藏加载提示
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (result != null && result.isSuccess() && result.getData() != null) {
                    MeetingDto meeting = result.getData();
                    String transcriptionResult = meeting.getMeetingText(); // content字段就是转写结果

                    android.util.Log.d("HistoryActivity", "获取到会议转写内容，长度: " +
                        (transcriptionResult != null ? transcriptionResult.length() : 0));

                    // 使用获取到的转写内容跳转
                    android.content.Intent intent = MeetingActivity.createIntent(
                        this,
                        meetingId.toString(), // 会议ID
                        transcriptionResult != null ? transcriptionResult : "", // 实际的转写结果
                        0 // tabType，会议内容（默认显示第一个tab）
                    );
                    startActivity(intent);

                } else {
                    // 查询失败，仍然跳转但不传递转写内容
                    android.util.Log.w("HistoryActivity", "获取会议详情失败: " +
                        (result != null ? result.getError() : "未知错误"));

                    android.content.Intent intent = MeetingActivity.createIntent(
                        this,
                        meetingId.toString(), // 会议ID
                        "", // 转写结果为空，让详情页自己加载
                        0 // tabType，会议内容
                    );
                    startActivity(intent);

                    android.widget.Toast.makeText(this,
                        "会议详情加载失败，但仍可查看",
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            // 隐藏加载提示
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            android.util.Log.e("HistoryActivity", "跳转到会议详情失败", e);
            android.widget.Toast.makeText(this,
                "打开会议详情失败",
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
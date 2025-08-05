package com.fxzs.lingxiagent.view.chat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.model.meeting.dto.MeetingDto;
import com.fxzs.lingxiagent.view.common.LoadingProgressDialog;
import com.fxzs.lingxiagent.view.meeting.MeetingActivity;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.chat.dto.DrawingToChatBean;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepository;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepositoryImpl;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepository;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepositoryImpl;
import com.fxzs.lingxiagent.model.chat.repository.ChatRepository;
import com.fxzs.lingxiagent.model.chat.repository.ChatRepositoryImpl;
import com.fxzs.lingxiagent.model.chat.dto.ConversationHistoryDto;
import com.fxzs.lingxiagent.model.meeting.dto.MeetingHistoryDto;
import com.fxzs.lingxiagent.network.ZNet.bean.getCatDetailListBean;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.view.common.CommonDialog;
import com.fxzs.lingxiagent.view.common.EditInfoDialog;
import com.fxzs.lingxiagent.view.common.GlobalToast;
import com.fxzs.lingxiagent.view.user.HistoryActivity;
import com.fxzs.lingxiagent.view.user.HistoryAdapter;
import com.fxzs.lingxiagent.view.user.HistoryItem;
import com.fxzs.lingxiagent.viewmodel.history.HistoryViewModelFactory;
import com.fxzs.lingxiagent.viewmodel.history.VMHistory;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 历史记录底部抽屉Fragment
 */
public class HistoryBottomSheetFragment extends BottomSheetDialogFragment {
    
    private ImageView ivClose;
    private TextView tvTitle;
    private TextView tvTabChat;
    private TextView tvTabAgent;
    private TextView tvTabDrawing;
    private TextView tvTabMeeting;
    private TextView tvTabPPT;
    private TextView tvTabTranslate;
    private RecyclerView rvHistory;
    
    private HistoryAdapter historyAdapter;
    private VMHistory viewModel;
    
    // Tab颜色
    private static final int COLOR_SELECTED = 0xFF1E1E1E;
    private static final int COLOR_UNSELECTED = 0xFF999999;
    private static int initTab = VMHistory.TAB_CHAT;
    /**
     * 创建实例并指定默认选中的tab
     * @param defaultTab 默认选中的tab索引
     * @return HistoryBottomSheetFragment实例
     */
    public static HistoryBottomSheetFragment newInstance(int defaultTab) {
        HistoryBottomSheetFragment fragment = new HistoryBottomSheetFragment();
        Bundle args = new Bundle();
        args.putInt("default_tab", defaultTab);
        fragment.setArguments(args);
        return fragment;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        
        // 设置底部抽屉行为
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                
                // 获取状态栏高度
                int statusBarHeight = getStatusBarHeight();
                
                // 设置高度为屏幕高度减去状态栏高度，让抽屉顶到状态栏下方
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = getResources().getDisplayMetrics().heightPixels - statusBarHeight;
                bottomSheet.setLayoutParams(layoutParams);
            }
        });
        
        return dialog;
    }
    
    /**
     * 获取状态栏高度
     */
    private int getStatusBarHeight() {
        if (getContext() == null) return 0;
        
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history_bottom_sheet, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        setupListeners();
        setupRecyclerView();
        observeViewModel();

        // 获取默认选中的tab，如果没有传递参数则默认选中对话Tab
        int defaultTab = VMHistory.TAB_CHAT;
        if (getArguments() != null) {
            defaultTab = getArguments().getInt("default_tab", VMHistory.TAB_CHAT);
        }
        viewModel.selectTab(defaultTab);
    }
    
    private void initViews(View view) {
        ivClose = view.findViewById(R.id.ivClose);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvTabChat = view.findViewById(R.id.tvTabChat);
        tvTabAgent = view.findViewById(R.id.tvTabAgent);
        tvTabDrawing = view.findViewById(R.id.tvTabDrawing);
        tvTabMeeting = view.findViewById(R.id.tvTabMeeting);
        tvTabPPT = view.findViewById(R.id.tvTabPPT);
        tvTabTranslate = view.findViewById(R.id.tvTabTranslate);
        rvHistory = view.findViewById(R.id.rvHistory);
    }

    private void initViewModel() {
        HistoryViewModelFactory factory = new HistoryViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(VMHistory.class);
    }

    private void observeViewModel() {
        // 观察当前Tab变化
        viewModel.getCurrentTabIndex().observe(getViewLifecycleOwner(), tabIndex -> {
            updateTabSelection(tabIndex);
            // 更新Adapter的绘画Tab模式
            if (historyAdapter != null) {
                historyAdapter.setIsDrawingTab(tabIndex == VMHistory.TAB_DRAWING);
            }
        });

        // 观察历史记录数据变化
        viewModel.getHistoryItems().observe(getViewLifecycleOwner(), items -> {
            if (historyAdapter != null) {
                historyAdapter.setItems(items);
            }
        });

        // 观察加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: 显示/隐藏加载指示器
        });

        // 观察刷新状态
        viewModel.getIsRefreshing().observe(getViewLifecycleOwner(), isRefreshing -> {
            // TODO: 显示/隐藏刷新指示器
        });

        // 观察错误信息
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                GlobalToast.show(getActivity(), error, GlobalToast.Type.ERROR);
            }
        });
    }
    
    private void setupListeners() {
        ivClose.setOnClickListener(v -> dismiss());
        
        tvTabChat.setOnClickListener(v -> viewModel.selectTab(VMHistory.TAB_CHAT));
        tvTabAgent.setOnClickListener(v -> viewModel.selectTab(VMHistory.TAB_AGENT));
        tvTabDrawing.setOnClickListener(v -> viewModel.selectTab(VMHistory.TAB_DRAWING));
        tvTabMeeting.setOnClickListener(v -> viewModel.selectTab(VMHistory.TAB_MEETING));
        tvTabPPT.setOnClickListener(v -> viewModel.selectTab(VMHistory.TAB_PPT));
        tvTabTranslate.setOnClickListener(v -> viewModel.selectTab(VMHistory.TAB_TRANSLATE));
        
        // 点击标题跳转到完整的历史记录页面
        tvTitle.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HistoryActivity.class);
            startActivity(intent);
            dismiss();
        });
    }
    
    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setAdapter(historyAdapter);
        
        // 添加滚动监听器实现分页加载
        rvHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // 只在向下滚动时检查
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    
                    // 当滚动到接近底部时（剩余3个item时）触发加载更多
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 3) {
                        // 检查是否正在加载或刷新，以及是否还有更多数据，避免重复请求
                        Boolean isLoading = viewModel.getIsLoading().getValue();
                        Boolean isRefreshing = viewModel.getIsRefreshing().getValue();
                        boolean hasMoreData = viewModel.hasMoreData();
                        
                        if (!Boolean.TRUE.equals(isLoading) && !Boolean.TRUE.equals(isRefreshing) && hasMoreData) {
                            android.util.Log.d("HistoryBottomSheet", "触发加载更多数据");
                            viewModel.loadMoreHistory();
                        } else if (!hasMoreData) {
                            android.util.Log.d("HistoryBottomSheet", "已加载全部数据，无更多数据");
                        }
                    }
                }
            }
        });
        
        // 设置点击监听器
        historyAdapter.setOnItemClickListener(item -> {
            Integer currentTab = viewModel.getCurrentTabIndex().getValue();
            if (currentTab == null) return;

            if (currentTab == VMHistory.TAB_CHAT && item.getConversationId() != null) {
                // 对话tab，跳转到对话页面
//                jumpToConversation(item.getConversationId(), item.getModelType(),0);
                jumpToConversation(item);
            } else if (currentTab == 1 && item.getConversationId() != null) {
                // 智能体tab，跳转到对话页面
                jumpToConversation(item);
            } else if (currentTab == 2 && item.getSessionId() != null) {
                // AI绘画tab，调用详情接口并跳转
                loadDrawingSessionDetail(item.getSessionId());
            } else if (currentTab == VMHistory.TAB_MEETING && item.getMeetingId() != null) {
                // 会议tab，跳转到会议详情页面
                jumpToMeetingDetail(item.getMeetingId(), item.getMeetingType());
            }
            // 其他tab的处理可以在这里添加
        });
        
        // 设置更多操作监听器
        historyAdapter.setOnMoreActionListener((anchor, item, actionType) -> {
            if (actionType == 0) { // 查看详情
//                handleViewDetail(item);
            } else if (actionType == 1) { // 重命名
                    // 弹出输入框，确认后重命名
                showEditNameDialog(item);
//                    android.widget.EditText editText = new android.widget.EditText(getContext());
//                    editText.setText(item.getTitle());
//                    new androidx.appcompat.app.AlertDialog.Builder(getContext())
//                        .setTitle("重命名")
//                        .setView(editText)
//                        .setPositiveButton("确定", (dialog, which) -> {
//                            String newName = editText.getText().toString().trim();
//                            // TODO: 调用重命名接口
//                            GlobalToast.show(getActivity(), "重命名为: " + newName, GlobalToast.Type.SUCCESS);
//                        })
//                        .setNegativeButton("取消", null)
//                        .show();
                } else if (actionType == 2) { // 删除
                    // 弹出确认对话框

                        CommonDialog.showWarningDialog(getContext(), "是否删除该内容？", "删除后，内容无法恢复，请谨慎操作。",
                                "删除", new CommonDialog.OnDialogClickListener() {
                                    @Override
                                    public void onConfirm() {

                                        deleteHistory(item);
                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                });
//                    new androidx.appcompat.app.AlertDialog.Builder(getContext())
//                        .setTitle("删除确认")
//                        .setMessage("确定要删除这条记录吗？")
//                        .setPositiveButton("删除", (dialog, which) -> {
//                            // TODO: 调用删除接口
//                            android.widget.Toast.makeText(getContext(), "已删除", android.widget.Toast.LENGTH_SHORT).show();
//                        })
//                        .setNegativeButton("取消", null)
//                        .show();
                }
//            }
        });
    }


    private void showEditNameDialog(HistoryItem item) {
        EditInfoDialog editDialog = new EditInfoDialog(getActivity())
                .setTitle("重命名")
                .setHint("请输入对话名称")
                .setText(item.getTitle())
                .setCancelText("取消")
                .setConfirmText("保存")
                .setConfirmTextColor(R.color.dialog_save)
                .setMaxLength(20)
                .setOnEditInfoDialogListener(new EditInfoDialog.OnEditInfoDialogListener() {
                    @Override
                    public void onConfirm(String inputText) {
                        if (inputText.isEmpty()) {
//                            showToast("昵称不能为空");
                            return;
                        }
                        viewModel.updateName(item,inputText);
                    }

                    @Override
                    public void onCancel() {
                        // 取消编辑
                    }
                });
        editDialog.show();
    }

    /**
     * 处理查看详情操作
     */
    private void handleViewDetail(HistoryItem item) {
        Integer currentTab = viewModel.getCurrentTabIndex().getValue();
        if (currentTab == null) return;

        if (currentTab == VMHistory.TAB_CHAT && item.getConversationId() != null) {
            // 对话tab，跳转到对话页面
            jumpToConversation(item);
        } else if (currentTab == VMHistory.TAB_AGENT && item.getConversationId() != null) {
            // 智能体tab，跳转到对话页面
            jumpToConversation(item);
        } else if (currentTab == VMHistory.TAB_DRAWING && item.getSessionId() != null) {
            // AI绘画tab，调用详情接口并跳转
            loadDrawingSessionDetail(item.getSessionId());
        } else if (currentTab == VMHistory.TAB_MEETING && item.getMeetingId() != null) {
            // 会议tab，跳转到会议详情页面
            jumpToMeetingDetail(item.getMeetingId(), item.getMeetingType());
        } else {
            GlobalToast.show(getActivity(), "暂不支持查看此类型详情", GlobalToast.Type.NORMAL);
        }
    }

    private void deleteHistory(HistoryItem item) {
        Integer currentTab = viewModel.getCurrentTabIndex().getValue();
        if (currentTab == null) return;

        long id = 0;
        switch (currentTab) {
            case VMHistory.TAB_CHAT:
            case VMHistory.TAB_AGENT:
                id = item.getConversationId();
                deleteConversation(id);
                break;
            case VMHistory.TAB_DRAWING:
                id = item.getSessionId();
                deleteDrawing(id);
                break;
            case VMHistory.TAB_MEETING:
                id = item.getMeetingId();
                deleteMeeting(id + "");
                break;
        }
    }
    public void deleteMeeting(String meetingId) {
//        setLoading(true);
        MeetingRepository repository = new MeetingRepositoryImpl();
        repository.deleteMeeting(meetingId).observeForever(result -> {
//            setLoading(false);
            if (result != null && result.isSuccess()) {
                GlobalToast.show(getActivity(),"删除成功",GlobalToast.Type.SUCCESS);
                viewModel.refreshHistory();
            } else {
//                setError(result != null ? result.getError() : "删除失败");
            }
        });
    }
    private void deleteDrawing(long id) {
        DrawingRepository drawingRepository = DrawingRepositoryImpl.getInstance();
        drawingRepository.deleteAllSessions(id).observeForever(result -> {
            if (result != null && result.isSuccess() && result.getData() != null) {
                GlobalToast.show(getActivity(),"删除成功",GlobalToast.Type.SUCCESS);
                viewModel.refreshHistory();
            } else {
                GlobalToast.show(getActivity(),"删除失败", GlobalToast.Type.ERROR);
            }
        });
    }
    private void deleteConversation(long id) {
        ChatRepository chatRepository = new ChatRepositoryImpl();
        chatRepository.deleteConversation(id, new ChatRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                GlobalToast.show(getActivity(),"删除成功",GlobalToast.Type.SUCCESS);
                viewModel.refreshHistory();
            }

            @Override
            public void onError(String error) {
                GlobalToast.show(getActivity(),"删除失败: " + error, GlobalToast.Type.ERROR);
            }
        });
    }

    /**
     * 更新Tab选中状态（由ViewModel触发）
     */
    private void updateTabSelection(int tabIndex) {
        // 重置所有Tab样式
        resetTabStyles();

        // 设置选中Tab样式
        TextView selectedTab = getTabByIndex(tabIndex);
        if (selectedTab != null) {
            selectedTab.setTextColor(COLOR_SELECTED);
            selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }
    
    private void resetTabStyles() {
        TextView[] tabs = {tvTabChat, tvTabAgent, tvTabDrawing, tvTabMeeting, tvTabPPT, tvTabTranslate};
        for (TextView tab : tabs) {
            tab.setTextColor(COLOR_UNSELECTED);
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
    
    private TextView getTabByIndex(int index) {
        switch (index) {
            case VMHistory.TAB_CHAT: return tvTabChat;
            case VMHistory.TAB_AGENT: return tvTabAgent;
            case VMHistory.TAB_DRAWING: return tvTabDrawing;
            case VMHistory.TAB_MEETING: return tvTabMeeting;
            case VMHistory.TAB_PPT: return tvTabPPT;
            case VMHistory.TAB_TRANSLATE: return tvTabTranslate;
            default: return null;
        }
    }
    
    // 移除了loadHistoryData方法，现在由ViewModel处理数据加载

    // 移除了loadChatConversationHistory方法，现在由ViewModel处理

    // 移除了loadAgentConversationHistory方法，现在由ViewModel处理

    // 移除了loadMeetingHistory方法，现在由ViewModel处理

    // 移除了loadDrawingHistory方法，现在由ViewModel处理

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
     * 将会议历史转换为HistoryItem列表
     */
    private List<HistoryItem> convertMeetingToHistoryItems(List<MeetingHistoryDto> meetings) {
        List<HistoryItem> items = new ArrayList<>();
        if (meetings == null || meetings.isEmpty()) {
            return items;
        }

        String lastDate = "";

        for (MeetingHistoryDto meeting : meetings) {
            // 使用会议名称中的时间戳或当前时间作为日期
            String date = getDateFromMeetingName(meeting.getCreateTime());
            if (!date.equals(lastDate)) {
                // 添加日期分组头
                items.add(new HistoryItem(HistoryItem.TYPE_DATE_HEADER, date, null, 0));
                lastDate = date;
            }

            // 构建显示标题
            String title = meeting.getName();
            if (title == null || title.trim().isEmpty()) {
                title = "会议记录";
            }
            // 去除分割title的逻辑，直接显示完整name字段

            // 构建副标题（显示会议内容摘要）
            String subtitle = "";
            if (meeting.getAbstractText() != null && !meeting.getAbstractText().trim().isEmpty()) {
                subtitle = meeting.getAbstractText();
                if (subtitle.length() > 50) {
                    subtitle = subtitle.substring(0, 50) + "...";
                }
            } else if (meeting.getContent() != null && !meeting.getContent().trim().isEmpty()) {
                // 提取纯文本内容
                String content = extractTextFromMeetingContent(meeting.getContent());
                if (content.length() > 50) {
                    content = content.substring(0, 50) + "...";
                }
                subtitle = content;
            } else {
                subtitle = "暂无内容";
            }

            // 创建HistoryItem，使用会议图标
            HistoryItem historyItem = new HistoryItem(HistoryItem.TYPE_ITEM, title, subtitle, R.drawable    .ic_nav_meeting);
            historyItem.setMeetingId(meeting.getId());
            historyItem.setMeetingType(meeting.getType());
            items.add(historyItem);
        }

        return items;
    }

    /**
     * 从会议名称中提取日期
     */
    private String getDateFromMeetingName(String timestamp) {
        try {
            if (timestamp == null) {
                return "未知日期";
            }
            return getDateFromTimestamp(Long.valueOf(timestamp));
        } catch (Exception e) {
            return "未知日期";
        }
    }

    /**
     * 从会议内容中提取纯文本
     */
    private String extractTextFromMeetingContent(String content) {
        if (content == null) return "";

        // 移除时间戳格式 [0:1.310,0:2.260,0]
        String text = content.replaceAll("\\[\\d+:\\d+\\.\\d+,\\d+:\\d+\\.\\d+,\\d+\\]\\s*", "");
        // 移除多余的换行符
        text = text.replaceAll("\\n+", " ");
        return text.trim();
    }

    /**
     * 将智能体对话历史转换为HistoryItem列表
     */
    private List<HistoryItem> convertConversationToHistoryItems(List<ConversationHistoryDto> conversations) {
        List<HistoryItem> items = new ArrayList<>();
        if (conversations == null || conversations.isEmpty()) {
            return items;
        }

        String lastDate = "";

        for (ConversationHistoryDto conversation : conversations) {
            // 获取日期字符串
            String date = getDateFromTimestamp(conversation.getCreateTime());
            if (!date.equals(lastDate)) {
                // 添加日期分组头
                items.add(new HistoryItem(HistoryItem.TYPE_DATE_HEADER, date, null, 0));
                lastDate = date;
            }

            // 构建显示标题
            String title = conversation.getTitle();
            if (title == null || title.trim().isEmpty()) {
                title = "新对话";
            }

            // 构建副标题（显示角色名称或最后一条消息）
            String subtitle = "";
            if (conversation.getRoleName() != null && !conversation.getRoleName().trim().isEmpty()) {
                subtitle = conversation.getRoleName();
            } else if (conversation.getLastMessage() != null &&
                       conversation.getLastMessage().getContent() != null) {
                String content = conversation.getLastMessage().getContent();
                if (content.length() > 30) {
                    content = content.substring(0, 30) + "...";
                }
                subtitle = content;
            }

            // 获取头像资源
            int avatarRes = getAvatarResourceByRoleName(conversation.getRoleName());

            // 创建HistoryItem
            HistoryItem historyItem = new HistoryItem(HistoryItem.TYPE_ITEM, title, subtitle, avatarRes);
            historyItem.setConversationId(conversation.getId());
            historyItem.setModelType(conversation.getModelType());
            historyItem.setModelType(conversation.getModelType());
            historyItem.setModelId(conversation.getModelId());
            items.add(historyItem);
        }

        return items;
    }

    /**
     * 根据时间戳获取日期字符串
     */
    private String getDateFromTimestamp(Long timestamp) {
        if (timestamp == null) return "";

        try {
            Date date = new Date(timestamp);
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
            android.util.Log.e("HistoryBottomSheet", "时间戳转换失败: " + timestamp, e);
            return "";
        }
    }

    /**
     * 根据角色名称获取头像资源
     */
    private int getAvatarResourceByRoleName(String roleName) {
        if (roleName == null) {
            return R.drawable.ic_avatar_ai_writer; // 默认头像
        }

        // 根据角色名称匹配头像
        switch (roleName) {
            case "瘦身小天使":
            case "健身教练":
                return R.drawable.ic_avatar_fitness;
            case "千变女友":
            case "恋爱助手":
                return R.drawable.ic_avatar_girlfriend;
            case "AI 写作":
            case "写作助手":
                return R.drawable.ic_avatar_ai_writer;
            default:
                return R.drawable.ic_avatar_ai_writer; // 默认头像
        }
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
            new LoadingProgressDialog(getContext())
                .setMessage("加载中...")
                .setCancelable(false);
        loadingDialog.show();

        // 调用API获取会话详情
        DrawingRepository drawingRepository = DrawingRepositoryImpl.getInstance();
        drawingRepository.getSessionDetailById(sessionId).observeForever(result -> {
            loadingDialog.dismiss();

            if (result.isSuccess() && result.getData() != null) {
                DrawingSessionDto sessionDetail = result.getData();

                // 跳转到DrawingChatActivity
//                android.content.Intent intent = new android.content.Intent(getContext(),
//                    com.fxzs.drawing.view.lingxiagent.DrawingChatActivity.class);
//                intent.putExtra("sessionId", sessionId);
//                intent.putExtra("sessionDetail", sessionDetail);
                android.content.Intent intent = new android.content.Intent(getContext(),
                    SuperChatContainActivity.class);
                intent.putExtra(Constant.INTENT_TYPE, SuperChatContainActivity.TYPE_DRAWING);

                DrawingToChatBean drawingToChatBean = new DrawingToChatBean();
                drawingToChatBean.setSessionId(sessionId);
                drawingToChatBean.setSessionDetail(sessionDetail);

                intent.putExtra(Constant.INTENT_DATA, drawingToChatBean);
                startActivity(intent);
                dismiss(); // 关闭底部抽屉
            } else {
                // 显示错误提示
                GlobalToast.show(getActivity(),
                    "获取详情失败：" + (result.getError() != null ? result.getError() : "未知错误"),
                    GlobalToast.Type.ERROR);
            }
        });
    }

    /**
     * 跳转到对话页面（支持对话和智能体）
     */
    private void jumpToConversation(HistoryItem item
           ) {
        long conversationId = item.getConversationId();
        int modelType = item.getModelType();
        long modeId = item.getModelId();
        android.util.Log.d("HistoryBottomSheet", "跳转到对话页面，conversationId: " + conversationId + ", modelType: " + modelType);

        try {
            // 跳转到SuperChatContainActivity
            android.content.Intent intent = new android.content.Intent(getContext(), SuperChatContainActivity.class);

            // 根据modelType设置不同的类型
            if (modelType == 1) {
                // 智能体对话
                getCatDetailListBean bean = new getCatDetailListBean();
                bean.setModelId(modeId);
                intent.putExtra(Constant.INTENT_DATA2, bean);
                intent.putExtra(Constant.INTENT_TYPE, SuperChatContainActivity.TYPE_AGENT);
            } else if (modelType == 8) {
                // 普通对话

                OptionModel selectOptionModel = new OptionModel();
                selectOptionModel.setId(item.getModelId());
                selectOptionModel.setName(item.getModelName());
                selectOptionModel.setModel(item.getModel());
                intent.putExtra(Constant.INTENT_DATA1, selectOptionModel);
                intent.putExtra(Constant.INTENT_TYPE, SuperChatContainActivity.TYPE_HOME);
            } else {
                // 默认类型
                intent.putExtra(Constant.INTENT_TYPE, SuperChatContainActivity.TYPE_HOME);
            }

            // 传递对话ID
            intent.putExtra(Constant.INTENT_ID, conversationId);
            intent.putExtra("modelType", modelType);

            startActivity(intent);
            dismiss(); // 关闭底部抽屉

        } catch (Exception e) {
            android.util.Log.e("HistoryBottomSheet", "跳转到对话页面失败", e);
            GlobalToast.show(getActivity(),
                "打开对话失败",
                GlobalToast.Type.ERROR);
        }
    }

    /**
     * 跳转到会议详情页面
     */
    private void jumpToMeetingDetail(Long meetingId, Integer meetingType) {
        android.util.Log.d("HistoryBottomSheet", "跳转到会议详情，meetingId: " + meetingId + ", meetingType: " + meetingType);

        // 显示加载提示
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
        progressDialog.setMessage("正在加载会议详情...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // 先查询会议详情获取转写内容
            MeetingRepository meetingRepository = new MeetingRepositoryImpl();
            meetingRepository.getMeetingDetail(meetingId.toString()).observeForever(result -> {
                // 隐藏加载提示
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (result != null && result.isSuccess() && result.getData() != null) {
                    MeetingDto meeting = result.getData();
                    String transcriptionResult = meeting.getMeetingText(); // content字段就是转写结果

                    // 获取会议标题，优先使用name，其次使用title
                    String meetingTitle = meeting.getName();
                    if (meetingTitle == null || meetingTitle.trim().isEmpty()) {
                        meetingTitle = meeting.getTitle();
                    }
                    if (meetingTitle == null || meetingTitle.trim().isEmpty()) {
                        meetingTitle = "会议详情";
                    }

                    android.util.Log.d("HistoryBottomSheet", "获取到会议转写内容，长度: " +
                        (transcriptionResult != null ? transcriptionResult.length() : 0) +
                        ", 会议标题: " + meetingTitle);

                    // 使用获取到的转写内容和标题跳转
                    android.content.Intent intent = MeetingActivity.createIntent(
                        getContext(),
                        meetingId.toString(), // 会议ID
                        transcriptionResult != null ? transcriptionResult : "", // 实际的转写结果
                        0, // tabType，会议内容（默认显示第一个tab）
                        meetingTitle // 会议标题
                    );
                    startActivity(intent);

                } else {
                    // 查询失败，仍然跳转但不传递转写内容
                    android.util.Log.w("HistoryBottomSheet", "获取会议详情失败: " +
                        (result != null ? result.getError() : "未知错误"));

                    android.content.Intent intent = MeetingActivity.createIntent(
                        getContext(),
                        meetingId.toString(), // 会议ID
                        "", // 转写结果为空，让详情页自己加载
                        0, // tabType，会议内容
                        "会议详情" // 默认标题
                    );
                    startActivity(intent);

                    GlobalToast.show(getActivity(),
                        "会议详情加载失败，但仍可查看",
                        GlobalToast.Type.NORMAL);
                }
            });

        } catch (Exception e) {
            // 隐藏加载提示
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            android.util.Log.e("HistoryBottomSheet", "跳转到会议详情失败", e);
            GlobalToast.show(getActivity(),
                "打开会议详情失败",
                GlobalToast.Type.ERROR);
        }
    }
}

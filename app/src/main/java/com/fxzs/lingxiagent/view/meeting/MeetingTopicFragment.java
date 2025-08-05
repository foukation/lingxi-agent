package com.fxzs.lingxiagent.view.meeting;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.AIResponseView;
import com.fxzs.lingxiagent.view.common.BaseFragment;
import com.fxzs.lingxiagent.view.common.CommonDialog;
import com.fxzs.lingxiagent.view.common.GlobalToast;
import com.fxzs.lingxiagent.util.WordExportUtil;
import com.fxzs.lingxiagent.viewmodel.meeting.VMMeetingTopic;

public class MeetingTopicFragment extends BaseFragment<VMMeetingTopic> {
    
    private static final String ARG_MEETING_ID = "meeting_id";
    private static final String ARG_TRANSCRIPTION_RESULT = "transcription_result";
    
    private String meetingId;
    private String transcriptionResult;
    private AIResponseView aiResponseView;

    public static MeetingTopicFragment newInstance(String meetingId, String transcriptionResult) {
        MeetingTopicFragment fragment = new MeetingTopicFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEETING_ID, meetingId);
        args.putString(ARG_TRANSCRIPTION_RESULT, transcriptionResult);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            meetingId = getArguments().getString(ARG_MEETING_ID);
            transcriptionResult = getArguments().getString(ARG_TRANSCRIPTION_RESULT);
        }
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_meeting_topic;
    }
    
    @Override
    protected Class<VMMeetingTopic> getViewModelClass() {
        return VMMeetingTopic.class;
    }
    
    @Override
    protected void initializeViews(View view) {
        aiResponseView = view.findViewById(R.id.ai_response_view);
        setupAIResponseView();
        setDefaultContent();
    }
    
    @Override
    protected void setupDataBinding() {
        // 初始化话题内容
        if (viewModel != null) {
            // 使用新的初始化方法
            viewModel.initializeTopics(meetingId, transcriptionResult);
        }
    }
    
    @Override
    protected void setupObservers() {
        // 观察话题生成结果 (使用LiveData)
        viewModel.getTopicsResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty()) {
                android.util.Log.d("MeetingTopicFragment", "话题内容更新: " + result.length() + " 字符");
                // 话题生成完成，直接显示内容
                updateContentDisplay(result);
            }
        });

        // 观察加载状态
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) {
                if (loading) {
                    // 显示加载状态
                    updateContentDisplay("正在生成会议话题...");
                }
            }
        });

        // 观察错误状态
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                GlobalToast.show(getActivity(), error, GlobalToast.Type.ERROR);
                // 错误时显示默认内容
                updateContentDisplay("当前未检测到有效会议内容，暂时无法生成会议话题...");
            }
        });

        // 观察话题生成结果
        viewModel.getTopicsResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty()) {
                android.util.Log.d("MeetingTopicFragment", "话题内容更新: " + result.length() + " 字符");
                // 话题生成完成，直接显示内容
                updateContentDisplay(result);
            }
        });

        // 观察刷新状态
        viewModel.getIsRefreshing().observe(getViewLifecycleOwner(), isRefreshing -> {
            if (isRefreshing != null && isRefreshing) {
                // 显示刷新状态
                updateContentDisplay("正在重新生成会议话题...");
            }
        });
    }
    
    private void setDefaultContent() {
        // setDefaultContent will be handled by initializeTopicContents()
        // Keep this method for consistency but content initialization is moved
        if (transcriptionResult == null || transcriptionResult.isEmpty()) {
            aiResponseView.setAsSimpleResponse("会议话题", "会议话题加载中...\n\n会议中讨论的主要话题将在此列出。");
        }
    }
    
    private void updateContentDisplay(String content) {
        android.util.Log.d("MeetingTopicFragment", "updateContentDisplay 调用 - 内容长度: " +
            (content != null ? content.length() : 0));

        if (aiResponseView != null) {
            if (content != null && !content.trim().isEmpty()) {
                android.util.Log.d("MeetingTopicFragment", "设置内容到AIResponseView: " + content.substring(0, Math.min(100, content.length())) + "...");
                aiResponseView.setContent(content);
                // 设置标题
                aiResponseView.setTitle("会议话题");
            } else {
                android.util.Log.w("MeetingTopicFragment", "内容为空，显示默认内容");
                aiResponseView.setContent("暂无会议话题内容");
            }
        } else {
            android.util.Log.e("MeetingTopicFragment", "aiResponseView 为空！");
        }
    }
    
    private void setupAIResponseView() {
        if (aiResponseView != null) {
            // 设置刷新按钮监听器
            aiResponseView.setOnRefreshClickListener(new AIResponseView.OnRefreshClickListener() {
                @Override
                public void onRefreshClick() {
                    refreshContent();
                }
            });
            
            // AIResponseView 已经内置了复制和语音播放功能
            // 显示所有按钮
            aiResponseView.showCopyButton(true);
            aiResponseView.showSpeakButton(true);
            aiResponseView.showDivider(true);
        }
    }
    

    

    
    // 复制功能已由 AIResponseView 内部处理
    
    private void refreshContent() {
        if (viewModel == null) {
            GlobalToast.show(getActivity(), "刷新失败，请重试", GlobalToast.Type.ERROR);
            return;
        }

        // 使用ViewModel的刷新方法
        viewModel.refreshTopics();
    }

    /**
     * 导出话题内容为Word文档
     */
    public void exportToWord() {
        if (aiResponseView == null) {
            GlobalToast.show(getActivity(), "暂无内容可导出", GlobalToast.Type.ERROR);
            return;
        }

        // 获取AIResponse的内容
        String content = aiResponseView.getContent();
        if (content == null || content.trim().isEmpty()) {
            GlobalToast.show(getActivity(), "暂无内容可导出", GlobalToast.Type.ERROR);
            return;
        }

        // 显示导出进度
        GlobalToast.show(getActivity(), "正在导出话题文档...", GlobalToast.Type.NORMAL);

        // 导出Word文档
        WordExportUtil.exportToWord(getContext(), "会议话题", content, new WordExportUtil.ExportCallback() {
            @Override
            public void onSuccess(java.io.File file) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // 导出成功后直接显示弹窗，不显示Toast
                        showOpenDocumentDialog(file);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        GlobalToast.show(getActivity(), "导出失败: " + error, GlobalToast.Type.ERROR);
                    });
                }
            }
        });
    }

    /**
     * 显示打开文档的对话框
     */
    private void showOpenDocumentDialog(java.io.File file) {
        if (getContext() == null) return;

        new CommonDialog.Builder(getContext())
                .setTitle("导出成功")
                .setMessage("话题文档已保存到:\n" + file.getName() + "\n\n是否立即打开？")
                .setConfirmText("打开")
                .setCancelText("稍后")
                .setOnClickListener(new CommonDialog.OnDialogClickListener() {
                    @Override
                    public void onConfirm() {
                        WordExportUtil.openDocument(getContext(), file);
                    }

                    @Override
                    public void onCancel() {
                        // 用户选择稍后，不做任何操作
                    }
                })
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (aiResponseView != null) {
            aiResponseView.release();
        }
    }

}
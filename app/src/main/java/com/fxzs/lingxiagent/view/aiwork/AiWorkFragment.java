package com.fxzs.lingxiagent.view.aiwork;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.aiwork.AiWorkFilterBean;
import com.fxzs.lingxiagent.model.chat.dto.DrawingToChatBean;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepository;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepositoryImpl;
import com.fxzs.lingxiagent.model.meeting.dto.MeetingDto;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepository;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepositoryImpl;
import com.fxzs.lingxiagent.network.ZNet.bean.getCatDetailListBean;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.util.ZUtil.ImageUtil;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.chat.SuperChatContainActivity;
import com.fxzs.lingxiagent.view.common.BaseFragment;
import com.fxzs.lingxiagent.view.common.CommonDialog;
import com.fxzs.lingxiagent.view.common.EditInfoDialog;
import com.fxzs.lingxiagent.view.common.GlobalToast;
import com.fxzs.lingxiagent.view.common.LoadingProgressDialog;
import com.fxzs.lingxiagent.view.drawing.DrawingContainActivity;
import com.fxzs.lingxiagent.view.meeting.MeetingActivity;
import com.fxzs.lingxiagent.view.meeting.MeetingContainActivity;
import com.fxzs.lingxiagent.view.user.HistoryItem;
import com.fxzs.lingxiagent.view.user.UserActivity;
import com.fxzs.lingxiagent.view.user.UserContainActivity;
import com.fxzs.lingxiagent.viewmodel.aiwork.VMAiWork;
import com.fxzs.lingxiagent.viewmodel.history.VMHistory;

import java.util.ArrayList;
import java.util.List;

public class AiWorkFragment extends BaseFragment<VMAiWork> {
    private View iv_filter;
    private RecyclerView rv;
    AiWorkAdapter adapter;
    private View ll_empty;
    private int selectFilter;
    private ImageView iv_avatar;

    @Override
    protected int getLayoutResource() {
        return R.layout.ai_work;
    }

    @Override
    protected Class<VMAiWork> getViewModelClass() {
        return VMAiWork.class;
    }

    @Override
    protected void initializeViews(View view) {

        iv_filter = view.findViewById(R.id.iv_filter);
        rv = view.findViewById(R.id.rv);
        ll_empty = view.findViewById(R.id.ll_empty);
        iv_avatar = view.findViewById(R.id.iv_avatar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        List<HistoryItem> list = new ArrayList<>();
//        list.add(new )
        adapter = new AiWorkAdapter(getActivity(),list);
        rv.setAdapter(adapter);
        // 设置更多操作监听器
        adapter.setOnMoreActionListener((anchor, item, actionType) -> {
            if (actionType == 0) { // 查看详情
//                handleViewDetail(item);
            } else if (actionType == 1) { // 重命名
                // 弹出输入框，确认后重命名
                showEditNameDialog(item);
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
            }
        });
        adapter.setOnItemClickListener(item -> {
            int type = adapter.getType();

            long id = 0;
            switch (type) {
                case AiWorkAdapter.TYPE_DRAWING:
                    // AI绘画tab，调用详情接口并跳转
                    loadDrawingSessionDetail(item.getSessionId());
                    break;
                case AiWorkAdapter.TYPE_MEETING:
                    // 会议tab，跳转到会议详情页面
                    jumpToMeetingDetail(item.getMeetingId(), item.getMeetingType());
                    break;
            }

            // 其他tab的处理可以在这里添加
        });

        iv_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZUtils.showAIWorkFilterPopup(getActivity(), view, null, new AiWorkFilterAdapter.OnOptionSelectedListener() {
                    @Override
                    public void onOptionSelected(AiWorkFilterBean option) {
                        selectFilter = option.getType();
                        viewModel.loadHistory(selectFilter);
                        adapter.setType(option.getType());
                    }
                });
            }
        });

        viewModel.setActivity(requireActivity());
        viewModel.loadHistory(AiWorkAdapter.TYPE_MEETING);

        findViewById(R.id.iv_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.ll_meeting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MeetingContainActivity.class));
            }
        });

        findViewById(R.id.ll_ppt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), MeetingContainActivity.class));
            }
        });

        findViewById(R.id.ll_drawing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DrawingContainActivity.class));
            }
        });

        findViewById(R.id.ll_translate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), MeetingContainActivity.class));
            }
        });
    }

    @Override
    protected void setupDataBinding() {

    }

    @Override
    protected void setupObservers() {

        viewModel.getVmHistory().getHistoryItems().observe(getViewLifecycleOwner(), items -> {
            if(items.size() > 0){
                ll_empty.setVisibility(View.GONE);
            }else {
                ll_empty.setVisibility(View.VISIBLE);
            }
            if (adapter != null) {
                adapter.setItems(items);
            }
        });


        viewModel.getVmUserProfile().getAvatarUrl().observe(getViewLifecycleOwner(), avatarUrl ->
//                    loadAvatarUrl(avatarUrl)
                        ImageUtil.netCircle(getActivity(),avatarUrl,iv_avatar)
        );
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
                        viewModel.getVmHistory().updateName(item,inputText);
                    }

                    @Override
                    public void onCancel() {
                        // 取消编辑
                    }
                });
        editDialog.show();
    }


    private void deleteHistory(HistoryItem item) {
        int type = adapter.getType();

        long id = 0;
        switch (type) {
            case AiWorkAdapter.TYPE_DRAWING:
                id = item.getSessionId();
                deleteDrawing(id);
                break;
            case AiWorkAdapter.TYPE_MEETING:
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
                viewModel.getVmHistory().refreshHistory();
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
                viewModel.getVmHistory().refreshHistory();
            } else {
                GlobalToast.show(getActivity(),"删除失败", GlobalToast.Type.ERROR);
            }
        });
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
            } else {
                // 显示错误提示
                GlobalToast.show(getActivity(),
                        "获取详情失败：" + (result.getError() != null ? result.getError() : "未知错误"),
                        GlobalToast.Type.ERROR);
            }
        });
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

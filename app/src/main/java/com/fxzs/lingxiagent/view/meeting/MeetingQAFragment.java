package com.fxzs.lingxiagent.view.meeting;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.chat.callback.MsgActionCallback;
import com.fxzs.lingxiagent.model.chat.callback.SoftCallback;
import com.fxzs.lingxiagent.model.chat.callback.SuperEditCallback;
import com.fxzs.lingxiagent.model.chat.dto.ChatFileBean;
import com.fxzs.lingxiagent.model.chat.dto.ChatMessage;
import com.fxzs.lingxiagent.network.ZNet.bean.getCatDetailListBean;
import com.fxzs.lingxiagent.util.ZInputMethod;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.util.ZUtil.SuperAgentUtil;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.chat.ChatAdapter;
import com.fxzs.lingxiagent.view.common.BaseFragment;
import com.fxzs.lingxiagent.viewmodel.chat.VMChat;
import com.fxzs.lingxiagent.util.ZUtil.AsrOneUtils;
import com.fxzs.lingxiagent.util.ZUtil.TTSUtils;
import com.fxzs.lingxiagent.view.chat.SuperChatFragment;


import java.util.List;

public class MeetingQAFragment extends BaseFragment<VMChat> {

    private static final String ARG_MEETING_ID = "meeting_id";
    private static final String ARG_TRANSCRIPTION_RESULT = "transcription_result";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private String meetingId;
    private String transcriptionResult;
    private LinearLayout root_view;
    private LinearLayout ll_bottom;
    private LinearLayout ll_edit_agent;
    private EditText ed;
    private RecyclerView rv_chat;
    private View ll_stop;
    private View ll_resend;
    private ImageView iv_scroll_down;
    private SuperAgentUtil superAgentUtil;
    private ChatAdapter chatAdapter;

    private boolean isUserTouch;//返回流时用户是否操作
    private boolean isSoft;
    private int type;//跳转类型
    private boolean mIsUserScrolling = false;
    public static MeetingQAFragment newInstance(String meetingId, String transcriptionResult) {
        MeetingQAFragment fragment = new MeetingQAFragment();
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
            viewModel.setMeetingId(meetingId);
            viewModel.setTranscriptionResult(transcriptionResult);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_meeting_qa;
    }

    @Override
    protected Class<VMChat> getViewModelClass() {
        return VMChat.class;
    }

    @Override
    protected void initializeViews(View view) {
        root_view = findViewById(R.id.root_view);
        ll_bottom = findViewById(R.id.ll_bottom);
        ll_edit_agent = findViewById(R.id.ll_edit_agent);
        rv_chat = findViewById(R.id.rv_chat);
        ed = findViewById(R.id.ed);

        ll_stop = findViewById(R.id.ll_stop);
        ll_resend = findViewById(R.id.ll_resend);
        iv_scroll_down = findViewById(R.id.iv_scroll_down);

        TTSUtils.getInstance().init(getActivity());
        setUI();
        setupButtonListeners();
        setDefaultContent();
    }

    private void setUI() {
        getCatDetailListBean bean = new getCatDetailListBean();
        bean.setName("智能问答");
        bean.setBotId("bot-20250307112049-znnjx");
        bean.setIcon("");
        bean.setDescription("我是移动语音助手，我可以基于AI会议内容回答问题，如：总结会议内容，生成待办等。\n" +
                "我的回答由AI生成，可能存在不准确或不完整的清空。\n" +
                "我们的对话内容仅您自己可见，欢迎向我提问。");
        viewModel.getChatMessages().getValue().add(
                new ChatMessage(bean.getDescription(), ChatAdapter.TYPE_USER_HEAD_MEETING,R.drawable.icon_app ));
        viewModel.setSelectAgentBean(bean);
        viewModel.getIsAutoPlay().postValue(false);
        setAgentBottomEdit();
        setChatRv();


        ll_stop.setOnClickListener(view -> {
            viewModel.closeSSE();
            ll_stop.setVisibility(View.GONE);
            ll_resend.setVisibility(View.VISIBLE);
        });

        iv_scroll_down.setOnClickListener(view -> {
            isUserTouch = false;
            iv_scroll_down.setVisibility(View.GONE);

            if(viewModel.getStreamEnd().getValue()){//已经结束流，手动调用代码滑动
                scroll2Last();
            }
        });
        ll_resend.setOnClickListener(view -> {
            ChatMessage message = viewModel.getResendMsg();
            if(message != null && message.getMsgType() == ChatAdapter.TYPE_USER){
                ed.setText(message.getMessage());
//                viewModel.resendMsg();
                ll_resend.setVisibility(View.GONE);
            }
        });  
        rv_chat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mIsUserScrolling = (newState == RecyclerView.SCROLL_STATE_DRAGGING);
            }
        });

        viewModel.initMeeting();
    }



    private void setAgentBottomEdit() {
        ll_edit_agent.setVisibility(View.VISIBLE);
        superAgentUtil = new SuperAgentUtil(getActivity(), ll_edit_agent);
        superAgentUtil.setOnListenSoft(root_view, new SoftCallback() {
            @Override
            public void show() {

                if(!isSoft){
                    scroll2Last();
                }
                isSoft = true;
            }
            @Override
            public void hide() {
                if(isSoft){
                    scroll2Last();
                }
                isSoft = false;
            }
        });
        superAgentUtil.setCallback(new SuperEditCallback() {
            @Override
            public void send(String content, OptionModel optionModel) {
//                ZUtils.print("SuperChatActivity send = "+content + " OptionModel = "+optionModel.getName());

                if (!content.isEmpty()) {
                    viewModel.sendMeetingMessage(content);
                    ZInputMethod.closeInputMethod(getActivity(),root_view);   //收起键盘
                }

            }

            @Override
            public void sendWithFile(String content, OptionModel selectOptionModel, List<ChatFileBean> fileList, boolean isFile) {
                //暂时无用
            }

            @Override
            public void voice() {
                checkAudioPermission();

            }

            @Override
            public void keyboard() {

            }

            @Override
            public void pressDown() {

            }

            @Override
            public void pressUp(boolean isInArea) {

            }
        });
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    int lastTextViewHeight = 0;
    private void scroll2Last() {
        ZUtils.print("scroll2Last isUserTouch = "+isUserTouch);
        if(isUserTouch){
            return;
        }
        if (mIsUserScrolling) {
            return;
        }
        if(viewModel.getChatMessages().getValue().size() <= 0){
            return;
        }
//        if(
//                viewModel.getChatMessages().getValue().size() > 0 &&
//                viewModel.getChatMessages().getValue().get(viewModel.getChatMessages().getValue().size()-1).getMessage() != null &&
//                viewModel.getChatMessages().getValue().get(viewModel.getChatMessages().getValue().size()-1).getMessage().length()/25==0){
//            return;
//        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) rv_chat.getLayoutManager();
        if (layoutManager != null) {
            // 获取最后一条消息的ViewHolder
            RecyclerView.ViewHolder holder = rv_chat.findViewHolderForAdapterPosition(viewModel.getChatMessages().getValue().size() - 1);
            if(holder == null){
                layoutManager.scrollToPosition(viewModel.getChatMessages().getValue().size() - 1);
                holder = rv_chat.findViewHolderForAdapterPosition(viewModel.getChatMessages().getValue().size() - 1);
            }
            if (holder !=null && holder instanceof ChatAdapter.ChatViewHolder) {
                LinearLayout textView = ((ChatAdapter.ChatViewHolder) holder).root_view;
                // 强制重新布局以确保高度准确
                textView.requestLayout();
                // 延迟获取高度，等待 Markdown 渲染完成
                textView.post(() -> {
                    if(isUserTouch){
                        return;
                    }
                    if (mIsUserScrolling) {
                        return;
                    }
                    int textViewHeight = textView.getHeight();
                    ZUtils.print( "TextView height: " + textViewHeight);
                    ZUtils.print( "lastTextViewHeight height: " + lastTextViewHeight);
//                    Log.d(TAG, "rv_chat height: " + rv_chat.getHeight());
                    // 仅当 TextView 高度超过 RecyclerView 高度时滚动
//                    if(lastTextViewHeight == textViewHeight){
//                        return;
//                    }
//                    lastTextViewHeight = textViewHeight;
                    if (textViewHeight > rv_chat.getHeight()) {
                        layoutManager.scrollToPositionWithOffset(viewModel.getChatMessages().getValue().size() - 1, -textViewHeight);
                    } else {
                        layoutManager.scrollToPosition(viewModel.getChatMessages().getValue().size() - 1);
                    }
                });
            }
        }
    }


    float lastX;
    float lastY;
    private void setChatRv() {
        rv_chat.setItemAnimator(null);
        rv_chat.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatAdapter = new ChatAdapter(getActivity(), viewModel.getChatMessages().getValue());
        chatAdapter.setCallback(new MsgActionCallback() {
            @Override
            public void refresh(String content) {
//                TTSUtils.getInstance().cancelAndPlay();
                viewModel.refreshSendMessage(content, SuperChatFragment.TYPE_MEETING_QA);
            }

            @Override
            public void msgClick() {
                ZInputMethod.closeInputMethod(getActivity(), rv_chat);

//                if(superEditUtil != null){
//                    superEditUtil.
//                }
            }

            @Override
            public void continueDrawing(ChatMessage message) {
            }
            @Override
            public void regenerateDrawing(ChatMessage message) {
            }

            @Override
            public void downloadDrawing(ChatMessage message) {
            }

            @Override
            public void viewDrawing(ChatMessage message) {
            }
        });
        rv_chat.setAdapter(chatAdapter);
        rv_chat.setOnTouchListener((view, motionEvent) -> {
            ZUtils.print("rv_chat motionEvent = "+motionEvent.getAction());
            ZUtils.print("rv_chat mChat.getStreamEnd().getValue() = "+viewModel.getStreamEnd().getValue());
            ZInputMethod.closeInputMethod(getActivity(), view);
//
//            if(type == TYPE_DRAWING){
//                return false;
//            }

            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                if(!viewModel.getStreamEnd().getValue()){
                    iv_scroll_down.setVisibility(View.VISIBLE);
                    isUserTouch = true;
                }

                lastX = motionEvent.getRawX();
                lastY = motionEvent.getRawY();
            }else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
                if(lastY-rawY>0){//向上滑，隐藏
//                    iv_scroll_down.setVisibility(View.GONE);
                    isUserTouch = false;
                }else {
                    iv_scroll_down.setVisibility(View.VISIBLE);
                    isUserTouch = true;
                }
            }
            return false;
        });
    }


    @Override
    protected void setupDataBinding() {
        // 监听 ViewModel 的 LiveData
        viewModel.getChatMessages().observe(this, messages -> {
//
//            rv_chat.scrollToPosition(messages.size() - 1);
            if(messages.size() == 0){
                return;
            }
            ZUtils.print("viewModel observe = "+messages.size());
            if(messages.size() == 1){
                chatAdapter.notifyDataSetChanged();
            }else {
                chatAdapter.notifyItemChanged(messages.size() - 1,0);
                scroll2Last();
            }
        });
        viewModel.getLoading().observe(this, isLoading -> {
            // 可根据 isLoading 显示/隐藏 loading UI
        });
        viewModel.getStreamEnd().observe(this, end -> {
            if (end) ll_stop.setVisibility(View.GONE);
        });
        viewModel.getThinkStatus().observe(this, status->{

            if(status == Constant.ThinkState.START){
//                TTSUtils.getInstance().ttsStart();
                ll_stop.setVisibility(View.VISIBLE);
                ll_resend.setVisibility(View.GONE);
            }else if(status == Constant.ThinkState.THINKING){
//                ll_stop.setVisibility(View.VISIBLE);
            }else if(status == Constant.ThinkState.END){
                ll_stop.setVisibility(View.GONE);
                isUserTouch = false;
            }

        });

        viewModel.getIsAutoPlay().observe(this, isAutoPlay -> {


//            if(isAutoPlay){
//                iv_top_play.setBackground(getResources().getDrawable(R.mipmap.chat_top_play));
//                TTSUtils.getInstance().startPlayer();
//            }else {
//                iv_top_play.setBackground(getResources().getDrawable(R.mipmap.chat_top_mute));
//                TTSUtils.getInstance().pausePlayer();
////                TTSUtils.getInstance().ttsCancel();
//            }
//            SharedPreferencesUtil.saveBoolean(Constants.KEY_IS_AUTO,isAutoPlay);

        });
    }
    
    @Override
    protected void setupObservers() {
//        // 观察问答内容变化
//        viewModel.getQaContent().observeForever(content -> {
//            if (getActivity() != null && isAdded()) {
//                getActivity().runOnUiThread(() -> {
//                    if (content != null && !content.trim().isEmpty()) {
//                        addAiMessage(content);
//                    }
//                });
//            }
//        });
//
//        // 观察问题输入状态
//        viewModel.getQuestionInput().observeForever(question -> {
//            updateSendButtonState();
//        });
//
//        // 观察加载状态
//        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
//            btnSend.setEnabled(!loading);
//            if (loading) {
//                btnSend.setAlpha(0.5f);
//            } else {
//                btnSend.setAlpha(1.0f);
//            }
//        });
//
//        // 观察错误状态
//        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
//            if (error != null) {
//                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
//            }
//        });
    }
    
    private void setDefaultContent() {
        // AI介绍已经在布局中设置，这里不需要额外操作
    }
    
    // 内容显示已由AIResponseView处理
    
//    private void updateSendButtonState() {
//        String question = viewModel.getQuestionInput().get();
//        boolean hasQuestion = !TextUtils.isEmpty(question) && !question.trim().isEmpty();
//        btnSend.setEnabled(hasQuestion);
//        btnSend.setAlpha(hasQuestion ? 1.0f : 0.5f);
//    }
    
    private void setupButtonListeners() {
        // 发送按钮
//        if (btnSend != null) {
//            btnSend.setOnClickListener(v -> {
//                sendQuestion();
//            });
//        }
        
        // 按钮功能已由AIResponseView内部处理
    }
    
//    private void sendQuestion() {
//        String question = viewModel.getQuestionInput().get();
//        if (TextUtils.isEmpty(question) || question.trim().isEmpty()) {
//            Toast.makeText(getContext(), "请输入问题", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (transcriptionResult == null || transcriptionResult.trim().isEmpty()) {
//            Toast.makeText(getContext(), "需要先完成语音识别", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 隐藏AI介绍
//        if (llAiIntroContainer != null) {
//            llAiIntroContainer.setVisibility(View.GONE);
//        }
//
//        // 保存用户消息
//        String trimmedQuestion = question.trim();
//        viewModel.setLastUserMessage(trimmedQuestion);
//
//        // 委托给ViewModel处理
//        viewModel.askQuestion(trimmedQuestion, transcriptionResult);
//
//        // 清空输入框
//        viewModel.getQuestionInput().set("");
//    }
    
    // 复制功能已由AIResponseView内部处理
    
    // 刷新功能已由AIResponseView内部处理
    
    // 更多选项功能暂时移除
    
//    private void addAiMessage(String message) {
//        // 创建ChatMessageView
//        ChatMessageView chatMessageView = new ChatMessageView(getContext());
//
//        // 获取最后一个用户消息
//        String lastUserMessage = viewModel.getLastUserMessage();
//
//        // 设置带思维链的消息
//        chatMessageView.setMessageWithThinking(
//            lastUserMessage,
//            message,
//            2, // 思考时间2秒
//            "正在分析会议内容，生成回答..."
//        );
//
//        // 设置刷新按钮监听器
//        chatMessageView.setOnRefreshClickListener(new AIResponseView.OnRefreshClickListener() {
//            @Override
//            public void onRefreshClick() {
//                String questionToRetry = chatMessageView.getUserInputView().getText();
//                if (questionToRetry != null && !questionToRetry.isEmpty()) {
//                    // 移除当前消息
//                    llChatContainer.removeView(chatMessageView);
//                    // 重新提问
//                    viewModel.askQuestion(questionToRetry, transcriptionResult);
//                }
//            }
//        });
//
//        // 设置布局参数
//        chatMessageView.setLayoutParams(new LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT));
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) chatMessageView.getLayoutParams();
//        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
//        chatMessageView.setLayoutParams(params);
//
//        llChatContainer.addView(chatMessageView);
//        scrollToBottom();
//    }
    
//    private void scrollToBottom() {
//        scrollContent.post(() -> {
//            scrollContent.fullScroll(View.FOCUS_DOWN);
//        });
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                showToast("需要录音权限才能使用功能");
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AsrOneUtils.getInstance().removeCallBack();
        // 释放所有ChatMessageView资源
//        if (llChatContainer != null) {
//            for (int i = 0; i < llChatContainer.getChildCount(); i++) {
//                View child = llChatContainer.getChildAt(i);
//                if (child instanceof ChatMessageView) {
//                    ((ChatMessageView) child).release();
//                }
//            }
//        }
    }
}
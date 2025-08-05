package com.fxzs.lingxiagent.view.chat;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.cmdc.ai.assist.api.AIFoundationKit;
import com.cmdc.ai.assist.api.SpeechRecognitionPersistent;
import com.cmdc.ai.assist.constraint.DialogueResult;
import com.cmdc.ai.assist.constraint.SpeechRecognitionPersistentData;
import com.example.service_api.HttpUrlConnectionHonor;
import com.fxzs.lingxiagent.conversation.AIConversationManager;
import com.fxzs.lingxiagent.conversation.ChatLingXiAdapter;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.ChatDataFormat;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.ChatManager;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.Message;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.TabEntity;
import com.fxzs.lingxiagent.lingxi.main.utils.BroadcastUtils;
import com.fxzs.lingxiagent.lingxi.multimodal.utils.TtsMediaPlayer;
import com.fxzs.lingxiagent.model.chat.callback.AIMeetingEditCallback;
import com.fxzs.lingxiagent.model.chat.callback.AITranslateEditCallback;
import com.fxzs.lingxiagent.model.chat.callback.MsgActionCallback;
import com.fxzs.lingxiagent.model.chat.callback.RequestCallback;
import com.fxzs.lingxiagent.model.chat.callback.SoftCallback;
import com.fxzs.lingxiagent.model.chat.callback.SuperEditCallback;
import com.fxzs.lingxiagent.model.chat.callback.SuperShareCallback;
import com.fxzs.lingxiagent.model.chat.dto.ChatFileBean;
import com.fxzs.lingxiagent.model.chat.dto.ChatFileListJsonBean;
import com.fxzs.lingxiagent.model.chat.dto.ChatFunctionBean;
import com.fxzs.lingxiagent.model.chat.dto.ChatMessage;
import com.fxzs.lingxiagent.model.chat.dto.ConversationDetailDto;
import com.fxzs.lingxiagent.model.chat.dto.DrawingToChatBean;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.chat.dto.ShareItem;
import com.fxzs.lingxiagent.model.chat.repository.ChatRepository;
import com.fxzs.lingxiagent.model.chat.repository.ChatRepositoryImpl;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingStyleDto;
import com.fxzs.lingxiagent.network.ZNet.ApiResponse;
import com.fxzs.lingxiagent.network.ZNet.HttpRequest;
import com.fxzs.lingxiagent.network.ZNet.bean.ChatContent;
import com.fxzs.lingxiagent.network.ZNet.bean.getCatDetailListBean;
import com.fxzs.lingxiagent.util.DocumentHelper;
import com.fxzs.lingxiagent.util.GlobalDataHolder;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.util.SuperEditUtil;
import com.fxzs.lingxiagent.util.TtsXiaDuMediaPlayer;
import com.fxzs.lingxiagent.util.ZInputMethod;
import com.fxzs.lingxiagent.util.ZUtil.AsrOneUtils;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.util.ZUtil.DrawingActionUtils;
import com.fxzs.lingxiagent.util.ZUtil.SuperAgentUtil;
import com.fxzs.lingxiagent.util.ZUtil.SuperEditAITranslateUtil;
import com.fxzs.lingxiagent.util.ZUtil.SuperEditAIWritingUtil;
import com.fxzs.lingxiagent.util.ZUtil.SuperLongPicUtil;
import com.fxzs.lingxiagent.util.ZUtil.TTSUtils;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.common.BaseFragment;
import com.fxzs.lingxiagent.view.common.ExportFileDialog;
import com.fxzs.lingxiagent.view.common.LoadingProgressDialog;
import com.fxzs.lingxiagent.view.common.VoiceRecordView;
import com.fxzs.lingxiagent.view.drawing.DrawingActivity;
import com.fxzs.lingxiagent.view.drawing.DrawingImageViewerActivity;
import com.fxzs.lingxiagent.view.meeting.MeetingSummaryFragment;
import com.fxzs.lingxiagent.viewmodel.chat.VMChat;
import com.fxzs.lingxiagent.viewmodel.meeting.VMMeetingSummary;
import com.fxzs.lingxiagent.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class SuperChatFragment extends BaseFragment<VMChat> {
    private static final String TAG = "SuperChatFragment";
    public static final int TYPE_HOME = 1;//首页
    public static final int TYPE_AGENT = 2;//智能体
    public static final int TYPE_DRAWING = 3;//绘画-对话界面
    public static final int TYPE_MEETING = 4;//会议-会议摘要
    public static final int TYPE_MEETING_QA = 5;//会议-智能问答
    public static final int DEFAULT_LAYOUT = 0;
    public static final int LONG_PIC_LAYOUT = 1;
    public static final int SAVE_FILE_LAYOUT = 2;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private static final int REQUEST_CONTINUE_EDIT = 2;
    private static final int REQUEST_DRAWING_VIEW = 3;
    private LinearLayout root_view;
    private LinearLayout ll_bottom;
    private LinearLayout ll_edit_writing;
    private LinearLayout ll_edit_translate;
    private LinearLayout ll_edit_main;
    private LinearLayout ll_edit_agent;
    private LinearLayout mShareBottom;
    private LinearLayout mLongPicLayout;
    private SuperEditUtil superEditUtil;
    private SuperAgentUtil superAgentUtil;
    private SuperEditAIWritingUtil superEditAIWritingUtil;
    private SuperEditAITranslateUtil superEditAITranslateUtil;
    private SuperLongPicUtil mLongPicUtil;
    private EditText ed;
    private RecyclerView rv_chat, rv_function;
    private ChatFunctionAdapter chatFunctionAdapter;
    private ChatAdapter chatAdapter;
    private TextView aiResponse;
    private View ll_stop;
    private View ll_resend;
    private TextView tv_mode;
    private ImageView iv_scroll_down;
    private ImageView iv_history;
    private ImageView iv_top_play;
    private View ll_header;
    private VMChat vmChat;

    List<ChatFileBean> fileList;
    private OptionModel selectOptionModel;
    private boolean isUserTouch;//返回流时用户是否操作
    private boolean isSoft;
    private int type;//跳转类型
    private boolean mIsUserScrolling = false;
    private ChatMessage currentViewChatMessage;
    private VMMeetingSummary vmMeetingSummary;
    Map<String, Object> meetingMap ;

    private VoiceRecordView voiceRecordView;
    private List<ChatMessage> mSelectMessages = new ArrayList<>();
    private RecyclerView mShareItemList;
    private ShareItemtAdapter mShareItemAdapter;
    private List<ShareItem> mShareDatas = new ArrayList<>();

    private AIFoundationKit aiFoundationKit;
    private SpeechRecognitionPersistent speechRecognitionPersistent;
    private boolean isVoiceCancel;

    private String curAsrResult = "";
    private final int PRESS_DOWN = 1;
    private final int PRESS_UP = 2;
    private final int PRESS_MOVE = 3;
    private LinearLayout llSelectAgent;
    private TextView tvHeaderSelectAgent;
    private ImageView ivHead,ivCreateChat;
    private final String LINGXI_MODEL = "10086";

    private String requestId;
    private ChatManager chatManager;
    private ChatDataFormat chatDataFormat;
    private HttpUrlConnectionHonor honorHttp;
    private AIConversationManager aiConversationManager;

    //历史进入
    public SuperChatFragment(int type,String input,long id,OptionModel optionModel ) {
        Bundle args = new Bundle();
        args.putInt(Constant.INTENT_TYPE, type);
        args.putLong(Constant.INTENT_ID, id);
        args.putString(Constant.INTENT_DATA, input);
        args.putSerializable(Constant.INTENT_DATA1, optionModel);
        setArguments(args);
    }
    //首页
    public SuperChatFragment(int type,String input,OptionModel optionModel,List<ChatFileBean> list ) {
        Bundle args = new Bundle();
        args.putInt(Constant.INTENT_TYPE, type);
        args.putString(Constant.INTENT_DATA, input);
        args.putSerializable(Constant.INTENT_DATA1, optionModel);
        args.putSerializable(Constant.INTENT_DATA2, (Serializable) list);
        setArguments(args);
    }
    //智能体
    public SuperChatFragment(int type, long id, getCatDetailListBean bean ) {
        Bundle args = new Bundle();
        args.putInt(Constant.INTENT_TYPE, type);
        args.putLong(Constant.INTENT_ID, id);
        args.putSerializable(Constant.INTENT_DATA2, (Serializable) bean);
        setArguments(args);
    }
    //绘画
    public SuperChatFragment(int type, DrawingToChatBean drawingToChatBean, DrawingStyleDto drawingStyleDto) {
        Bundle args = new Bundle();
        args.putInt(Constant.INTENT_TYPE, type);
        args.putSerializable(Constant.INTENT_DATA, drawingToChatBean);
        args.putSerializable(Constant.INTENT_DATA1, drawingStyleDto);
        setArguments(args);
    }

    //会议摘要
    public SuperChatFragment(int type, Map<String,Object> map) {
        Bundle args = new Bundle();
        args.putInt(Constant.INTENT_TYPE, type);
        args.putSerializable(Constant.INTENT_DATA, (Serializable) map);
        setArguments(args);
    }
    public void init() {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.act_super_chat);
        GlobalDataHolder.init(requireContext()); // 初始化全局共享数据
        vmChat = new ViewModelProvider(this).get(VMChat.class);

        ll_bottom = findViewById(R.id.ll_bottom);
        ll_edit_writing = findViewById(R.id.ll_edit_writing);
        ll_edit_translate = findViewById(R.id.ll_edit_translate);
        ll_edit_main = findViewById(R.id.ll_edit_main);
        ll_edit_agent = findViewById(R.id.ll_edit_agent);
        root_view = findViewById(R.id.root_view);
        rv_chat = findViewById(R.id.rv_chat);
        rv_function = findViewById(R.id.rv_function);
        ed = findViewById(R.id.ed);
        aiResponse = findViewById(R.id.aiResponse);
        ll_stop = findViewById(R.id.ll_stop);
        ll_resend = findViewById(R.id.ll_resend);
        tv_mode = findViewById(R.id.tv_mode);
        iv_scroll_down = findViewById(R.id.iv_scroll_down);
        iv_history = findViewById(R.id.iv_history);
        iv_top_play = findViewById(R.id.iv_top_play);
        ll_header = findViewById(R.id.ll_header);
        mShareBottom = findViewById(R.id.ll_share_bottom);
		voiceRecordView = findViewById(R.id.voiceRecordView);
        mLongPicLayout = findViewById(R.id.ll_longpic_layout);
        mShareItemList = findViewById(R.id.share_list);
        llSelectAgent = findViewById(R.id.ll_select_agent);
        tvHeaderSelectAgent = findViewById(R.id.tv_header_select_agent);
        ivHead = findViewById(R.id.iv_head);
        ivCreateChat = findViewById(R.id.iv_create_chat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        mShareItemList.setLayoutManager(layoutManager);

        mShareItemAdapter = new ShareItemtAdapter(mShareDatas, mItemClickListener);
        mShareItemList.setAdapter(mShareItemAdapter);
        initHonor();
        initChatManager();
        initAIConversationManager();
        if (getArguments() != null) {
            type = getArguments().getInt(Constant.INTENT_TYPE, SuperChatFragment.TYPE_HOME);
            if(type == TYPE_HOME){//首页
                ivHead.setVisibility(View.VISIBLE);
                llSelectAgent.setVisibility(View.VISIBLE);
                ivCreateChat.setVisibility(View.VISIBLE);
                selectOptionModel = (OptionModel) getArguments().getSerializable(Constant.INTENT_DATA1);
                setFunctionRv();
                setBottomEdit();
                String input = getArguments().getString(Constant.INTENT_DATA);
                long id = getArguments().getLong(Constant.INTENT_ID,0);
                vmChat.setSelectOptionModel(selectOptionModel);
                if(id != 0){
                    vmChat.getConversationId().setValue(id);
                    loadConversationHistory(id);
                    if(superEditUtil!= null){
                        superEditUtil.setSelectOptionModel(selectOptionModel);
                        superEditUtil.setBanSelectModel(true);
                    }
                }else{
                    fileList = (List<ChatFileBean>) getArguments().getSerializable(Constant.INTENT_DATA2);
                    if(fileList != null){//带图片的/带文件
                        vmChat.setSelectOptionModel(selectOptionModel);
                        if (input != null) {
                            vmChat.sendMessageWithFile(input,fileList);
                        }
                    }else if(selectOptionModel != null){//纯文本对话
                        vmChat.setSelectOptionModel(selectOptionModel);
                        if(superEditUtil!= null){
                            superEditUtil.setSelectOptionModel(selectOptionModel);
                        }
                        if (input != null && selectOptionModel != null) {
                            tv_mode.setText(selectOptionModel.getName());
                            vmChat.sendMessage(input);
                        }
                    }
                }
            }else if(type == TYPE_AGENT){//智能体
                getCatDetailListBean bean = (getCatDetailListBean) getArguments().getSerializable(Constant.INTENT_DATA2);

                if(bean != null){
                    if(bean.getIcon() != null){
                        vmChat.getChatMessages().getValue().add(new ChatMessage(bean.getDescription(),ChatAdapter.TYPE_USER_HEAD_AGENT,bean.getIcon()));

                        ChatMessage msg = vmChat.addAIMsg(bean.getPreInput());
                        msg.setHideActionRefresh(true);
                        vmChat.setSelectAgentBean(bean);
                    }else {
                        getAgentHeadInfo(bean.getModelId(), new RequestCallback<getCatDetailListBean>() {
                            @Override
                            public void callback(getCatDetailListBean data) {
                                if(data != null){

                                    vmChat.getChatMessages().getValue().add(new ChatMessage(data.getDescription(),ChatAdapter.TYPE_USER_HEAD_AGENT,data.getIcon()));

                                    ChatMessage msg = vmChat.addAIMsg(data.getPreInput());
                                    msg.setHideActionRefresh(true);
                                    vmChat.setSelectAgentBean(data);
                                    long id = getArguments().getLong(Constant.INTENT_ID,0);
                                    if(id != 0){
                                        loadConversationHistory(id);
                                    }
                                }
                            }
                        });
                    }
                }

                setAgentBottomEdit();
                iv_history.setVisibility(View.GONE);
            }else if(type == TYPE_DRAWING){//绘画
                DrawingToChatBean bean = (DrawingToChatBean) getArguments().getSerializable(Constant.INTENT_DATA);
                DrawingStyleDto styleDto = (DrawingStyleDto) getArguments().getSerializable(Constant.INTENT_DATA1);
                vmChat.setSelectDrawingToChatBean(bean);
                vmChat.setSelectDrawingStyleDto(styleDto);
                if(bean != null){

                    // 首先检查是否从历史记录进入
                    Long sessionId =  bean.getSessionId();
                    vmChat.getConversationId().postValue(Long.parseLong(sessionId.toString()));
                    DrawingSessionDto sessionDetail = bean.getSessionDetail();

                    if (sessionDetail != null && sessionDetail.getAiImageList() != null) {
                        // 从历史记录进入，显示会话详情
                        displaySessionHistory(sessionDetail);

                    }else{

                        // 正常的生成流程
                        String prompt = bean.getPrompt();
                        String style = bean.getStyle();
                        String styleId = bean.getStyle_id();
                        String ratio = bean.getRatio();
                        String referenceImageUrl = bean.getReference_image_url();

                        android.util.Log.d("DrawingChatActivity", "Intent data - prompt: " + prompt + ", style: " + style +
                                ", styleId: " + styleId + ", ratio: " + ratio + ", referenceImageUrl: " + referenceImageUrl);

                        vmChat.setSelectedRatio(ratio);
                        if (prompt != null && !prompt.isEmpty()) {
                            // 添加用户消息
                            vmChat.sendDrawingMessage(prompt);
                        }
                    }

                }
                setAgentBottomEdit();
                iv_history.setVisibility(View.GONE);
                iv_top_play.setVisibility(View.GONE);
            }else if(type == TYPE_MEETING){//会议摘要
                ll_header.setVisibility(View.GONE);
                Bundle args = getArguments();
                if (args != null) {
                    Map<String, Object> map = (Map<String, Object>) args.getSerializable(Constant.INTENT_DATA); // 获取 Map
                    // 使用 type 和 map 进行后续逻辑
                    if (map != null) {
                        // 处理 map 数据
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            ZUtils.print("SuperChatFragment == "+"Key: " + entry.getKey() + ", Value: " + entry.getValue().toString());
                        }
                    }

                    initMeetingSummary(map);
                }
            }
        }

        setChatRv();
        TTSUtils.getInstance().init(getActivity());
        TTSUtils.getInstance().process();

        ZUtils.print("TTSUtils.getInstance().ttsStart");
        TTSUtils.getInstance().ttsInit();




        ll_stop.setOnClickListener(view -> {
            vmChat.closeSSE();
            ll_stop.setVisibility(View.GONE);
            ll_resend.setVisibility(View.VISIBLE);
            
            // 取消正在进行的Markdown渲染
            if (chatAdapter != null) {
                chatAdapter.cancelAllMarkdownRendering();
            }
        });

        iv_scroll_down.setOnClickListener(view -> {
            isUserTouch = false;
            iv_scroll_down.setVisibility(View.GONE);

//            if(vmChat.getStreamEnd().getValue()){//已经结束流，手动调用代码滑动
                scroll2Last();
//            }
        });
        ll_resend.setOnClickListener(view -> {
            ChatMessage message = vmChat.getResendMsg();
            if(message != null && message.getMsgType() == ChatAdapter.TYPE_USER){
                ZUtils.print("message.getMessage() == "+message.getMessage());
                ed.setText(message.getMessage());
                ed.setSelection(message.getMessage().length());
//                vmChat.resendMsg();
                ll_resend.setVisibility(View.GONE);
                if(superEditUtil != null){
                    superEditUtil.switchMode(0);
                }
                if(superAgentUtil != null){
                    superAgentUtil.switchMode(0);
                }
            }
        });
        vmChat.getIsAutoPlay().postValue(SharedPreferencesUtil.getBoolean(Constants.KEY_IS_AUTO,true));
        iv_top_play.setOnClickListener(view -> {
            vmChat.getIsAutoPlay().postValue(!vmChat.getIsAutoPlay().getValue());
        });
        iv_history.setOnClickListener(view -> {
        });
        rv_chat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mIsUserScrolling = (newState == RecyclerView.SCROLL_STATE_DRAGGING);


               if(recyclerView.canScrollVertically(1) == false){//的值表示是否能向上滚动，false表示已经滚动到底部
                   iv_scroll_down.setVisibility(View.GONE);
               }

            }
        });

        llSelectAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (superEditUtil != null ){
                    superEditUtil.showChooseModelPopup(tvHeaderSelectAgent);
                }
            }
        });
    }

    private void initMeetingSummary(Map<String, Object> map) {
        meetingMap = map;
        vmMeetingSummary = new ViewModelProvider(this).get(VMMeetingSummary.class);

        String transcriptionResult = (String) map.get(MeetingSummaryFragment.ARG_TRANSCRIPTION_RESULT);
        int meetingIdInt = (int) map.get(MeetingSummaryFragment.ARG_MEETING_ID);
        String botKey = (String) map.get(MeetingSummaryFragment.ARG_BOTKEY);
        // android.util.Log.i("MeetingSummaryFragment", "切换到标签: " + getSelectedTagName() + ", botKey: " + botKey);
        vmMeetingSummary.generateMeetingSummaryWithCheck(transcriptionResult, meetingIdInt, botKey);
    }

    private void getAgentHeadInfo(long id, RequestCallback<getCatDetailListBean> callback) {

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.getDetailByModel(id + "", new Observer<ApiResponse<getCatDetailListBean>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ApiResponse<getCatDetailListBean> getCatDetailListBeanApiResponse) {
                callback.callback(getCatDetailListBeanApiResponse.getData());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void loadConversationHistory(long id) {
        // 显示加载框
        LoadingProgressDialog loadingDialog =
                new LoadingProgressDialog(getActivity())
                        .setMessage("加载中...")
                        .setCancelable(false);
        loadingDialog.show();

        ChatRepository repository = new ChatRepositoryImpl();
        // 调用API获取会话详情
        repository.getListByConversationId(id, new ChatRepository.Callback<List<ConversationDetailDto>>() {
            @Override
            public void onSuccess(List<ConversationDetailDto> list) {
                loadingDialog.dismiss();
                if(list != null && list.size() > 0){
                    for (int i = 0; i < list.size(); i++) {
                        ConversationDetailDto dto =  list.get(i);
                        if(!TextUtils.isEmpty(dto.getContent())){
                            if(dto.getType().equals("assistant")){
                                ZUtils.print("onSuccess assistant = "+dto.getContent());
                                vmChat.addAIMsgHistory(dto.getContent());
                            }else if(dto.getType().equals("user")){
                                ZUtils.print("onSuccess user = "+dto.getContent());
                                vmChat.sendMessageHistory(dto.getContent());
                                List<ChatFileBean> files = new ArrayList<>();
                                if(dto.getFileListJson() != null){
                                    List<ChatFileListJsonBean> eventList = new Gson().fromJson(dto.getFileListJson() ,
                                            new TypeToken<List<ChatFileListJsonBean>>() {}.getType());

                                    if(eventList != null && eventList.size() > 0){
                                        for (int j = 0; j < eventList.size(); j++) {
                                            ChatFileListJsonBean bean =  eventList.get(j);
                                            ChatFileBean chatFileBean = new ChatFileBean(bean.getName(),bean.getFileUrl(),false);
                                            chatFileBean.setFileType(bean.getType());
                                            files.add(chatFileBean);
                                        }
                                    }
                                }else if(dto.getImages()!=null){
                                        String[] results = dto.getImages().split(",");

                                        if(results != null && results.length > 0){
                                            for (int j = 0; j < results.length; j++) {
                                                String url =  results[j];
                                                ChatFileBean chatFileBean = new ChatFileBean(url,true);
                                                chatFileBean.setPath(url);
                                                chatFileBean.setPercent(100);
                                                files.add(chatFileBean);
                                            }
                                        }

                                }

                                vmChat.addUserMsgWithFile(files);
                            }
                        }
                    }
                }

            }

            @Override
            public void onError(String error) {

            }
        });

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.act_super_chat;
    }

    @Override
    protected Class getViewModelClass() {
        return VMChat.class;
    }

    @Override
    protected void initializeViews(View view) {
        initializeViews();
    }

    @Override
    protected void setupDataBinding() {

    }

    public  VMChat getVMChat(){
        return  vmChat;
    }
    protected void initializeViews() {
        // 设置状态栏为白色，与账号信息页统一
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            getWindow().setStatusBarColor(android.graphics.Color.parseColor("#FFFFFF"));
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            }
//        }
        init();
    }

    @Override
    protected void setupObservers() {
        // 监听 ViewModel 的 LiveData
        vmChat.getChatMessages().observe(this, messages -> {
//
//            rv_chat.scrollToPosition(messages.size() - 1);
            if(messages.size() == 0){
                return;
            }
            ZUtils.print("vmChat observe = "+messages.size());
            if(messages.size() == 1){
                chatAdapter.notifyDataSetChanged();
            }else {
                chatAdapter.notifyItemChanged(messages.size() - 1,0);
                scroll2Last();
            }
        });
        vmChat.getAiResponse().observe(this, response -> aiResponse.setText(response));
        vmChat.getLoading().observe(this, isLoading -> {
            // 可根据 isLoading 显示/隐藏 loading UI
        });
        vmChat.getStreamEnd().observe(this, end -> {
            if (end) ll_stop.setVisibility(View.GONE);
            if (chatAdapter != null) {
                chatAdapter.notifyDataSetChanged();
            }
        });
        vmChat.getThinkStatus().observe(this, status->{

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

        vmChat.getIsAutoPlay().observe(this, isAutoPlay -> {


            if(isAutoPlay){
                iv_top_play.setBackground(getResources().getDrawable(R.mipmap.chat_top_play));
                TTSUtils.getInstance().startPlayer();
            }else {
                iv_top_play.setBackground(getResources().getDrawable(R.mipmap.chat_top_mute));
                TTSUtils.getInstance().pausePlayer();
//                TTSUtils.getInstance().ttsCancel();
            }
            SharedPreferencesUtil.saveBoolean(Constants.KEY_IS_AUTO,isAutoPlay);

        });
        // 其他 LiveData 监听可按需添加
        vmChat.getConversationId().observe(this,id ->{
            if(superEditUtil != null){
                superEditUtil.conversationId = id;
            }
        });

        // 观察流式摘要内容变化
        if(vmMeetingSummary != null){
            vmMeetingSummary.getSummaryStreamContent().observeForever(content -> {
                ZUtils.print("vmMeetingSummary = "+vmChat.getChatMessages().getValue().size() );
                ZUtils.print("vmMeetingSummary = "+content );
                if(vmChat.getChatMessages().getValue().size() == 0){

                    ChatMessage aiMessage = vmChat.addAIMsg();
                    aiMessage.setThinkMessage("");
                    aiMessage.setStatus(Constant.ThinkState.END);
//                    thinkMessageTitle.postValue("思考中");
//                    thinkStatus.postValue(Constant.ThinkState.START);
//                    isStreamEnd = false;
//                    streamEnd.postValue(false);
                }else {
                    MutableLiveData<List<ChatMessage>>  chatMessages = vmChat.getChatMessages();
                    int length = chatMessages.getValue().size();
                    ChatMessage aiMessage = chatMessages.getValue().get(length - 1);
//                    aiMessage.setThinkMessage(ResponseThink);
                    aiMessage.setThinkMessage("");
                    aiMessage.setMessage(content);
                    aiMessage.setStatus(Constant.ThinkState.END);

                    chatMessages.postValue(chatMessages.getValue());
                    scroll2Last();
                }
//            if (aiResponseView != null && content != null && !content.isEmpty()) {
//                // android.util.Log.d("MeetingSummaryFragment", "流式内容更新: " + content.length() + " 字符");
//
//                // 更新内容
//                aiResponseView.setContent(content);
//
//                // 检查是否需要自动滚动
//                checkAndAutoScroll();
//            }
            });
        }
    }


    float lastX;
    float lastY;
    private void setChatRv() {
        rv_chat.setItemAnimator(null);
        rv_chat.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatAdapter = new ChatAdapter(getActivity(), vmChat.getChatMessages().getValue());
        chatAdapter.setCallback(new MsgActionCallback() {
            @Override
            public void refresh(String content) {
                TTSUtils.getInstance().cancelAndPlay();
                if(type == TYPE_MEETING){
                    if(meetingMap != null){

                        String transcriptionResult = (String) meetingMap.get(MeetingSummaryFragment.ARG_TRANSCRIPTION_RESULT);
                        int meetingIdInt = (int) meetingMap.get(MeetingSummaryFragment.ARG_MEETING_ID);
                        String botKey = (String) meetingMap.get(MeetingSummaryFragment.ARG_BOTKEY);
                        vmMeetingSummary.forceRegenerateSummary(transcriptionResult,meetingIdInt,botKey);
//                        vmMeetingSummary.generateMeetingSummaryStream(transcriptionResult,meetingIdInt,botKey);
                    }
                }else{
                    vmChat.refreshSendMessage(content,type);
                }
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
                continueDrawings(message);
            }
            @Override
            public void regenerateDrawing(ChatMessage message) {
                String prompt = message.getMessage();
                vmChat.sendDrawingMessage(message.getDrawingImageDto()); // 直接显示原始prompt
//                vmChat.updateAIDrawingMsg(message.getDrawingImageDto());
                Toast.makeText(getActivity(), "正在重新生成图片...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void downloadDrawing(ChatMessage message) {
               DrawingActionUtils.performDownload(getActivity(),message.getUrl());
            }

            @Override
            public void viewDrawing(ChatMessage message) {
                currentViewChatMessage = message;
                Intent intent = new Intent(getActivity(), DrawingImageViewerActivity.class);
                intent.putExtra("image_url", message.getUrl());

                String prompt = vmChat.getGeneratedImage().getValue().getPrompt();
                if (prompt == null || prompt.isEmpty()) {
                    prompt =  vmChat.getSelectDrawingToChatBean().getPrompt();
                }
                intent.putExtra("prompt", prompt);
                startActivityForResult(intent,REQUEST_DRAWING_VIEW);
            }
        });
        chatAdapter.setShareListener(mShareClickListener);
        rv_chat.setAdapter(chatAdapter);
        rv_chat.setOnTouchListener((view, motionEvent) -> {
            ZUtils.print("rv_chat motionEvent = "+motionEvent.getAction());
            ZUtils.print("rv_chat mChat.getStreamEnd().getValue() = "+vmChat.getStreamEnd().getValue());
            ZInputMethod.closeInputMethod(getActivity(), view);
//
//            if(type == TYPE_DRAWING){
//                return false;
//            }

            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                if(!vmChat.getStreamEnd().getValue()){
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
                    boolean canScroll = rv_chat.canScrollVertically(1) || rv_chat.canScrollVertically(-1);
                    if (canScroll) {
//                        Log.d("RecyclerView", "内容可滚动，说明内容高度大于 RecyclerView 高度");
                        iv_scroll_down.setVisibility(View.VISIBLE);
                    }
                    isUserTouch = true;
                }
            }
            return false;
        });
    }

    private void setFunctionRv() {
        List<ChatFunctionBean> list = new ArrayList<>();
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_TRAVEL, R.drawable.ic_trive, "出行规划"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_PART, R.drawable.ic_part, "同城聚餐"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_THINK, R.drawable.ic_think, "深度思考"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_LIFE, R.mipmap.ic_life, "生活"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_PHONE, R.mipmap.ic_phone, "通话"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_WORK, R.mipmap.ic_work, "办公"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_PLAY, R.mipmap.ic_play, "娱乐"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_AI_WRITE, R.mipmap.ic_ai_write, "AI写作"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_AI_TRANSLATE, R.mipmap.ic_ai_translate, "AI翻译"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_PPT, R.mipmap.ic_ppt, "PPT生成"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_AI_PIC, R.mipmap.ic_ai_pic, "AI绘画"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_AI_MEETING, R.mipmap.ic_ai_meeting, "AI会议"));
        list.add(new ChatFunctionBean(Constant.ChatFunction.TYPE_VOICE, R.mipmap.ic_voice, "同声传译"));
        rv_function.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        chatFunctionAdapter = new ChatFunctionAdapter(getActivity(), list, position -> {
            ChatFunctionBean bean = list.get(position);
            if (bean.getId() == Constant.ChatFunction.TYPE_LIFE) {
                // 生活
            } else  if (bean.getId() == Constant.ChatFunction.TYPE_PHONE) {
                // 通话
            } else  if (bean.getId() == Constant.ChatFunction.TYPE_WORK) {
                // 办公
            } else  if (bean.getId() == Constant.ChatFunction.TYPE_PLAY) {
                // 娱乐
            } else if (bean.getId() == Constant.ChatFunction.TYPE_AI_WRITE) {
                // AI写作
                ll_bottom.setVisibility(View.GONE);
                ll_edit_writing.setVisibility(View.VISIBLE);
                vmChat.getConversationId().postValue(0l);
                 setBottomAIWritingEdit();
            } else if (bean.getId() == Constant.ChatFunction.TYPE_AI_TRANSLATE) {
                // AI翻译
                vmChat.getConversationId().postValue(0l);
                ll_bottom.setVisibility(View.GONE);
                ll_edit_translate.setVisibility(View.VISIBLE);
                 setBottomTranslateEdit();

            } else if (bean.getId() == Constant.ChatFunction.TYPE_PPT) {
                // PPT 生成
            } else if (bean.getId() == Constant.ChatFunction.TYPE_AI_PIC) {
                // AI绘画
//                finish();
            } else if (bean.getId() == Constant.ChatFunction.TYPE_AI_MEETING) {
                // AI 会议
            } else if (bean.getId() == Constant.ChatFunction.TYPE_VOICE) {
                // 同声传译
            } else if (bean.getId() == Constant.ChatFunction.TYPE_TRAVEL) {
                // 出行规划
            } else if (bean.getId() == Constant.ChatFunction.TYPE_PART) {
                // 同城聚餐
            } else if (bean.getId() == Constant.ChatFunction.TYPE_THINK) {
                // 深度思考
            }
        });
        rv_function.setAdapter(chatFunctionAdapter);
    }

    private void setBottomEdit() {
        TabEntity.agentType = TabEntity.TabType.CHAT;
        ll_edit_main.setVisibility(View.VISIBLE);
        superEditUtil = new SuperEditUtil(getActivity(), ll_edit_main);
        showVoiceAnimate();
        superEditUtil.setOnListenSoft(root_view, new SoftCallback() {
            @Override
            public void show() {
                if(type == TYPE_HOME) {//首页
                    rv_function.setVisibility(View.GONE);
                }
                if(!isSoft){
                    scroll2Last();
                }
                isSoft = true;
            }
            @Override
            public void hide() {
                if(type == TYPE_HOME) {//首页
                    rv_function.setVisibility(View.VISIBLE);
                }
                if(isSoft){
                    scroll2Last();
                }
                isSoft = false;
            }
        });
        superEditUtil.setCallback(new SuperEditCallback() {
            @Override
            public void send(String content, OptionModel optionModel) {
                ZUtils.print("SuperChatActivity send = "+content + " OptionModel = "+optionModel.getName());

                if (!content.isEmpty()) {
                    if(selectOptionModel != null && optionModel != null){
                        ZUtils.print("选中模型: " + selectOptionModel.getName() + ", ID: " + selectOptionModel.getId());
                        ZUtils.print("选中模型optionModel: " + optionModel.getName() + ", ID: " + optionModel.getId());
                        if (selectOptionModel.getId() != optionModel.getId()) {
                            //如果切换模型，就重置conversationId，重新建对话
                            vmChat.getConversationId().setValue(0l);
                        }
                    }
                    selectOptionModel = optionModel;
//                    if (selectOptionModel.getModel().equals(LINGXI_MODEL)){//灵犀
//                        String requestUid = UUID.randomUUID().toString();
//                        requestId = requestUid;
//                        if (!Objects.equals(content, "")) {
//                            chatManager.sendQuestion(content);
//                            chatManager.simulateReply("正在为您加载..", false, false);
//                            handleChatData(content);
//                        }
//                    }else {
                        vmChat.setSelectOptionModel(optionModel);
                        vmChat.sendMessage(content);
                        TTSUtils.getInstance().cancelAndPlay();
//                    }
                    ZInputMethod.closeInputMethod(getActivity(),root_view);   //收起键盘
                }

            }

            @Override
            public void sendWithFile(String content, OptionModel selectOptionModel, List<ChatFileBean> fileList, boolean isFile) {
                vmChat.setSelectOptionModel(selectOptionModel);
                if (content != null) {
                    List<ChatFileBean> list = new ArrayList<>();
                    list.addAll(fileList);
                    vmChat.sendMessageWithFile(content,list);
                }
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
               voiceStatusHandle(PRESS_DOWN,false,false);

//                checkAudioPermission();
//                //暂停tts
//                TTSUtils.getInstance().pausePlayer();
//                TTSUtils.getInstance().ttsCancel();
            }

            @Override
            public void pressUp(boolean isInArea) {
                voiceStatusHandle(PRESS_UP,isInArea,false);

            }

            @Override
            public void voiceMove(boolean status) {
                SuperEditCallback.super.voiceMove(status);
                voiceStatusHandle(PRESS_MOVE,false,status);
            }

            @Override
            public void modeChange(OptionModel model) {
                SuperEditCallback.super.modeChange(model);
                if (model != null && model.getName() !=null){
                    selectOptionModel = model;
                    tvHeaderSelectAgent.setText(model.getName());
                }
            }
        });
    }

    private void setAgentBottomEdit() {
        TabEntity.agentType = TabEntity.TabType.TRIP_AI_AGENT;
        ll_edit_agent.setVisibility(View.VISIBLE);
        ed = ll_edit_agent.findViewById(R.id.ed);
        superAgentUtil = new SuperAgentUtil(getActivity(), ll_edit_agent);
        showVoiceAnimate();
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
//                    selectOptionModel = optionModel;
//                    optionModel = new OptionModel();
//                    vmChat.setSelectOptionModel(optionModel);
                    if(type == TYPE_AGENT){
                        vmChat.sendAgentMessage(content);

                    }else if(type == TYPE_DRAWING){

                        vmChat.sendDrawingMessage(content);
                    }

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
                voiceStatusHandle(PRESS_DOWN,false,false);

                checkAudioPermission();
            }

            @Override
            public void pressUp(boolean isInArea) {
                voiceStatusHandle(PRESS_UP,isInArea,false);

            }

            @Override
            public void voiceMove(boolean status) {
                SuperEditCallback.super.voiceMove(status);
                voiceStatusHandle(PRESS_MOVE,false,status);
            }
        });
    }

    private void setBottomAIWritingEdit() {
        TabEntity.agentType = TabEntity.TabType.TRIP_AI_WRITING;
        ll_stop.setVisibility(View.GONE);
        superEditAIWritingUtil = new SuperEditAIWritingUtil(getActivity(),ll_edit_writing);
        superEditAIWritingUtil.setCallback(
                new AIMeetingEditCallback() {
                    @Override
                    public void send(String content) {
                        vmChat.sendAIWritingMessage(content);
                        ll_bottom.setVisibility(View.VISIBLE);
                        ll_edit_writing.setVisibility(View.GONE);

                    }

                    @Override
                    public void close() {

                        ll_bottom.setVisibility(View.VISIBLE);
                        ll_edit_writing.setVisibility(View.GONE);
                        vmChat.getConversationId().postValue(0l);

                        if (vmChat.getThinkStatus().getValue() != Constant.ThinkState.END) {
                            ll_stop.setVisibility(View.VISIBLE);
                        }

                        ZInputMethod.closeInputMethod(getActivity(),superEditAIWritingUtil.getIv_close());   //收起键盘
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
                        voiceStatusHandle(PRESS_DOWN,false,false);

//                        checkAudioPermission();
                    }


                    @Override
                    public void pressUp(boolean isInArea) {
                        voiceStatusHandle(PRESS_UP,isInArea,false);

                    }

                    @Override
                    public void voiceMove(boolean status) {
                        AIMeetingEditCallback.super.voiceMove(status);
                        Timber.tag(TAG).d("翻译移动"+status);
                        voiceStatusHandle(PRESS_MOVE,false,status);
                    }
                }
        );
    }
    private void setBottomTranslateEdit() {
        TabEntity.agentType = TabEntity.TabType.TRANSLATE;
        ll_stop.setVisibility(View.GONE);
        ll_edit_main.setVisibility(View.VISIBLE);
        superEditAITranslateUtil = new SuperEditAITranslateUtil(getActivity(),ll_edit_translate);
        showVoiceAnimate();

        superEditAITranslateUtil.setCallback(
                new AITranslateEditCallback() {

                    @Override
                    public void send(String content, String prompt) {
                        vmChat.sendTranslateMessage(content,prompt);
                        ll_bottom.setVisibility(View.VISIBLE);
                        ll_edit_translate.setVisibility(View.GONE);
                    }
                    @Override
                    public void close() {

                        ll_bottom.setVisibility(View.VISIBLE);
                        ll_edit_translate.setVisibility(View.GONE);
                        vmChat.getConversationId().postValue(0l);

                        if (vmChat.getThinkStatus().getValue() != Constant.ThinkState.END) {
                            ll_stop.setVisibility(View.VISIBLE);
                        }

                        TTSUtils.getInstance().cancelAndPlay();
                        ZInputMethod.closeInputMethod(getActivity(),superEditAITranslateUtil.getIv_close());   //收起键盘
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
                        voiceStatusHandle(PRESS_DOWN,false,false);

//                        checkAudioPermission();
                    }


                    @Override
                    public void pressUp(boolean isInArea) {
                        voiceStatusHandle(PRESS_UP,isInArea,false);

                    }

                    @Override
                    public void voiceMove(boolean status) {
                        AITranslateEditCallback.super.voiceMove(status);
                        voiceStatusHandle(PRESS_MOVE,false,status);
                    }
                }
        );
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
        if(vmChat.getChatMessages().getValue().size() <= 0){
            return;
        }
//        if(type != TYPE_DRAWING &&
//                vmChat.getChatMessages().getValue().size() > 0 ){
//
//            ChatMessage chatMessage = vmChat.getChatMessages().getValue().get(vmChat.getChatMessages().getValue().size()-1);
//
//            if((chatMessage.getMessage() != null &&
//                    chatMessage.getMessage().length()/25==0)||
//                    (chatMessage.getThinkMessage() != null &&
//                            chatMessage.getThinkMessage().length()/25==0)){
//                    return;
//            }
//        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) rv_chat.getLayoutManager();
        if (layoutManager != null) {
            // 获取最后一条消息的ViewHolder
            RecyclerView.ViewHolder holder = rv_chat.findViewHolderForAdapterPosition(vmChat.getChatMessages().getValue().size() - 1);
            if(holder == null){
                layoutManager.scrollToPosition(vmChat.getChatMessages().getValue().size() - 1);
                holder = rv_chat.findViewHolderForAdapterPosition(vmChat.getChatMessages().getValue().size() - 1);
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
                        layoutManager.scrollToPositionWithOffset(vmChat.getChatMessages().getValue().size() - 1, -textViewHeight);
                    } else {
                        layoutManager.scrollToPosition(vmChat.getChatMessages().getValue().size() - 1);
                    }
                });
            }
        }
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    public void continueDrawings(ChatMessage message) {
//        DrawingImageDto currentImage = vmChat.getGeneratedImage().getValue();

        if(vmChat.getIsGenerating().getValue()){
            ZUtils.showToast("请等待生成成功");
//            GlobalToast.show(IYAApplication.getInstance(),"请等待生成成功", GlobalToast.Type.NORMAL);
            return;
        }
        // 继续编辑
        if (message != null) {
            Intent intent = new Intent(getActivity(), DrawingActivity.class);
            intent.putExtra("continue_edit", true);
            intent.putExtra("from_chat", true);
            intent.putExtra("reference_image_url", message.getUrl());
//            intent.putExtra("original_prompt", currentImage.getPrompt());
            if (vmChat.getSelectDrawingStyleDto() != null) {
                intent.putExtra("style", vmChat.getSelectDrawingStyleDto().getName());
            }
            if (vmChat.getSelectedRatio()!= null) {
                intent.putExtra("ratio", vmChat.getSelectedRatio());
            }

            android.util.Log.d("DrawingChatActivity", "Starting continue edit activity for result");
            // 使用startActivityForResult等待返回结果
            startActivityForResult(intent, REQUEST_CONTINUE_EDIT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                ZUtils.showToast("需要录音权限才能使用功能");
            }
        }

        if (superEditUtil != null) {
            superEditUtil.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        android.util.Log.d("SuperChatFragment", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == REQUEST_CONTINUE_EDIT && resultCode == RESULT_OK && data != null) {
            // 从继续编辑页面返回，获取用户输入的编辑要求
            String editPrompt = data.getStringExtra("edit_prompt");
            String referenceImageUrl = data.getStringExtra("reference_image_url");
            String originalPrompt = data.getStringExtra("original_prompt");
            String style = data.getStringExtra("style");
            String ratio = data.getStringExtra("ratio");
            DrawingStyleDto styleDto = (DrawingStyleDto) data.getSerializableExtra("DrawingStyleDto");

            android.util.Log.d("DrawingChatActivity", "Received continue edit result:");
            android.util.Log.d("DrawingChatActivity", "editPrompt: " + editPrompt);
            android.util.Log.d("DrawingChatActivity", "referenceImageUrl: " + referenceImageUrl);
            android.util.Log.d("DrawingChatActivity", "originalPrompt: " + originalPrompt);
            android.util.Log.d("DrawingChatActivity", "style: " + style);
            android.util.Log.d("DrawingChatActivity", "ratio: " + ratio);

            vmChat.setSelectDrawingStyleDto(styleDto);
            vmChat.setSelectedRatio(ratio);

            if (editPrompt != null && !editPrompt.isEmpty()) {
                // 在当前对话页面中继续编辑
                handleContinueEditInConversation(editPrompt, referenceImageUrl, originalPrompt, style);
            }
        }else if(requestCode == REQUEST_DRAWING_VIEW && resultCode == RESULT_OK){
            //大图浏览-继续编辑
            continueDrawings(currentViewChatMessage);
        }

        if (superEditUtil != null) {
            superEditUtil.onActivityResult(requestCode,resultCode,data);
        }
    }
    /**
     * 在对话页面中处理继续编辑
     */
    private void handleContinueEditInConversation(String editPrompt, String referenceImageUrl, String originalPrompt, String style) {
        android.util.Log.d("DrawingChatActivity", "handleContinueEditInConversation called");

        DrawingToChatBean bean = new DrawingToChatBean();
        // 设置继续编辑模式
        vmChat.setContinueEditMode(true);

        // 设置参考图片和原始提示词
        if (referenceImageUrl != null) {
            vmChat.setReferenceImageUrl(referenceImageUrl);
            // 仅在继续编辑时同步赋值到selectDrawingToChatBean
//            DrawingToChatBean bean = vmChat.getSelectDrawingToChatBean();
            if (bean != null) {
                bean.setReference_image_url(referenceImageUrl);
            }
        }
        if (originalPrompt != null) {
            vmChat.setHiddenPrompt(originalPrompt);
        }

        // 添加用户的编辑要求消息
//        DrawingToChatBean bean = vmChat.getSelectDrawingToChatBean();
        if (bean != null) {
            bean.setPrompt(editPrompt);
        }
//        vmChat.setSelectDrawingToChatBean(bean);
        vmChat.sendDrawingMessage(editPrompt,referenceImageUrl);

        // 不再单独添加AI回复文字，将在气泡中一起显示

        // 设置新的prompt并触发生成
//        viewModel.getPrompt().set(editPrompt);

        android.util.Log.d("DrawingChatActivity", "About to call generateImage()");
//        vmChat.generateImage();

        android.util.Log.d("DrawingChatActivity", "Continue edit in conversation completed");
    }


    // 显示会话历史内容
    private void displaySessionHistory(DrawingSessionDto sessionDetail) {
        // 显示所有历史图片和对话
        if (sessionDetail.getAiImageList() != null && !sessionDetail.getAiImageList().isEmpty()) {
            for (DrawingImageDto imageDto : sessionDetail.getAiImageList()) {
                // 显示每个图片的提示词作为用户消息
                if (imageDto.getPrompt() != null && !imageDto.getPrompt().isEmpty()) {
                    vmChat.sendDrawingMessageHistory(imageDto.getPrompt());
                }

                // 不再单独添加AI回复文字，将在气泡中一起显示

                // 设置当前图片，以便支持继续编辑功能
                vmChat.getGeneratedImage().postValue(imageDto);
                // 直接添加结果图片，不再调用displayResult避免重复预加载
//                addResultImage(imageDto);
//                vmChat.addAIDrawingMsg();
                vmChat.updateAIDrawingMsg(100);
                vmChat.updateAIDrawingMsg(imageDto.getImageUrl());
                vmChat.updateAIDrawingMsg(imageDto);
            }

            // 如果有历史图片，获取最后一张图片的URL作为参考图片
            if (!sessionDetail.getAiImageList().isEmpty()) {
                DrawingImageDto lastImage =
                        sessionDetail.getAiImageList().get(sessionDetail.getAiImageList().size() - 1);
                if (lastImage != null && lastImage.getImageUrl() != null) {
                    String referenceImageUrl = lastImage.getImageUrl();
                    String prompt = lastImage.getPrompt();
                    vmChat.setReferenceImageUrl(referenceImageUrl);
                    vmChat.getSelectDrawingToChatBean().setPrompt(prompt);
                    vmChat.getSelectDrawingToChatBean().setReference_image_url(referenceImageUrl);

                }
            }
        }

        // 设置输入框提示文字
        if (ed != null) {
            ed.setHint("继续编辑这个会话...");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        vmChat.closeSSE();

        AsrOneUtils.getInstance().removeCallBack();
        superEditUtil = null;
        ZUtils.print("SuperChatActivity onDestroy = "+superEditUtil);
        TTSUtils.getInstance().stopPlayer();
        TTSUtils.getInstance().ttsCancel();
        
        // 清理ChatAdapter资源
        if (chatAdapter != null) {
            chatAdapter.cleanup();
        }

    }

    private ChatAdapter.OnShareClickListener mShareClickListener = new ChatAdapter.OnShareClickListener() {
        @Override
        public void onShareIconClick() {
            mShareBottom.setVisibility(View.VISIBLE);
            switchShareDatas(DEFAULT_LAYOUT);
        }

    };

    private ShareItemtAdapter.OnItemClickListener mItemClickListener = new ShareItemtAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            boolean closeBottom = true;
            ShareItem item = mShareDatas.get(position);
            mSelectMessages = chatAdapter.getSelectMessages();
            if (mSelectMessages.isEmpty()) {
                Toast.makeText(getActivity(), "请选择要分享的内容", Toast.LENGTH_SHORT);
                return;
            }

            if (item == ShareItem.COPY_LINK) {
                handleCopyLink();
            } else if (item == ShareItem.WECHAT) {
                handleWechat();
            } else if (item == ShareItem.WECHAT_MOMENT) {
                shareImageToWeChatMoments(BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.share_long_pic));
            } else if (item == ShareItem.LONG_PIC) {
                handleLongPic();
                closeBottom = false;
            } else if (item == ShareItem.COPY_TEXT) {
                ZUtils.copy(getActivity(), mSelectMessages.get(0).getMessage());
            } else if (item == ShareItem.SHARE_FILE) {
                handleExportFile();
            } else if (item == ShareItem.SHARE_DELETE) {

            } else if (item == ShareItem.SHARE_SETTING) {
                handleShareAction(mSelectMessages.get(0).getMessage());
            } else if (item == ShareItem.SAVE_PIC) {
                handleSavePic();
            }
            if (closeBottom) {
                mShareBottom.setVisibility(View.GONE);
                mLongPicLayout.setVisibility(View.GONE);
            }
        }
    };

    private void handleCopyLink() {
        List<ChatContent> contents = new ArrayList<>();
        for (ChatMessage message : mSelectMessages) {
            contents.add(new ChatContent(
                    (message.getMsgType() == ChatAdapter.TYPE_USER) ? "user" : "assistant",
                    message.getMessage()));
        }
        new HttpRequest().sendChatLink("分享链接", contents, new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
//                try {
//                    String response = responseBody.string();
//                    JSONObject jsonObject = new JSONObject(response);
//                    String shortUrl = jsonObject.getString("short_url");
//                    ZUtils.copy(mContext, shortUrl);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void handleWechat() {
        List<ChatContent> contents = new ArrayList<>();
        for (ChatMessage message : mSelectMessages) {
            contents.add(new ChatContent(
                    (message.getMsgType() == ChatAdapter.TYPE_USER) ? "user" : "assistant",
                    message.getMessage()));
        }
        new HttpRequest().sendChatLink("分享链接", contents, new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
//                try {
//                    String response = responseBody.string();
//                    JSONObject jsonObject = new JSONObject(response);
//                    String shortUrl = jsonObject.getString("short_url");
//                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                    shareIntent.setType("text/plain");
//                    shareIntent.putExtra(Intent.EXTRA_TEXT, "灵犀对话：" + shortUrl);
//                    ComponentName component = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
//                    shareIntent.setComponent(component);
//                    mContext.startActivity(shareIntent);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void shareImageToWeChatMoments(Bitmap bitmap) {
        try {
            File cachePath = new File(getActivity().getExternalCacheDir(), "share_image.jpg");
            FileOutputStream fOut = new FileOutputStream(cachePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            Uri contentUri = FileProvider.getUriForFile(
                    getActivity(),
                    getActivity().getPackageName() + ".fileprovider",
                    cachePath
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            intent.setPackage("com.tencent.mm");
            intent.setClassName("com.tencent.mm",
                    "com.tencent.mm.ui.tools.ShareToTimeLineUI");

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                getActivity().startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "无法打开微信", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLongPic() {
        mLongPicUtil = new SuperLongPicUtil(getActivity(), mLongPicLayout, rv_chat);
        mLongPicUtil.setCallback(new SuperShareCallback() {

            @Override
            public List<ChatMessage> getSelectMessages() {
                if (chatAdapter != null) {
                    return chatAdapter.getSelectMessages();
                }
                return null;
            }

            @Override
            public void closeBottomLayout() {
                mShareBottom.setVisibility(View.GONE);
            }

            @Override
            public void onShareLongPic() {
                switchShareDatas(LONG_PIC_LAYOUT);
            }
        });
    }

    private void handleExportFile() {
        ExportFileDialog.showExportDialog(getActivity(), new ExportFileDialog.OnExportOptionSelected() {

            @Override
            public void onWordSelected() {
                Toast.makeText(getActivity(), "导出Word文档成功", Toast.LENGTH_SHORT);
            }

            @Override
            public void onPdfSelected() {
                DocumentHelper helper = new DocumentHelper(getActivity());
                helper.createPdfWithTextAndImage(mSelectMessages.get(0).getMessage(),
                        null, "share_pdf_" + System.currentTimeMillis());
                Toast.makeText(getActivity(), "导出PDF文档成功", Toast.LENGTH_SHORT);
            }

            @Override
            public void onTxtSelected() {
                DocumentHelper helper = new DocumentHelper(getActivity());
                StringBuilder texts = new StringBuilder();
                for (ChatMessage message : mSelectMessages) {
                    texts.append(message.getMessage());
                    texts.append("\n");
                }
                helper.generateTextFile(texts.toString(),
                        "share_text_" + System.currentTimeMillis());
                Toast.makeText(getActivity(), "导出TXT文件成功", Toast.LENGTH_SHORT);
            }
        });
    }

    private void handleShareAction(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        getActivity().startActivity(Intent.createChooser(shareIntent, "分享消息"));
    }

    private void handleSavePic() {
        mLongPicUtil.saveLongPic();
    }

    public void switchShareDatas(int type) {
        mShareDatas.clear();
        mShareDatas = new ArrayList<>();
        switch (type) {
            case DEFAULT_LAYOUT:
                mShareDatas.add(ShareItem.COPY_LINK);
                mShareDatas.add(ShareItem.WECHAT);
                mShareDatas.add(ShareItem.WECHAT_MOMENT);
                mShareDatas.add(ShareItem.LONG_PIC);
                mShareDatas.add(ShareItem.COPY_TEXT);
                mShareDatas.add(ShareItem.SHARE_COLLECT);
                mShareDatas.add(ShareItem.SHARE_FILE);
                mShareDatas.add(ShareItem.SHARE_DELETE);
                mShareDatas.add(ShareItem.SHARE_SETTING);
                break;
            case LONG_PIC_LAYOUT:
                mShareDatas.add(ShareItem.SAVE_PIC);
                mShareDatas.add(ShareItem.WECHAT);
                mShareDatas.add(ShareItem.WECHAT_MOMENT);
                break;
            case SAVE_FILE_LAYOUT:
                break;
            default:
                break;
        }
        mShareItemAdapter.updateData(mShareDatas);
    }


    private void showVoiceAnimate() {
        if (voiceRecordView == null) {
            voiceRecordView = findViewById(R.id.voiceRecordView);
        }
    }

    private void toggleAsrRecognition() {
        closeAsr();
        startAsr();
    }

    private void startAsr() {
        Timber.tag(TAG).d("初始化语音识别");
        isVoiceCancel = false;
        if (aiFoundationKit == null) {
            aiFoundationKit = new AIFoundationKit();
        }
        speechRecognitionPersistent = aiFoundationKit.speechRecognitionPersistentHelp();

        speechRecognitionPersistent.setListener(new SpeechRecognitionPersistent.ASRListener() {
            @Override
            public void onMessageReceived(@Nullable SpeechRecognitionPersistentData speechRecognitionPersistentData) {
                Timber.tag(TAG).d("speechRecognition_ onMessageReceived : %s", speechRecognitionPersistentData.toString());
                if (speechRecognitionPersistentData == null) {
                    curAsrResult = "";
                    isVoiceCancel = false;
                    return;
                }
                String type = speechRecognitionPersistentData.getType();
                if (type.equals("FIN_TEXT")) {
                    curAsrResult += speechRecognitionPersistentData.getResult();
                }
                int errNo = speechRecognitionPersistentData.getErrorNumber();
                if (!TextUtils.isEmpty(curAsrResult) && isVoiceCancel && errNo >= 0 && type.equals("FIN_TEXT")) {
                    Timber.tag(TAG).d("识别结果内容 : %s", curAsrResult);
                    if (TabEntity.agentType == TabEntity.TabType.CHAT){
                        if (superEditUtil == null){
                            return;
                        }
                        superEditUtil.sendCommon(curAsrResult);
                    }else if (TabEntity.agentType == TabEntity.TabType.TRIP_AI_AGENT){
                        if (superAgentUtil == null){
                            return;
                        }
                        superAgentUtil.voiceSendText(curAsrResult);
                    }
                    else if (TabEntity.agentType == TabEntity.TabType.TRIP_AI_WRITING){
                        if (superEditAIWritingUtil == null){
                            return;
                        }
                        superEditAIWritingUtil.sendMsg(curAsrResult);
                    }
                    else if (TabEntity.agentType == TabEntity.TabType.TRANSLATE){
                        if (superEditAITranslateUtil == null){
                            return;
                        }
                        superEditAITranslateUtil.sendMsg(curAsrResult);
                    }

                    isVoiceCancel = false;

                }

            }

            @Override
            public void onMessageReceived(@Nullable ByteBuffer byteBuffer) {

            }

            @Override
            public void onClose(int i, @Nullable String s, boolean b) {
                Timber.tag(TAG).d("speechRecognition_ onClose %s", s);

            }

            @Override
            public void onError(@Nullable Exception e) {
                Timber.tag(TAG).d("speechRecognition_ onError %s", e.getMessage());
            }
        });
        Timber.tag(TAG).d("speechRecognition_ startRecognition()");
        speechRecognitionPersistent.startRecognition();
        curAsrResult = "";
    }

    public void closeAsr() {
        if (speechRecognitionPersistent != null) {
            Timber.tag(TAG).d("speechRecognition_ release()");
            speechRecognitionPersistent.release();
            isVoiceCancel = false;
            speechRecognitionPersistent = null;
        }

    }

    public void cancelAsr() {
        if (speechRecognitionPersistent != null) {
            Timber.tag(TAG).d("speechRecognition_ cancel()");
            speechRecognitionPersistent.cancel();
            isVoiceCancel = true;
        }
    }


    /**
     * 录音按下、移动、松开处理
     * @param type 状态
     * @param isInArea 是否在区域
     * @param status 切换显示
     */
    private void voiceStatusHandle(int type,boolean isInArea,boolean status){
        if (type == PRESS_DOWN){
            if (voiceRecordView != null && voiceRecordView.startRecording()){
                voiceRecordView.show();
            }
            TtsXiaDuMediaPlayer.getInstance().stop();
            TtsMediaPlayer.getInstance().stop();
            toggleAsrRecognition();
        }
        else if (type == PRESS_MOVE){
            if (voiceRecordView != null){
                voiceRecordView.switchVoiceStatus(status);
            }
        }
        else if (type == PRESS_UP){
            if (voiceRecordView != null){
                voiceRecordView.stopRecording();
            }
            if (!isInArea){
                closeAsr();
            }else {
                cancelAsr();
            }
        }
    }


    /**
     * 灵犀智能体
     */
    private void initChatManager() {
        this.chatManager = new ChatManager();
        this.chatManager.init(requireActivity());
        if (chatDataFormat == null) {
            chatDataFormat = new ChatDataFormat();
        }
    }

    private void initAIConversationManager() {
        aiConversationManager = new AIConversationManager();
        aiConversationManager.setAllowInterrupt(false);

    }

    private void initHonor() {
        honorHttp = new HttpUrlConnectionHonor(requireContext());
    }

    private void handleChatData(String curAsrResult) {
        chatDataFormat.init(requireActivity(), curAsrResult);
        if (TabEntity.agentType == TabEntity.TabType.CHAT) {
            new ChatLingXiAdapter(aiConversationManager, requestId).insideRcChat(curAsrResult, (DialogueResult result) -> {
                if (result == null) {
                    return null;
                }
                requireActivity().runOnUiThread(() -> {
                    chatDataFormat.startFlow(result, honorHttp);
                });
                return null;
            });
        }  else {
            chatDataFormat.startFlow(TabEntity.matchLocalModule(TabEntity.agentType), honorHttp);
        }
    }

}

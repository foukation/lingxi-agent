package com.fxzs.lingxiagent.viewmodel.chat;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cmdc.ai.assist.constraint.DialogueResult;
import com.example.service_api.HttpUrlConnectionHonor;
import com.fxzs.lingxiagent.conversation.AIConversationManager;
import com.fxzs.lingxiagent.lingxi.config.ChatFlowCallback;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.ChatLingXiAdapter;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.ChatDataFormat;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.ChatManager;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.LocalModule;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.TabEntity;
import com.fxzs.lingxiagent.model.chat.callback.CreateMyCallback;
import com.fxzs.lingxiagent.model.chat.callback.SSECallback;
import com.fxzs.lingxiagent.model.chat.dto.ChatFileBean;
import com.fxzs.lingxiagent.model.chat.dto.ChatMessage;
import com.fxzs.lingxiagent.model.chat.dto.DrawingToChatBean;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.common.ObservableField;
import com.fxzs.lingxiagent.model.drawing.api.GenerateImageRequest;
import com.fxzs.lingxiagent.model.drawing.dto.AspectRatioDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingStyleDto;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepository;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepositoryImpl;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepository;
import com.fxzs.lingxiagent.model.meeting.repository.MeetingRepositoryImpl;
import com.fxzs.lingxiagent.network.ZNet.ApiResponse;
import com.fxzs.lingxiagent.network.ZNet.HttpRequest;
import com.fxzs.lingxiagent.network.ZNet.bean.SSEBean;
import com.fxzs.lingxiagent.network.ZNet.bean.getCatDetailListBean;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.util.ZUtil.TTSUtils;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.chat.ChatAdapter;
import com.fxzs.lingxiagent.view.chat.SuperChatFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class VMChat extends BaseViewModel {
    private final MutableLiveData<List<ChatMessage>> chatMessages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> aiResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> streamEnd = new MutableLiveData<>(false);
    private final MutableLiveData<String> thinkMessage = new MutableLiveData<>("");
    private final MutableLiveData<String> thinkMessageTitle = new MutableLiveData<>("");
    private final MutableLiveData<Integer> thinkStatus = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isAutoPlay = new MutableLiveData<>(true);
    private final HttpRequest request;
    private OptionModel selectOptionModel;
    private getCatDetailListBean selectAgentBean;
    private DrawingToChatBean selectDrawingToChatBean;
    private DrawingStyleDto selectDrawingStyleDto;
    //    private long conversationId;
    private boolean isStreamEnd = false;
    private String ResponseThink = "";
    private String fullResponse = "";
    private int currentIndex = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable typewriterRunnable;
    private Disposable sseDisposable;
    private long startTime = 0;
    private long endTime = 0;

	//drawing
    private String selectedRatio = "1:1";
    private final ObservableField<Long> conversationId = new ObservableField<>(0l);
    private final ObservableField<Integer> progress = new ObservableField<>(0);
    private final ObservableField<String> progressText = new ObservableField<>("");

    // 业务状态
    private final MutableLiveData<List<DrawingStyleDto>> styles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<AspectRatioDto>> aspectRatios = new MutableLiveData<>();
    private final MutableLiveData<DrawingImageDto> generatedImage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showResult = new MutableLiveData<>(false);
    private final ObservableField<Boolean> isGenerating = new ObservableField<>(false);
    private final MutableLiveData<DrawingSessionDto> currentSession = new MutableLiveData<>();
    private DrawingStyleDto selectedStyle = null;
    private DrawingRepository repository = null;
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
    private ChatMessage aiDrawingMsg;//正在生成的ai图片
    List<ChatFileBean> mFiles = new ArrayList<>();//选取的图片和文件
    List<String> mFileAnalyseUrl = new ArrayList<>();//选取的图片和文件url

    private String meetingId;
    private String transcriptionResult;

    private String requestId;
	private ChatDataFormat chatDataFormat;
    private AIConversationManager aiConversationManager;

    public VMChat(@NonNull Application application) {
        super(application);
        request = new HttpRequest();
        repository = DrawingRepositoryImpl.getInstance();
        initChatManager();
        initAIConversationManager();
    }

    public MutableLiveData<List<ChatMessage>> getChatMessages() { return chatMessages; }
    public LiveData<String> getAiResponse() { return aiResponse; }
    public MutableLiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getStreamEnd() { return streamEnd; }
    public LiveData<String> getThinkMessage() { return thinkMessage; }
    public LiveData<String> getThinkMessageTitle() { return thinkMessageTitle; }
    public LiveData<Integer> getThinkStatus() { return thinkStatus; }
    public ObservableField<Long> getConversationId() {
        return conversationId;
    }

    private void initChatManager() {
        if (chatDataFormat == null) {
            chatDataFormat = new ChatDataFormat();
        }
    }

    private void initAIConversationManager() {
        aiConversationManager = new AIConversationManager();
        aiConversationManager.setAllowInterrupt(false);
    }

    public void setSelectOptionModel(OptionModel option) {
        this.selectOptionModel = option;
    }

    public void setSelectAgentBean(getCatDetailListBean selectAgentBean) {
        this.selectAgentBean = selectAgentBean;
    }

    public MutableLiveData<Boolean> getIsAutoPlay() {
        return isAutoPlay;
    }

    public void setSelectDrawingStyleDto(DrawingStyleDto selectDrawingStyleDto) {
        this.selectDrawingStyleDto = selectDrawingStyleDto;
    }

    public DrawingStyleDto getSelectDrawingStyleDto() {
        return selectDrawingStyleDto;
    }

    public void setSelectDrawingToChatBean(DrawingToChatBean selectDrawingToChatBean) {
        this.selectDrawingToChatBean = selectDrawingToChatBean;
    }

    public DrawingToChatBean getSelectDrawingToChatBean() {
        return selectDrawingToChatBean;
    }

    public MutableLiveData<DrawingImageDto> getGeneratedImage() {
        return generatedImage;
    }



    public void setContinueEditMode(boolean continueEditMode) {
        isContinueEditMode = continueEditMode;
    }

    public void setReferenceImageUrl(String referenceImageUrl) {
        this.referenceImageUrl = referenceImageUrl;
    }

    public String getReferenceImageUrl() {
        return referenceImageUrl;
    }

    public void setHiddenPrompt(String hiddenPrompt) {
        this.hiddenPrompt = hiddenPrompt;
    }

    public ObservableField<Boolean> getIsGenerating() {
        return isGenerating;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public void setTranscriptionResult(String transcriptionResult) {
        this.transcriptionResult = transcriptionResult;
    }

    public void setSelectedRatio(String selectedRatio) {
        this.selectedRatio = selectedRatio;
    }

    public String getSelectedRatio() {
        return selectedRatio;
    }

    public void sendMessage(String input) {
        sendMsg(input,true);
    }
    public void sendMessageHistory(String input) {
        sendMsg(input,false);
    }
    public void sendMsg(String input,boolean isSendStream) {
        if (input == null || input.trim().isEmpty()) return;
        loading.setValue(true);
        addUserMsg(input);
        mFiles.clear();//普通文本消息把文件信息清除
        if(isSendStream && selectOptionModel != null){
            createMy(selectOptionModel.getModel(), input, new CreateMyCallback() {
                @Override
                public void back() {
                    sendStream(conversationId.getValue(), input);
                }
            });
        }
    }

    //发送带图片的消息
    public void sendMessageWithFile(String input, List<ChatFileBean> files) {
        if (input == null || input.trim().isEmpty() || files == null) return;
//        {"id":136,"keyId":null,"name":"腾讯混元","model":"hunyuan-turbo-latest","
        mFiles = files;
        mFileAnalyseUrl.clear();
        if(files != null && files.size() > 0){
            for (int i = 0; i < files.size(); i++) {
                mFileAnalyseUrl.add(files.get(i).getPath());
            }
        }
        selectOptionModel = new OptionModel();
        selectOptionModel.setId(132);
        selectOptionModel.setName("腾讯混元");
        selectOptionModel.setModel("hunyuan-t1-vision");
        loading.setValue(true);
        addUserMsg(input);
        addUserMsgWithFile();
        boolean isImage = mFiles.get(0).isImage();
//        "新对话 | 详细总结文档内容"

        String newInput = input;
        if(!isImage){
            newInput =  "新对话 | "+input;
        }
        createMy(selectOptionModel.getModel(), newInput, new CreateMyCallback() {
            @Override
            public void back() {
                sendStream(conversationId.get(), input);
            }
        });
    }

    //发送绘画消息
    public void sendDrawingMessage(String input) {

        if(isGenerating.getValue()){
            ZUtils.showToast("请等待生成成功");
//            GlobalToast.show(IYAApplication.getInstance(),"请等待生成成功", GlobalToast.Type.NORMAL);
            return;
        }
        selectDrawingToChatBean = new DrawingToChatBean();
        selectDrawingToChatBean.setPrompt(input);
        sendDrawingMessage(input,true);
    }
    //发送绘画消息(重新生成)
    public void sendDrawingMessage(DrawingImageDto imageDto) {

        if(isGenerating.getValue()){
            ZUtils.showToast("请等待生成成功");
//            GlobalToast.show(IYAApplication.getInstance(),"请等待生成成功", GlobalToast.Type.NORMAL);
            return;
        }
        if(imageDto == null){
            return;
        }
        String input = imageDto.getPrompt();
        selectDrawingToChatBean = new DrawingToChatBean();
        selectDrawingToChatBean.setPrompt(input);
        selectDrawingToChatBean.setReference_image_url(imageDto.getPicUrl());

        selectDrawingStyleDto = new DrawingStyleDto();
        selectDrawingStyleDto.setId(imageDto.getStyleId());

        selectDrawingToChatBean.setRatio(imageDto.getWidth()+":"+imageDto.getHeight());
        selectedRatio = imageDto.getWidth()+":"+imageDto.getHeight();
        sendDrawingMessage(input,true);
    }
    public void sendDrawingMessage(String input,String referenceImageUrl) {

        if(isGenerating.getValue()){
            ZUtils.showToast("请等待生成成功");
//            GlobalToast.show(IYAApplication.getInstance(),"请等待生成成功", GlobalToast.Type.NORMAL);
            return;
        }
        selectDrawingToChatBean = new DrawingToChatBean();
        selectDrawingToChatBean.setPrompt(input);
        selectDrawingToChatBean.setReference_image_url(referenceImageUrl);
        sendDrawingMessage(input,true);
    }
    //发送绘画消息（历史记录）
    public void sendDrawingMessageHistory(String input) {
        sendDrawingMessage(input,false);
    }
    public void sendDrawingMessage(String input,boolean isGenerate) {
//        if (input == null || input.trim().isEmpty() || selectOptionModel == null) return;
        loading.setValue(true);
        addUserMsg(input);
        aiDrawingMsg = addAIDrawingMsg();
        if(isGenerate){
            generateImage();
        }
    }
    public void refreshSendMessage(String input,int type) {
        if (input == null || input.trim().isEmpty()) return;
        if(type == SuperChatFragment.TYPE_HOME){
            if (selectOptionModel == null) return;
            chatMessages.getValue().remove(chatMessages.getValue().size()-1);
            loading.setValue(true);
//        addUserMsg(input);
            createMy(selectOptionModel.getModel(), input, new CreateMyCallback() {
                @Override
                public void back() {
                    sendStream(conversationId.get(), input);
                }
            });
        }else if(type == SuperChatFragment.TYPE_AGENT){
            sendAgentMessage(input);
        }else if(type == SuperChatFragment.TYPE_MEETING_QA){
            sendMeetingMessage(input);
        }
    }

    //智能体消息
    public void sendAgentMessage(String input) {
        if (input == null || input.trim().isEmpty() || selectAgentBean == null) return;
        loading.setValue(true);
        addUserMsg(input);
        createMyAgent(selectAgentBean.getBotId(),selectAgentBean.getModelName(),selectAgentBean.getMenuId()+"", new CreateMyCallback() {
            @Override
            public void back() {
                sendStream(conversationId.get(), input);
            }
        });
    }

    //会议-智能问答消息
    public void sendMeetingMessage(String input) {
        if (input == null || input.trim().isEmpty() ) return;
        loading.setValue(true);
        addUserMsg(input);
        sendStream(conversationId.get(), input);
    }
    //会议-智能问答初始化
    public void initMeeting() {
        String model = "bot-20250307112049-znnjx";
        String title = "智能问答";
        String systemMessage = "你是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1。如您有任何任何问题，我会尽我所能为您提供帮助。";
        createMyMeeting(model,title,systemMessage, new CreateMyCallback() {
            @Override
            public void back() {

                bindMeetingAndConversationId(meetingId, conversationId.get()+"", new CreateMyCallback() {
                    @Override
                    public void back() {

//                        String transcription = Constant.transcription;

                        String  systemMessage = transcriptionResult;//TODO 填充会议内容
                        updateMyMeeting(conversationId.get()+"", systemMessage, new CreateMyCallback() {
                            @Override
                            public void back() {

                            }
                        });
                    }
                });
            }
        });
    }


    public void sendAIWritingMessage(String input) {
        List<ChatMessage> list;
        ZUtils.print("conversationId = "+conversationId.get());
        if(conversationId.get() == 0){
//            list = chatMessages.getValue();
//            list.clear();
//            chatMessages.postValue(list);
        }else{
            list = chatMessages.getValue();
            chatMessages.postValue(list);
        }
//        ZUtils.print(" chatMessages.getValue() = "+ chatMessages.getValue().size());
        addUserMsg(input);
        if(conversationId.get() == 0){
            createMy(selectOptionModel.getModel(), input, new CreateMyCallback() {
                @Override
                public void back() {
                    sendStream(conversationId.get(), input);
//                    conversationId.setValue(0l);
                }
            });

        }else {
            sendStream(conversationId.get(), input);
        }
    }

    public void sendTranslateMessage(String content,String prompt) {
        List<ChatMessage> list;
        ZUtils.print("conversationId = "+conversationId.get());
        if(conversationId.get() == 0){
//            list = chatMessages.getValue();
//            list.clear();
//            chatMessages.postValue(list);
        }else{
            list = chatMessages.getValue();
            chatMessages.postValue(list);
        }
//        chatMessages.getValue().clear();
//        List<ChatMessage> list = chatMessages.getValue();
//        chatMessages.postValue(list);

        addUserMsg(content);
        if(conversationId.get() == 0 ){
            createMy(selectOptionModel.getModel(), content, new CreateMyCallback() {
                @Override
                public void back() {
                    sendStream(conversationId.get(), prompt);

//                    conversationId.setValue(0l);
                }
            });

        }else {
            sendStream(conversationId.get(), prompt);
        }
    }

    private void addUserMsg(String input) {
        List<ChatMessage> list = chatMessages.getValue();
        if (list == null) list = new ArrayList<>();
        ChatMessage userMsg = new ChatMessage(input, true);
        list.add(userMsg);
        ZUtils.print("addUserMsg = "+list.size());
        chatMessages.postValue(list);
    }
    private void addUserMsgWithFile() {
        List<ChatMessage> list = chatMessages.getValue();
        if (list == null) list = new ArrayList<>();
//        ChatMessage userMsg = new ChatMessage(input, true);

        boolean isImage = mFiles.get(0).isImage();
        ChatMessage userMsg = new ChatMessage(mFiles,isImage? ChatAdapter.TYPE_USER_FILE_IMAGE:ChatAdapter.TYPE_USER_FILE);
        list.add(userMsg);
        ZUtils.print("addUserMsg = "+list.size());
        chatMessages.postValue(list);
    }
    public void addUserMsgWithFile(List<ChatFileBean> mFiles) {
        if(mFiles == null || mFiles.size() == 0){
            return;
        }
        List<ChatMessage> list = chatMessages.getValue();
        if (list == null) list = new ArrayList<>();
//        ChatMessage userMsg = new ChatMessage(input, true);

        boolean isImage = mFiles.get(0).isImage();
        ChatMessage userMsg = new ChatMessage(mFiles,isImage? ChatAdapter.TYPE_USER_FILE_IMAGE:ChatAdapter.TYPE_USER_FILE);
        list.add(userMsg);
        ZUtils.print("addUserMsg = "+list.size());
        chatMessages.postValue(list);
    }

    public ChatMessage addAIMsg() {
        List<ChatMessage> list = chatMessages.getValue();
        if (list == null) list = new ArrayList<>();
        ChatMessage aiMsg = new ChatMessage("", false);
        list.add(aiMsg);
        chatMessages.postValue(list);
        return aiMsg;
    }
    public ChatMessage addAIMsgHistory(String input) {
        List<ChatMessage> list = chatMessages.getValue();
        if (list == null) list = new ArrayList<>();
        ChatMessage aiMsg = new ChatMessage(input, false);
        aiMsg.setStatus(Constant.ThinkState.END);
        aiMsg.setThinkMessage("");
        list.add(aiMsg);
        chatMessages.postValue(list);
        return aiMsg;
    }
    //添加AI绘画回复
    public ChatMessage addAIDrawingMsg() {
        List<ChatMessage> list = chatMessages.getValue();
        if (list == null) list = new ArrayList<>();
        ChatMessage aiMsg = new ChatMessage("", ChatAdapter.TYPE_AI_DRAWING);
        list.add(aiMsg);
        chatMessages.postValue(list);
        return aiMsg;
    }
    public void updateAIDrawingMsg(int progress) {
         aiDrawingMsg.setProgress(progress);

        chatMessages.postValue(chatMessages.getValue());
    }
    public void updateAIDrawingMsg(String url) {
         aiDrawingMsg.setUrl(url);

        chatMessages.postValue(chatMessages.getValue());
    }
    public void updateAIDrawingMsg(DrawingImageDto imageDto) {
         aiDrawingMsg.setDrawingImageDto(imageDto);

        chatMessages.postValue(chatMessages.getValue());
    }
    public ChatMessage addAIMsg(String msg) {
        List<ChatMessage> list = chatMessages.getValue();
        if (list == null) list = new ArrayList<>();
        ChatMessage aiMsg = new ChatMessage(msg, false,Constant.ThinkState.END);
        aiMsg.setThinkMessage("");
        list.add(aiMsg);
        chatMessages.postValue(list);
        return aiMsg;
    }

    public void createMy(String model, String title, CreateMyCallback callback) {
        if(conversationId.get() != 0){//创建过的就直接返回
            if (callback != null){
                callback.back();
            }
            return;
        }
        request.createMy(model, title, new Observer<ApiResponse<Integer>>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(ApiResponse<Integer> res) {
                if (res.getCode() == 0) {
                    conversationId.setValue(Long.parseLong(res.getData().toString()));
                    conversationId.postValue(Long.parseLong(res.getData().toString()));
                    SharedPreferencesUtil.saveString(Constants.PREF_CONVERSATION_ID,conversationId.getValue()+"");
                    if (callback != null) callback.back();
                }
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }
    public void createMyWithFile(String model, String title, CreateMyCallback callback) {
        request.createMy(model, title, new Observer<ApiResponse<Integer>>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(ApiResponse<Integer> res) {
                if (res.getCode() == 0) {
                    conversationId.setValue(Long.parseLong(res.getData().toString()));
                    if (callback != null) callback.back();
                }
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }
    public void createMyAgent(String model, String title,String aiMenuId, CreateMyCallback callback) {
        if(conversationId.get() != 0){//创建过的就直接返回
            if (callback != null){
                callback.back();
            }
            return;
        }
        request.createMy(model, title,aiMenuId, new Observer<ApiResponse<Integer>>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(ApiResponse<Integer> res) {
                if (res.getCode() == 0) {
                    conversationId.setValue(Long.parseLong(res.getData().toString()));
                    if (callback != null) callback.back();
                }
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }
    public void createMyMeeting(String model, String title,String systemMessage, CreateMyCallback callback) {
        request.createMyMeeting(model, title,systemMessage, new Observer<ApiResponse<Integer>>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(ApiResponse<Integer> res) {
                if (res.getCode() == 0) {
                    conversationId.setValue(Long.parseLong(res.getData().toString()));
                    if (callback != null) callback.back();
                }
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }
    public void bindMeetingAndConversationId(String meetingId, String conversionId, CreateMyCallback callback) {
        MeetingRepository repository = new MeetingRepositoryImpl();
        repository.bindMeetingAndConversationId(meetingId,conversionId)  .observeForever(updateResult -> {
            if (updateResult != null) {
                if (updateResult.isSuccess()) {
                    if (callback != null) callback.back();
                } else {
                }
            }
        });
    }
    public void updateMyMeeting(String id,String systemMessage, CreateMyCallback callback) {
        request.updateMyMeeting(id,systemMessage, new Observer<ApiResponse<Boolean>>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(ApiResponse<Boolean> res) {
                if (res.getCode() == 0) {
                    if (callback != null) callback.back();
                }
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    public void sendStream(long conversationId, String title) {
        closeSSE();
        fullResponse = "";
        ResponseThink = "";
        startTime = System.currentTimeMillis();
        ChatMessage aiMessage = addAIMsg();
        aiMessage.setThinkMessageTitle("思考中");
        aiMessage.setStatus(Constant.ThinkState.START);
        thinkMessageTitle.postValue("思考中");
        thinkStatus.postValue(Constant.ThinkState.START);
        isStreamEnd = false;
        streamEnd.postValue(false);

	    String LING_XI_MODEL = "10086";

	    if (Objects.equals(selectOptionModel.getModel(), LING_XI_MODEL)) {
            requestId = UUID.randomUUID().toString();
            chatDataFormat.init();
            if (TabEntity.agentType == TabEntity.TabType.CHAT) {
                new ChatLingXiAdapter(aiConversationManager, requestId).insideRcChat(title, (DialogueResult result) -> {
                    if (result == null) {
                        setError("生成失败");
                    }
                    mainHandler.post(() -> {
                        chatDataFormat.startFlow(result, new ChatFlowCallback() {
                            @Override
                            public void receive(LocalModule curModel, Boolean isBreak, String content) {
                                Timber.tag("chatDataFormat").d("startFlow:%s", content);
                                fullResponse = content;
                                ResponseThink = "";
                                aiMessage.setThinkMessage(ResponseThink);
                                aiMessage.setMessage(fullResponse);
                                aiMessage.setStatus(Constant.ThinkState.THINKING);
                                thinkMessage.postValue(ResponseThink);
                                aiResponse.postValue(fullResponse);
                                thinkStatus.postValue(Constant.ThinkState.THINKING);
                                currentIndex = fullResponse.length();
                                chatMessages.postValue(chatMessages.getValue());
                            }

                            @Override
                            public void end() {
                                isStreamEnd = true;
                                endTime = System.currentTimeMillis();
                                long second = (endTime - startTime) / 1000;
                                aiMessage.setThinkMessageTitle("思考过程（用时" + second + "秒）");
                                aiMessage.setStatus(Constant.ThinkState.END);
                                thinkMessageTitle.postValue("思考过程（用时" + second + "秒）");
                                thinkStatus.postValue(Constant.ThinkState.END);
                                chatMessages.postValue(chatMessages.getValue());
                                streamEnd.postValue(true);
                            }
                        });
                    });
                    sseDisposable = request.getSseDisposable();
                    return null;
                });
            }
        }
        else {
            request.sendStreams(conversationId, selectOptionModel != null?selectOptionModel.getId():selectAgentBean.getModelId(), title, mFiles, new SSECallback() {
                @Override
                public void receive(String responseBodyString) {
                    Gson gson = new GsonBuilder().setLenient().create();
                    Type type = new TypeToken<ApiResponse<SSEBean>>() {}.getType();
                    ApiResponse<SSEBean> res = gson.fromJson(responseBodyString, type);
                    if (res.getCode() == 0) {
//                    if("".equals(fullResponse)){
//                        ZUtils.print("TTSUtils.getInstance().ttsStart");
//                        TTSUtils.getInstance().ttsStart();
//                    }
                        if (res.getData().getReceive().getType().equals("assistant-reason")) {
                            ResponseThink += res.getData().getReceive().getContent();
                        } else {
                            fullResponse += res.getData().getReceive().getContent();
                            if(isAutoPlay.getValue()){
                                TTSUtils.getInstance().ttsText(res.getData().getReceive().getContent(),false);
                            }
                        }

                        aiMessage.setThinkMessage(ResponseThink);
                        aiMessage.setMessage(fullResponse);
                        aiMessage.setStatus(Constant.ThinkState.THINKING);
                        thinkMessage.postValue(ResponseThink);
                        aiResponse.postValue(fullResponse);
                        thinkStatus.postValue(Constant.ThinkState.THINKING);
                        currentIndex = fullResponse.length();
                        chatMessages.postValue(chatMessages.getValue());
                    }
                }
                @Override
                public void end() {
                    isStreamEnd = true;
                    endTime = System.currentTimeMillis();
                    long second = (endTime - startTime) / 1000;
                    aiMessage.setThinkMessageTitle("思考过程（用时" + second + "秒）");
                    aiMessage.setStatus(Constant.ThinkState.END);
                    thinkMessageTitle.postValue("思考过程（用时" + second + "秒）");
                    thinkStatus.postValue(Constant.ThinkState.END);
                    chatMessages.postValue(chatMessages.getValue());
                    streamEnd.postValue(true);
//                TTSUtils.getInstance().ttsStop();
                    if(isAutoPlay.getValue()){
                        TTSUtils.getInstance().ttsText("",true);
                    }
                }
            });
            sseDisposable = request.getSseDisposable();
        }
    }

    public void closeSSE() {
        if (sseDisposable != null && !sseDisposable.isDisposed()) {
            sseDisposable.dispose();
        }
    }

    @Override
    protected void onCleared() {
        closeSSE();
        handler.removeCallbacksAndMessages(null);
    }

    public void resendMsg() {
        List<ChatMessage> list = chatMessages.getValue();
        ChatMessage msg =  getResendMsg();

    }
    public void removeLast2Msg() {
        List<ChatMessage> list = chatMessages.getValue();
        list.remove(list.size()-1);
        list.remove(list.size()-1);
        chatMessages.postValue(list);

    }
    public ChatMessage getResendMsg() {
        List<ChatMessage> list = chatMessages.getValue();
        if(list.size()>1){
         return list.get(list.size()-2);

        }
        return null;
    }


    // 生成图片（后续重构）
    public void generateImage() {
        android.util.Log.d("VMDrawing", "=== generateImage called ===");
//        android.util.Log.d("VMDrawing", "Call stack: " + android.util.Log.getStackTraceString(new Throwable()));
//        android.util.Log.d("VMDrawing", "generateEnabled: " + generateEnabled.get());
//        android.util.Log.d("VMDrawing", "isGenerating: " + isGenerating.get());
//        android.util.Log.d("VMDrawing", "prompt: " + prompt.get());
//        android.util.Log.d("VMDrawing", "selectedStyle: " + (selectedStyle != null ? selectedStyle.getName() + " (ID: " + selectedStyle.getId() + ")" : "null"));
//        android.util.Log.d("VMDrawing", "selectedRatio: " + selectedRatio.get());
//        android.util.Log.d("VMDrawing", "referenceImageUrl: " + referenceImageUrl);
//        android.util.Log.d("VMDrawing", "isContinueEditMode: " + isContinueEditMode);



        if (/*!generateEnabled.get() ||*/ isGenerating.get()) {
//            android.util.Log.w("VMDrawing", "Generation blocked: generateEnabled=" + generateEnabled.get() + ", isGenerating=" + isGenerating.get());
            return;
        }

        // 准备请求参数
        GenerateImageRequest request = new GenerateImageRequest();

        if(selectDrawingToChatBean == null){
            return;
        }
        // 组合提示词
        String prompt = selectDrawingToChatBean.getPrompt().trim();
        String fullPrompt = selectDrawingToChatBean.getPrompt().trim();
        String referenceImageUrl = selectDrawingToChatBean.getReference_image_url();
        DrawingStyleDto selectedStyle = selectDrawingStyleDto;

//        android.util.Log.d("VMDrawing", "Original prompt: " + fullPrompt);
//
//        // 如果有hiddenPrompt（继续编辑模式），则将其与用户输入组合
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
        String selectedRatioStr = selectedRatio;
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
        if (conversationId.get() == 0 && !isCreatingSession) {
            // 如果没有会话，先创建会话
            android.util.Log.d("VMDrawing", "Creating session before generating image");
            pendingGeneration = true; // 标记有待处理的生成请求
            createNewSession(prompt);

            // 等待会话创建完成后再生成图片
            isGenerating.set(false);
            return;
        } else if (conversationId.get() == 0 && isCreatingSession) {
            // 如果正在创建会话，提示等待
            android.util.Log.d("VMDrawing", "Waiting for session creation");
            isGenerating.set(false);
            setError("会话创建中，请稍后重试");
            return;
        } else {
            // 有会话ID，设置到请求中
            request.setSessionId(conversationId.get());
            android.util.Log.d("VMDrawing", "Using sessionId: " + conversationId.get());
        }

        // 设置参考图片URL
        if (/*isContinueEditMode &&*/ referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
            request.setImagUrls(new String[]{referenceImageUrl});
            android.util.Log.d("VMDrawing", "Setting reference image URL: " + referenceImageUrl);
        }

        // 开始生成
        isGenerating.set(true);
        progress.set(0);
        progressText.set("正在生成中...");
        showResult.postValue(false);
        updateAIDrawingMsg(0);
        DrawingImageDto imageDto = new DrawingImageDto();
        imageDto.setPrompt(prompt);
        if(request.getStyleId() != null){
            imageDto.setStyleId(request.getStyleId());
        }
        imageDto.setWidth(width);
        imageDto.setHeight(height);
        updateAIDrawingMsg(imageDto);

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
//                    progress.set(0);
                    updateAIDrawingMsg(0);
                });
                setError(result.getError() != null ? result.getError() : "生成失败");

                // 生成失败也清除参考图片URL
                clearReferenceImageUrl();
            }
        });
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


    // 创建新会话
    private void createNewSession(String sessionName) {
        // 如果已经在创建会话，则不重复创建
        if (isCreatingSession) {
            android.util.Log.d("VMDrawing", "Session creation already in progress");
            return;
        }

        // 如果已经有会话ID，则不需要创建
        if (conversationId.get() != 0) {
            android.util.Log.d("VMDrawing", "Session already exists with ID: " + conversationId.get());
            return;
        }

        isCreatingSession = true;
        android.util.Log.d("VMDrawing", "Creating new image session with name: " + sessionName);

        repository.createImageSession(sessionName).observeForever(result -> {
            isCreatingSession = false;

            if (result.isSuccess() && result.getData() != null) {
                conversationId.setValue(Long.parseLong(result.getData().toString()));
                android.util.Log.d("VMDrawing", "New session created with ID: " + conversationId.get());

                // 创建会话DTO
                DrawingSessionDto session = new DrawingSessionDto();
                session.setId(conversationId.get());
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


    // 开始轮询任务状态
    @SuppressWarnings("deprecation")
    private void startPollingTaskStatus() {
        if (currentTaskId == null) {
            android.util.Log.e("VMDrawing", "Cannot start polling: currentTaskId is null");
            return;
        }

        android.util.Log.d("VMDrawing", "Starting polling for task ID: " + currentTaskId);

        Timer pollingTimer = new Timer();
        final int[] pollCount = {0}; // 轮询次数计数器
        final int maxPolls = 60; // 最多轮询60次（2分钟）

        pollingTimer.schedule(new TimerTask() {
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

                                    DrawingImageDto imageDto = aiDrawingMsg.getDrawingImageDto();
                                    imageDto.setPicUrl(finalImageUrl);
                                    updateAIDrawingMsg(imageDto);

                                    updateAIDrawingMsg(100);
                                    updateAIDrawingMsg(finalImageUrl);
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


                                    updateAIDrawingMsg(currentProgress + 5);
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
//                    if (progress.get() == 0) {
//                        progressText.set("正在初始化...");
//                    }
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

    /**
     * 清除参考图片URL
     */
    public void clearReferenceImageUrl() {
        this.referenceImageUrl = null;
    }


}
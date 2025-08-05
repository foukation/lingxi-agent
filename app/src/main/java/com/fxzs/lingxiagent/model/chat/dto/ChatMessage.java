package com.fxzs.lingxiagent.model.chat.dto;


import com.fxzs.lingxiagent.view.chat.ChatAdapter;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage {
    private String message;
    private String thinkMessage;
    private String thinkMessageTitle;
    private String avatar;
    private int avatarRes;
//    private boolean isUser;
//    private boolean isHeader;
    private boolean isEnd;
    private int status;
    private int thinkingTime; // 思考时间（秒）
    private boolean hideActionRefresh;

    private int progress; // 绘图进度
    private String url; // 绘图URL
    private List<ChatFileBean> chatFileBeanList; // 图片/文件

    private int msgType;//0-用户-普通消息，1-ai-文字消息，2-用户-智能体头部（固定头部），3-ai-绘画消息

    DrawingImageDto drawingImageDto;//绘画消息使用，包含prompt，宽高，url等信息

//    public ChatMessage(String message, boolean isHeader, String avatar) {
//        this.message = message;
//        this.isHeader = isHeader;
//        this.avatar = avatar;
//    }
    public ChatMessage(String message, int msgType, String avatar) {
        this.message = message;
        this.msgType = msgType;
        this.avatar = avatar;
    }
    public ChatMessage(String message, int msgType, int avatarRes) {
        this.message = message;
        this.msgType = msgType;
        this.avatarRes = avatarRes;
    }

    public ChatMessage(String message, int msgType) {
        this.message = message;
        this.msgType = msgType;
    }
    public ChatMessage(List<ChatFileBean> list, int msgType) {
        this.chatFileBeanList = new ArrayList<>();
        this.chatFileBeanList.addAll(list);
        this.msgType = msgType;
    }

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.msgType = isUser?ChatAdapter.TYPE_USER:ChatAdapter.TYPE_AI;
    }

    public ChatMessage(String message, boolean isUser, int status) {
        this.message = message;
        this.msgType = isUser? ChatAdapter.TYPE_USER:ChatAdapter.TYPE_AI;
        this.status = status;
    }

//    public ChatMessage(String message, String thinkMessage, boolean isUser) {
//        this.message = message;
//        this.thinkMessage = thinkMessage;
//        this.isUser = isUser;
//    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

//    public boolean isUser() {
//        return isUser;
//    }

    public String getThinkMessage() {
        return thinkMessage;
    }

    public void setThinkMessage(String thinkMessage) {
        this.thinkMessage = thinkMessage;
    }

    public String getThinkMessageTitle() {
        return thinkMessageTitle;
    }

    public void setThinkMessageTitle(String thinkMessageTitle) {
        this.thinkMessageTitle = thinkMessageTitle;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

//    public boolean isHeader() {
//        return isHeader;
//    }

//    public void setHeader(boolean header) {
//        isHeader = header;
//    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public int getThinkingTime() {
        return thinkingTime;
    }
    
    public void setThinkingTime(int thinkingTime) {
        this.thinkingTime = thinkingTime;
    }


    public boolean isHideActionRefresh() {
        return hideActionRefresh;
    }

    public void setHideActionRefresh(boolean hideActionRefresh) {
        this.hideActionRefresh = hideActionRefresh;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public List<ChatFileBean> getChatFileBeanList() {
        return chatFileBeanList;
    }

    public void setChatFileBeanList(List<ChatFileBean> chatFileBeanList) {
        this.chatFileBeanList = chatFileBeanList;
    }

    public int getAvatarRes() {
        return avatarRes;
    }

    public void setAvatarRes(int avatarRes) {
        this.avatarRes = avatarRes;
    }

    public DrawingImageDto getDrawingImageDto() {
        return drawingImageDto;
    }

    public void setDrawingImageDto(DrawingImageDto drawingImageDto) {
        this.drawingImageDto = drawingImageDto;
    }
    // 便捷构造方法 - 带思维链和思考时间
//    public ChatMessage(String message, String thinkMessage, int thinkingTime, boolean isUser) {
//        this.message = message;
//        this.thinkMessage = thinkMessage;
//        this.thinkingTime = thinkingTime;
//        this.isUser = isUser;
//        this.thinkMessageTitle = "思考过程 (用时 " + thinkingTime + " 秒)";
//    }
}
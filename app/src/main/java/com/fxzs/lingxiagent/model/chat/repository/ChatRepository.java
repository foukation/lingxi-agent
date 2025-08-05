package com.fxzs.lingxiagent.model.chat.repository;

import com.fxzs.lingxiagent.model.chat.dto.ConversationDetailDto;
import com.fxzs.lingxiagent.model.chat.dto.ModelTypeResponse;
import com.fxzs.lingxiagent.model.chat.dto.ConversationHistoryListDto;

import java.util.List;
import java.util.Map;

public interface ChatRepository {
    interface Callback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
    
    void getModelTypeList(int modelType, Callback<ModelTypeResponse> callback);
    
    void getEngineModelType(Callback<Map<String, String>> callback);
    
    /**
     * 获取对话历史记录列表
     * @param modelType 模型类型（1 表示智能体模型）
     * @param params 请求参数
     * @param callback 回调接口
     */
    void getConversationHistoryList(int modelType, Map<String, Object> params, Callback<ConversationHistoryListDto> callback);
    /**
     *
     *      获得指定对话的消息列表
     */
    void getListByConversationId(long id,Callback<List<ConversationDetailDto>> callback);
    void deleteConversation(long id,Callback<Boolean> callback);
}
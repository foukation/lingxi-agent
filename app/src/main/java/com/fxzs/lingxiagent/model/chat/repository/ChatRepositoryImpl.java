package com.fxzs.lingxiagent.model.chat.repository;

import com.fxzs.lingxiagent.model.chat.api.ChatApiService;
import com.fxzs.lingxiagent.model.chat.dto.ConversationDetailDto;
import com.fxzs.lingxiagent.model.chat.dto.ModelTypeResponse;
import com.fxzs.lingxiagent.model.chat.dto.ConversationHistoryListDto;
import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.network.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class ChatRepositoryImpl implements ChatRepository {
    private final ChatApiService apiService;
    
    public ChatRepositoryImpl() {
        this.apiService = RetrofitClient.getInstance().create(ChatApiService.class);
    }
    
    @Override
    public void getModelTypeList(int modelType, Callback<ModelTypeResponse> callback) {
        apiService.getModelTypeList(modelType).enqueue(new retrofit2.Callback<BaseResponse<ModelTypeResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<ModelTypeResponse>> call, Response<BaseResponse<ModelTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<ModelTypeResponse> baseResponse = response.body();
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg() != null ? baseResponse.getMsg() : "获取模型列表失败");
                    }
                } else {
                    callback.onError("网络请求失败");
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<ModelTypeResponse>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void getEngineModelType(Callback<Map<String, String>> callback) {
        apiService.getEngineModelType().enqueue(new retrofit2.Callback<BaseResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<BaseResponse<Map<String, String>>> call, Response<BaseResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<Map<String, String>> baseResponse = response.body();
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg() != null ? baseResponse.getMsg() : "获取引擎模型类型失败");
                    }
                } else {
                    callback.onError("网络请求失败");
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<Map<String, String>>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void getConversationHistoryList(int modelType, Map<String, Object> params, Callback<ConversationHistoryListDto> callback) {
        apiService.getConversationHistoryList(modelType, params).enqueue(new retrofit2.Callback<BaseResponse<ConversationHistoryListDto>>() {
            @Override
            public void onResponse(Call<BaseResponse<ConversationHistoryListDto>> call, Response<BaseResponse<ConversationHistoryListDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<ConversationHistoryListDto> baseResponse = response.body();
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg() != null ? baseResponse.getMsg() : "获取对话历史记录失败");
                    }
                } else {
                    callback.onError("网络请求失败");
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<ConversationHistoryListDto>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    @Override
    public void getListByConversationId(long id, Callback<List<ConversationDetailDto>> callback) {
        apiService.getListByConversationId(id).enqueue(new retrofit2.Callback<BaseResponse<List<ConversationDetailDto>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ConversationDetailDto>>> call, Response<BaseResponse<List<ConversationDetailDto>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<List<ConversationDetailDto>> baseResponse = response.body();
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg() != null ? baseResponse.getMsg() : "获取对话历史记录失败");
                    }
                } else {
                    callback.onError("网络请求失败");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ConversationDetailDto>>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteConversation(long id, Callback<Boolean> callback) {

        apiService.deleteConversation(id).enqueue(new retrofit2.Callback<BaseResponse<Boolean>>() {
            @Override
            public void onResponse(Call<BaseResponse<Boolean>> call, Response<BaseResponse<Boolean>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<Boolean> baseResponse = response.body();
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg() != null ? baseResponse.getMsg() : "获取对话历史记录失败");
                    }
                } else {
                    callback.onError("网络请求失败");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Boolean>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    @Override
    public void addConversationHistory(String conversationId, List<Map<String, Object>> messages, Callback<Integer> callback) {
        Map<String, Object> params = createConversationHistoryParams(conversationId, messages);
        apiService.addConversationHistory(params).enqueue(new retrofit2.Callback<BaseResponse<Integer>>() {
            @Override
            public void onResponse(Call<BaseResponse<Integer>> call, Response<BaseResponse<Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<Integer> baseResponse = response.body();
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        callback.onSuccess(baseResponse.getData());
                    } else {
                        callback.onError(baseResponse.getMsg() != null ? baseResponse.getMsg() : "添加历史记录失败");
                    }
                } else {
                    callback.onError("网络请求失败");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Integer>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 创建添加单条历史记录请求参数
     */
    private Map<String, Object> createConversationHistoryParams(String conversationId, List<Map<String, Object>> messages) {
        // 创建最外层的 conversation 数据结构
        Map<String, Object> conversation = new HashMap<>();
        conversation.put("conversationId", conversationId);
        // 将 messages 列表添加到 conversation
        conversation.put("messages", messages);

        return conversation;
    }
}
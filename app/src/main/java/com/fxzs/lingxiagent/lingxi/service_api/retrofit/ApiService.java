package com.fxzs.lingxiagent.lingxi.service_api.retrofit;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("generate-html/chat_history")
    Single<ResponseBody> sendChatHistory(@Body ChatHistoryRequest request);
}
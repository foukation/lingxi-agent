package com.fxzs.lingxiagent.lingxi.service_api.retrofit;

import android.util.Log;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class ChatApiManager {
    private static final String TAG = "ChatApiManager";

    // Listener interface for callback
    public interface ChatApiListener {
        void onSuccess(String response);

        void onFailure(String errorMessage);
    }

    public void sendChatHistory(List<ChatMessage> history, ChatApiListener listener) {
        ChatHistoryRequest request = new ChatHistoryRequest("分享链接", history);

        RetrofitClient.getApiService().sendChatHistory(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe");
                    }

                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        try {
                            String response = responseBody.string();
                            Log.d(TAG, "onSuccess: " + response);
                            if (listener != null) {
                                listener.onSuccess(response);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onSuccess error: ", e);
                            if (listener != null) {
                                listener.onFailure("Failed to parse response");
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                        if (listener != null) {
                            listener.onFailure(e.getMessage());
                        }
                    }
                });
    }
}

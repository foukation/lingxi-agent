package com.fxzs.lingxiagent.model.honor.repository;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.honor.api.HonorApiService;
import com.fxzs.lingxiagent.model.honor.dto.MessageRole;
import com.fxzs.lingxiagent.model.honor.dto.TripHonorRes;
import com.fxzs.lingxiagent.network.ZNet.RetrofitClient;
import com.fxzs.lingxiagent.util.DeviceUUIDGenerator;
import com.fxzs.lingxiagent.util.GMapHelper;
import com.fxzs.lingxiagent.util.ZUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.internal.http2.StreamResetException;

/**
 * 认证仓库实现
 */
public class HonorRepositoryImpl implements HonorRepository {
    
    private static final String TAG = "HonorRepositoryImpl";
    private final HonorApiService honorApiService;
    private final Context context;
    private String currentApiUrl = "";
    private JSONArray messages = new JSONArray();
    private String sessionId;
    private boolean interruptMessage = false;
    private Disposable sseDisposable;
    private static final Gson gson = new GsonBuilder().create();
    
    public HonorRepositoryImpl(Context context) {
        this.context = context;
        this.honorApiService = RetrofitClient.createHonorApi();
        updateSession();
    }

    public void updateRequestInfo(String apiUrl) {
        if (!apiUrl.equals(this.currentApiUrl)) {
            this.currentApiUrl = apiUrl;
            updateSession();
        }
    }

    public void updateSession() {
        this.sessionId = String.valueOf(System.currentTimeMillis() + (System.nanoTime() % 1_000_000));
        this.messages = new JSONArray();
        this.interruptMessage = false;
    }

    public void updateMessages(String role, String content, String type) {
        try {
            JSONObject message = new JSONObject();
            message.put("role", role);
            message.put("content", content);
            message.put("type", type);
            messages.put(message);
        } catch (Exception e) {
            Log.d(TAG, "updateMessages error " + e);
        }
    }
    
    @Override
    public void sendStreamRequest(String inputString, StreamHandler handler) {
        this.interruptMessage = false;
        String timestamp = String.valueOf(System.currentTimeMillis() + (System.nanoTime() % 1_000_000));
        String signature = generateSign(timestamp);

        try {
            JSONObject requestBodyJson = createRequestBody(inputString, sessionId, timestamp);
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBodyJson.toString()
            );

            // 创建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("accessKey", Constants.HONOR_ACCESS_KEY);
            headers.put("ts", timestamp);
            headers.put("sign", signature);
            headers.put("X-Request-Source", "lingxiapp_main");

            // 发送请求
            Observable<ResponseBody> responseBodyObservable;
            if (currentApiUrl.contains(Constants.HONOR_MEET)) {
                responseBodyObservable = honorApiService.sendStreamMeetRequest(headers, requestBody);
            } else {
                responseBodyObservable = honorApiService.sendStreamTripRequest(headers, requestBody);
            }

            Observable<String> sseObservable = parseSseStream(responseBodyObservable);
            // 流结束
            sseDisposable = sseObservable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            data -> {
                                // 处理接收到的 SSE 数据
                                handleDataChunk(data, handler);
                            },
                            throwable -> {
                                // 处理错误
                                handler.onError("网络请求失败: " + throwable.getMessage());
                            },
                            handler::onStreamComplete
                    );
        } catch (Exception e) {
            handler.onError("请求创建失败: " + e.getMessage());
        }
    }

    public Observable<String> parseSseStream(Observable<ResponseBody> responseBodyObservable) {
        return responseBodyObservable
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody -> Observable.create(emitter -> {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                        String line;
                        while (!emitter.isDisposed() && (line = reader.readLine()) != null) {
                            if (line.contains("[DONE]")) {
                                emitter.onComplete();
                            } else if (line.startsWith("data:")) {
                                String data = line.substring(5).trim();
                                if (!data.isEmpty()) {
                                    emitter.onNext(data);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (!emitter.isDisposed()) {
                            // 忽略 StreamResetException
                            if (!(e instanceof StreamResetException && e.getMessage().contains("CANCEL"))) {
                                emitter.onError(e);
                            } else {
                                ZUtils.print("Stream canceled, ignoring StreamResetException");
                                emitter.onComplete(); // 或者不发送任何事件
                            }
                        }
                    } finally {
                        // 确保资源关闭
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (Exception ignored) {}
                        }
                        responseBody.close(); // 关闭 ResponseBody
                    }
                }));
    }

    private void handleDataChunk(String dataStr, StreamHandler handler) {
        try {
            TripHonorRes resp = gson.fromJson(dataStr, TripHonorRes.class);
            handler.onDataChunk(resp);
        } catch (Exception e) {
            handler.onError("数据解析失败: " + e.getMessage());
        }
    }

    public void interruptMessage() {
        this.interruptMessage = true;
        if (sseDisposable != null && !sseDisposable.isDisposed()) {
            sseDisposable.dispose();
        }
    }

    private JSONObject createRequestBody(String inputString, String sessionId, String ts) {
        try {
            JSONObject data = new JSONObject();
            data.put("requestId", ts);
            data.put("model", "qwen2.5-vl-32b-instruct");
            data.put("stream", true);
            data.put("temperature", 0.7);
            data.put("top_p", 0.9);
            data.put("max_tokens", 2048);

            JSONObject endpoint = new JSONObject();
            JSONObject location = new JSONObject();
            location.put("longitude", String.valueOf(GMapHelper.getInstance().getLongitude()));
            location.put("latitude", String.valueOf(GMapHelper.getInstance().getLatitude()));
            location.put("locationSystem", "GCJ02");
            endpoint.put("location", location);

            JSONObject device = new JSONObject();
            JSONObject base = new JSONObject();
            base.put("deviceUuid", DeviceUUIDGenerator.getDeviceUUID(context));
            device.put("base", base);
            endpoint.put("device", device);
            data.put("endpoint", endpoint);

            JSONObject session = new JSONObject();
            session.put("sessionId", sessionId);
            session.put("attributes", "{\"key\":\"value\"}");
            data.put("session", session);

            updateMessages(MessageRole.USER.getAlias(), inputString, "text");
            data.put("messages", messages);

            Log.d(TAG,"请求体: %s" + data.toString());
            return data;
        } catch (Exception e) {
            Log.d(TAG, "createRequestBody error " + e);
            return new JSONObject();
        }
    }

    private String generateSign(String timestamp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    Constants.HONOR_SECRET_KEY.getBytes("UTF-8"),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] byteHMAC = mac.doFinal(timestamp.getBytes("UTF-8"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(byteHMAC);
            }
            return "";
        } catch (Exception e) {
            Log.d(TAG, "generateSign error " + e);
            return "";
        }
    }
}
package com.fxzs.lingxiagent.network.ZNet;


import android.util.Log;


import com.fxzs.lingxiagent.model.chat.callback.SSECallback;
import com.fxzs.lingxiagent.model.chat.dto.ChatFileBean;
import com.fxzs.lingxiagent.network.ZNet.bean.ChatContent;
import com.fxzs.lingxiagent.network.ZNet.bean.ChatContentRequest;
import com.fxzs.lingxiagent.util.ZUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okhttp3.internal.http2.StreamResetException;

public class HttpRequest extends Request {
    public static String TAG = "HttpRequest";

    public void sendSmsCode(String mobile,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("mobile",mobile);
        map.put("scene",1);
        Observable observable = retrofit.create(HttpApi.class).sendSmsCode(map);
        toSubscribe(observable, observer);
    }


    public void smsLogin(String mobile,String code,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("mobile",mobile);
        map.put("code",code);
        map.put("device_id","");
        Observable observable = retrofit.create(HttpApi.class).smsLogin(map);
        toSubscribe(observable, observer);
    }

    public void getImageCatList(Observer observer) {
        Map<String,Object> map = new HashMap<>();
        Observable observable = retrofit.create(HttpApi.class).getImageCatList(map);
        toSubscribe(observable, observer);
    }

    public void getMenuList(Observer observer) {
        Map<String,Object> map = new HashMap<>();
        Observable observable = retrofit.create(HttpApi.class).getMenuList(map);
        toSubscribe(observable, observer);
    }

    //风格-绘图-编辑弹窗-横向风格选择
    public void styleList(Observer observer) {
        Map<String,Object> map = new HashMap<>();
        Observable observable = retrofit.create(HttpApi.class).styleList(map);
        toSubscribe(observable, observer);
    }

    //绘图-list
    public void sampleList(int catId,int pageNo,Observer observer) {
        Map<String,Object> map = new HashMap<>();
//        {pageNo: 1, pageSize: 30, catId: 1}
        map.put("pageNo",pageNo);
        map.put("pageSize",30);
        map.put("catId",catId);
        Observable observable = retrofit.create(HttpApi.class).sampleList(map);
        toSubscribe(observable, observer);
    }

    public void getCatDetailList(int menuCatId,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("menuCatId",menuCatId);
        Observable observable = retrofit.create(HttpApi.class).getCatDetailList(map);
        toSubscribe(observable, observer);
    }
    public void getModelTypeList(Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("modelType",8);
        Observable observable = retrofit.create(HttpApi.class).getModelTypeList(map);
        toSubscribe(observable, observer);
    }

    public void createMy(String model,String title,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("model",model);
        if(title.length() > 50){
            title = title.substring(0,50);
        }
        map.put("title",title);
        Observable observable = retrofit.create(HttpApi.class).createMy(map);
        toSubscribe(observable, observer);
    }

    //智能体使用
    public void createMy(String model,String title,String aiMenuId,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("aiMenuId",aiMenuId);
        map.put("model",model);

        if(title.length() > 50){
            title = title.substring(0,50);
        }
        map.put("title",title);
        Observable observable = retrofit.create(HttpApi.class).createMy(map);
        toSubscribe(observable, observer);
    }

    //会议-智能问答使用
    public void createMyMeeting(String model,String title,String systemMessage,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("model",model);

        if(title.length() > 50){
            title = title.substring(0,50);
        }
        map.put("title",title);
        map.put("systemMessage",systemMessage);
        Observable observable = retrofit.create(HttpApi.class).createMy(map);
        toSubscribe(observable, observer);
    }
    public void updateMy(long id,String modelId,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("modelId",modelId);
        Observable observable = retrofit.create(HttpApi.class).updateMyPUT(map);
        toSubscribe(observable, observer);
    }

    public void updateMyMeeting(String id,String systemMessage,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("systemMessage",systemMessage);
        Observable observable = retrofit.create(HttpApi.class).updateMyPUT(map);
        toSubscribe(observable, observer);
    }

    public void updateMyEditName(String id,String name,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("title",name);
        Observable observable = retrofit.create(HttpApi.class).updateMyPUT(map);
        toSubscribe(observable, observer);
    }


    private Disposable sseDisposable;
    public void sendStreams(long conversationId, long modelId, String content, List<ChatFileBean> fileAnalyseUrl, SSECallback callback) {
        Map<String,Object> map = new HashMap<>();
        map.put("conversationId",conversationId+"");
        map.put("modelId",modelId);
        map.put("content",content);
        map.put("useContext",true);
        map.put("isOutline",false);
        List<String> list = new ArrayList<>();
        boolean isImage = false;
        Map<String,Object> fileListJsonMap = new HashMap<>();
        JSONArray fileArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        if(fileAnalyseUrl != null && fileAnalyseUrl.size() > 0){
            for (int i = 0; i < fileAnalyseUrl.size(); i++) {
                isImage = fileAnalyseUrl.get(i).isImage();
                ChatFileBean bean = fileAnalyseUrl.get(i);
                if(isImage){
                    list.add(fileAnalyseUrl.get(i).getPath());
                }else{
                    list.add(bean.getFileAnalyse());
                }
                try {
                    JSONObject fileJson = new JSONObject();
                    fileJson.put("name", bean.getName());
                    fileJson.put("size", bean.getFileSize());
                    fileJson.put("url", bean.getFileAnalyse());
                    fileJson.put("fileUrl", bean.getPath());
                    fileJson.put("schedule", bean.getPercent());
                    fileJson.put("type", bean.getFileType().toUpperCase());
                    fileJson.put("loading", false);
                    fileJson.put("status", "success");
                    fileArray.put(fileJson);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            if(isImage) {
                map.put("images", list);
                map.put("fileListJson",fileArray.toString());
            } else {
                map.put("fileAnalyseUrl",list);
//                try {
//                    jsonObject.put("fileList", fileArray);
//                    jsonObject.toString();
                    map.put("fileListJson",fileArray.toString());
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
            }
        }else {
            map.put("images", list);
        }

        map.put("formatBody","");
        ZUtils.print("map = "+map.toString());

        SseApi api = RetrofitClient.createSseApi();
        ZUtils.print("SseApi = "+api.toString());
        Observable<String> sseObservable = parseSseStream(api.sendStream(map));

//        toSubscribe(sseObservable, observer);
        sseDisposable = sseObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            // 处理接收到的 SSE 数据
                            Log.d("SSE", "Received: " + data);
                            callback.receive(data);
                        },
                        throwable -> {
                            // 处理错误
                            Log.e("SSE", "Error: " + throwable.getMessage());
                        },
                        () -> {
                            // 流结束
                            Log.d("SSE", "Stream completed");
                            callback.end();
                        }
                );
    }
    public void sendFileAnalyseStreams(String fileType,  String fileUrl, SSECallback callback) {
        Map<String,Object> map = new HashMap<>();
        map.put("fileType",fileType.toUpperCase()+"");
        map.put("fileUrl",fileUrl);

        SseApi api = RetrofitClient.createSseApi();
        ZUtils.print("SseApi = "+api.toString());
        Observable<String> sseObservable = parseSseStream(api.analyse(map));

//        toSubscribe(sseObservable, observer);
        sseDisposable = sseObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            // 处理接收到的 SSE 数据
                            Log.d("SSE", "Received: " + data);
                            callback.receive(data);
                        },
                        throwable -> {
                            // 处理错误
                            Log.e("SSE", "Error: " + throwable.getMessage());
                        },
                        () -> {
                            // 流结束
                            Log.d("SSE", "Stream completed");
                            callback.end();
                        }
                );
    }
    public void sendStream(long conversationId, int modelId, String content, List<String> fileAnalyseUrl, SSECallback callback) {
        Map<String,Object> map = new HashMap<>();
        map.put("conversationId",conversationId+"");
        map.put("modelId",modelId);
        map.put("content",content);
        map.put("useContext",true);
        map.put("isOutline",false);
        if(fileAnalyseUrl.size() > 0){
            String name = fileAnalyseUrl.get(0);
            if (name.endsWith("jpg") ||
                    name.endsWith("png")) {
                map.put("images",fileAnalyseUrl);
            }else {
                map.put("fileAnalyseUrl",fileAnalyseUrl);
            }
        }
        map.put("formatBody","");
        ZUtils.print("map = "+map.toString());

        SseApi api = RetrofitClient.createSseApi();
        ZUtils.print("SseApi = "+api.toString());
        Observable<String> sseObservable = parseSseStream(api.sendStream(map));

//        toSubscribe(sseObservable, observer);
        sseDisposable = sseObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            // 处理接收到的 SSE 数据
                            Log.d("SSE", "Received: " + data);
                            callback.receive(data);
                        },
                        throwable -> {
                            // 处理错误
                            Log.e("SSE", "Error: " + throwable.getMessage());
                        },
                        () -> {
                            // 流结束
                            Log.d("SSE", "Stream completed");
                            callback.end();
                        }
                );
    }

    public Disposable getSseDisposable() {
        return sseDisposable;
    }

    //
//    public Observable<String> parseSseStream(Observable<ResponseBody> responseBodyObservable) {
//        return responseBodyObservable
//                .subscribeOn(Schedulers.io())
//                .flatMap(responseBody -> Observable.create(emitter -> {
//                    try (BufferedReader reader = new BufferedReader(
//                            new InputStreamReader(responseBody.byteStream()))) {
//                        String line;
//                        while (!emitter.isDisposed() && (line = reader.readLine()) != null ) {
//                           ZUtils.print("line = "+line.toString());
//                            if (line.startsWith("data:")) {
//                                String data = line.substring(5).trim(); // 去掉 "data:" 前缀
//                                if (!data.isEmpty()) {
//                                    emitter.onNext(data);
//                                }
//                            }
//                        }
//                        emitter.onComplete();
//                    } catch (Exception e) {
//                        emitter.onError(e);
//                    }
//                }));
//    }

    public Observable<String> parseSseStream(Observable<ResponseBody> responseBodyObservable) {
        return responseBodyObservable
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody -> Observable.create(emitter -> {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                        String line;
                        while (!emitter.isDisposed() && (line = reader.readLine()) != null) {
//                            ZUtils.print("line = " + line);
                            if (line.startsWith("data:")) {
                                String data = line.substring(5).trim();
                                if (!data.isEmpty()) {
                                    emitter.onNext(data);
                                }
                            }
                        }
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
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
    public void getStsConfig(Observer observer) {
        Observable observable = retrofit.create(HttpApi.class).getStsConfig();
        toSubscribe(observable, observer);
    }

    public void getDetailByModel(String modelId,Observer observer) {
        Map<String,Object> map = new HashMap<>();
        map.put("modelId",modelId);
        Observable observable = retrofit.create(HttpApi.class).getDetailByModel(map);
        toSubscribe(observable, observer);
    }

    public void sendChatLink(String title, List<ChatContent> contents, Observer observer) {
        ChatContentRequest request = new ChatContentRequest(title, contents);
        Observable observable = retrofit.create(HttpApi.class).sendChatLink(request);
        toSubscribe(observable, observer);
    }

}
package com.fxzs.lingxiagent.util;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.fxzs.lingxiagent.model.chat.callback.StsCallback;
import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.meeting.api.MeetingApiService;
import com.fxzs.lingxiagent.model.meeting.dto.SoundRecordRecognitionRequestDto;
import com.fxzs.lingxiagent.model.meeting.dto.SoundRecordTaskCheckRequestDto;
import com.fxzs.lingxiagent.model.meeting.dto.SoundRecordTaskResponseDto;
import com.fxzs.lingxiagent.model.meeting.dto.SoundRecordTaskSubmitResponseDto;
import com.fxzs.lingxiagent.model.meeting.dto.*;
import com.fxzs.lingxiagent.model.network.RetrofitClient;
import com.fxzs.lingxiagent.util.ZUtil.SessionUpload;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 音频识别管理器
 * 负责文件上传、任务提交和结果查询的完整流程
 */
public class AudioRecognitionManager {

    private static final String TAG = "AudioRecognitionManager";
    private boolean isCancel;

    public interface AudioRecognitionCallback {
        void onUploadProgress(int progress);
        void onUploadSuccess(String fileUrl);
        void onTaskSubmitted(Long taskId);
        void onRecognitionProgress(int progress);
        void onRecognitionCompleted(String result, Integer meetingId);
        void onError(String error);
    }
    
    private Context context;
    private MeetingApiService apiService;
    private AudioRecognitionCallback callback;
    private String selectedLanguage = "16k_zh_large";
    
    public AudioRecognitionManager(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getInstance().createService(MeetingApiService.class);
    }
    
    /**
     * 开始完整的音频识别流程
     * @param audioFilePath 本地音频文件路径
     * @param meetingId 会议ID
     * @param callback 回调接口
     */
    public void startAudioRecognition(String audioFilePath, Integer meetingId, AudioRecognitionCallback callback) {
        this.callback = callback;
        
        Log.i(TAG, "开始音频识别流程，文件路径: " + audioFilePath);
        
        // 检查文件是否存在
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            if (callback != null) {
                callback.onError("音频文件不存在: " + audioFilePath);
            }
            return;
        }
        
        // 第一步：上传文件到云端
        uploadAudioFile(audioFilePath, meetingId);
    }
    
    /**
     * 上传音频文件到云端
     */
    private void uploadAudioFile(String audioFilePath, Integer meetingId) {
        Log.i(TAG, "开始上传音频文件...");
        Log.d(TAG, "上传参数: audioFilePath=" + audioFilePath + ", meetingId=" + meetingId);
        
        // 检查文件信息
        File file = new File(audioFilePath);
        Log.d(TAG, "文件信息: 大小=" + file.length() + "bytes, 存在=" + file.exists());
        
        if (callback != null) {
            callback.onUploadProgress(0);
        }

        SessionUpload.upload(context, audioFilePath, new StsCallback() {
            @Override
            public void progress(long percent) {

            }

            @Override
            public void callback(String fileUrl) {
                Log.i(TAG, "文件上传成功，URL: " + fileUrl);
                Log.d(TAG, "上传回调: fileUrl=" + fileUrl);
                
                if (callback != null) {
                    callback.onUploadProgress(100);
                    callback.onUploadSuccess(fileUrl);
                }
                
                // 第二步：提交识别任务
                submitRecognitionTask(fileUrl, meetingId);
            }
        });
    }
    
    /**
     * 提交录音识别任务
     */
    private void submitRecognitionTask(String fileUrl, Integer meetingId) {
        Log.i(TAG, "提交录音识别任务...");
        Log.d(TAG, "提交参数: fileUrl=" + fileUrl + ", meetingId=" + meetingId);
        
        SoundRecordRecognitionRequestDto request = new SoundRecordRecognitionRequestDto();
        request.setFileUrl(fileUrl);
        request.setEngineModelType(selectedLanguage);
        request.setMeetingId(meetingId);
        
        Log.d(TAG, "请求体: " + request.toString());
        
        Call<BaseResponse<SoundRecordTaskSubmitResponseDto>> call = apiService.submitAudioRecognitionTask(request);
        Log.d(TAG, "发送API请求: " + call.request().url());
        
        call.enqueue(new Callback<BaseResponse<SoundRecordTaskSubmitResponseDto>>() {
            @Override
            public void onResponse(Call<BaseResponse<SoundRecordTaskSubmitResponseDto>> call, 
                                 Response<BaseResponse<SoundRecordTaskSubmitResponseDto>> response) {
                Log.d(TAG, "提交任务API响应: isSuccessful=" + response.isSuccessful() + ", code=" + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<SoundRecordTaskSubmitResponseDto> baseResponse = response.body();
                    Log.d(TAG, "响应体: code=" + baseResponse.getCode() + ", msg=" + baseResponse.getMsg());
                    
                    if (baseResponse.getCode() == 0 && baseResponse.getData() != null) {
                        SoundRecordTaskSubmitResponseDto taskResponse = baseResponse.getData();
                        Long taskIdLong = null;
                        
                        // 从嵌套的data结构中获取taskId
                        if (taskResponse.getData() != null) {
                            taskIdLong = taskResponse.getData().getTaskId();
                        }
                        
                        Log.i(TAG, "任务提交成功，taskId: " + taskIdLong);
                        Log.d(TAG, "任务详情: " + taskResponse.toString());
                        
                        if (taskIdLong != null) {
                            // 直接使用Long类型的taskId，避免溢出
                            if (callback != null) {
                                callback.onTaskSubmitted(taskIdLong); // 现在回调使用Long类型
                            }
                            
                            // 第三步：开始轮询查询结果，使用Long类型
                            startPollingForResult(taskIdLong, meetingId);
                        } else {
                            String error = "任务提交成功但未返回taskId";
                            Log.e(TAG, error);
                            if (callback != null) {
                                callback.onError(error);
                            }
                        }
                    } else {
                        String error = "任务提交失败: " + baseResponse.getMsg() + " (code=" + baseResponse.getCode() + ")";
                        Log.e(TAG, error);
                        if (callback != null) {
                            callback.onError(error);
                        }
                    }
                } else {
                    String error = "任务提交请求失败，HTTP状态码: " + response.code();
                    Log.e(TAG, error);
                    
                    // 尝试获取错误详情
                    if (response.errorBody() != null) {
                        try {
                            String errorDetail = response.errorBody().string();
                            Log.e(TAG, "错误详情: " + errorDetail);
                            error += ", 详情: " + errorDetail;
                        } catch (Exception e) {
                            Log.w(TAG, "解析错误详情失败", e);
                        }
                    }
                    
                    if (callback != null) {
                        callback.onError(error);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<BaseResponse<SoundRecordTaskSubmitResponseDto>> call, Throwable t) {
                String error = "任务提交网络错误: " + t.getMessage();
                Log.e(TAG, error, t);
                Log.e(TAG, "请求URL: " + call.request().url());
                
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }
    
    /**
     * 开始轮询查询识别结果
     */
    private void startPollingForResult(Long taskId, Integer meetingId) {
        Log.i(TAG, "开始轮询查询识别结果，taskId: " + taskId);
        
        // 创建一个线程来处理轮询
        new Thread(() -> {
            int maxPollingAttempts = 60; // 最多查询60次
            int pollingAttempt = 0;
            int intervalMs = 5000; // 每5秒查询一次
            
            while (pollingAttempt < maxPollingAttempts && !isCancel) {
                try {
                    Thread.sleep(intervalMs);
                    pollingAttempt++;
                    
                    Log.d(TAG, "第 " + pollingAttempt + " 次查询识别结果...");
                    
                    // 调用查询结果
                    boolean shouldContinue = queryRecognitionResult(taskId, meetingId, pollingAttempt, maxPollingAttempts);
                    
                    // 如果识别完成或失败，停止轮询
                    if (!shouldContinue) {
                        break;
                    }
                    
                } catch (InterruptedException e) {
                    Log.e(TAG, "轮询线程被中断", e);
                    break;
                }
            }
            
            // 如果达到最大轮询次数仍未完成
            if (pollingAttempt >= maxPollingAttempts) {
                Log.w(TAG, "达到最大轮询次数(" + maxPollingAttempts + ")，识别超时");
                if (callback != null) {
                    callback.onError("识别超时，请稍后重试");
                }
            }
        }).start();
    }
    
    /**
     * 查询识别结果
     * @param taskId 任务ID
     * @param meetingId 会议ID
     * @param pollingAttempt 轮询次数
     * @param maxPollingAttempts 最大轮询次数
     * @return 是否继续轮询
     */
    private boolean queryRecognitionResult(Long taskId, Integer meetingId, int pollingAttempt, int maxPollingAttempts) {
        Log.d(TAG, "开始查询识别结果，taskId: " + taskId + ", pollingAttempt: " + pollingAttempt);
        
        // 网络异常重试机制
        int maxNetworkRetries = 3;
        int retryDelay = 2000; // 2秒
        
        for (int retryCount = 0; retryCount < maxNetworkRetries; retryCount++) {
            try {
                if (retryCount > 0) {
                    Log.i(TAG, "网络异常，第 " + (retryCount + 1) + " 次重试查询结果...");
                    Thread.sleep(retryDelay);
                }
                
                QueryResult result = queryRecognitionResultSync(taskId, meetingId);
                
                if (result.networkError) {
                    // 网络异常，继续重试
                    Log.w(TAG, "网络异常 (第" + (retryCount + 1) + "次重试): " + result.errorMessage);
                    
                    if (retryCount == maxNetworkRetries - 1) {
                        // 达到最大重试次数
                        Log.e(TAG, "网络异常达到最大重试次数(" + maxNetworkRetries + ")，查询失败");
                        if (callback != null) {
                            callback.onError("网络异常，查询识别结果失败，已重试 " + maxNetworkRetries + " 次: " + result.errorMessage);
                        }
                        return false; // 停止轮询
                    }
                    continue; // 继续重试
                }
                
                // 非网络异常，处理业务逻辑
                if (result.success) {
                    Log.i(TAG, "========== 查询成功 ==========");
                    Log.i(TAG, "任务状态: " + result.status);
                    Log.i(TAG, "进度: " + result.progress + "%");
                    Log.i(TAG, "错误信息: " + (result.errorMessage != null ? result.errorMessage : "无"));
                    Log.i(TAG, "结果长度: " + (result.recognitionResult != null ? result.recognitionResult.length() : 0));
                    
                    if ("completed".equals(result.status)) {
                        // 识别完成
                        Log.i(TAG, "🎉 识别完成！");
                        Log.i(TAG, "完整识别结果:");
                        Log.i(TAG, "====================");
                        Log.i(TAG, result.recognitionResult != null ? result.recognitionResult : "结果为空");
                        Log.i(TAG, "====================");
                        
                        if (callback != null) {
                            callback.onRecognitionCompleted(result.recognitionResult, meetingId);
                        }
                        return false; // 停止轮询
                        
                    } else if ("failed".equals(result.status)) {
                        // 识别失败
                        String error = "识别失败: " + result.errorMessage;
                        Log.e(TAG, "❌ " + error);
                        if (callback != null) {
                            callback.onError(error);
                        }
                        return false; // 停止轮询
                        
                    } else if ("processing".equals(result.status)) {
                        // 正在处理中
                        Log.d(TAG, "⏳ 识别进行中，进度: " + result.progress + "%");
                        if (result.progress != null && callback != null) {
                            callback.onRecognitionProgress(result.progress);
                        }
                        return true; // 继续轮询
                    } else {
                        // 其他状态
                        Log.w(TAG, "⚠️ 未知状态: " + result.status + ", 继续轮询...");
                        return true; // 继续轮询
                    }
                } else {
                    // API返回错误或data为null，但不是网络异常
                    if (result.isTaskNotFound) {
                        Log.w(TAG, "任务未找到或还未开始处理，继续轮询...");
                        return true; // 继续轮询
                    } else {
                        Log.e(TAG, "API返回错误: " + result.errorMessage);
                        if (callback != null) {
                            callback.onError("查询识别结果失败: " + result.errorMessage);
                        }
                        return false; // 停止轮询
                    }
                }
                
            } catch (InterruptedException e) {
                Log.e(TAG, "重试延迟被中断", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return true; // 继续轮询
    }


    /**
     * 同步查询识别结果
     */
    private QueryResult queryRecognitionResultSync(Long taskId, Integer meetingId) {
        try {
            SoundRecordTaskCheckRequestDto request = new SoundRecordTaskCheckRequestDto();
            request.setTaskId(taskId); // 直接使用Long类型
            request.setMeetingId(meetingId);
            
            Log.d(TAG, "发送API请求: taskId=" + taskId + ", meetingId=" + meetingId);
            Log.d(TAG, "请求对象: " + request.toString());
            
            Call<BaseResponse<SoundRecordTaskResponseDto>> call = apiService.queryAudioRecognitionResult(request);
            Response<BaseResponse<SoundRecordTaskResponseDto>> response = call.execute();
            
            Log.d(TAG, "API响应: isSuccessful=" + response.isSuccessful() + ", code=" + response.code());
            
            if (response.isSuccessful() && response.body() != null) {
                BaseResponse<SoundRecordTaskResponseDto> baseResponse = response.body();
                Log.d(TAG, "响应体: code=" + baseResponse.getCode() + ", msg=" + baseResponse.getMsg());
                
                if (baseResponse.getCode() == 0) {
                    if (baseResponse.getData() != null) {
                        SoundRecordTaskResponseDto result = baseResponse.getData();
                        
                        QueryResult queryResult = new QueryResult();
                        queryResult.success = true;
                        
                        // 从嵌套结构中获取状态
                        if (result.getData() != null) {
                            SoundRecordTaskResponseDto.TaskData taskData = result.getData();
                            
                            Log.d(TAG, "========== API响应解析 ==========");
                            Log.d(TAG, "原始状态字符串: " + taskData.getStatusStr());
                            Log.d(TAG, "数字状态: " + taskData.getStatus());
                            Log.d(TAG, "识别结果: " + (taskData.getResult() != null ? taskData.getResult().length() + "字符" : "null"));
                            Log.d(TAG, "错误信息: " + taskData.getErrorMsg());
                            Log.d(TAG, "音频时长: " + taskData.getAudioDuration());
                            Log.d(TAG, "任务ID: " + taskData.getTaskId());
                            
                            // 映射状态
                            String originalStatus = taskData.getStatusStr();
                            if ("doing".equals(originalStatus)) {
                                queryResult.status = "processing";
                            } else if ("success".equals(originalStatus)) {
                                queryResult.status = "completed";
                            } else if ("failed".equals(originalStatus)) {
                                queryResult.status = "failed";
                            } else {
                                queryResult.status = originalStatus;
                            }
                            
                            queryResult.recognitionResult = taskData.getResult();
                            queryResult.errorMessage = taskData.getErrorMsg();
                            
                            // 根据数字状态推断进度
                            if (taskData.getStatus() != null) {
                                switch (taskData.getStatus()) {
                                    case 1: // 处理中
                                        queryResult.progress = 50;
                                        break;
                                    case 2: // 完成
                                        queryResult.progress = 100;
                                        break;
                                    case 3: // 失败
                                        queryResult.progress = 0;
                                        break;
                                    default:
                                        queryResult.progress = 0;
                                }
                            }
                            
                            Log.d(TAG, "状态映射: " + originalStatus + " -> " + queryResult.status);
                            Log.d(TAG, "==============================");
                        } else {
                            // 兼容旧格式
                            Log.d(TAG, "使用兼容格式解析");
                            queryResult.status = result.getStatus();
                            queryResult.recognitionResult = result.getResult();
                            queryResult.progress = result.getProgress();
                            queryResult.errorMessage = result.getMessage();
                        }
                        
                        Log.d(TAG, "最终解析结果: status=" + queryResult.status + ", progress=" + queryResult.progress);
                        
                        return queryResult;
                    } else {
                        // data为null，表示任务还未开始处理或查询太早
                        QueryResult queryResult = new QueryResult();
                        queryResult.success = false;
                        queryResult.isTaskNotFound = true;
                        queryResult.errorMessage = "任务还未开始处理，继续等待...";
                        return queryResult;
                    }
                } else {
                    QueryResult queryResult = new QueryResult();
                    queryResult.success = false;
                    queryResult.errorMessage = "API返回错误: " + baseResponse.getMsg() + " (code=" + baseResponse.getCode() + ")";
                    return queryResult;
                }
            } else {
                QueryResult queryResult = new QueryResult();
                queryResult.success = false;
                queryResult.networkError = true; // 标记为网络错误
                queryResult.errorMessage = "HTTP请求失败，状态码: " + response.code();
                if (response.errorBody() != null) {
                    try {
                        queryResult.errorMessage += ", 错误详情: " + response.errorBody().string();
                    } catch (Exception e) {
                        Log.w(TAG, "解析错误详情失败", e);
                    }
                }
                return queryResult;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "同步查询识别结果异常", e);
            QueryResult queryResult = new QueryResult();
            queryResult.success = false;
            queryResult.networkError = true; // 标记为网络错误
            queryResult.errorMessage = "网络异常: " + e.getMessage();
            return queryResult;
        }
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
        ZUtils.print("isCancel = "+isCancel);
    }

    /**
     * 查询结果封装类
     */
    private static class QueryResult {
        boolean success;
        String status;
        String recognitionResult;
        Integer progress;
        String errorMessage;
        boolean networkError = false; // 是否为网络错误
        boolean isTaskNotFound = false; // 任务未找到或未开始处理
    }
}
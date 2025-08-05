package com.fxzs.lingxiagent.network;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 自定义OkHttp日志拦截器
 * 打印完整的请求和响应内容
 */
public class CustomLoggingInterceptor implements Interceptor {
    
    private static final String TAG = "HTTP_LOG";
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        
        // 打印请求信息
        logRequest(request);
        
        long startNs = System.nanoTime();
        Response response = chain.proceed(request);
        long tookMs = (System.nanoTime() - startNs) / 1000000;
        
        // 打印响应信息
        logResponse(response, tookMs);
        
        return response;
    }
    
    private void logRequest(Request request) throws IOException {
        Log.d(TAG, "==================== 请求开始 ====================");
        Log.d(TAG, "请求方法: " + request.method());
        Log.d(TAG, "请求URL: " + request.url());
        
        // 打印请求头
        Headers headers = request.headers();
        if (headers.size() > 0) {
            Log.d(TAG, "请求头:");
            for (int i = 0; i < headers.size(); i++) {
                Log.d(TAG, "  " + headers.name(i) + ": " + headers.value(i));
            }
        }
        
        // 打印请求体
        RequestBody requestBody = request.body();
        if (requestBody != null) {
            try {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                
                MediaType contentType = requestBody.contentType();
                Charset charset = UTF8;
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                
                if (isPlaintext(buffer)) {
                    String body = buffer.readString(charset);
                    Log.d(TAG, "请求体:");
                    Log.d(TAG, body);
                } else {
                    Log.d(TAG, "请求体: (二进制内容，长度 " + requestBody.contentLength() + " 字节)");
                }
            } catch (Exception e) {
                Log.e(TAG, "打印请求体失败: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "请求体: (无)");
        }
        Log.d(TAG, "==================== 请求结束 ====================");
    }
    
    private void logResponse(Response response, long tookMs) throws IOException {
        Log.d(TAG, "==================== 响应开始 ====================");
        Log.d(TAG, "响应URL: " + response.request().url());
        Log.d(TAG, "响应状态码: " + response.code() + " " + response.message());
        Log.d(TAG, "响应耗时: " + tookMs + " ms");
        
        // 打印响应头
        Headers headers = response.headers();
        if (headers.size() > 0) {
            Log.d(TAG, "响应头:");
            for (int i = 0; i < headers.size(); i++) {
                Log.d(TAG, "  " + headers.name(i) + ": " + headers.value(i));
            }
        }
        
        // 打印响应体
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // 缓冲整个响应体
            Buffer buffer = source.getBuffer();
            
            MediaType contentType = responseBody.contentType();
            Charset charset = UTF8;
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            
            if (responseBody.contentLength() != 0 && isPlaintext(buffer)) {
                String body = buffer.clone().readString(charset);
                Log.d(TAG, "响应体:");
                
                // 如果响应体太长，分段打印
                int maxLogLength = 4000; // Android Log的最大长度限制
                for (int i = 0; i < body.length(); i += maxLogLength) {
                    int end = Math.min(body.length(), i + maxLogLength);
                    Log.d(TAG, body.substring(i, end));
                }
            } else if (responseBody.contentLength() == 0) {
                Log.d(TAG, "响应体: (空)");
            } else {
                Log.d(TAG, "响应体: (二进制内容，长度 " + responseBody.contentLength() + " 字节)");
            }
        } else {
            Log.d(TAG, "响应体: (无)");
        }
        Log.d(TAG, "==================== 响应结束 ====================");
    }
    
    /**
     * 判断是否为纯文本内容
     */
    private boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
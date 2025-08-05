package com.fxzs.lingxiagent.model.network;

import android.util.Log;

import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 意图相关接口认证拦截器 - 自动添加Token和X-Client-Ip到请求头
 */
public class IntentionAuthInterceptor implements Interceptor {
    
    private static final String TAG = "AuthInterceptor";
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();
        
        String url = originalRequest.url().toString();
        requestBuilder.header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);

        // 这些接口不需要认证
        boolean noAuthRequired = url.contains("/app-api/member/auth/send-sms-code");
        
        // 如果需要认证且有Token，添加认证头
        if (!noAuthRequired) {
            String token = SharedPreferencesUtil.getIntentionToken();
            Log.d(TAG, "Token used: " + token);
            if (!token.isEmpty()) {
                requestBuilder.header(Constants.HEADER_AUTHORIZATION, Constants.HEADER_BEARER + token);
                Log.d(TAG, "Added auth header for URL: " + url);
            } else {
                Log.d(TAG, "No token available for URL: " + url);
            }
            String clientIp = SharedPreferencesUtil.getClientIP();
            Log.d(TAG, "clientIp used: " + clientIp);
            if (!clientIp.isEmpty()) {
                requestBuilder.header(Constants.X_CLIENT_IP, clientIp);
                Log.d(TAG, "Added clientIp auth header for URL: " + url);
            } else {
                Log.d(TAG, "No clientIp available for URL: " + url);
            }
        } else {
            Log.d(TAG, "No auth required for URL: " + url);
        }
        
        return chain.proceed(requestBuilder.build());
    }
}
package com.fxzs.lingxiagent.network.ZNet;

import android.text.TextUtils;
import android.util.Log;

import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.honor.api.HonorApiService;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.util.ZUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {


    private String TAG = "RetrofitClient";
    private static final RetrofitClient instance = new RetrofitClient();

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constants.BASE_URL) //基础url,其他部分在GetRequestInterface里
            .client(httpClient())

            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                    .setLenient()
                    .create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

    public static RetrofitClient getInstance() {
        return instance;
    }

    private OkHttpClient httpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
//                Log.e(TAG, message);
                ZUtils.print(message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request;

                String token = SharedPreferencesUtil.getToken();
                //TODO 上线删除 306de8b9b5f5437880ad99e54e1e0994
//                token = "306de8b9b5f5437880ad99e54e1e0994";
                Log.d("TAGMY","token = "+token);
                if(!TextUtils.isEmpty(token)){
                 request = chain.request()
                        .newBuilder()
                        .addHeader("Authorization","Bearer "+token)
                        .build();
                } else {
                request = chain.request()
                        .newBuilder()
                        .build();
                }
                ZUtils.print("request = "+request.toString());
                ZUtils.print("request headers = "+request.headers().toString());


                Response response = chain.proceed(request);

                // 解析响应体
                if (response.isSuccessful()) {
                    try {
                        // 获取响应体的字符串（注意：只能读取一次）
                        String responseBodyString = response.body().string();
                        ZUtils.print("response body = " + responseBodyString);

                        // 使用 Gson 解析为 ApiResponse
                        Gson gson = new GsonBuilder().setLenient().create();
                        Type type = new TypeToken<ApiResponse<Object>>() {}.getType();
                        ApiResponse<Object> apiResponse = gson.fromJson(responseBodyString, type);

                        // 判断 code
                        if (apiResponse.getCode() == 0) {
                            ZUtils.print("Request succeeded with code: " + apiResponse.getCode());
                        } else {
                            ZUtils.print("Request failed with code: " + apiResponse.getCode() + ", msg: " + apiResponse.getMsg());
                            // 可根据 code 进行特定处理，例如抛出异常或修改响应

//                            MyApp.getContext().startActivity(new Intent( MyApp.getContext(), LoginActivity.class));
//                            EventBus.getDefault().post(new LoginEvent());
                        }

                        // 重新构建响应体，因为 body 已被消费
                        ResponseBody newResponseBody = ResponseBody.create(
                                response.body().contentType(),
                                responseBodyString
                        );
                        response = response.newBuilder().body(newResponseBody).build();
                    } catch (Exception e) {
                        ZUtils.print("Error parsing response: " + e.getMessage());
                    }
                } else {
                    ZUtils.print("Request failed with HTTP code: " + response.code());
                }


                return response;
            }
        };
        return new OkHttpClient.Builder()
//                .addInterceptor(new AccessTokenInterceptor())
                .addInterceptor(loggingInterceptor)
                .addInterceptor(interceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();
    }


    private static OkHttpClient httpClientSSE() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
//                Log.e(TAG, message);
                ZUtils.print(message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request;

                String token = SharedPreferencesUtil.getToken();
                if(!TextUtils.isEmpty(token)){
                    request = chain.request()
                            .newBuilder()
                            .header("Authorization","Bearer "+token)
//                            .header("Accept", "application/json") // 明确指定 SSE
//                            .header("Connection", "keep-alive") // 确保长连接
                            .header("Accept", "text/event-stream") // 明确指定 SSE
                            .build();
                } else {
                    request = chain.request()
                            .newBuilder()
                            .build();
                }
                ZUtils.print("httpClientSSE request = "+request.toString());
                ZUtils.print("httpClientSSE request headers = "+request.headers().toString());


                Response response = chain.proceed(request);


                return response;
            }
        };
        return new OkHttpClient.Builder()
//                .addInterceptor(new AccessTokenInterceptor())
//                .addInterceptor(loggingInterceptor)
                .addInterceptor(interceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public static SseApi createSseApi() {
//        OkHttpClient client = new OkHttpClient.Builder()
//
////                .addInterceptor(loggingInterceptor)
//                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(httpClientSSE())
                .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit.create(SseApi.class);
    }

    public static HonorApiService createHonorApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL_HONOR)
                .client(httpClientSSE())
                .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit.create(HonorApiService.class);
    }
}

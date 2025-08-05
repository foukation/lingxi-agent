package com.fxzs.lingxiagent;

import android.app.Application;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.fxzs.lingxiagent.model.auth.AuthHelper;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.util.LocaleManager;
import com.fxzs.lingxiagent.util.GlobalSettings;
import com.fxzs.lingxiagent.lingxi.lingxi_conversation.AIServiceManager;

import com.fxzs.lingxiagent.util.ZUtil.MarkdownUtils;

import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.vove7.andro_accessibility_api.AccessibilityApi;
import cn.vove7.andro_accessibility_api.BaseAccessibilityService;
import okhttp3.OkHttpClient;
import cn.jpush.android.api.JPushInterface;
import timber.log.Timber;

/**
 * 应用Application类
 */
public class IYAApplication extends Application {
    
    private static IYAApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 初始化SharedPreferences
        SharedPreferencesUtil.init(this);
        
        // 初始化全局设置
        GlobalSettings.getInstance().init(this);
        
        // 初始化语言管理器
        LocaleManager.init(this);
        
        // 应用保存的语言设置
        LocaleManager.applyLanguage(this);

        // 预加载字体，提升Markdown渲染性能
        MarkdownUtils.preloadFonts(this);

        // 初始化极光推送
        JPushInterface.setDebugMode(true);  // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);           // 初始化 JPush

        // 解决glide加载https证书问题
        try {
            Glide.get(this).getRegistry().replace(
                    GlideUrl.class, InputStream.class,
                    new OkHttpUrlLoader.Factory(getSSLOkHttpClient()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 初始化登录接口
        AuthHelper.getInstance().init(this);

        AIServiceManager.Companion.initialize(this);
        initializeServices();


    }
    
    public static IYAApplication getInstance() {
        return instance;
    }

    /**
     * 设置https 访问的时候对所有证书都进行信任
     *
     * @throws Exception
     */
    private OkHttpClient getSSLOkHttpClient() throws Exception {
        final X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        return new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .build();
    }


    public static void initializeServices() {

        AccessibilityApi.BASE_SERVICE_CLS = BaseAccessibilityService.class;
        AccessibilityApi.GESTURE_SERVICE_CLS = BaseAccessibilityService.class;
        AccessibilityApi.Companion.init(instance, BaseAccessibilityService.class,BaseAccessibilityService.class);

        Timber.DebugTree tree = new Timber.DebugTree();
        Timber.plant(tree);
    }
}
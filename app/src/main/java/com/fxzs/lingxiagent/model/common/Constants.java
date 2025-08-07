package com.fxzs.lingxiagent.model.common;

/**
 * 应用常量类
 */
public class Constants {
    // Unified Certification Center
    public static final String APP_ID = "02009401";
    public static final String APP_KEY = "F725672A4D107AB2";
    // Key for data encryption
    public static final String KEY_ALIAS = "F13x3TyMoUToL3gh";
    public static final String CLIENT_ID = "lingxi_android";
    public static final String PROJECT_CODE = "lingxi";

    // API基础URL
    public static final String BASE_URL = "https://ivs.chinamobiledevice.com:11443/lingxi/";
    public static final String BASE_URL_V1 = "http://36.213.71.163:11453/";
    public static final String BASE_URL_V2 = "http://36.213.71.163:11470/";
    public static final String BASE_URL_V3 = "http://36.213.71.163:11507/";
    public static final String BASE_URL_V4 = "http://36.213.71.163:11508/";
    public static final String BASE_URL_HONOR = "https://honor.tscfn.cn/";
    public static final String HONOR_MEET = "https://honor.tscfn.cn/honor-agent/v1/medical-advice";   // 荣耀同城聚会
    public static final String HONOR_TRIP = "https://honor.tscfn.cn/honor-agent/v2/travel-planning";  // 荣耀同城出行
    // 用户协议
    public static final String USER_AGREEMENT_URL = "https://mobile-web.jmkjsh.com/user_contract.html";
    // 隐私政策
    public static final String PRIVACY_POLICY_URL = "https://mobile-web.jmkjsh.com/privacy.html";
    public static final String HONOR_ACCESS_KEY = "2fe3e88fc9a7f943c5c2cdb8f4a6199c";
    public static final String HONOR_SECRET_KEY= "ecea44d8f0143567a8a45555ac2c6dcfbb4ab231d8185c623f5735c5947d24af";

    // SharedPreferences相关
    public static final String PREF_NAME = "lingxi_pref";
    public static final String PREF_TOKEN = "user_token";
    public static final String PREF_REFRESH_TOKEN = "refresh_token";
    public static final String PREF_EXPIRES_TIME = "expires_time";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_PHONE = "user_phone";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_AVATAR = "user_avatar";
    public static final String PREF_CLIENT_IP = "client_ip";
    public static final String PREF_INTENTION_TOKEN = "intention_token";
    public static final String PREF_CONVERSATION_ID = "conversation_id";//主页最近一次对话id
    // 网络请求超时时间（秒）
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
    
    // 验证码发送场景
    public static final int SCENE_LOGIN = 1;         // 登录
    public static final int SCENE_CHANGE_PHONE = 2;  // 更换手机号
    public static final int SCENE_RESET_PWD = 3;     // 重置密码
    public static final int SCENE_REGISTER = 4;      // 注册用户
    public static final int SCENE_DELETE_USER = 5;   // 注销用户

    // 验证码倒计时（秒）
    public static final int SMS_COUNTDOWN = 60;
    // 验证码长度
    public static final int VERIFICATION_CODE_LEN = 6;
    
    // 请求Header
    public static final String HEADER_CLIENT_ID = "client-id";
    public static final String HEADER_PROJECT_CODE = "project-code";
    public static final String HEADER_VERSION = "version";
    public static final String HEADER_TIME = "ts";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_BEARER = "Bearer ";
    public static final String X_CLIENT_IP = "X-Client-Ip";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json; charset=utf-8";
    // SharedPreferences Keys（用于VMLogin）
    public static final String KEY_TOKEN = PREF_TOKEN;
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_USER_PHONE = PREF_USER_PHONE;
    public static final String KEY_USER_ID = PREF_USER_ID;
    public static final String KEY_IS_AUTO = "KEY_IS_AUTO";
}
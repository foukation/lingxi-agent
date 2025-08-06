package com.fxzs.lingxiagent.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.fxzs.lingxiagent.model.auth.dto.LoginResponse;
import com.fxzs.lingxiagent.model.user.dto.UserDto;
import com.fxzs.lingxiagent.model.common.Constants;

/**
 * SharedPreferences工具类
 */
public class SharedPreferencesUtil {
    private static SharedPreferences sPreferences;

    public static void init(Context context) {
        if (sPreferences == null) {
            sPreferences = context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    /**
     * 保存登录信息
     */
    public static void saveLoginInfo(LoginResponse loginResponse) {
        if (loginResponse == null) return;

        SharedPreferences.Editor editor = sPreferences.edit();
        if (loginResponse.getAccessToken() != null) {
            editor.putString(Constants.PREF_TOKEN, loginResponse.getAccessToken());
        }
        if (loginResponse.getRefreshToken() != null) {
            editor.putString(Constants.PREF_REFRESH_TOKEN, loginResponse.getRefreshToken());
        }
        if (loginResponse.getExpiresTime() != null) {
            editor.putLong(Constants.PREF_EXPIRES_TIME, loginResponse.getExpiresTime());
        }
        if (loginResponse.getUserId() != null) {
            editor.putLong(Constants.PREF_USER_ID, loginResponse.getUserId());
        }

        editor.apply();
    }

    /**
     * 保存用户信息
     */
    public static void saveUserInfo(UserDto userDto) {
        if (userDto == null) return;

        SharedPreferences.Editor editor = sPreferences.edit();
        if (userDto.getAvatar() != null) {
            editor.putString(Constants.PREF_USER_AVATAR, userDto.getAvatar());
        }
        if (userDto.getNickname() != null) {
            editor.putString(Constants.PREF_USER_NAME, userDto.getNickname());
        }
        if (userDto.getMobile() != null) {
            editor.putString(Constants.PREF_USER_PHONE, userDto.getMobile());
        }
        if (userDto.getId() != null) {
            editor.putLong(Constants.PREF_USER_ID, userDto.getId());
        }

        editor.apply();
    }

    /**
     * 获取Token
     */
    public static String getToken() {
        return sPreferences.getString(Constants.PREF_TOKEN, "");
    }

    /**
     * 更新intenttion Token
     */
    public static void updateIntentionToken(String token) {
        sPreferences.edit().putString(Constants.PREF_INTENTION_TOKEN, token).apply();
    }

    /**
     * 更新ClientIP
     */
    public static void updateClientIP(String publicIp) {
        sPreferences.edit().putString(Constants.PREF_CLIENT_IP, publicIp).apply();
    }

    /**
     * 获取intenttion Token
     */
    public static String getIntentionToken() {
        return sPreferences.getString(Constants.PREF_INTENTION_TOKEN, "");
    }

    /**
     * 获取ClientIP
     */
    public static String getClientIP() {
        return sPreferences.getString(Constants.PREF_CLIENT_IP, "");
    }

    /**
     * 获取AccessToken (alias for getToken)
     */
    public static String getAccessToken() {
        return getToken();
    }

    /**
     * 获取RefreshToken
     */
    public static String getRefreshToken() {
        return sPreferences.getString(Constants.PREF_REFRESH_TOKEN, "");
    }

    /**
     * 获取用户ID
     */
    public static long getUserId() {
        return sPreferences.getLong(Constants.PREF_USER_ID, 0L);
    }

    /**
     * 获取用户ID字符串 (支持测试账号)
     */
    public static String getUserIdStr() {
        long userId = sPreferences.getLong(Constants.PREF_USER_ID, 0L);
        return userId > 0 ? String.valueOf(userId) : "";
    }

    /**
     * 保存用户ID (测试用)
     */
    public static void saveUserId(String userId) {
        sPreferences.edit().putString(Constants.PREF_USER_ID, userId).apply();
    }

    /**
     * 保存Token (测试用)
     */
    public static void saveToken(String token) {
        sPreferences.edit().putString(Constants.PREF_TOKEN, token).apply();
    }

    /**
     * 获取用户手机号
     */
    public static String getUserPhone() {
        return sPreferences.getString(Constants.PREF_USER_PHONE, "");
    }

    /**
     * 保存用户手机号
     */
    public static void saveUserPhone(String phoneNum) {
        sPreferences.edit().putString(Constants.PREF_USER_PHONE, phoneNum).apply();
    }

    /**
     * 获取用户昵称
     */
    public static String getUserName() {
        return sPreferences.getString(Constants.PREF_USER_NAME, "");
    }

    /**
     * 清除登录信息
     */
    public static void clearLoginInfo() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(Constants.PREF_TOKEN);
        editor.remove(Constants.PREF_REFRESH_TOKEN);
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_PHONE);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_USER_AVATAR);
        editor.remove(Constants.PREF_EXPIRES_TIME);
        editor.apply();
    }

    /**
     * 更新Token
     */
    public static void updateToken(String token) {
        sPreferences.edit().putString(Constants.PREF_TOKEN, token).apply();
    }

    /**
     * 获取Token有效期
     */
    public static long getExpires() {
        return sPreferences.getLong(Constants.PREF_EXPIRES_TIME, 0L);
    }

    /**
     * 保存选中的模型
     */
    public static void setSelectedModel(Context context, String model) {
        init(context);
        sPreferences.edit().putString("selected_model", model).apply();
    }

    /**
     * 获取选中的模型
     */
    public static String getSelectedModel(Context context) {
        init(context);
        return sPreferences.getString("selected_model", "10086");
    }

    /**
     * 获取模型显示名称
     */
    public static String getModelDisplayName(Context context) {
        String model = getSelectedModel(context);
        switch (model) {
            case "deepseek_r1":
                return "DeepSeek R1";
            case "doubao":
                return "豆包";
            case "liantong_yuanjing":
                return "联通元景";
            case "tencent_hunyuan":
                return "腾讯混元";
            default:
                return "DeepSeek R1";
        }
    }

    /**
     * 保存语言设置
     */
    public static void setLanguage(Context context, String language) {
        init(context);
        sPreferences.edit().putString("app_language", language).apply();
    }

    /**
     * 获取语言设置
     */
    public static String getLanguage(Context context) {
        init(context);
        return sPreferences.getString("app_language", "16k_zh");
    }

    /**
     * 保存语言设置 (无Context参数)
     */
    public static void saveLanguage(String language) {
        sPreferences.edit().putString("app_language", language).apply();
    }

    /**
     * 获取语言设置 (无Context参数)
     */
    public static String getLanguage() {
        return sPreferences.getString("app_language", "16k_zh");
    }

    /**
     * 保存灵犀对话的conversationId
     */
    public static void saveLingxiConversationId(String conversationId) {
        sPreferences.edit().putString("lingxi_conversationId", conversationId).apply();
    }

    /**
     * 获取犀对话的conversationId
     */
    public static String getLingxiConversationId() {
        return sPreferences.getString("lingxi_conversationId", "");
    }


    /**
     * 保存用户头像路径
     */
    public static void saveUserAvatar(String avatarPath) {
        sPreferences.edit().putString(Constants.PREF_USER_AVATAR, avatarPath).apply();
    }

    /**
     * 保存用户名称
     */
    public static void saveUserName(String name) {
        sPreferences.edit().putString(Constants.PREF_USER_NAME, name).apply();
    }

    /**
     * 获取用户头像路径
     */
    public static String getUserAvatar() {
        return sPreferences.getString(Constants.PREF_USER_AVATAR, "");
    }

    /**
     * 清除所有数据
     */
    public static void clearAllData() {
        clearLoginInfo();
        // 清除其他数据
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove("selected_model");
        editor.remove("app_language");
        editor.remove("test_user_id");
        editor.apply();
    }

    /**
     * 保存字符串值
     */
    public static void saveString(String key, String value) {
        sPreferences.edit().putString(key, value).apply();
    }

    /**
     * 保存布尔值
     */
    public static void saveBoolean(String key, boolean value) {
        sPreferences.edit().putBoolean(key, value).apply();
    }

    /**
     * 获取字符串值
     */
    public static String getString(String key, String defaultValue) {
        return sPreferences.getString(key, defaultValue);
    }

    /**
     * 获取布尔值
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return sPreferences.getBoolean(key, defaultValue);
    }

    /**
     * 保存会议话题到本地
     */
    public static void saveMeetingTopic(String meetingId, String topicContent) {
        String key = "meeting_topic_" + meetingId;
        saveString(key, topicContent);
    }

    /**
     * 获取本地保存的会议话题
     */
    public static String getMeetingTopic(String meetingId) {
        String key = "meeting_topic_" + meetingId;
        return getString(key, "");
    }

    /**
     * 清除会议话题缓存
     */
    public static void clearMeetingTopic(String meetingId) {
        String key = "meeting_topic_" + meetingId;
        sPreferences.edit().remove(key).apply();
    }
}
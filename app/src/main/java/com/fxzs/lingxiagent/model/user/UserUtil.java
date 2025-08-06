package com.fxzs.lingxiagent.model.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.provider.Settings;
import android.text.TextUtils;

import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.util.AesUtil;

import java.util.UUID;

import timber.log.Timber;

public class UserUtil {
    private static final String TAG = "UserUtil";

    public static String formatPhone(String phone) {
        if (phone == null || phone.length() <= 11) {
            return phone;
        }
        String phoneNum = AesUtil.decrypt(phone, Constants.KEY_ALIAS);
        return phoneNum.substring(0, 3) + "****" + phoneNum.substring(7);
    }

    public static int dp2px(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(androidId)) {
            androidId = UUID.randomUUID().toString();
        }

        return androidId;
    }

    public static int getAppVersionCode(Context context) {
        try {
            String pkgName = context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            Timber.tag(TAG).e(e);
        }
        return 1;
    }

    public static String getAppVersionName(Context context) {
        try {
            String pkgName = context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            Timber.tag(TAG).e(e);
        }
        return "1.0.0";
    }
}
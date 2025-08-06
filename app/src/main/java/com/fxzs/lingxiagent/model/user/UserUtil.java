package com.fxzs.lingxiagent.model.user;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.util.AesUtil;

import timber.log.Timber;

public class UserUtil {
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

    public static String getAppVersionName(Context context) {
        try {
            String pkgName = context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            Timber.tag("UserUtil").e(e);
        }
        return "";
    }
}
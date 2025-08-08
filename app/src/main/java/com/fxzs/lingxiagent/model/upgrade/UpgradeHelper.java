package com.fxzs.lingxiagent.model.upgrade;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.user.UserUtil;
import com.fxzs.lingxiagent.model.user.dto.AppVersionResponse;
import com.fxzs.lingxiagent.view.common.GlobalToast;

import java.util.HashMap;
import java.util.Map;

public class UpgradeHelper {
    // 请求body参数
    public static Map<String, String> getRequestBody(Context context) {
        Map<String, String> params = new HashMap<>();
        params.put("brand", Build.BRAND);
        params.put("model", Build.MODEL);
        params.put("os", "android");
        params.put("osVersion", Build.VERSION.RELEASE);
        params.put("androidId", UserUtil.getAndroidId(context));
        params.put("versionCode", String.valueOf(UserUtil.getAppVersionCode(context)));
        params.put("versionName", UserUtil.getAppVersionName(context));
        params.put("packageName", context.getPackageName());

        return params;
    }

    // 弹出升级框
    public static void showUpgradeDialog(Activity activity, AppVersionResponse versionInfo) {
        try {
            // 创建自定义对话框
            Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_version_update_figma);

            float density = activity.getResources().getDisplayMetrics().density;
            // 设置对话框宽度和位置
            Window window = dialog.getWindow();
            if (window != null) {
                // 设置背景透明
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // 设置对话框宽度和高度
                WindowManager.LayoutParams params = window.getAttributes();
                // 将dp转换为px
                int widthInDp = 319;
                params.width = (int) (widthInDp * density);
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;

                // 设置对话框位置为屏幕中央
                params.gravity = Gravity.CENTER;
                window.setAttributes(params);
            }

            // 获取控件引用
            TextView tvVersionInfo = dialog.findViewById(R.id.tv_version_info);
            TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
            TextView btnUpdate = dialog.findViewById(R.id.btn_update);

            // 使用更新描述替代默认文本
            if (TextUtils.isEmpty(versionInfo.getUpdateContent())) {
                // 设置版本信息
                String versionText = "发现新版本" + versionInfo.getVersionName() + "，新版本大小"
                        + (versionInfo.getSize() / 1024 / 1024) + "MB，是否确定升级？";
                tvVersionInfo.setText(versionText);
            } else {
                tvVersionInfo.setText(versionInfo.getUpdateContent());
            }

            // 设置按钮点击事件
            btnUpdate.setOnClickListener(v -> {
                dialog.dismiss();
                if (TextUtils.isEmpty(versionInfo.getDownloadUrl())) {
                    GlobalToast.show(activity, "下载链接不可用", GlobalToast.Type.NORMAL);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.getDownloadUrl()));
                    activity.startActivity(intent);
                    // 如果是强制更新，关闭应用
                    if (versionInfo.getUpdateMode() == 1) {
                        activity.finish();
                    }
                }
            });

            // 如果是强制更新，不显示取消按钮
            if (versionInfo.getUpdateMode() == 1) {
                btnCancel.setVisibility(View.GONE);
                dialog.setCancelable(false);
            } else {
                btnCancel.setOnClickListener(v -> dialog.dismiss());
            }

            // 显示对话框
            dialog.show();
            Window window2 = dialog.getWindow();
            if (window2 != null) {
                window2.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                WindowManager.LayoutParams params2 = window2.getAttributes();
                params2.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                params2.y = (int) (80 * density + 0.5f); // 84dp转px
                window2.setAttributes(params2);
            }
        } catch (Exception e) {
            String errorMsg = "显示更新对话框失败: " + e.getMessage();
            GlobalToast.show(activity, errorMsg, GlobalToast.Type.NORMAL);
        }
    }
}

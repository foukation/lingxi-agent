package com.fxzs.lingxiagent.view.common;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.fxzs.lingxiagent.R;

public class GlobalToast {
    public enum Type {
        SUCCESS, ERROR, NORMAL
    }

    public static void show(@NonNull Activity activity, @NonNull String message, @NonNull Type type) {
        show(activity, message, type, 2000);
    }

    public static void show(@NonNull Activity activity, @NonNull String message, @NonNull Type type, int durationMs) {
        activity.runOnUiThread(() -> {
            Toast toast = new Toast(activity.getApplicationContext());
            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_global, null);
            ImageView ivIcon = layout.findViewById(R.id.iv_toast_icon);
            TextView tvMsg = layout.findViewById(R.id.tv_toast_message);
            tvMsg.setText(message);

            switch (type) {
                case SUCCESS:
                    ivIcon.setImageResource(R.drawable.ic_toast_succ);
                    ivIcon.setVisibility(View.VISIBLE);
                    break;
                case ERROR:
                    ivIcon.setImageResource(R.drawable.ic_toast_err);
                    ivIcon.setVisibility(View.VISIBLE);
                    break;
                case NORMAL:
                default:
                    ivIcon.setVisibility(View.GONE);
                    break;
            }

            toast.setView(layout);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, getNavBarHeight(activity) + dp2px(activity, 12));
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();

            // 手动控制时长
            if (durationMs > 2000) {
                new Handler(Looper.getMainLooper()).postDelayed(toast::cancel, durationMs);
            }
        });
    }

    private static int getNavBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static int dp2px(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
} 
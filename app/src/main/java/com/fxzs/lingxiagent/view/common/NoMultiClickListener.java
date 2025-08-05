package com.fxzs.lingxiagent.view.common;
import android.os.SystemClock;
import android.view.View;

public abstract class NoMultiClickListener implements View.OnClickListener {
    private static final int MIN_CLICK_DELAY_TIME = 500;
    private static long lastClickTime;

    public abstract void onNoMultiClick(View v);

    @Override
    public void onClick(View v) {
        long curClickTime = SystemClock.elapsedRealtime();
        if ((curClickTime - lastClickTime) > MIN_CLICK_DELAY_TIME){
            lastClickTime = curClickTime;
            onNoMultiClick(v);
        }
    }
}

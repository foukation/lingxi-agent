package com.fxzs.lingxiagent.view.user;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.lingxi.help.FunctionHelpActivity;
import com.fxzs.lingxiagent.model.user.dto.AppVersionResponse;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.user.VMAboutApp;
import com.fxzs.lingxiagent.viewmodel.user.VMUserProfile;
import com.fxzs.lingxiagent.viewmodel.user.VMUserSettings;

public class AgreementActivity extends BaseActivity<VMUserSettings> {
    
    private ImageView ivBack;
    private LinearLayout rlAgreementPri1;
    private LinearLayout rlAgreementPri2;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_agreement_app;
    }

    @Override
    protected Class<VMUserSettings> getViewModelClass() {
         return VMUserSettings.class;
    }

    @Override
    protected void setupDataBinding() {

    }

    @Override
    protected void initializeViews() {
        // 设置状态栏颜色为白色，与背景一致，并保证内容不被遮挡
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(android.graphics.Color.parseColor("#FFFFFF"));
            getWindow().getDecorView().postDelayed(() -> {
                getWindow().setStatusBarColor(android.graphics.Color.parseColor("#FFFFFF"));
            }, 100);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        // 初始化控件
        ivBack = findViewById(R.id.iv_back);
        rlAgreementPri1 = findViewById(R.id.rl_agreement_pri1);
        rlAgreementPri2 = findViewById(R.id.rl_agreement_pri2);

        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());

        rlAgreementPri1.setOnClickListener(view -> {

        });
        rlAgreementPri2.setOnClickListener(view -> {

        });
    }
    
    @Override
    protected void setupObservers() {

    }
    

}
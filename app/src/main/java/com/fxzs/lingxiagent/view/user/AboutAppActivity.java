package com.fxzs.lingxiagent.view.user;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.lingxi.help.FunctionHelpActivity;
import com.fxzs.lingxiagent.model.upgrade.UpgradeHelper;
import com.fxzs.lingxiagent.model.user.UserUtil;
import com.fxzs.lingxiagent.model.user.dto.AppVersionResponse;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.user.VMAboutApp;

public class AboutAppActivity extends BaseActivity<VMAboutApp> {
    
    private ImageView ivBack;
    private TextView tvVersion;
    private LinearLayout rlLearnMore;
    private LinearLayout rlVersionUpdate;
    private LinearLayout rlAgreement;
    private View viewUpdateDot; // 新版本提示红点
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_about_app;
    }
    
    @Override
    protected Class<VMAboutApp> getViewModelClass() {
        return VMAboutApp.class;
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
                getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        // 初始化控件
        ivBack = findViewById(R.id.iv_back);
        tvVersion = findViewById(R.id.tv_version);
        rlLearnMore = findViewById(R.id.rl_learn_more);
        rlVersionUpdate = findViewById(R.id.rl_version_update);
        viewUpdateDot = findViewById(R.id.view_update_dot);
        rlAgreement = findViewById(R.id.rl_agreement);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        rlLearnMore.setOnClickListener(v -> learnMore());
        rlVersionUpdate.setOnClickListener(v -> upgradeAPP());
        rlAgreement.setOnClickListener(view -> serviceAgreement());
        
        // 获取当前应用版本号
        viewModel.setVersionDisplay(UserUtil.getAppVersionName(this));

        // 获取最新版本信息
        viewModel.fetchAppUpgradeInfo(this);
    }

    // 服务协议
    private void serviceAgreement() {
        Intent aboutIntent = new Intent(this, AgreementActivity.class);
        startActivity(aboutIntent);
    }

    // 了解更多
    private void learnMore() {
        Intent aboutIntent = new Intent(this, FunctionHelpActivity.class);
        startActivity(aboutIntent);
    }

    // 检查版本更新
    private void upgradeAPP() {
        AppVersionResponse versionInfo = viewModel.getVersionInfo().getValue();
        if (versionInfo != null && !TextUtils.isEmpty(versionInfo.getDownloadUrl())) {
            UpgradeHelper.showUpgradeDialog(this, versionInfo);
        } else {
            showToast("已是最新版本");
        }
    }
    
    @Override
    protected void setupDataBinding() {
        // 绑定版本号
        DataBindingUtils.bindTextView(tvVersion, viewModel.getVersionText(), this);
    }
    
    @Override
    protected void setupObservers() {
        // 观察版本信息
        viewModel.getVersionInfo().observe(this, versionInfo -> {
            if (versionInfo != null) {
                // 如果需要更新，显示红点而不是弹窗
                if (TextUtils.isEmpty(versionInfo.getDownloadUrl())) {
                    // 隐藏红点
                    viewUpdateDot.setVisibility(android.view.View.GONE);
                } else {
                    // 显示红点
                    viewUpdateDot.setVisibility(android.view.View.VISIBLE);
                    if (versionInfo.getUpdateMode() == 1) {
                        UpgradeHelper.showUpgradeDialog(this, versionInfo);
                    }
                }
            }
        });
    }
}
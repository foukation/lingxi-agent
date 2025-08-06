package com.fxzs.lingxiagent.view.user;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.lingxi.help.FunctionHelpActivity;
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
            showUpdateDialog(versionInfo);
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
                        showUpdateDialog(versionInfo);
                    }
                }
            }
        });
    }
    
    private void showUpdateDialog(AppVersionResponse versionInfo) {
        try {
            // 创建自定义对话框
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_version_update_figma);
            
            // 设置对话框宽度和位置
            android.view.Window window = dialog.getWindow();
            if (window != null) {
                // 设置背景透明
                window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                
                // 设置对话框宽度和高度
                android.view.WindowManager.LayoutParams params = window.getAttributes();
                // 将dp转换为px
                int widthInDp = 319;
                float density = getResources().getDisplayMetrics().density;
                params.width = (int) (widthInDp * density);
                params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
                
                // 设置对话框位置为屏幕中央
                params.gravity = android.view.Gravity.CENTER;
                
                window.setAttributes(params);
            }
            
            // 获取控件引用
            TextView tvVersionInfo = dialog.findViewById(R.id.tv_version_info);
            TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
            TextView btnUpdate = dialog.findViewById(R.id.btn_update);
            
            // 设置版本信息
            String versionText = "发现新版本" + versionInfo.getVersionName() + "，新版本大小" + (versionInfo.getSize() / 1024 / 1024) + "MB，是否确定升级？";
            
            // 使用更新描述替代默认文本
            if (TextUtils.isEmpty(versionInfo.getUpdateContent())) {
                tvVersionInfo.setText(versionText);
            } else {
                tvVersionInfo.setText(versionInfo.getUpdateContent());
            }
            
            // 设置按钮点击事件
            btnUpdate.setOnClickListener(v -> {
                if (TextUtils.isEmpty(versionInfo.getDownloadUrl())) {
                    dialog.dismiss();
                    showToast("下载链接不可用");
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.getDownloadUrl()));
                    startActivity(intent);
                    dialog.dismiss();

                    // 如果是强制更新，关闭应用
                    if (versionInfo.getUpdateMode() == 1) {
                        finish();
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
                android.view.WindowManager.LayoutParams params2 = window2.getAttributes();
                params2.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
                params2.y = (int) (80 * getResources().getDisplayMetrics().density + 0.5f); // 84dp转px
                window2.setAttributes(params2);
            }
        } catch (Exception e) {
            showToast("显示更新对话框失败: " + e.getMessage());
        }
    }
}
package com.fxzs.lingxiagent.view.user;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.user.dto.AppVersionResponse;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.user.VMAboutApp;

public class AboutAppActivity extends BaseActivity<VMAboutApp> {
    
    private ImageView ivBack;
    private TextView tvVersion;
    private LinearLayout rlVersion;
    private LinearLayout rlLearnMore;
    private LinearLayout rlVersionUpdate;
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
        rlVersion = findViewById(R.id.rl_version);
        rlLearnMore = findViewById(R.id.rl_learn_more);
        rlVersionUpdate = findViewById(R.id.rl_version_update);
        viewUpdateDot = findViewById(R.id.view_update_dot);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        
        rlVersion.setOnClickListener(v -> viewModel.onVersionClicked());
        rlLearnMore.setOnClickListener(v -> viewModel.onLearnMoreClicked());
        rlVersionUpdate.setOnClickListener(v -> viewModel.onVersionUpdateClicked());
        
        // 获取当前应用版本号
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            viewModel.setVersionDisplay(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            viewModel.setVersionDisplay("1.0.0");
        }
        
        // 获取最新版本信息
        viewModel.fetchVersionInfo();
    }
    
    @Override
    protected void setupDataBinding() {
        // 绑定版本号
        DataBindingUtils.bindTextView(tvVersion, viewModel.getVersionText(), this);
    }
    
    @Override
    protected void setupObservers() {
        // 观察版本点击事件
        viewModel.getVersionEvent().observe(this, event -> {
            if (event != null && event) {
                // TODO: 显示版本详情
//                showToast("版本 1.2.5");
            }
        });
        
        // 观察了解更多事件
        viewModel.getLearnMoreEvent().observe(this, event -> {
            if (event != null && event) {
                // 从版本信息中获取了解更多链接
                AppVersionResponse versionInfo = viewModel.getVersionInfo().getValue();
                if (versionInfo != null && versionInfo.getLearnMoreUrl() != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.getLearnMoreUrl()));
                    startActivity(intent);
                } else {
                    showToast("暂无更多信息");
                }
            }
        });
        
        // 观察版本更新事件
        viewModel.getVersionUpdateEvent().observe(this, event -> {
            if (event != null && event) {
                // 如果已经有版本信息且需要更新，直接显示更新对话框
                AppVersionResponse versionInfo = viewModel.getVersionInfo().getValue();
                if (versionInfo != null && versionInfo.getNeedUpdate() != null && versionInfo.getNeedUpdate()) {
                    showUpdateDialog(versionInfo);
                } else {
                    // 否则使用检查更新接口，传入当前版本号
                    try {
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        String currentVersion = packageInfo.versionName;
                        viewModel.checkVersionUpdate(currentVersion);
                    } catch (PackageManager.NameNotFoundException e) {
                        showToast("获取当前版本失败");
                    }
                }
            }
        });
        
        // 观察版本信息
        viewModel.getVersionInfo().observe(this, versionInfo -> {
            if (versionInfo != null) {
                // 如果需要更新，显示红点而不是弹窗
                if (versionInfo.getNeedUpdate() != null && versionInfo.getNeedUpdate()) {
                    // 显示红点
                    viewUpdateDot.setVisibility(android.view.View.VISIBLE);
                } else {
                    // 隐藏红点
                    viewUpdateDot.setVisibility(android.view.View.GONE);
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
            String versionText = "发现新版本" + (versionInfo.getVersion() != null ? versionInfo.getVersion() : "1.0.0") + 
                               "，新版本大小18.2MB，是否确定升级？";
            
            // 使用更新描述替代默认文本
            if (versionInfo.getUdpateDesc() != null && !versionInfo.getUdpateDesc().isEmpty()) {
                tvVersionInfo.setText(versionInfo.getUdpateDesc());
            } else {
                tvVersionInfo.setText(versionText);
            }
            
            // 设置按钮点击事件
            btnUpdate.setOnClickListener(v -> {
                if (versionInfo.getApkUrl() != null && !versionInfo.getApkUrl().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.getApkUrl()));
                    startActivity(intent);
                    dialog.dismiss();
                    
                    // 如果是强制更新，关闭应用
                    if (versionInfo.getForceUpdate() != null && versionInfo.getForceUpdate()) {
                        finish();
                    }
                } else {
                    dialog.dismiss();
                    showToast("下载链接不可用");
                }
            });
            
            // 如果是强制更新，不显示取消按钮
            if (versionInfo.getForceUpdate() != null && versionInfo.getForceUpdate()) {
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
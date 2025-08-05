package com.fxzs.lingxiagent.view.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.user.VMUserSettings;

public class UserSettingsActivity extends BaseActivity<VMUserSettings> {
    
    // 顶部栏
    private ImageView ivBack;
    
    // 设置项
    private LinearLayout layoutModel;
    private LinearLayout layoutSecurity;
    private LinearLayout layoutLanguage;
    private TextView tvModelName;
    private TextView tvLanguage;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_user_settings;
    }
    
    @Override
    protected Class<VMUserSettings> getViewModelClass() {
        return VMUserSettings.class;
    }
    
    @Override
    protected void initializeViews() {
        // 顶部栏
        ivBack = findViewById(R.id.iv_back);
        
        // 设置项
        layoutModel = findViewById(R.id.layout_model);
        layoutSecurity = findViewById(R.id.layout_security);
        layoutLanguage = findViewById(R.id.layout_language);
        tvModelName = findViewById(R.id.tv_model_name);
        tvLanguage = findViewById(R.id.tv_language);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        layoutModel.setOnClickListener(v -> {
            // 直接跳转到模型选择页面
            Intent intent = new Intent(this, ModelSelectionActivity.class);
            startActivity(intent);
        });
        layoutSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountSafetyActivity.class);
            startActivity(intent);
        });
        layoutLanguage.setOnClickListener(v -> {
            // 直接跳转到语言设置页面
            Intent intent = new Intent(this, LanguageSettingsActivity.class);
            startActivity(intent);
        });
    }
    
    @Override
    protected void setupDataBinding() {
        // 绑定显示文本
        DataBindingUtils.bindTextView(tvModelName, viewModel.getSelectedModel(), this);
        DataBindingUtils.bindTextView(tvLanguage, viewModel.getSelectedLanguage(), this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 刷新显示的模型和语言
        viewModel.refreshDisplayData();
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
    }
    
    @Override
    protected void setupObservers() {
        // 观察模型选择对话框
        viewModel.getShowModelDialog().observe(this, show -> {
            if (show != null && show) {
                showModelSelectionDialog();
            }
        });
        
        // 观察语言选择对话框
        viewModel.getShowLanguageDialog().observe(this, show -> {
            if (show != null && show) {
                showLanguageSelectionDialog();
            }
        });
        
        // 观察导航事件
        viewModel.getNavigationTarget().observe(this, target -> {
            if (target == null) return;
            
            if (target == VMUserSettings.NAV_SECURITY) {
                Intent intent = new Intent(this, AccountSafetyActivity.class);
                startActivity(intent);
            }
            
            // 清除导航事件
            viewModel.clearNavigationTarget();
        });
    }
    
    private void showModelSelectionDialog() {
        String[] models = viewModel.getAvailableModels();
        String currentModel = viewModel.getSelectedModel().get();
        int checkedItem = -1;
        
        // 找到当前选中的模型索引
        for (int i = 0; i < models.length; i++) {
            if (models[i].equals(currentModel)) {
                checkedItem = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("选择大模型")
                .setSingleChoiceItems(models, checkedItem, (dialog, which) -> {
                    viewModel.selectModel(models[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private void showLanguageSelectionDialog() {
        String[] languages = viewModel.getAvailableLanguages();
        String currentLanguage = viewModel.getSelectedLanguage().get();
        int checkedItem = -1;
        
        // 找到当前选中的语言索引
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equals(currentLanguage)) {
                checkedItem = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("选择语音识别语言")
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    viewModel.selectLanguage(languages[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
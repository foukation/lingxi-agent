package com.fxzs.lingxiagent.view.common;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.util.ZUtil.DisplayUtil;
import com.fxzs.lingxiagent.util.ZUtils;

/**
 * 基础Activity，提供双向绑定支持
 * @param <T> ViewModel类型
 */
public abstract class BaseActivity<T extends BaseViewModel> extends AppCompatActivity {
    
    protected T viewModel;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置透明状态栏
        setTransparentStatusBar();
        
        setContentView(getLayoutResource());
        
        // 自动处理状态栏适配
        handleStatusBarAdaptation();
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(getViewModelClass());
        
        // 观察公共状态
        observeCommonStates();
        
        // 子类初始化
        initializeViews();
        
        // 设置双向绑定（必须在initializeViews之后）
        setupDataBinding();
        
        setupObservers();
        ZUtils.setSystem(this);
    }
    
    /**
     * 获取布局资源ID
     * @return 布局资源ID
     */
    protected abstract int getLayoutResource();
    
    /**
     * 获取ViewModel类
     * @return ViewModel类
     */
    protected abstract Class<T> getViewModelClass();
    
    /**
     * 设置数据绑定
     */
    protected abstract void setupDataBinding();
    
    /**
     * 初始化视图
     */
    protected abstract void initializeViews();
    
    /**
     * 设置观察者
     */
    protected abstract void setupObservers();
    
    /**
     * 观察公共状态
     */
    private void observeCommonStates() {
        // 观察加载状态
        viewModel.getLoading().observe(this, loading -> {
            if (loading != null) {
                handleLoadingState(loading);
            }
        });
        
        // 观察错误信息
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                handleError(error);
            }
        });
        
        // 观察成功消息
        viewModel.getSuccess().observe(this, success -> {
            if (success != null && !success.isEmpty()) {
                handleSuccess(success);
            }
        });
    }
    
    /**
     * 处理加载状态
     * @param loading 是否加载中
     */
    protected void handleLoadingState(boolean loading) {
        // 子类可以重写此方法来自定义加载状态显示
        // 例如显示/隐藏进度条
    }
    
    /**
     * 处理错误信息
     * @param error 错误信息
     */
    protected void handleError(String error) {
        GlobalToast.show(this, error, GlobalToast.Type.ERROR);
    }
    
    /**
     * 处理成功消息
     * @param success 成功消息
     */
    protected void handleSuccess(String success) {
        GlobalToast.show(this, success, GlobalToast.Type.SUCCESS);
    }
    
    /**
     * 显示Toast消息
     * @param message 消息内容
     */
    protected void showToast(String message) {
        GlobalToast.show(this, message, GlobalToast.Type.NORMAL);
    }
    
    /**
     * 设置透明状态栏
     */
    private void setTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            
            // 设置状态栏图标为深色（适合浅色背景）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            } else {
                window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
            }
        }
    }
    
    /**
     * 处理状态栏适配
     * 自动为根布局设置fitsSystemWindows属性
     */
    private void handleStatusBarAdaptation() {
        // 获取根视图
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            View contentView = ((android.view.ViewGroup) rootView).getChildAt(0);
            if (contentView != null) {
                // 对于Android 12及以上版本，自动设置fitsSystemWindows
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    contentView.setFitsSystemWindows(true);
                }
            }
        }
    }
    static float fontScale = 1f;

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        return DisplayUtil.getResources(this,resources,fontScale);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(DisplayUtil.attachBaseContext(newBase,fontScale));
    }

    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
        DisplayUtil.recreate(this);
    }
}
package com.fxzs.lingxiagent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.auth.AuthHelper;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.util.SignatureUtil;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.agent.AgentFragment;
import com.fxzs.lingxiagent.view.auth.OneClickLoginActivity;
import com.fxzs.lingxiagent.view.chat.ChatFragment;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.view.drawing.DrawingNewFragment;
import com.fxzs.lingxiagent.view.meeting.MeetingFragment;
import com.fxzs.lingxiagent.view.user.UserFragment;
import com.fxzs.lingxiagent.viewmodel.main.VMMain;

public class MainActivity extends BaseActivity<VMMain> {
    
    // 底部导航栏
    private LinearLayout navTabGui;
    private LinearLayout navTabPhone;
    private LinearLayout navTabLingxi;
    private LinearLayout navTabJob;
    private LinearLayout navTabAgent;
    
    // Fragment实例
    private ChatFragment chatFragment;
    private AgentFragment agentFragment;
    private DrawingNewFragment drawingFragment;
    private MeetingFragment meetingFragment;
    private UserFragment userFragment;
    
    // 当前选中的导航项
    private LinearLayout currentNavItem;
    private Fragment currentFragment;
    
    // Tab索引常量
    private static final int TAB_GUI = 0;
    private static final int TAB_AI_PHONE = 1;
    private static final int TAB_LINGXI = 2;
    private static final int TAB_AI_JOB = 3;
    private static final int TAB_AGENT = 4;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 打印签名信息（用于极光后台配置）
        SignatureUtil.logSignatureInfo(this);
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }
    
    @Override
    protected Class<VMMain> getViewModelClass() {
        return VMMain.class;
    }
    
    @Override
    protected void setupDataBinding() {
        // Fragment模式下暂时不需要特定的数据绑定
    }
    
    @Override
    protected void initializeViews() {
        // 初始化底部导航栏
        navTabGui = findViewById(R.id.nav_tab_gui);
        navTabPhone = findViewById(R.id.nav_tab_phone);
        navTabLingxi = findViewById(R.id.nav_tab_lingxi);
        navTabJob = findViewById(R.id.nav_tab_job);
        navTabAgent = findViewById(R.id.nav_tab_agent);
        
        // 设置点击事件
        navTabGui.setOnClickListener(v -> selectTab(TAB_GUI));
        navTabPhone.setOnClickListener(v -> selectTab(TAB_AI_PHONE));
        navTabLingxi.setOnClickListener(v -> selectTab(TAB_LINGXI));
        navTabJob.setOnClickListener(v -> selectTab(TAB_AI_JOB));
        navTabAgent.setOnClickListener(v -> selectTab(TAB_AGENT));
        
        // 初始化Fragment
        initFragments();
        
        // 检查Intent是否指定了要选中的Tab
        int selectedTab = getIntent().getIntExtra("selected_tab", TAB_LINGXI);
        selectTab(selectedTab);
    }
    
    @Override
    protected void setupObservers() {
        // Fragment模式下暂时不需要特定的观察者
    }
    
    @Override
    protected void handleLoadingState(boolean loading) {
        // Fragment模式下的加载状态处理
    }
    
    /**
     * 初始化所有Fragment
     */
    private void initFragments() {
        chatFragment = new ChatFragment();
        agentFragment = new AgentFragment();
        drawingFragment = new DrawingNewFragment();
        meetingFragment = new MeetingFragment();
        userFragment = new UserFragment();
    }
    
    /**
     * 选择Tab
     * @param tabIndex Tab索引
     */
    private void selectTab(int tabIndex) {
        // 检查登录状态 - 除了对话Tab，其他Tab都需要登录
        if (tabIndex != TAB_LINGXI && !AuthHelper.getInstance().isLogin()) {
            // 未登录，跳转到一键登录页面
            Intent intent = new Intent(this, OneClickLoginActivity.class);
            intent.putExtra("from_home", true);
            intent.putExtra("selected_tab", tabIndex);
            startActivity(intent);
            return;
        }
//        if(tabIndex != TAB_MEETING && Constant.isLoadMeetingExchange){
//
//            CommonDialog.showConfirmDialog(MainActivity.this, "将不保存会议内容",
//                    "请确认是否退出", "退出",
//                    new CommonDialog.OnDialogClickListener() {
//                        @Override
//                        public void onConfirm() {
//                            EventBus.getDefault().post(new MessageEvent());
//                        }
//
//                        @Override
//                        public void onCancel() {
//                            // 用户点击不同意，不做任何操作
//                        }
//                    });
//            return;
//        }

        // 重置所有Tab状态
        resetTabState();

        // 根据选中的Tab设置状态和显示对应Fragment
        LinearLayout selectedNavItem = null;
        Fragment selectedFragment = null;

        switch (tabIndex) {
            case TAB_GUI:
                selectedNavItem = navTabGui;
                selectedFragment = drawingFragment;
                break;
            case TAB_AI_PHONE:
                selectedNavItem = navTabPhone;
                selectedFragment = userFragment;
                break;
            case TAB_LINGXI:
                selectedNavItem = navTabLingxi;
                selectedFragment = chatFragment;
                break;
            case TAB_AI_JOB:
                selectedNavItem = navTabJob;
                selectedFragment = meetingFragment;
                break;
            case TAB_AGENT:
                selectedNavItem = navTabAgent;
                selectedFragment = agentFragment;
                break;
        }

        if (selectedNavItem != null && selectedFragment != null) {
            // 设置选中状态
            setNavItemSelected(selectedNavItem, true);
            currentNavItem = selectedNavItem;

            // 切换Fragment
            switchFragment(selectedFragment);
        }
    }
    
    /**
     * 重置所有Tab状态
     */
    private void resetTabState() {
        setNavItemSelected(navTabGui, false);
        setNavItemSelected(navTabPhone, false);
        setNavItemSelected(navTabLingxi, false);
        setNavItemSelected(navTabJob, false);
        setNavItemSelected(navTabAgent, false);
    }
    
    /**
     * 设置导航项选中状态
     * @param navItem 导航项
     * @param selected 是否选中
     */
    private void setNavItemSelected(LinearLayout navItem, boolean selected) {
        ImageView icon = (ImageView) navItem.getChildAt(0);
        TextView text = (TextView) navItem.getChildAt(1);
        
        // 设置选中状态，让selector自动切换图标和文字颜色
        icon.setSelected(selected);
        if(text != null) {
            text.setSelected(selected);
        }
    }
    
    /**
     * 切换Fragment
     * @param fragment 要显示的Fragment
     */
    private void switchFragment(Fragment fragment) {
        if (currentFragment == fragment) {
            return;
        }
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        // 隐藏当前Fragment
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        
        // 如果Fragment未添加则添加，否则显示
        if (!fragment.isAdded()) {
            transaction.add(R.id.fragment_container, fragment);
        } else {
            transaction.show(fragment);
        }
        
        transaction.commit();
        currentFragment = fragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ZUtils.print("MainActivity onActivityResult ====== > requestCode = "+requestCode+". resultCode = "+resultCode);

        if (currentFragment != null && currentFragment instanceof ChatFragment) {
            ((ChatFragment)currentFragment).onActivityResult(requestCode, resultCode, data);
        }
    }
}
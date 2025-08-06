package com.fxzs.lingxiagent.view.user;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.user.UserUtil;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.view.common.BaseFragment;
import com.fxzs.lingxiagent.view.common.DataBindingUtils;
import com.fxzs.lingxiagent.viewmodel.user.VMUserProfile;
import com.bumptech.glide.Glide;
import java.io.File;

public class UserFragment extends BaseFragment<VMUserProfile> {
    
    private FrameLayout layoutUserInfo;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvUserId;
    private LinearLayout layoutSettings;
    private LinearLayout layoutAbout;
    private LinearLayout layoutHelpFeedback;
   // private LinearLayout layoutLogout;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_user;
    }
    
    @Override
    protected Class<VMUserProfile> getViewModelClass() {
        return VMUserProfile.class;
    }
    
    @Override
    protected void initializeViews(View view) {
        layoutUserInfo = findViewById(R.id.layout_user_info);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvUserId = findViewById(R.id.tv_user_id);
        layoutSettings = findViewById(R.id.layout_settings);
        layoutAbout = findViewById(R.id.layout_about);
        layoutHelpFeedback = findViewById(R.id.layout_help_feedback);
      //  layoutLogout = findViewById(R.id.layout_logout);
        
        layoutUserInfo.setOnClickListener(v -> navigateToAccountInfo());
        layoutSettings.setOnClickListener(v -> viewModel.navigateToSettings());
        layoutAbout.setOnClickListener(v -> viewModel.navigateToAbout());
        layoutHelpFeedback.setOnClickListener(v -> navigateToHelpFeedback());
     //   layoutLogout.setOnClickListener(v -> performLogout());
    }
    
    @Override
    protected void setupDataBinding() {
        DataBindingUtils.bindTextView(tvUsername, viewModel.getUsername(), this);
        // UserProfile ViewModel doesn't have getUserId, using phone instead
        DataBindingUtils.bindTextView(tvUserId, viewModel.getPhone(), this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次返回"我的"页面时重新加载用户信息，确保显示最新的信息
        loadLocalUserProfile();
    }

    private void loadLocalUserProfile() {
        // 优先加载本地的数据
        String avatarUrl = SharedPreferencesUtil.getUserAvatar();
        loadAvatarUrl(avatarUrl);
        String userId = SharedPreferencesUtil.getUserIdStr();
        String nickName = SharedPreferencesUtil.getUserName();
        tvUsername.setText(nickName.isEmpty() ? "用户" + userId : nickName);
        String mobile = SharedPreferencesUtil.getUserPhone();
        tvUserId.setText(UserUtil.formatPhone(mobile));
    }
    
    @Override
    protected void setupObservers() {
        // 观察头像URL变化
        viewModel.getAvatarUrl().observe(getViewLifecycleOwner(), avatarUrl ->
            loadAvatarUrl(avatarUrl)
        );
        
        viewModel.getNavigationTarget().observe(getViewLifecycleOwner(), target -> {
            if (target == null) return;
            
            switch (target) {
                case VMUserProfile.NAV_SETTINGS:
                    Intent settingsIntent = new Intent(getActivity(), UserSettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                case VMUserProfile.NAV_ABOUT:
                    Intent aboutIntent = new Intent(getActivity(), AboutAppActivity.class);
                    startActivity(aboutIntent);
                    break;
                case VMUserProfile.NAV_HELP:
                    Intent helpIntent = new Intent(getActivity(), HelpCenterActivity.class);
                    startActivity(helpIntent);
                    break;
            }
            
            viewModel.clearNavigationTarget();
        });
    }
    
    private void loadAvatarUrl(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            // 处理本地文件路径
            Object loadUrl = avatarUrl;
            if (avatarUrl.startsWith("file://")) {
                loadUrl = new File(avatarUrl.substring(7));
            }

            if (getContext() != null) {
                Glide.with(getContext())
                        .load(loadUrl)
                        .placeholder(R.drawable.icon_user_head)
                        .error(R.drawable.icon_user_head)
                        .transform(new CenterCrop(), new RoundedCorners(UserUtil.dp2px(getContext(), 12.8f)))
                        .into(ivAvatar);
            }
        }
    }
    
    private void navigateToHelpFeedback() {
        Intent intent = new Intent(getActivity(), FeedbackActivity.class);
        startActivity(intent);
    }
    
    private void navigateToAccountInfo() {
        Intent intent = new Intent(getActivity(), AccountInfoActivity.class);
        startActivity(intent);
    }
}

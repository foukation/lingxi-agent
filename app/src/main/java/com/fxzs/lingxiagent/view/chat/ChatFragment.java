package com.fxzs.lingxiagent.view.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.chat.callback.SoftCallback;
import com.fxzs.lingxiagent.model.chat.callback.SuperEditCallback;
import com.fxzs.lingxiagent.model.chat.dto.ChatFileBean;
import com.fxzs.lingxiagent.util.SuperEditUtil;
import com.fxzs.lingxiagent.util.ZInputMethod;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.viewmodel.chat.VMChat;
import com.fxzs.lingxiagent.view.common.BaseFragment;
import com.fxzs.lingxiagent.viewmodel.history.VMHistory;

import java.io.Serializable;
import java.util.List;

public class ChatFragment extends BaseFragment<VMChat> {
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private View root_view;
    private LinearLayout ll_center;
    private LinearLayout ll_all;
    private LinearLayout ll_expand;

    private SuperEditUtil superEditUtil;
    private EditText ed;
    private VMChat vmChat;
    private ImageView ivHistory;
    ViewPager2 viewPager = null;

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_chat;
    }

    @Override
    protected Class<VMChat> getViewModelClass() {
        return VMChat.class;
    }

    @Override
    protected void initializeViews(View view) {
        root_view = view.findViewById(R.id.root_view);
        ll_all = view.findViewById(R.id.ll_all);
        ll_center = view.findViewById(R.id.ll_center);
        ll_expand = view.findViewById(R.id.ll_expand);
        ed = view.findViewById(R.id.ed);
        ivHistory = view.findViewById(R.id.iv_history);

        superEditUtil = new SuperEditUtil(getActivity(), ll_center);
        vmChat = new ViewModelProvider(requireActivity()).get(VMChat.class);
        setBottomEdit();
 
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            ZUtils.print("Fragment"+ " parentFragment != null " + parentFragment);
            // 假设 ViewPager 在父 Fragment 的布局中，ID 为 view_pager
            viewPager = parentFragment.getView().findViewById(R.id.view_pager);
        }
//        if (viewPager != null) {
//            // 成功获取 ViewPager
//            ZUtils.print("Fragment"+ "ViewPager found: " + viewPager);
//            viewPager.setUserInputEnabled(false);
//        } else {
//            ZUtils.print("Fragment"+ "ViewPager not found");
//        }
//        viewPager = getActivity().findViewById(R.id.view_pager);

        // 设置历史记录按钮点击事件
        if (ivHistory != null) {
            ivHistory.setOnClickListener(v -> {
                android.util.Log.d("ChatFragment", "History button clicked!");
                showHistoryBottomSheet();
            });
            android.util.Log.d("ChatFragment", "Click listener set for history button");
        } else {
            android.util.Log.e("ChatFragment", "ivHistory is null, cannot set click listener");
        }
    }

    private void setBottomEdit() {
        superEditUtil.setOnListenSoft(root_view, new SoftCallback() {
            @Override
            public void show() {
                // 可根据需要处理软键盘弹出时的 UI
                isUp = true;
//                toggleMarginWithAnimation();

            }
            @Override
            public void hide() {
                // 可根据需要处理软键盘收起时的 UI
                if(isUp){//一定要显示过才能收起
                    isUp = false;
//                    toggleMarginWithAnimation();
                }

            }
        });
        superEditUtil.setCallback(new SuperEditCallback() {
                                      @Override
                                      public void send(String content, OptionModel selectOptionModel) {
                                          ZUtils.print("ChatFragment send = "+content + " OptionModel = "+selectOptionModel.getName());

//                String input = ed.getText().toString().trim();
                                          if (TextUtils.isEmpty(content) || selectOptionModel == null) {
                                              return;
                                          }
                                          Activity activity = getActivity();
                                          if (activity == null) {
                                              ZUtils.print("ChatFragment"+"Activity is null, aborting send");
                                              return;
                                          }
                                          String className = ZUtils.getTopActivity(getActivity());
                                          ZUtils.print("className"+className);
                                          if("com.fxzs.lingxiagent.MainActivity".equals(className)){
//                                              if(selectOptionModel.getModel().equals("10086")){
//                                                  //TODO 移动预留入口
//                                                  ZUtils.showToast("跳转移动页面");
//
//                                              }else{
//                                                  Intent intent = new Intent(getActivity(), SuperChatContainActivity.class);
//                                                  intent.putExtra(Constant.INTENT_TYPE, SuperChatContainActivity.TYPE_HOME);
//                                                  intent.putExtra(Constant.INTENT_DATA, content);
//                                                  intent.putExtra(Constant.INTENT_DATA1, selectOptionModel);
//                                                  getActivity().startActivity(intent);
//                                              }
                                              Intent intent = new Intent(getActivity(), SuperChatContainActivity.class);
                                              intent.putExtra(Constant.INTENT_TYPE, SuperChatContainActivity.TYPE_HOME);
                                              intent.putExtra(Constant.INTENT_DATA, content);
                                              intent.putExtra(Constant.INTENT_DATA1, selectOptionModel);
                                              getActivity().startActivity(intent);
                                          }


                                      }

                                      @Override
                                      public void sendWithFile(String content, OptionModel selectOptionModel, List<ChatFileBean> fileList, boolean isFile) {

                                          Intent intent = new Intent(getActivity(), SuperChatContainActivity.class);
                                          intent.putExtra(Constant.INTENT_TYPE, SuperChatContainActivity.TYPE_HOME);
                                          intent.putExtra(Constant.INTENT_DATA, content);
                                          intent.putExtra(Constant.INTENT_DATA1, selectOptionModel);
                                          intent.putExtra(Constant.INTENT_DATA2, /*(ArrayList<String>)*/(Serializable)fileList);
                                          getActivity().startActivity(intent);
                                      }

                                      @Override
                                      public void voice() {
                                          checkAudioPermission();
                                          ll_expand.setVisibility(View.VISIBLE);

                                      }

                                      @Override
                                      public void keyboard() {
                                          ll_expand.setVisibility(View.GONE);

                                      }

                                      @Override
                                      public void pressDown() {
                                          checkAudioPermission();
                                          if (viewPager != null) {
                                              ZUtils.print("Fragment"+ "pressDown");
                                              // 示例：切换到第一个页面
                                              viewPager.setUserInputEnabled(false);
//                                              viewPager.getParent().requestDisallowInterceptTouchEvent(true);
                                          }
                                      }

                                      @Override
                                      public void pressUp(boolean isInArea) {
                                          if (viewPager != null) {
                                              ZUtils.print("Fragment"+ "pressUp: ");
                                              // 示例：切换到第一个页面
                                              viewPager.setUserInputEnabled(true);
//                                              viewPager.getParent().requestDisallowInterceptTouchEvent(true);
                                          }

                                      }
                                  }
                );
        root_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ZInputMethod.closeInputMethod(getContext(),view);
                return false;
            }
        });
    }

    @Override
    protected void setupDataBinding() {
        // 可选：如有 DataBinding 需求可在此实现
    }

    @Override
    protected void setupObservers() {
        // 可选：如有额外 LiveData 监听需求可在此实现
    }

    boolean isUp = false;
    private void toggleMarginWithAnimation() {
        // 获取当前 LayoutParams
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ll_all.getLayoutParams();
//        int newMargin = isUp ? 70 : 144; // dp，切换 margin 值
        int newMargin = 70; // dp，切换 margin 值
        int marginPx = (int) (newMargin * getResources().getDisplayMetrics().density); // 转换为像素
        int Px144 = (int) (144 * getResources().getDisplayMetrics().density); // 转换为像素

        ZUtils.print("isMarginIncreased = " + isUp);
        ZUtils.print("marginPx = " + marginPx);
        ZUtils.print("Px144 = " + Px144);
        ZUtils.print("params.topMargin = " + params.topMargin);
        // 平移动画（以 marginLeft 为例）
        float translationY = isUp ? (marginPx - params.topMargin) : 0/* -(marginPx - params.topMargin)*/;
        ZUtils.print("translationY = " + translationY);
        if (marginPx != params.topMargin) {

            ll_all.animate()
                    .translationY(translationY)
                    .setDuration(300) // 动画时长
                    .setInterpolator(new DecelerateInterpolator()) // 缓动效果
                    .withEndAction(() -> {
                        // 动画结束后更新 margin
//                    params.setMargins(params.leftMargin,marginPx, params.rightMargin, params.bottomMargin);
//                    ll_all.setLayoutParams(params);
                        // 重置 translationX 以避免影响后续布局
//                    ll_all.setTranslationY(0);
//                        isUp = !isUp;
                    })
                    .start();
        }
    }


    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                showToast("需要录音权限才能使用功能");
            }
        }

        if (superEditUtil != null) {
          superEditUtil.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        showToast("onActivityResult");
        ZUtils.print("Fragment onActivityResult ====== >");

        if (superEditUtil != null) {
            superEditUtil.onActivityResult(requestCode,resultCode,data);
        }
    }

    /**
     * 显示历史记录底部抽屉
     */
    private void showHistoryBottomSheet() {
        android.util.Log.d("ChatFragment", "showHistoryBottomSheet called");
        try {
            HistoryBottomSheetFragment bottomSheet = HistoryBottomSheetFragment.newInstance( VMHistory.TAB_CHAT);
            bottomSheet.show(getChildFragmentManager(), "HistoryBottomSheet");
            android.util.Log.d("ChatFragment", "BottomSheet shown successfully");
        } catch (Exception e) {
            android.util.Log.e("ChatFragment", "Error showing bottom sheet", e);
        }
    }

}
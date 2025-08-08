package com.fxzs.lingxiagent.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.fxzs.lingxiagent.IYAApplication;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.aiwork.AiWorkFilterBean;
import com.fxzs.lingxiagent.model.chat.dto.AiWritingTypeBean;
import com.fxzs.lingxiagent.model.chat.dto.OptionBean;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.view.aiwork.AiWorkAdapter;
import com.fxzs.lingxiagent.view.aiwork.AiWorkFilterAdapter;
import com.fxzs.lingxiagent.view.chat.OptionAdapter;
import com.fxzs.lingxiagent.view.chat.OptionAiMeetingAdapter;
import com.fxzs.lingxiagent.view.chat.OptionModelAdapter;
import com.fxzs.lingxiagent.view.common.GlobalToast;
import com.fxzs.lingxiagent.view.widget.CustomDividerItemDecoration;
import com.fxzs.smartassist.util.ZUtil.ScreenUtils;
import com.fxzs.smartassist.util.ZUtil.SizeUtils;

import java.util.ArrayList;
import java.util.List;
public class ZUtils {
    public static void print(String msg){
        Log.d("MyLog",msg);

    }

    public static void showToast(String text) {
        if(!TextUtils.isEmpty(text)){
            Toast.makeText(IYAApplication.getInstance(), text, Toast.LENGTH_SHORT).show();
        }
    }

    public static void setTextBg(Context context, TextView tv, int res){
        tv.setBackground(context.getResources().getDrawable(res));
    }
    public static void setTextColor(Context context, TextView tv,int res){
        tv.setTextColor(context.getResources().getColor(res));
    }

    public static void setIvBg(Context context, ImageView iv, int res){
        iv.setBackground(context.getResources().getDrawable(res));
    }

    public static void setViewBg(Context context, View iv, int res){
        iv.setBackground(context.getResources().getDrawable(res));
    }

    public static void setViewBgTint(Context context, View view, int res){
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(res));
    }

    public static void showSingleChoicePopup(Context context, View anchorView, OptionAdapter.OnOptionSelectedListener callback) {
        // 加载弹窗布局
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_single_choice, null);
        PopupWindow      popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        LinearLayout llChoice = popupView.findViewById(R.id.ll_choice);
        ShadowUtils.applyDefaultShadow(llChoice,context);
        // 初始化RecyclerView
        RecyclerView optionsRecyclerView = popupView.findViewById(R.id.optionsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        optionsRecyclerView.setLayoutManager(layoutManager);

        // 设置选项数据和适配器
        List<OptionBean> options =new ArrayList<>();
        options.add(new OptionBean(R.mipmap.option_photo, "拍照"));
        options.add(new OptionBean(R.mipmap.option_picture, "图片"));
        options.add(new OptionBean(R.mipmap.option_local_file, "本地文件"));

        OptionAdapter optionAdapter = new OptionAdapter(context,options, selected -> {
            callback.onOptionSelected(selected);
            popupWindow.dismiss();
//            selectedOption = selected;
//            chatMessages.add(new ChatMessage("选择了: " + selectedOption, true));
//            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//            recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
//            popupWindow.dismiss();
        });
        optionsRecyclerView.setAdapter(optionAdapter);

        // 设置弹窗背景
//        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.edit_text));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);

        // 计算弹窗显示位置（anchorView上方）
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int anchorHeight = anchorView.getHeight();
        int anchorWidth = anchorView.getWidth();
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();
        int popupWidth = popupView.getMeasuredWidth();
        int xOffset =  -anchorHeight -popupWidth/2+ZDpUtils.dpToPx((Activity) context, 8);;
//        int yOffset = -anchorHeight - popupHeight - 10; // 在anchorView上方10px处显示
        int yOffset = -anchorHeight - popupHeight - 10; // 在anchorView上方10px处显示

        // 显示弹窗
//        popupWindow.showAtLocation(anchorView, xOffset, yOffset);
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset);
    }

    public static void showChooseModelPopup(Context context,View anchorView,
                                            List<OptionModel> options,
                                            OptionModel selectOptionModel,
                                            OptionModelAdapter.OnOptionSelectedListener listener) {
        int popupWidth = ZDpUtils.dpToPx((Activity) context, 240);

        // 加载弹窗布局
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_single_choice, null);
        PopupWindow      popupWindow = new PopupWindow(popupView,  popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // 初始化RecyclerView
        RecyclerView optionsRecyclerView = popupView.findViewById(R.id.optionsRecyclerView);
        LinearLayout llChoice = popupView.findViewById(R.id.ll_choice);
        ShadowUtils.applyDefaultShadow(llChoice,context);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        optionsRecyclerView.setLayoutManager(layoutManager);
        optionsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
        });

        OptionModelAdapter optionAdapter = new OptionModelAdapter(context,options, selected -> {
            listener.onOptionSelected(selected);
            popupWindow.dismiss();
        });
        optionsRecyclerView.setAdapter(optionAdapter);
        if(selectOptionModel != null){
            for (int i = 0; i < options.size(); i++) {
                if(options.get(i).getName().equals(selectOptionModel.getName())){
                    optionAdapter.setSelectedPosition(i);
                }
            }
        }

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);

        // 居中显示在 anchorView 下方
        int anchorWidth = anchorView.getWidth();
        int xOffset = (anchorWidth - popupWidth) / 2;

        // 显示弹窗
        popupWindow.showAsDropDown(anchorView, xOffset, -ZDpUtils.dpToPx((Activity) context, 4));
    }


    //翻译，写作弹窗
    public static void showAIMeetingPopup(Context context,View anchorView,
                                            List<AiWritingTypeBean> options,
                                          AiWritingTypeBean selectOptionModel,
                                          OptionAiMeetingAdapter.OnOptionSelectedListener listener) {
        // 加载弹窗布局
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_single_choice, null);
        PopupWindow popupWindow = new PopupWindow(popupView,  ZDpUtils.dpToPx((Activity) context,234), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        LinearLayout llChoice = popupView.findViewById(R.id.ll_choice);
        ShadowUtils.applyDefaultShadow(llChoice,context);
        // 初始化RecyclerView
        RecyclerView optionsRecyclerView = popupView.findViewById(R.id.optionsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        optionsRecyclerView.setLayoutManager(layoutManager);

        // 设置选项数据和适配器
//        List<OptionModel> options =new ArrayList<>();

        OptionAiMeetingAdapter optionAdapter = new OptionAiMeetingAdapter(context,options, selected -> {
            listener.onOptionSelected(selected);
            popupWindow.dismiss();
        });
        optionsRecyclerView.setAdapter(optionAdapter);

        if(selectOptionModel != null){
            for (int i = 0; i < options.size(); i++) {
                if(options.get(i).getName().equals(selectOptionModel.getName())){
                    optionAdapter.setSelectedPosition(i);
                }
            }
        }

        //添加自定义分割线
        DividerItemDecoration divider = new DividerItemDecoration((Activity) context,DividerItemDecoration.VERTICAL);
        divider.setDrawable(context.getDrawable(R.drawable.custom_divider));
        optionsRecyclerView.addItemDecoration(divider);
//        optionsRecyclerView.addItemDecoration(new DividerItemDecoration((Activity) context,DividerItemDecoration.VERTICAL));

        // 设置弹窗背景
//        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.edit_text));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);

        // 计算弹窗显示位置（anchorView上方）
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int anchorHeight = anchorView.getHeight();
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();
        int xOffset = -ZDpUtils.dpToPx((Activity) context, 8);
        int yOffset = -anchorHeight - popupHeight - ZDpUtils.dpToPx((Activity) context,12); // 在anchorView上方10px处显示

        // 显示弹窗
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset);
    }

    //ai办公筛选
    public static void showAIWorkFilterPopup(Context context,View anchorView,
                                             AiWorkFilterBean selectOptionModel,
                                             AiWorkFilterAdapter.OnOptionSelectedListener listener) {
        // 加载弹窗布局
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_single_choice, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
//                ZDpUtils.dpToPx((Activity) context,234)
                ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT, true);
        LinearLayout llChoice = popupView.findViewById(R.id.ll_choice);
        ShadowUtils.applyDefaultShadow(llChoice,context);
        // 初始化RecyclerView
        RecyclerView optionsRecyclerView = popupView.findViewById(R.id.optionsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        optionsRecyclerView.setLayoutManager(layoutManager);

        List<AiWorkFilterBean> options = new ArrayList<>();
//        options.add(new AiWorkFilterBean("全部"));
        options.add(new AiWorkFilterBean("AI会议", AiWorkAdapter.TYPE_MEETING));
        options.add(new AiWorkFilterBean("AI PPT",AiWorkAdapter.TYPE_PPT));
        options.add(new AiWorkFilterBean("同声传译",AiWorkAdapter.TYPE_TRANSLATE));
        options.add(new AiWorkFilterBean("AI 绘画",AiWorkAdapter.TYPE_DRAWING));

        // 设置选项数据和适配器
//        List<OptionModel> options =new ArrayList<>();

        AiWorkFilterAdapter optionAdapter = new AiWorkFilterAdapter(context,options, selected -> {
            listener.onOptionSelected(selected);
            popupWindow.dismiss();
        });
        optionsRecyclerView.setAdapter(optionAdapter);

        if(selectOptionModel != null){
            for (int i = 0; i < options.size(); i++) {
                if(options.get(i).getName().equals(selectOptionModel.getName())){
                    optionAdapter.setSelectedPosition(i);
                }
            }
        }

        //添加自定义分割线
//        DividerItemDecoration divider = new DividerItemDecoration((Activity) context,DividerItemDecoration.VERTICAL);
//        divider.setDrawable(context.getDrawable(R.drawable.custom_divider));
        CustomDividerItemDecoration divider = new CustomDividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        optionsRecyclerView.addItemDecoration(divider);
//        optionsRecyclerView.addItemDecoration(new DividerItemDecoration((Activity) context,DividerItemDecoration.VERTICAL));

        // 设置弹窗背景
//        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.edit_text));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);

        // 计算弹窗显示位置（anchorView上方）
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int anchorHeight = anchorView.getHeight();
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();
        int xOffset = -ZDpUtils.dpToPx((Activity) context, 8);;
        int yOffset = ZDpUtils.dpToPx((Activity) context,15);
//        int yOffset = -anchorHeight - popupHeight - ZDpUtils.dpToPx((Activity) context,15); // 在anchorView上方10px处显示

        // 显示弹窗
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset);
    }



    public static void copy(Context context,String text){

        ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label",text);
        clipboardManager.setPrimaryClip(clipData);
//        showToast("已复制");
        GlobalToast.show((Activity) context, "已复制", GlobalToast.Type.SUCCESS);
    }

    public static void setSystem(Activity activity) {

        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, android.R.color.transparent));
            window.setBackgroundDrawable( AppCompatResources.getDrawable(activity, R.mipmap.login_bg2));
//            window.setBackgroundDrawable( AppCompatResources.getDrawable(activity, R.drawable.login_bg));
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    // 显示控件（从下方滑入）
    public static void slideInDown2Up(View view) {
        view.setTranslationY(view.getHeight()); // 初始位置在视图下方
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0) // 目标位置为原位置
                .setDuration(300)
                .setListener(null);
    }

    // 显示控件（从上方滑入）
    public static void slideInUp2Down(View view) {
        view.setTranslationY(-view.getHeight()); //
        view.setAlpha(0f); // 初始透明度为0
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0) // 目标位置为原位置
                .alpha(1f) // 目标透明度为1
                .setDuration(50)
                .setListener(null);
    }

    // 隐藏控件（向下滑出）
    public static void slideOut(View view) {
        view.animate()
                .translationY(view.getHeight()) // 目标位置在视图下方
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE); // 动画结束后设置可见性为GONE
                        view.setTranslationY(0);
                    }
                });
    }

    // 禁用子视图的 tooltip
    public static void disableTooltipForChildViews(View view) {
//        if (view instanceof ViewGroup) {
//            ViewGroup viewGroup = (ViewGroup) view;
//            for (int i = 0; i < viewGroup.getChildCount(); i++) {
//                View child = viewGroup.getChildAt(i);
//                child.setLongClickable(false);
//                child.setOnLongClickListener(null);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    child.setTooltipText(null);
//                }
//                // 递归处理子视图
//                disableTooltipForChildViews(child);
//            }
//        }
    }

    public static String getTopActivity(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.d("测试", "pkg:"+cn.getPackageName());//包名
        Log.d("测试", "cls:"+cn.getClassName());//包名加类名
        return cn.getClassName();
    }


}

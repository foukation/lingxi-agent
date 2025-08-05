package com.fxzs.lingxiagent.util.ZUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.auth.AuthHelper;
import com.fxzs.lingxiagent.model.chat.callback.AsrCallback;
import com.fxzs.lingxiagent.model.chat.dto.AiWritingTypeBean;
import com.fxzs.lingxiagent.model.chat.callback.AITranslateEditCallback;
import com.fxzs.lingxiagent.util.ShadowUtils;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.util.ZInputMethod;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.auth.OneClickLoginActivity;
import com.fxzs.lingxiagent.view.chat.OptionAiMeetingAdapter;

import java.util.ArrayList;
import java.util.List;

public class SuperEditAITranslateUtil {

    Context context;
    LinearLayout root_view;
    View ll_bottom_edit;
    View ll_voice_edit;



    private EditText ed;
    ImageView iv_send;
    private AITranslateEditCallback callback;
    List<AiWritingTypeBean> listModel;
    List<AiWritingTypeBean> listModel2;
    private ImageView iv_close;
    private LinearLayout ll_from;
    private LinearLayout ll_to;
    private TextView tv_from;
    private TextView tv_to;


    ImageView iv_voice;
    private LinearLayout ll_bottom_voice;
    TextView tv_voice_hint;
    View rl_voice;
    ImageView iv_keyboard;
    ImageView iv_add;
    TextView tv_press;
    View iv_logo;
    private View ll_edit;


    boolean isVoice = false;
    boolean isRecord = false;
    private boolean isInArea = true;

    AiWritingTypeBean selectOption1;
    AiWritingTypeBean selectOption2;

    String sleet1="自动检测";
    String sleet2="简体中文";
    private int[] location = new int[2];
    public SuperEditAITranslateUtil(Context context, LinearLayout root_view) {
        this.context = context;
        this.root_view = root_view;

        setUI();
        getModel();
        AsrOneUtils.getInstance().init((Activity) context);

        AsrOneUtils.getInstance().setCallBack(new AsrCallback() {
            @Override
            public void callback(String result) {

                ZUtils.print("isInArea = "+isInArea);
                ZUtils.print("result = "+result);
                if(isInArea){
                    sendMsg(result);
                }

            }
        });
    }

    private void setUI() {
        ll_from = root_view.findViewById(R.id.ll_from);
        ll_to = root_view.findViewById(R.id.ll_to);
        tv_from = root_view.findViewById(R.id.tv_from);
        tv_to = root_view.findViewById(R.id.tv_to);
        ed = root_view.findViewById(R.id.ed);
        iv_send = root_view.findViewById(R.id.iv_send);
        iv_close = root_view.findViewById(R.id.iv_close);
        ll_bottom_voice = root_view.findViewById(R.id.ll_bottom_voice);
        ll_edit = root_view.findViewById(R.id.ll_edit);
        ll_bottom_edit = root_view.findViewById(R.id.ll_bottom_edit);
        ll_voice_edit = root_view.findViewById(R.id.ll_voice_edit);

        iv_keyboard = root_view.findViewById(R.id.iv_keyboard);
        iv_add = root_view.findViewById(R.id.iv_add);
        iv_send = root_view.findViewById(R.id.iv_send);
        tv_voice_hint = root_view.findViewById(R.id.tv_voice_hint);
        rl_voice = root_view.findViewById(R.id.rl_voice);
        iv_voice = root_view.findViewById(R.id.iv_voice);
        tv_press = root_view.findViewById(R.id.tv_press);
        rl_voice = root_view.findViewById(R.id.rl_voice);
        iv_logo = root_view.findViewById(R.id.iv_logo);
        ShadowUtils.applyDefaultShadow(ll_bottom_edit,context);
        ZInputMethod.openInputMethod(ed);

        ll_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZUtils.showAIMeetingPopup(context,view,listModel,selectOption1,new OptionAiMeetingAdapter.OnOptionSelectedListener() {
                    @Override
                    public void onOptionSelected(AiWritingTypeBean option) {
                        selectOption1 = option;
                        tv_from.setText(option.getName());
                        sleet1 = option.getName();
                    }
                });
            }
        });

        ll_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZUtils.showAIMeetingPopup(context,view,listModel2,selectOption2,new OptionAiMeetingAdapter.OnOptionSelectedListener() {
                    @Override
                    public void onOptionSelected(AiWritingTypeBean option) {
                        selectOption2 = option;
                        tv_to.setText(option.getName());
                        sleet2 = option.getName();
                    }
                });
            }
        });
        iv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (callback != null) {
                    String content = ed.getText().toString();
                    if (content.isEmpty()) {
                       ZUtils.showToast("请输入内容");
                        return;
                    }
                    sendMsg(content);
                }
            }
        });
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsrOneUtils.getInstance().removeCallBack();
                changeSoftkey(0,null);
                if (callback != null) {
                    callback.close();
                }
            }
        });

        ed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 文本改变前
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 文本改变中
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 文本改变后，s 是当前文本内容
                String input = s.toString();
                // 比如，实时显示输入内容
                Log.d("EditText", "当前输入: " + input);
                if (input.length() > 0) {
//                    iv_voice.setVisibility(View.GONE);
//                    iv_add.setVisibility(View.GONE);
                    iv_send.setVisibility(View.VISIBLE);
                }else {
//                    iv_voice.setVisibility(View.VISIBLE);
//                    iv_add.setVisibility(View.VISIBLE);
                    iv_send.setVisibility(View.GONE);
                }
            }
        });


        iv_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSoftkey(1,view);

            }
        });

        iv_keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSoftkey(0,view);

            }
        });

        ll_voice_edit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Log.d("TouchEvent", "onLongClick ====== >");
//                if(isRecord){
//                    AsrOneUtils.stop();
//                    ll_bottom_voice.setVisibility(View.GONE);
//                    ll_bottom_edit.setVisibility(View.VISIBLE);
//                }else{
//                    AsrOneUtils.recognizer();
//                    ll_bottom_voice.setVisibility(View.VISIBLE);
//                    ll_bottom_edit.setVisibility(View.GONE);
//                }
//                isRecord = !isRecord;
                if (isVoice){
                    return true;
                }
                return false;
            }
        });


        ll_voice_edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!isVoice){
                    return false;
                }
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 手指按下
                        Log.d("TouchEvent", "手指按下 TextView");
//                        tv_press.setBackgroundColor(Color.LTGRAY); // 示例：改变背景色
                        isInArea = true;

                        AsrOneUtils.getInstance().recognizer();
//                        ll_bottom_voice.setVisibility(View.VISIBLE);
//                        ll_bottom_edit.setVisibility(View.GONE);
                        callback.pressDown();
                        break;

                    case MotionEvent.ACTION_UP:
                        // 手指松开
                        Log.d("TouchEvent", "手指松开 TextView");
//                        tv_press.setBackgroundColor(Color.TRANSPARENT); // 示例：恢复背景色
//                        rl_voice.setBackground(context.getResources().getDrawable(R.drawable.bg_voice_blue_r20));
//                        tv_voice_hint.setText("松手发送，上移取消");

//                        Utils.print("is_running == "+is_running);
//                        if (is_running) {
//                            controller.stop();

                        AsrOneUtils.getInstance().stop();
//                        ll_bottom_voice.setVisibility(View.GONE);
                        ll_bottom_edit.setVisibility(View.VISIBLE);
//                            Utils.print("setEnabled == stop");
//                            is_running = false;
//                        }
                        callback.pressUp(isInArea);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // 获取屏幕坐标
                        float rawX = motionEvent.getRawX();
                        float rawY = motionEvent.getRawY();

//                        // 获取视图在屏幕中的位置
                        ll_voice_edit.getLocationOnScreen(location);
                        int viewLeft = location[0];
                        int viewTop = location[1];
                        int viewRight = viewLeft + ll_voice_edit.getWidth();
                        int viewBottom = viewTop + ll_voice_edit.getHeight();

                        Log.d("TouchEvent", "rawX: " + rawX + ", rawY: " + rawY);
                        Log.d("TouchEvent", "viewLeft: " + viewLeft + ", viewTop: " + viewTop+ ", viewRight: " + viewRight+ ", viewBottom: " + viewBottom);
                        // 转换为相对坐标
//                        float x = rawX - viewLeft;
//                        float y = rawY - viewTop;
//
//                        Log.d("TouchEvent", "x: " + x + ", y: " + y);
//                        Log.d("TouchEvent", "viewWidth: " + ll_bottom_edit.getWidth() + ", viewHeight: " + ll_bottom_edit.getHeight());

                        // 判断是否在视图范围内
                        if (rawX < viewLeft || rawX > viewRight || rawY < viewTop || rawY > viewBottom) {
                            Log.d("TouchEvent", "手指移出 TextView 范围");
//                            rl_voice.setBackground(context.getResources().getDrawable(R.drawable.bg_voice_red_r20));
//                            tv_voice_hint.setText("松开取消");
                            isInArea = false;
                        } else {
                            Log.d("TouchEvent", "手指在 TextView 范围内移动");
//                            rl_voice.setBackground(context.getResources().getDrawable(R.drawable.bg_voice_blue_r20));
//                            tv_voice_hint.setText("松手发送，上移取消");
                            isInArea = true;
                        }
                        callback.voiceMove(isInArea);
//                        // 手指移动
//                        float x = motionEvent.getX();
//                        float y = motionEvent.getY();
//
//                        Log.d("TouchEvent", "x "+x);
//                        Log.d("TouchEvent", "ll_bottom_edit.getWidth() "+ll_bottom_edit.getWidth());
//                        Log.d("TouchEvent", "y "+y);
//                        Log.d("TouchEvent", "ll_bottom_edit.getHeight() "+ll_bottom_edit.getHeight());
//                        // 判断手指是否在 TextView 范围内
//                        if (x < 0 || x > ll_bottom_edit.getWidth() || y < 0 || y > ll_bottom_edit.getHeight()) {
//                            // 手指移出 TextView 范围
//                            Log.d("TouchEvent", "手指移出 TextView 范围");
//                            rl_voice.setBackground(context.getResources().getDrawable(R.drawable.bg_voice_red_r20));
//                            tv_voice_hint.setText("松开取消");
//                            isInArea = false;
//                        } else {
//                            // 手指仍在 TextView 范围内
//                            Log.d("TouchEvent", "手指在 TextView 范围内移动");
//                            rl_voice.setBackground(context.getResources().getDrawable(R.drawable.bg_voice_blue_r20)); // 示例：恢复背景色
//                            tv_voice_hint.setText("松手发送，上移取消");
//                            isInArea = true;
//                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:
                        // 触摸取消（例如被父视图拦截）
                        Log.d("TouchEvent", "触摸取消");
//                        rl_voice.setBackground(context.getResources().getDrawable(R.drawable.bg_voice_blue_r20)); // 示例：恢复背景色
                        isInArea = false;
                        AsrOneUtils.getInstance().stop();
//                        ll_bottom_voice.setVisibility(View.GONE);
                        ll_bottom_edit.setVisibility(View.VISIBLE);
                        break;
                }
                return false; // 返回 true 表示消费事件
            }
        });
    }

    private void changeSoftkey(int state,View view) {
        if(state == 0){//键盘模式
            isVoice = false;

            if (callback != null) {
                callback.keyboard();
            }

            iv_keyboard.setVisibility(View.GONE);
            tv_press.setVisibility(View.GONE);
            iv_logo.setVisibility(View.GONE);
            iv_voice.setVisibility(View.VISIBLE);
            ll_edit.setVisibility(View.VISIBLE);
            if(view !=null){
                ZInputMethod.openInputMethod(ed);
            }
        }else if(state == 1){//语音模式

            if ( !AuthHelper.getInstance().isLogin()) {
                // 未登录，跳转到一键登录页面
                Intent intent = new Intent(context, OneClickLoginActivity.class);
                context.startActivity(intent);
                return;
            }
            isVoice = true;
            if (callback != null) {
                callback.voice();
            }
            ed.setText("");
            iv_keyboard.setVisibility(View.VISIBLE);
            tv_press.setVisibility(View.VISIBLE);
            iv_logo.setVisibility(View.VISIBLE);
            iv_voice.setVisibility(View.GONE);
            ll_edit.setVisibility(View.GONE);
            ZInputMethod.hideKeyboard(context,view.getWindowToken());
        }
    }

    private void sendMsg(String content) {

        if(TextUtils.isEmpty(content.trim())){
            ZUtils.showToast("请输入内容");
            return;
        }
        changeSoftkey(0,null);
        String sendContent = content+" 翻译为"+sleet2;
        content = "帮我把这段文本翻译成"+sleet2+":\""+content+"\"";
        ed.setText("");
        callback.send(content,sendContent);

        AsrOneUtils.getInstance().removeCallBack();
    }

    public  void setOnListenSoft(View root_view){


        root_view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                root_view.getWindowVisibleDisplayFrame(rect);
                int screenHeight = root_view.getRootView().getHeight();

                int keyHeight = screenHeight - rect.bottom;
                if(keyHeight < screenHeight*0.15){
                    Log.e("HomeFragment", "隐藏了.");
                }else {
                    Log.e("HomeFragment", "弹出.");
                }
            }
        });
    }

    public void setCallback(AITranslateEditCallback callback) {
        // 设置回调接口
        this.callback = callback;
    }

    public void getModel(){
        listModel = new ArrayList<>();
        listModel.add(new AiWritingTypeBean("自动检测"));
        listModel.add(new AiWritingTypeBean("英语"));
        listModel.add(new AiWritingTypeBean("简体中文"));
        listModel.add(new AiWritingTypeBean("繁体中文"));

        selectOption1 = listModel.get(0);

        listModel2 = new ArrayList<>();
        listModel2.add(new AiWritingTypeBean("英语"));
        listModel2.add(new AiWritingTypeBean("简体中文"));
        listModel2.add(new AiWritingTypeBean("繁体中文"));

        selectOption2 = listModel2.get(1);
        tv_to.setText(selectOption2.getName());
        sleet2 = selectOption2.getName();
    }

    public ImageView getIv_close() {
        return iv_close;
    }
}

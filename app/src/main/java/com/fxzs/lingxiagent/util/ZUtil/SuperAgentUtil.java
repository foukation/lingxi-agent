package com.fxzs.lingxiagent.util.ZUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.auth.AuthHelper;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.chat.callback.AsrCallback;
import com.fxzs.lingxiagent.model.chat.callback.SoftCallback;
import com.fxzs.lingxiagent.model.chat.callback.SuperEditCallback;
import com.fxzs.lingxiagent.network.ZNet.ApiResponse;
import com.fxzs.lingxiagent.network.ZNet.HttpRequest;
import com.fxzs.lingxiagent.util.ShadowUtils;
import com.fxzs.lingxiagent.util.SharedPreferencesUtil;
import com.fxzs.lingxiagent.util.ZInputMethod;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.auth.OneClickLoginActivity;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class SuperAgentUtil {

    Context context;
    LinearLayout root_view;
    View ll_bottom_edit;


//    private TextView tv_ed_fake;
    private LinearLayout ll_edit;
//    private LinearLayout ll_functions;
    private LinearLayout ll_bottom_voice;

    private EditText ed;
    ImageView iv_voice;
    ImageView iv_keyboard;
//    ImageView iv_add;
    ImageView iv_send;
//    TextView tv_mode;
    TextView tv_press;
    TextView tv_voice_hint;
    View rl_voice;
    View iv_logo;
    private SuperEditCallback callback;
    List<OptionModel> listModel;
    OptionModel selectOptionModel;

    boolean isVoice = false;
    boolean isRecord = false;
    private boolean isInArea = true;

    private int viewLeft;
    private int viewRight;
    private int viewTop;
    private int viewBottom;

    private int[] location = new int[2];
    public SuperAgentUtil(Context context, LinearLayout root_view) {
        this.context = context;
        this.root_view = root_view;

//        listModel = SpUtils.loadDataList(context);
        setUI();
//        if(listModel == null || listModel.size() == 0){
//            getModel(); // 获取模型列表
//        }
        AsrOneUtils.getInstance().init((Activity) context);

        AsrOneUtils.getInstance().setCallBack(new AsrCallback() {
            @Override
            public void callback(String result) {

                ZUtils.print("isInArea = "+isInArea);
                ZUtils.print("result = "+result);
//                ZUtils.print("selectOptionModel = "+selectOptionModel.toString());
                if(isInArea){
//                    String text = AsrOneUtils.getResult();
                    callback.send(result,selectOptionModel);
                }

            }
        });
    }

    private void setUI() {

//        tv_ed_fake = root_view.findViewById(R.id.tv_ed_fake);
        ll_edit = root_view.findViewById(R.id.ll_edit);
        ll_bottom_edit = root_view.findViewById(R.id.ll_bottom_edit);
        ll_bottom_voice = root_view.findViewById(R.id.ll_bottom_voice);
        ed = root_view.findViewById(R.id.ed);
        iv_voice = root_view.findViewById(R.id.iv_voice);
        iv_keyboard = root_view.findViewById(R.id.iv_keyboard);
//        iv_add = root_view.findViewById(R.id.iv_add);
        iv_send = root_view.findViewById(R.id.iv_send);
//        ll_functions = root_view.findViewById(R.id.ll_functions);
//        tv_mode = root_view.findViewById(R.id.tv_mode);
        tv_press = root_view.findViewById(R.id.tv_press);
        tv_voice_hint = root_view.findViewById(R.id.tv_voice_hint);
        rl_voice = root_view.findViewById(R.id.rl_voice);
        iv_logo = root_view.findViewById(R.id.iv_logo);
        ShadowUtils.applyDefaultShadow(ll_bottom_edit,context);

//        tv_ed_fake.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.e("HomeFragment", "点击了tv_ed_fake.");
//                if ( !SharedPreferencesUtil.isLoggedIn()) {
//                    // 未登录，跳转到一键登录页面
//                    Intent intent = new Intent(context, OneClickLoginActivity.class);
//                    context.startActivity(intent);
//                    return;
//                }
//
////                Toast.makeText(getActivity(), "点击了tv_ed_fake", Toast.LENGTH_SHORT).show();
//
//                ZInputMethod.openInputMethod(ed);
////                ed.requestFocus();
//            }
//        });

        iv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZUtils.print("点击发送");

                sendText();
            }
        });

//        iv_add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                ZUtils.showSingleChoicePopup(context, iv_add);
//            }
//        });
        iv_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switchMode(1);
                // 获取视图在屏幕中的位置

                ll_bottom_edit.getLocationOnScreen(location);
                viewLeft = location[0];
                viewTop = location[1];
                viewRight = viewLeft + ll_bottom_edit.getWidth();
                viewBottom = viewTop + ll_bottom_edit.getHeight();
            }
        });

        ll_bottom_edit.setOnLongClickListener(new View.OnLongClickListener() {
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


        ll_bottom_edit.setOnTouchListener(new View.OnTouchListener() {
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

//                        AsrOneUtils.getInstance().recognizer();
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

//                        AsrOneUtils.getInstance().stop();
                        ll_bottom_voice.setVisibility(View.GONE);
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

                        // 获取视图在屏幕中的位置
//                        int[] location = new int[2];
//                        rl_voice.getLocationOnScreen(location);
//                        int viewLeft = location[0];
//                        int viewTop = location[1];
//                        int viewRight = viewLeft + rl_voice.getWidth();
//                        int viewBottom = viewTop + rl_voice.getHeight();

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
                        callback.voiceMove(isInArea);
                        break;

                    case MotionEvent.ACTION_CANCEL:
                        // 触摸取消（例如被父视图拦截）
                        Log.d("TouchEvent", "触摸取消");
                        rl_voice.setBackground(context.getResources().getDrawable(R.drawable.bg_voice_blue_r20)); // 示例：恢复背景色
                        isInArea = false;
//                        AsrOneUtils.getInstance().stop();
                        ll_bottom_voice.setVisibility(View.GONE);
                        ll_bottom_edit.setVisibility(View.VISIBLE);
                        break;
                }
                return false; // 返回 true 表示消费事件
            }
        });
        iv_keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchMode(0);
            }
        });
//        tv_mode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(listModel == null || listModel.size()==0){
//                    ZUtils.showToast("模型未加载");
//                    return;
//                }
//
//                ZUtils.showChooseModelPopup(context, tv_mode,listModel,selectOptionModel,
//                        new OptionModelAdapter.OnOptionSelectedListener() {
//                            @Override
//                            public void onOptionSelected(OptionModel option) {
//                                selectOptionModel = option;
//                                ZUtils.print("选中模型: " + selectOptionModel.getName() + ", ID: " + selectOptionModel.getId());
//                                tv_mode.setText(selectOptionModel.getName());
//                            }
//
//                        });
//            }
//        });

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
//                ZUtils.setTextColor(context,tv_ed_fake,input.length() > 0?R.color.text_black:R.color.text_hint_color);
//                if(input.length() == 0){
//                    tv_ed_fake.setText("发消息...");
//                }else {
//                    tv_ed_fake.setText(input);
//                }
                if (input.length() > 0) {
                    iv_voice.setVisibility(View.GONE);
//                    iv_add.setVisibility(View.GONE);
                    iv_send.setVisibility(View.VISIBLE);
                }else {
                    iv_voice.setVisibility(View.VISIBLE);
//                    iv_add.setVisibility(View.VISIBLE);
                    iv_send.setVisibility(View.GONE);
                }
            }
        });
        ed.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND){
                    sendText();
                    return true;
                }
                return false;
            }

        });
    }

    private void sendText() {
        if (callback != null) {
            String content = ed.getText().toString();
            if (content.isEmpty()) {
                ZUtils.showToast("请输入内容");
                return;
            }
//            if (selectOptionModel == null) {
//                ZUtils.showToast("模型未加载");
//                return;
//            }

            ed.setText("");
            callback.send(content,selectOptionModel);
        }
    }

    public void voiceSendText(String content){
        if (callback == null){
            return;
        }
        callback.send(content,selectOptionModel);
    }

    public  void setOnListenSoft(View root_view, SoftCallback callback){


        root_view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                root_view.getWindowVisibleDisplayFrame(rect);
                int screenHeight = root_view.getRootView().getHeight();

                int keyHeight = screenHeight - rect.bottom;
                if(keyHeight < screenHeight*0.15){
                    Log.e("HomeFragment", "隐藏了.");
//                    if(isVoice){
//                        tv_ed_fake.setVisibility(View.GONE);
//                    }else{
//                        tv_ed_fake.setVisibility(View.VISIBLE);
//                    }
//                    ll_edit.setVisibility(View.GONE);
//                    ll_functions.setVisibility(View.GONE);
                    callback.hide();
                }else {
                    Log.e("HomeFragment", "弹出.");
//                    tv_ed_fake.setVisibility(View.GONE);
//                    ll_edit.setVisibility(View.VISIBLE);
//                    ll_functions.setVisibility(View.VISIBLE);
//                    ZUtils.slideInUp2Down(ll_functions);
                    callback.show();

                }
            }
        });
    }

    public void setCallback(SuperEditCallback callback) {
        // 设置回调接口
        this.callback = callback;
    }

    public void getModel(){
        HttpRequest request = new HttpRequest();
        request.getModelTypeList(new Observer<ApiResponse<List<OptionModel>>>(){

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ApiResponse<List<OptionModel>> res) {
                if (res.getCode() == 0){
                    List<OptionModel> list = res.getData();
                    listModel = list;
//                    SpUtils.saveDataList(context,listModel);
                    Log.d("TAG", "初始化 Model: " + selectOptionModel);
                    if (selectOptionModel == null) {
                        selectOptionModel = listModel.get(0); // 默认选中第一个模型
                        Log.d("TAG", "初始化 Model: " + selectOptionModel.getName() + ", ID: " + selectOptionModel.getId());
//                        tv_mode.setText(selectOptionModel.getName());
                    }
                    if (list != null && list.size() > 0) {
                        for (OptionModel model : list) {
                            Log.d("TAG", "Model: " + model.getName() + ", ID: " + model.getId());
                        }
                    }
                }

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }


    public void setSelectOptionModel(OptionModel option) {
        this.selectOptionModel = option;
    }

    public void switchMode(int mode) {
        //mode:0-文字输入，1-语音模式
        if(mode == 0){

            isVoice = false;

            if (callback != null) {
                callback.keyboard();
            }

            iv_keyboard.setVisibility(View.GONE);
            tv_press.setVisibility(View.GONE);
            iv_logo.setVisibility(View.GONE);
            iv_voice.setVisibility(View.VISIBLE);
//                tv_ed_fake.setVisibility(View.VISIBLE);
            ll_edit.setVisibility(View.VISIBLE);
            ZInputMethod.openInputMethod(ed);
        }else if(mode == 1){

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
            iv_keyboard.setVisibility(View.VISIBLE);
            tv_press.setVisibility(View.VISIBLE);
            iv_logo.setVisibility(View.VISIBLE);
            iv_voice.setVisibility(View.GONE);
//                tv_ed_fake.setVisibility(View.GONE);
            ll_edit.setVisibility(View.GONE);
            ZInputMethod.hideKeyboard(context,root_view.getWindowToken());
        }
    }
//    public static void clear(){
//        AsrOneUtils.getInstance().removeCallBack();
//    }
}

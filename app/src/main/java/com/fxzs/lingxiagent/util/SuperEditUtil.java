package com.fxzs.lingxiagent.util;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.auth.AuthHelper;
import com.fxzs.lingxiagent.model.chat.dto.OptionBean;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.chat.callback.AsrCallback;
import com.fxzs.lingxiagent.model.chat.callback.DialogEditCallback;
import com.fxzs.lingxiagent.model.chat.callback.OnFileItemClick;
import com.fxzs.lingxiagent.model.chat.callback.SSECallback;
import com.fxzs.lingxiagent.model.chat.callback.SoftCallback;
import com.fxzs.lingxiagent.model.chat.callback.StsCallback;
import com.fxzs.lingxiagent.model.chat.callback.SuperEditCallback;
import com.fxzs.lingxiagent.model.chat.dto.ChatFileBean;
import com.fxzs.lingxiagent.network.ZNet.ApiResponse;
import com.fxzs.lingxiagent.network.ZNet.HttpRequest;
import com.fxzs.lingxiagent.network.ZNet.bean.SSEFileAnalyseBean;
import com.fxzs.lingxiagent.util.ZUtil.AsrOneUtils;
import com.fxzs.lingxiagent.util.ZUtil.DialogUtils;
import com.fxzs.lingxiagent.util.ZUtil.FileUtils;
import com.fxzs.lingxiagent.util.ZUtil.SessionUpload;
import com.fxzs.lingxiagent.view.auth.OneClickLoginActivity;
import com.fxzs.lingxiagent.view.chat.ChatFileAdapter;
import com.fxzs.lingxiagent.view.chat.OptionAdapter;
import com.fxzs.lingxiagent.view.chat.OptionModelAdapter;
import com.fxzs.lingxiagent.view.common.GlobalToast;
import com.fxzs.lingxiagent.view.auth.OneClickLoginActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class SuperEditUtil {

    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 20861;
    public static final int REQUEST_CAMERA_PERMISSION = 20861;
    public static final int REQUEST_PHOTO = 10862;
    public static final int REQUEST_PIC = 10863;
    public static final int REQUEST_LOCAL_FILE = 10864;
    Context context;
    LinearLayout root_view;
    View ll_bottom_edit;


    private TextView tv_ed_fake;
    private LinearLayout ll_edit;
    private LinearLayout ll_functions;
    private LinearLayout ll_bottom_voice;

    private EditText ed;
    ImageView iv_voice;
    ImageView iv_keyboard;
    ImageView iv_add;
    ImageView iv_send;
    View ll_mode;
    TextView tv_mode;
    TextView tv_press;
    TextView tv_voice_hint;
    View rl_voice;
    View iv_logo;
    View iv_edit_open;
    View iv_edit_open_expand;
    private SuperEditCallback callback;
    List<OptionModel> listModel = new ArrayList<>();
    List<OptionModel> listModelSave = new ArrayList<>();
    OptionModel selectOptionModel;

    boolean isVoice = false;
    boolean isRecord = false;
    private boolean isInArea = true;


    private String currentPhotoPath;
    private Uri photoUri;
    private RecyclerView rv_file;
    private RecyclerView rv_quick_prompt;
    private View ll_files;
    private ChatFileAdapter chatFileAdapter;
    private ChatFileAdapter chatQuickPromptAdapter;

    List<ChatFileBean> list_file = new ArrayList<>();
    List<ChatFileBean> list_quick_prompt = new ArrayList<>();
    private boolean isUpload = false;
    private boolean isAnalyse = false;//文件还需要分析
    private int actionType = 0;
    private int tempActionType = 0;

    boolean banSelectModel = false;//历史禁止选
    public long conversationId = 0;//当前会话id

    private int[] location = new int[2];
    private int viewLeft;
    private int viewRight;
    private int viewTop;
    private int viewBottom;
    private LinearLayout ll_expand;
    private String lingXiModel = "10086";
    public SuperEditUtil(Context context,LinearLayout root_view) {
        this.context = context;
        this.root_view = root_view;

//        listModel = SpUtils.loadDataList(context);
        setUI();
        if(listModel == null || listModel.size() == 0){
            getModel(); // 获取模型列表
        }
//        AsrOneUtils.getInstance().init((Activity) context);
//
//        AsrOneUtils.getInstance().setCallBack(new AsrCallback() {
//            @Override
//            public void callback(String result) {
//
//                ZUtils.print("isInArea = "+isInArea);
//                ZUtils.print("result = "+result);
//                ZUtils.print("selectOptionModel = "+selectOptionModel.toString());
//                if(isInArea){
////                    String text = AsrOneUtils.getResult();
////                    callback.send(result,selectOptionModel);
//                    if(!TextUtils.isEmpty(result)){
////                        sendText(result);
//                        sendCommon(result);
//                    }
//                }
//
//            }
//        });
    }

    private void setUI() {

        tv_ed_fake = root_view.findViewById(R.id.tv_ed_fake);
        ll_edit = root_view.findViewById(R.id.ll_edit);
        ll_bottom_edit = root_view.findViewById(R.id.ll_bottom_edit);
        ll_bottom_voice = root_view.findViewById(R.id.ll_bottom_voice);
        ed = root_view.findViewById(R.id.ed);
        iv_voice = root_view.findViewById(R.id.iv_voice);
        iv_keyboard = root_view.findViewById(R.id.iv_keyboard);
        iv_add = root_view.findViewById(R.id.iv_add);
        iv_send = root_view.findViewById(R.id.iv_send);
        ll_functions = root_view.findViewById(R.id.ll_functions);
        ll_mode = root_view.findViewById(R.id.ll_mode);
        tv_mode = root_view.findViewById(R.id.tv_mode);
        tv_press = root_view.findViewById(R.id.tv_press);
        tv_voice_hint = root_view.findViewById(R.id.tv_voice_hint);
        rl_voice = root_view.findViewById(R.id.rl_voice);
        iv_logo = root_view.findViewById(R.id.iv_logo);
        iv_edit_open = root_view.findViewById(R.id.iv_edit_open);
        iv_edit_open_expand = root_view.findViewById(R.id.iv_edit_open_expand);
        ll_expand = root_view.findViewById(R.id.ll_expand);

        rv_file = root_view.findViewById(R.id.rv_file);
        rv_quick_prompt = root_view.findViewById(R.id.rv_quick_prompt);
        ll_files = root_view.findViewById(R.id.ll_files);
        ShadowUtils.applyDefaultShadow(ll_bottom_edit,context);
        tv_ed_fake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HomeFragment", "点击了tv_ed_fake.");
                if ( !AuthHelper.getInstance().isLogin()) {
                    // 未登录，跳转到一键登录页面
                    Intent intent = new Intent(context, OneClickLoginActivity.class);
                    context.startActivity(intent);
                    return;
                }

//                Toast.makeText(getActivity(), "点击了tv_ed_fake", Toast.LENGTH_SHORT).show();

                ZInputMethod.openInputMethod(ed);
//                ed.requestFocus();
            }
        });

        iv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZUtils.print("点击发送");
//                if(list_file.size() > 0){
//                        String content = ed.getText().toString();
//                        if (content.isEmpty()) {
//                            ZUtils.showToast("请输入内容");
//                            return;
//                        }
//                        sendWithFile(content);
//                }else{
//                    sendText("");
//                }
                sendCommon("");
            }
        });

        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkReadFilePermission();
                ZUtils.showSingleChoicePopup(context, iv_add, new OptionAdapter.OnOptionSelectedListener() {
                    @Override
                    public void onOptionSelected(OptionBean option) {
                        if(list_file.size() >= 5){
                            GlobalToast.show((Activity) context,"最多上传5个",GlobalToast.Type.NORMAL);
                            return;
                        }
                        if(isUpload ||
                                (actionType == REQUEST_LOCAL_FILE && isAnalyse)){
                            GlobalToast.show((Activity) context,"请等待上传完毕",GlobalToast.Type.NORMAL);
                            return;
                        }

                        int res = option.getResId();
                        ZUtils.print("actionType == "+actionType);
//                        int[] actionTypeList = new int[2]{R.mipmap.option_photo,R.mipmap.option_picture};
                        boolean isImage = res == R.mipmap.option_photo || res == R.mipmap.option_picture;
                        if(actionType!=0
                                && ((res == R.mipmap.option_local_file && actionType != res)||(isImage && actionType == R.mipmap.option_local_file))){
                            GlobalToast.show((Activity) context,"请选择同类型文件",GlobalToast.Type.NORMAL);
                            return;
                        }
                        tempActionType = res;
                        if(res == R.mipmap.option_photo){//拍照
                            checkCameraPermission();
                        }else if(res == R.mipmap.option_picture){//图片
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            ((Activity)context).startActivityForResult(intent, REQUEST_PIC);
                        }else if(res == R.mipmap.option_local_file){//本地文件
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*"); // 任意文件类型，图片可设为 "image/*"
                            ((Activity)context).startActivityForResult(intent, REQUEST_LOCAL_FILE);
                        }
                    }
                });
            }
        });
        iv_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !AuthHelper.getInstance().isLogin()) {
                    // 未登录，跳转到一键登录页面
                    Intent intent = new Intent(context, OneClickLoginActivity.class);
                    context.startActivity(intent);
                    return;
                }
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

//                        // 获取视图在屏幕中的位置
                        ll_bottom_edit.getLocationOnScreen(location);
                        int viewLeft = location[0];
                        int viewTop = location[1];
                        int viewRight = viewLeft + ll_bottom_edit.getWidth();
                        int viewBottom = viewTop + ll_bottom_edit.getHeight();

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
        ll_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(banSelectModel){
                    return;
                }
                if(listModel == null || listModel.size()==0){
                    ZUtils.showToast("模型未加载");
                    return;
                }

                ZUtils.showChooseModelPopup(context, tv_mode,listModel,selectOptionModel,
                        new OptionModelAdapter.OnOptionSelectedListener() {
                            @Override
                            public void onOptionSelected(OptionModel option) {
                                selectOptionModel = option;
                                ZUtils.print("选中模型: " + selectOptionModel.getName() + ", ID: " + selectOptionModel.getId());
//                                tv_mode.setText(selectOptionModel.getName());
                                callback.modeChange(selectOptionModel);
//                                if(conversationId != 0){
//                                    updateMy(conversationId,option.getId()+"");
//                                }
                            }

                        });
            }
        });
        iv_edit_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 点击弹窗
                DialogUtils.showInputDialog(context, ed.getEditableText().toString(), new DialogEditCallback() {
                    @Override
                    public void callback(String result) {
//                        ed.setText("");
//                        callback.send(result,selectOptionModel);
//                        sendText(result);
                        sendCommon(result);
                    }

                    @Override
                    public void onCancel(String result) {
                        DialogEditCallback.super.onCancel(result);
                        ed.setText(result);
                    }
                });
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
                int lineCount = ed.getLineCount();
                // 比如，实时显示输入内容
                Log.d("EditText", "当前输入: " + input);
                ZUtils.setTextColor(context,tv_ed_fake,input.length() > 0?R.color.text_black:R.color.text_hint_color);
                if(input.length() == 0){
                    tv_ed_fake.setText("发消息...");
                }else {
                    tv_ed_fake.setText(input);
                }
                if (input.length() > 0) {
                    iv_voice.setVisibility(View.GONE);
                    iv_add.setVisibility(View.GONE);
                    iv_send.setVisibility(View.VISIBLE);
//                    setEditVisible();
                }else {
                    if (isVoice){
                        iv_voice.setVisibility(View.GONE);
                    }else {
                        iv_voice.setVisibility(View.VISIBLE);
                    }

                    iv_add.setVisibility(View.VISIBLE);
                    iv_send.setVisibility(View.GONE);
//                    iv_edit_open_expand.setVisibility(View.GONE);
                }

                if (lineCount > 1) {
                    iv_edit_open_expand.setVisibility(View.VISIBLE);
                    setLayoutGravity(ll_expand, Gravity.BOTTOM);
                    if (lineCount > 2){
                        iv_edit_open.setVisibility(View.VISIBLE);
                    }else {
                        iv_edit_open.setVisibility(View.GONE);
                    }
                } else {
                    iv_edit_open_expand.setVisibility(View.GONE);
                    iv_edit_open.setVisibility(View.GONE);
                    setLayoutGravity(ll_expand, Gravity.CENTER);
                }

            }
        });
        ed.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND){
//                    sendText("");
                    sendCommon("");
                    return true;
                }
                return false;
            }
        });

//        list_file.add(new ChatFileBean("","",true));
//        list_file.add(new ChatFileBean("","",true));
//        setFileRv(list_file);
        setQuickPromptRv(list_quick_prompt);
    }

    private void setEditVisible() {
        ed.post(new Runnable() {
            @Override
            public void run() {
                ZUtils.print("ed.getHeight() === "+ed.getHeight());
                ZUtils.print("ZDpUtils.dpToPx(.50) === "+ZDpUtils.dpToPx((Activity) context,50));
                if(ed.getHeight() >= ZDpUtils.dpToPx((Activity) context,50)){
                    iv_edit_open_expand.setVisibility(View.VISIBLE);
                    iv_edit_open.setVisibility(View.VISIBLE);
                }else {
                    iv_edit_open.setVisibility(View.GONE);
                }
            }
        });
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
                    if(isVoice){
                        tv_ed_fake.setVisibility(View.GONE);
                    }else{
                        tv_ed_fake.setVisibility(View.VISIBLE);
                    }
                    ll_edit.setVisibility(View.GONE);
//                    ll_functions.setVisibility(View.GONE);
//                    setEditVisible();
                    callback.hide();
                }else {
                    Log.e("HomeFragment", "弹出.");
                    tv_ed_fake.setVisibility(View.GONE);
                    ll_edit.setVisibility(View.VISIBLE);
//                    ll_functions.setVisibility(View.VISIBLE);

//                    iv_edit_open.setVisibility(View.GONE);
//                    iv_edit_open_expand.setVisibility(View.GONE);
//                    ZUtils.slideOut(tv_ed_fake);
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
                    listModelSave.clear();
                    listModelSave.addAll(list);
                    listModel.clear();
                    listModel.addAll(list);
//                    listModel = list;
//                    SpUtils.saveDataList(context,listModel);
                    Log.d("TAG", "初始化 Model: " + selectOptionModel);
//                    if (selectOptionModel == null) {
//                        selectOptionModel = listModel.get(0); // 默认选中第一个模型
//                        Log.d("TAG", "初始化 Model: " + selectOptionModel.getName() + ", ID: " + selectOptionModel.getId());
//                        tv_mode.setText(selectOptionModel.getName());
//                    }
                    if (selectOptionModel == null) {
                        changeModel(0);
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
//        tv_mode.setText(selectOptionModel.getName());
        callback.modeChange(selectOptionModel);
    }

    public void setBanSelectModel(boolean banSelectModel) {
        this.banSelectModel = banSelectModel;
    }

    public void checkReadFilePermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }
    private void checkCameraPermission() {
//        Log.d(TAG, "Checking camera permission");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "Camera permission not granted, requesting...");
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
//            Log.d(TAG, "Camera permission already granted");
            openCamera();
        }
    }

    private void openCamera() {
//        Log.d(TAG, "Opening camera");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
//                Log.d(TAG, "Created image file: " + photoFile.getAbsolutePath());
            } catch (IOException ex) {
//                Log.e(TAG, "Error creating image file", ex);
//                showToast("创建图片文件失败");
                ex.printStackTrace();
                return;
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".fileprovider",
                        photoFile);
//                Log.d(TAG, "Photo URI: " + photoUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                ((Activity)context).startActivityForResult(takePictureIntent, REQUEST_PHOTO);
            }
        } else {
//            Log.e(TAG, "No camera app found");
//            showToast("没有找到相机应用");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir("Pictures");

        // 确保目录存在
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

         if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                GlobalToast.show((Activity) context,"需要权限才能使用功能", GlobalToast.Type.NORMAL);
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        ZUtils.print("onActivityResult ====== >");
        if (resultCode == RESULT_OK /*&& data != null*/) {
            if (requestCode == REQUEST_PHOTO) {//拍照
                changeModel(1);
                if (photoUri != null) {
                    ZUtils.print( "FileContent = "+photoUri.getPath().toString());
                    ChatFileBean bean = new ChatFileBean("",true);
                    bean.setFileUri(photoUri);
                    list_file.add(bean);
                    ll_files.setVisibility(View.VISIBLE);
                    File file = FileUtils.uriToFile(photoUri,context,bean);
//                    chatFileAdapter.setType(ChatFileAdapter.TYPE_IMAGE);
                    setFileRv(list_file,ChatFileAdapter.TYPE_IMAGE);
                    chatFileAdapter.notifyDataSetChanged();
                    uploadFile(file.getPath(),false);

                    list_quick_prompt.clear();
                    list_quick_prompt.add(new ChatFileBean("解读图片"));
                    list_quick_prompt.add(new ChatFileBean("提取文字"));
                    chatQuickPromptAdapter.notifyDataSetChanged();
                    actionType = tempActionType;
                }
            } else if (requestCode == REQUEST_PIC && data != null) {
                changeModel(1);
                Uri uri = data.getData();
                // 处理图片
                if (uri != null) {
                    ZUtils.print( "FileContent = "+uri.getPath().toString());
                    ChatFileBean bean = new ChatFileBean("",true);
                    bean.setFileUri(uri);
                    list_file.add(bean);
                    ll_files.setVisibility(View.VISIBLE);
                    File file = FileUtils.uriToFile(uri,context,bean);
                    setFileRv(list_file,ChatFileAdapter.TYPE_IMAGE);
//                    chatFileAdapter.setType(ChatFileAdapter.TYPE_IMAGE);
                    chatFileAdapter.notifyDataSetChanged();
                    uploadFile(file.getPath(),false);

                    list_quick_prompt.clear();
                    list_quick_prompt.add(new ChatFileBean("解读图片"));
                    list_quick_prompt.add(new ChatFileBean("提取文字"));
                    chatQuickPromptAdapter.notifyDataSetChanged();
                    actionType = tempActionType;
                }
            } else if (requestCode == REQUEST_LOCAL_FILE && data != null) {
                changeModel(2);
                Uri uri = data.getData();
                // 处理文件
                if (uri != null) {
                    ZUtils.print( "FileContent = "+uri.getPath().toString());
                    ChatFileBean bean = new ChatFileBean("",false);
                    bean.setFileUri(uri);
                    list_file.add(bean);
//                    chatFileAdapter.setType(ChatFileAdapter.TYPE_FILE);
                    setFileRv(list_file,ChatFileAdapter.TYPE_FILE);
                    ll_files.setVisibility(View.VISIBLE);
                    File file = FileUtils.uriToFile(uri,context,bean);
                    chatFileAdapter.notifyDataSetChanged();
                    uploadFile(file.getPath(),true);

                    list_quick_prompt.clear();
                    list_quick_prompt.add(new ChatFileBean("总结内容"));
                    list_quick_prompt.add(new ChatFileBean("生成脑图"));
                    chatQuickPromptAdapter.notifyDataSetChanged();
                    actionType = tempActionType;
                }

//                list_quick_prompt.clear();
//                list_quick_prompt.add(new ChatFileBean("总结内容"));
//                list_quick_prompt.add(new ChatFileBean("生成脑图"));
//                chatQuickPromptAdapter.notifyDataSetChanged();
//                chatFileAdapter.notifyDataSetChanged();
//                chatQuickPromptAdapter.notifyDataSetChanged();
            }
        }
    }

    private void readFile(Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            ZUtils.print( "FileContent = "+content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将 Uri 转换为 File
//    private File uriToFile(Uri uri, Context context) {
//        try {
//            // 获取 ContentResolver
//            InputStream inputStream = context.getContentResolver().openInputStream(uri);
//            if (inputStream == null) return null;
//
//            // 创建临时文件
//            String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
//            File directory = context.getCacheDir(); // 或使用 getFilesDir()
//            File file = new File(directory, fileName);
//
//            // 复制 Uri 内容到 File
//            FileOutputStream outputStream = new FileOutputStream(file);
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
//
//            // 关闭流
//            outputStream.flush();
//            outputStream.close();
//            inputStream.close();
//
//            return file;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
    public void uploadFile(String filePath,boolean isFile){
        isUpload = true;
        chatQuickPromptAdapter.setUpload(isUpload);
//        String filePath = "";
        SessionUpload.upload(context, filePath, new StsCallback() {
            @Override
            public void progress(long percent) {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ChatFileBean chatFileBean =  list_file.get(list_file.size() - 1);

                        chatFileBean.setPercent(chatFileBean.getPercent());
                        chatFileAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void callback(String fileUrl) {
                if (fileUrl != null && !fileUrl.isEmpty()) {
                    ZUtils.print( "文件上传成功: " + fileUrl);
                    isUpload = false;

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ChatFileBean chatFileBean =  list_file.get(list_file.size() - 1);

                            chatFileBean.setPath(fileUrl);
                            if(isFile){
                                chatFileBean.setPercent(98);
                                FileAnalyse(chatFileBean.getFileType(),chatFileBean.getPath());
                            }else{
                                chatFileBean.setPercent(100);
                                chatQuickPromptAdapter.setUpload(isUpload);
                            }
                            chatFileAdapter.notifyDataSetChanged();
                        }
                    });

                } else {
                    ZUtils.print("文件上传失败: 返回URL为空");
                }
            }
        });
    }

    public void FileAnalyse(String fileType,  String fileUrl) {
        isAnalyse = true;
        HttpRequest request = new HttpRequest();
        request.sendFileAnalyseStreams(fileType,fileUrl,new SSECallback() {
            @Override
            public void receive(String responseBodyString) {
                Gson gson = new GsonBuilder().setLenient().create();
                Type type = new TypeToken<SSEFileAnalyseBean>() {
                }.getType();
                SSEFileAnalyseBean res = gson.fromJson(responseBodyString, type);
                if (res.getProgress().equals("100")) {
                   String result = res.getDocumentRecognizeResultUrl();
                    ChatFileBean chatFileBean =  list_file.get(list_file.size() - 1);

                    chatFileBean.setFileAnalyse(result);
                    chatFileBean.setPercent(100);
                    chatQuickPromptAdapter.setUpload(false);
                    chatFileAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void end() {
                isAnalyse = false;
            }
        });
    }


    private void setFileRv(List<ChatFileBean> list,int type) {

        rv_file.setLayoutManager(new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false));


        chatFileAdapter = new ChatFileAdapter(context,list,type, new OnFileItemClick() {
            @Override
            public void onItemClick(int position) {
            }

            @Override
            public void onClose(int position) {

                if (list.size() == 0) {
                    actionType = 0;
                    ll_files.setVisibility(View.GONE);
                    isUpload = false;

                    changeModel(0);
                }
            }
        });

        chatFileAdapter.setShowClose(true);
        rv_file.setAdapter(chatFileAdapter);

    }
    private void setQuickPromptRv(List<ChatFileBean> list) {

        rv_quick_prompt.setLayoutManager(new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false));


        chatQuickPromptAdapter = new ChatFileAdapter(context,list,ChatFileAdapter.TYPE_PROMPT, new OnFileItemClick() {
            @Override
            public void onItemClick(int position) {
                if(isUpload || isAnalyse){
                    return;
                }
//                sendWithFile(list_quick_prompt.get(position).getName());
                sendCommon(list_quick_prompt.get(position).getName());

            }

            @Override
            public void onClose(int position) {

            }
        });
        rv_quick_prompt.setAdapter(chatQuickPromptAdapter);

    }

    private void clearFileInfo() {
        list_file.clear();
        ll_files.setVisibility(View.GONE);
        isUpload = false;

        changeModel(0);

    }


    public void sendCommon(String prompt) {
        if(list_file.size() > 0){
            if(isUpload || isAnalyse){
                GlobalToast.show((Activity) context,"请等待上传完毕",GlobalToast.Type.NORMAL);
                return;
            }
            String content = "";
            if(!TextUtils.isEmpty(prompt.trim())){
                content = prompt;
            }else {
                content = ed.getText().toString();
                if (content.trim().isEmpty()) {
                    ZUtils.showToast("请输入内容");
                    return;
                }
            }
            sendWithFile(content);
        }else{
            sendText(prompt);
        }
    }
    private void sendWithFile(String prompt) {
        if(callback != null){
            actionType = 0;
//            changeModel(1);
            if (selectOptionModel == null) {
                ZUtils.showToast("模型未加载");
                return;
            }
            callback.sendWithFile(prompt,selectOptionModel,list_file,chatFileAdapter.getType() == ChatFileAdapter.TYPE_FILE);
            clearFileInfo();

        }
    }

    String content = "";
    private void sendText(String result) {
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(context);
        if(!isNetworkAvailable){
            GlobalToast.show((Activity) context,"网络错误，请检查网络连接", GlobalToast.Type.ERROR);
            return;
        }

        if (callback != null) {
            if(!TextUtils.isEmpty(result.trim())){
                content = result;
            }else {
                content = ed.getText().toString();
                if (content.trim().isEmpty()) {
                    ZUtils.showToast("请输入内容");
                    return;
                }
            }
            if (selectOptionModel == null) {
                ZUtils.showToast("模型未加载");
                return;
            }

            ed.setText("");
            ZInputMethod.hideKeyboard(context,root_view.getWindowToken());
            //键盘收起期间如果请求网络数据会导致UI 500ms 的卡顿，先执行收起，延时100ms执行网络请求
            root_view.postDelayed(() -> callback.send(content,selectOptionModel),100);

        }
    }
    public void changeModel(int type){
        if(type == 0){
            if(listModelSave != null){
                listModel.clear();
                listModel.addAll(listModelSave);

                selectOptionModel = listModel.get(0); // 默认选中第一个模型
                for (int i = 0; i < listModel.size(); i++) {
                    String model = listModel.get(i).getModel();
                    String selectModel =  GlobalSettings.getInstance().getSelectedModelCode();
                    if(model.equals(selectModel)){
                        selectOptionModel = listModel.get(i); // 设置中的首选大模型
                    }
                }

                Log.d("TAG", "初始化 Model: " + selectOptionModel.getName() + ", ID: " + selectOptionModel.getId());
//                tv_mode.setText(selectOptionModel.getName());
                callback.modeChange(selectOptionModel);
            }
        }else if(type == 1){//图片-默认选混元
            listModel.clear();
            selectOptionModel = new OptionModel();
            selectOptionModel.setId(132);
            selectOptionModel.setName("腾讯混元");
            selectOptionModel.setModel("hunyuan-t1-vision");
            listModel.add(selectOptionModel);
//            tv_mode.setText(selectOptionModel.getName());
            callback.modeChange(selectOptionModel);
        }else if(type == 2){//文档问答，选第一个非移动模型
            if(listModelSave != null){
                listModel.clear();
                listModel.addAll(listModelSave);
                for (int i = 0; i < listModel.size(); i++) {
                    if(!listModel.get(i).getModel().equals("10086")){
                        selectOptionModel = listModel.get(i); // 默认选中第一个模型
                        Log.d("TAG", "初始化 Model: " + selectOptionModel.getName() + ", ID: " + selectOptionModel.getId());
//                        tv_mode.setText(selectOptionModel.getName());
                        callback.modeChange(selectOptionModel);
                        break;
                    }
                }
            }
        }

    }


    public void updateMy(long id,String modelId) {
        HttpRequest request = new HttpRequest();
        request.updateMy(id,modelId, new Observer<ApiResponse<Boolean>>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(ApiResponse<Boolean> res) {
                if (res.getCode() == 0) {

                }
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
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

           String content = ed.getText().toString();
            if (content.isEmpty()) {
                iv_voice.setVisibility(View.VISIBLE);
            }else {
                iv_voice.setVisibility(View.GONE);
            }
            tv_ed_fake.setVisibility(View.VISIBLE);
            ZInputMethod.openInputMethod(ed);
        }else if(mode == 1){

            isVoice = true;
            if (callback != null) {
                callback.voice();
            }
            iv_keyboard.setVisibility(View.VISIBLE);
            tv_press.setVisibility(View.VISIBLE);
            iv_logo.setVisibility(View.VISIBLE);
            iv_voice.setVisibility(View.GONE);
            tv_ed_fake.setVisibility(View.GONE);
            ZInputMethod.hideKeyboard(context,root_view.getWindowToken());
        }
    }


    private void setLayoutGravity(View view, int gravity) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.gravity = gravity;
        view.setLayoutParams(params);
    }



    public void showChooseModelPopup(View view){
        if(banSelectModel){
            return;
        }
        if(listModel == null || listModel.size()==0){
            ZUtils.showToast("模型未加载");
            return;
        }

        ZUtils.showChooseModelPopup(context, view,listModel,selectOptionModel,
                new OptionModelAdapter.OnOptionSelectedListener() {
                    @Override
                    public void onOptionSelected(OptionModel option) {
                        selectOptionModel = option;
                        ZUtils.print("选中模型: " + selectOptionModel.getName() + ", ID: " + selectOptionModel.getId());
//                                tv_mode.setText(selectOptionModel.getName());
                        callback.modeChange(selectOptionModel);
                        if(conversationId != 0){
                            updateMy(conversationId,option.getId()+"");
                        }
                    }

                });
    }



}

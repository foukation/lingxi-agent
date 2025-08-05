package com.fxzs.lingxiagent.view.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.model.chat.dto.ChatFileBean;
import com.fxzs.lingxiagent.model.chat.dto.DrawingToChatBean;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingStyleDto;
import com.fxzs.lingxiagent.network.ZNet.bean.getCatDetailListBean;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.viewmodel.chat.VMChat;
import com.fxzs.lingxiagent.R;

import java.util.List;


public class SuperChatContainActivity extends BaseActivity {
    public static final int TYPE_HOME = 1;//首页
    public static final int TYPE_AGENT = 2;//智能体
    public static final int TYPE_DRAWING = 3;//绘画-对话界面

    private int type;//跳转类型
    SuperChatFragment superChatFragment;


    @Override
    protected int getLayoutResource() {
        return R.layout.act_super_chat_container;
    }

    @Override
    protected Class getViewModelClass() {
        return VMChat.class;
    }

    @Override
    protected void setupDataBinding() {

    }

    @Override
    protected void initializeViews() {
        init();
    }



    public void init() {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.act_super_chat);

        findViewById(R.id.back).setOnClickListener(view -> finish());


        if (getIntent() != null) {
            type = getIntent().getIntExtra(Constant.INTENT_TYPE, SuperChatContainActivity.TYPE_HOME);
            if (type == TYPE_HOME) {//首页
                findViewById(R.id.back).setVisibility(View.GONE);
                long id = getIntent().getLongExtra(Constant.INTENT_ID, 0);

                String input = getIntent().getStringExtra(Constant.INTENT_DATA);
                OptionModel selectOptionModel = (OptionModel) getIntent().getSerializableExtra(Constant.INTENT_DATA1);
                if (id == 0) {
                    List<ChatFileBean> fileList = (List<ChatFileBean>) getIntent().getSerializableExtra(Constant.INTENT_DATA2);
                    superChatFragment = new SuperChatFragment(type,input,selectOptionModel,fileList);
                } else {
                    superChatFragment = new SuperChatFragment(type,input,id,selectOptionModel);
                }
            } else if (type == TYPE_AGENT) {//智能体
                getCatDetailListBean bean = (getCatDetailListBean) getIntent().getSerializableExtra(Constant.INTENT_DATA2);
                long id = getIntent().getLongExtra(Constant.INTENT_ID, 0);
                superChatFragment = new SuperChatFragment(type,id,bean);
            } else if (type == TYPE_DRAWING) {//绘画
                DrawingToChatBean bean = (DrawingToChatBean) getIntent().getSerializableExtra(Constant.INTENT_DATA);
                DrawingStyleDto styleDto = (DrawingStyleDto) getIntent().getSerializableExtra(Constant.INTENT_DATA1);
                superChatFragment = new SuperChatFragment(type,bean,styleDto);
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragment_container, superChatFragment);
            transaction.commit();

        }
    }
    @Override
    protected void setupObservers() {
        // 监听 ViewModel 的 LiveData

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        android.util.Log.d("SuperChatContainActivity", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);
        if(superChatFragment != null){
            superChatFragment.onActivityResult( requestCode,  resultCode,  data);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}

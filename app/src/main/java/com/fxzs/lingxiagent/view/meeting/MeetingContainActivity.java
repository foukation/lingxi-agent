package com.fxzs.lingxiagent.view.meeting;

import android.content.Intent;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.chat.dto.ChatFileBean;
import com.fxzs.lingxiagent.model.chat.dto.DrawingToChatBean;
import com.fxzs.lingxiagent.model.chat.dto.OptionModel;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingStyleDto;
import com.fxzs.lingxiagent.network.ZNet.bean.getCatDetailListBean;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.view.chat.SuperChatFragment;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.viewmodel.chat.VMChat;

import java.util.List;


public class MeetingContainActivity extends BaseActivity {

    MeetingFragment meetingFragment;


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

        findViewById(R.id.back).setOnClickListener(view -> finish());



            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            meetingFragment = new MeetingFragment();
            transaction.add(R.id.fragment_container, meetingFragment);
            transaction.commit();

    }
    @Override
    protected void setupObservers() {
        // 监听 ViewModel 的 LiveData

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        android.util.Log.d("SuperChatContainActivity", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);
        if(meetingFragment != null){
            meetingFragment.onActivityResult( requestCode,  resultCode,  data);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}

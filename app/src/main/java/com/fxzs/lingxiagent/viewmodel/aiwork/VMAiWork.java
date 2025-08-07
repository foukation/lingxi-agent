package com.fxzs.lingxiagent.viewmodel.aiwork;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.fxzs.lingxiagent.model.common.BaseViewModel;
import com.fxzs.lingxiagent.model.drawing.repository.DrawingRepositoryImpl;
import com.fxzs.lingxiagent.network.ZNet.HttpRequest;
import com.fxzs.lingxiagent.view.aiwork.AiWorkAdapter;
import com.fxzs.lingxiagent.viewmodel.history.HistoryViewModelFactory;
import com.fxzs.lingxiagent.viewmodel.history.VMHistory;

public class VMAiWork extends BaseViewModel {

    private VMHistory vmHistory;
    Activity mActivity;

    public VMAiWork(@NonNull Application application) {
        super(application);
//        request = new HttpRequest();
//        repository = DrawingRepositoryImpl.getInstance();

    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理资源
    }

    public VMHistory getVmHistory() {
        return vmHistory;
    }

    /**
     * 加载历史记录
     */
    public void loadHistory(int tabIndex/*,boolean isRefresh*/) {
//        Integer tabIndex = currentTabIndex.getValue();
//        if (tabIndex == null) return;

//        if (isRefresh) {
//            isRefreshing.setValue(true);
//        } else {
//            setLoading(true);
//        }

//        android.util.Log.d("VMHistory", "loadHistory for tab: " + tabIndex + ", isRefresh: " + isRefresh);

        switch (tabIndex) {
            case AiWorkAdapter.TYPE_DRAWING:
                android.util.Log.d("VMHistory", "加载绘画历史");
                vmHistory.loadDrawingHistory(true);
                vmHistory.getCurrentTabIndex().setValue(VMHistory.TAB_DRAWING);
                break;
            case AiWorkAdapter.TYPE_MEETING:
                android.util.Log.d("VMHistory", "加载会议历史");
                vmHistory.loadMeetingHistory(true);
                vmHistory.getCurrentTabIndex().setValue(VMHistory.TAB_MEETING);
                break;
            case AiWorkAdapter.TYPE_PPT:
            case AiWorkAdapter.TYPE_TRANSLATE:
                android.util.Log.d("VMHistory", "加载PPT/翻译历史（暂未实现）");
                // TODO: 实现PPT和翻译历史记录
//                handleEmptyResult(isRefresh);
                break;
            default:
                android.util.Log.w("VMHistory", "未知的Tab类型: " + tabIndex);
//                handleEmptyResult(isRefresh);
                break;
        }
    }

    public void setActivity(Activity activity) {
        mActivity = activity;

        HistoryViewModelFactory factory = new HistoryViewModelFactory();
        vmHistory = new ViewModelProvider((ViewModelStoreOwner) activity,factory).get(VMHistory.class);
    }
}
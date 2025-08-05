package com.fxzs.lingxiagent.view.drawing;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.network.ZNet.ApiResponse;
import com.fxzs.lingxiagent.network.ZNet.HttpRequest;
import com.fxzs.lingxiagent.network.ZNet.bean.GetImageCat;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.agent.HomeViewPagerAdapter;
import com.fxzs.lingxiagent.view.common.BaseFragment;
import com.fxzs.lingxiagent.viewmodel.agent.VMAgent;
import com.fxzs.lingxiagent.view.chat.HistoryBottomSheetFragment;
import com.fxzs.lingxiagent.viewmodel.history.VMHistory;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class DrawingNewFragment extends BaseFragment<VMAgent> {
    
    private TextView tvAgentTitle;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private HomeViewPagerAdapter adapter;

    List<GetImageCat> list = new ArrayList<>();
    private LinearLayout recordingInputContainer;
    
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_drawing_new;
    }
    
    @Override
    protected Class<VMAgent> getViewModelClass() {
        return VMAgent.class;
    }
    
    @Override
    protected void initializeViews(View view) {
//        tvAgentTitle = findViewById(R.id.tv_agent_title);

        recordingInputContainer = findViewById(R.id.recording_input_container);
        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

//        setupTabs();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(),false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 可以在这里处理未选中的Tab
                int position = tab.getPosition();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        requestData();
        setupRecordingInput();

        findViewById(R.id.iv_history).setOnClickListener(v -> {
            android.util.Log.d("ChatFragment", "History button clicked!");
            showHistoryBottomSheet();
        });
    }
    
    @Override
    protected void setupDataBinding() {
        // 设置数据绑定
    }
    
    @Override
    protected void setupObservers() {
        // 观察数据变化
    }




    private void requestData() {
        HttpRequest request = new HttpRequest();
        request.getImageCatList(new Observer<ApiResponse<List<GetImageCat>>>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ApiResponse<List<GetImageCat>> res) {

                if (res.getCode() == 0) {
                    List<GetImageCat> listApiResponse =  res.getData();
                    list.addAll(listApiResponse);
                    setUI();
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


    private void setUI() {
        if (list == null || list.size() == 0){
            return;
        }
        List<Fragment> fragments = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            GetImageCat plate = list.get(i);
            String data = plate.getName();
            fragments.add(new DrawingSubFragment(plate.getId()));

        }

        adapter = new HomeViewPagerAdapter(getActivity(), fragments);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
        viewPager.setOffscreenPageLimit(adapter.getItemCount());

        setupTabs();
    }
    private void setupTabs() {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            TabLayout.Tab tab = tabLayout.newTab();

            tab.view.setLongClickable(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tab.view.setTooltipText("");
                // 遍历子视图，禁用 tooltip
                ZUtils.disableTooltipForChildViews(tab.view);
            }
            tab.view.setOnLongClickListener(null);
            GetImageCat bean = list.get(i);
            String text = bean.getName();
            tab.setText(text);
            tabLayout.addTab(tab);
        }
    }

    // 设置录音输入
    private void setupRecordingInput() {
        recordingInputContainer.setOnClickListener(v -> {
            // 跳转到绘画聊天界面
            Intent intent = new Intent(getActivity(), DrawingActivity.class);
            startActivity(intent);
        });
    }


    /**
     * 显示历史记录底部抽屉，默认选中绘画历史
     */
    private void showHistoryBottomSheet() {
        android.util.Log.d("DrawingFragment", "showHistoryBottomSheet called");
        try {
            HistoryBottomSheetFragment bottomSheet = HistoryBottomSheetFragment.newInstance( VMHistory.TAB_DRAWING);
            // 传递绘画tab索引，默认选中绘画历史
            bottomSheet.show(getChildFragmentManager(), "HistoryBottomSheet");
            android.util.Log.d("DrawingFragment", "BottomSheet shown successfully with drawing tab selected");
        } catch (Exception e) {
            android.util.Log.e("DrawingFragment", "Error showing bottom sheet", e);
        }
    }
}
package com.fxzs.lingxiagent.view.agent;

import android.os.Build;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.network.ZNet.ApiResponse;
import com.fxzs.lingxiagent.network.ZNet.HttpRequest;
import com.fxzs.lingxiagent.network.ZNet.bean.GetMenuBean;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.common.BaseFragment;
import com.fxzs.lingxiagent.viewmodel.agent.VMAgent;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class AgentFragment extends BaseFragment<VMAgent> {
    
    private TextView tvAgentTitle;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private HomeViewPagerAdapter adapter;

//    List<HashMap<Integer,String>> fakeData = new ArrayList<>();
    List<GetMenuBean> list = new ArrayList<>();
    
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_agent;
    }
    
    @Override
    protected Class<VMAgent> getViewModelClass() {
        return VMAgent.class;
    }
    
    @Override
    protected void initializeViews(View view) {
//        tvAgentTitle = findViewById(R.id.tv_agent_title);

        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.view_pager);
//        fakeData.add(new HashMap() {{
//            put(-1,"出行规划");
//        }});
//        fakeData.add(new HashMap() {{
//            put(-1,"同城聚餐");
//        }});
//        fakeData.add(new HashMap() {{
//            put(9,"办公");
//        }});
//        fakeData.add(new HashMap() {{
//            put(5,"情感助手");
//        }});
//        fakeData.add(new HashMap() {{
//            put(6,"历史人物");
//        }});
//        fakeData.add(new HashMap() {{
//            put(7,"游戏IP");
//        }});
//        fakeData.add(new HashMap() {{
//            put(8,"行业专家");
//        }});
//        fakeData.add(new HashMap() {{
//            put(10,"宠物");
//        }});
//        fakeData.add(new HashMap() {{
//            put(11,"学习教育");
//        }});
//        fakeData.add(new HashMap() {{
//            put(12,"日常生活");
//        }});
//        fakeData.add(new HashMap() {{
//            put(13,"情感");
//        }});
//
//        List<Fragment> fragments = new ArrayList<>();
//
//
//        for (int i = 0; i < fakeData.size(); i++) {
//            HashMap<Integer, String> map = fakeData.get(i);
//            int key = map.keySet().iterator().next();
//            if(key == -1){
//                YiDongFragment fragment = new YiDongFragment();
//                fragments.add(fragment);
//            }else {
//                String value = map.get(key);
//                ZNSubFragment fragment = new ZNSubFragment(key);
//                fragments.add(fragment);}
//        }


//        adapter = new HomeViewPagerAdapter(requireActivity(), fragments);
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
        request.getMenuList(new Observer<ApiResponse<List<GetMenuBean>>>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ApiResponse<List<GetMenuBean>> res) {

                if (res.getCode() == 0) {
                    List<GetMenuBean> listApiResponse =  res.getData();
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
            GetMenuBean plate = list.get(i);
            String data = plate.getName();
            fragments.add(new ZNSubFragment(plate.getId()));

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
            GetMenuBean bean = list.get(i);
            String text = bean.getName();
            tab.setText(text);
            tab.view.setLongClickable(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tab.view.setTooltipText("");
                // 遍历子视图，禁用 tooltip
                ZUtils.disableTooltipForChildViews(tab.view);
            }
            tab.view.setOnLongClickListener(null);
            tabLayout.addTab(tab);
        }
    }

}
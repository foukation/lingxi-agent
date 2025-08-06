package com.fxzs.lingxiagent.view.aiwork;

import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.aiwork.AiWorkFilterBean;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.common.BaseFragment;
import com.fxzs.lingxiagent.view.drawing.DrawingContainActivity;
import com.fxzs.lingxiagent.view.meeting.MeetingContainActivity;
import com.fxzs.lingxiagent.view.user.HistoryItem;
import com.fxzs.lingxiagent.viewmodel.aiwork.VMAiWork;

import java.util.ArrayList;
import java.util.List;

public class AiWorkFragment extends BaseFragment<VMAiWork> {
    private View iv_filter;
    private RecyclerView rv;
    AiWorkAdapter adapter;
    private View ll_empty;

    @Override
    protected int getLayoutResource() {
        return R.layout.ai_work;
    }

    @Override
    protected Class<VMAiWork> getViewModelClass() {
        return VMAiWork.class;
    }

    @Override
    protected void initializeViews(View view) {

        iv_filter = view.findViewById(R.id.iv_filter);
        rv = view.findViewById(R.id.rv);
        ll_empty = view.findViewById(R.id.ll_empty);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        List<HistoryItem> list = new ArrayList<>();
//        list.add(new )
        adapter = new AiWorkAdapter(getActivity(),list);
        rv.setAdapter(adapter);


        iv_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZUtils.showAIWorkFilterPopup(getActivity(), view, null, new AiWorkFilterAdapter.OnOptionSelectedListener() {
                    @Override
                    public void onOptionSelected(AiWorkFilterBean option) {

                        viewModel.loadHistory(option.getType());
                        adapter.setType(option.getType());
                    }
                });
            }
        });

        viewModel.setActivity(requireActivity());
        viewModel.loadHistory(AiWorkAdapter.TYPE_DRAWING);

        findViewById(R.id.ll_meeting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MeetingContainActivity.class));
            }
        });

        findViewById(R.id.ll_ppt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), MeetingContainActivity.class));
            }
        });

        findViewById(R.id.ll_drawing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DrawingContainActivity.class));
            }
        });

        findViewById(R.id.ll_translate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), MeetingContainActivity.class));
            }
        });
    }

    @Override
    protected void setupDataBinding() {

    }

    @Override
    protected void setupObservers() {

        viewModel.getVmHistory().getHistoryItems().observe(getViewLifecycleOwner(), items -> {
            if(items.size() > 0){
                ll_empty.setVisibility(View.GONE);
            }else {
                ll_empty.setVisibility(View.VISIBLE);
            }
            if (adapter != null) {
                adapter.setItems(items);
            }
        });
    }
}

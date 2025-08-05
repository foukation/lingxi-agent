package com.fxzs.lingxiagent.view.ppt;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.viewmodel.ppt.VMPptOutlineEdit;

import java.util.ArrayList;
import java.util.List;

public class PptOutlineEditActivity extends BaseActivity<VMPptOutlineEdit> {
    
    private ImageButton backButton;
    private RecyclerView outlineRecyclerView;
    private Button changeOutlineButton;
    private Button selectTemplateButton;
    private TextView generatingText;
    private ImageButton stopGeneratingButton;
    
    private OutlineAdapter outlineAdapter;
    private String pptId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pptId = getIntent().getStringExtra("ppt_id");
        setupRecyclerView();
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_ppt_outline_edit;
    }
    
    @Override
    protected Class<VMPptOutlineEdit> getViewModelClass() {
        return VMPptOutlineEdit.class;
    }
    
    @Override
    protected void initializeViews() {
        backButton = findViewById(R.id.back_button);
        outlineRecyclerView = findViewById(R.id.outline_recycler_view);
        changeOutlineButton = findViewById(R.id.change_outline_button);
        selectTemplateButton = findViewById(R.id.select_template_button);
        generatingText = findViewById(R.id.generating_text);
        stopGeneratingButton = findViewById(R.id.stop_generating_button);
        
        backButton.setOnClickListener(v -> finish());
        changeOutlineButton.setOnClickListener(v -> viewModel.regenerateOutline());
        selectTemplateButton.setOnClickListener(v -> navigateToTemplateSelection());
        stopGeneratingButton.setOnClickListener(v -> viewModel.stopGenerating());
    }
    
    @Override
    protected void setupDataBinding() {
        viewModel.getIsGenerating().observeForever(isGenerating -> {
            generatingText.setVisibility(isGenerating ? View.VISIBLE : View.GONE);
            stopGeneratingButton.setVisibility(isGenerating ? View.VISIBLE : View.GONE);
        });
    }
    
    @Override
    protected void setupObservers() {
        viewModel.getOutlineItems().observeForever(items -> {
            if (outlineAdapter != null) {
                outlineAdapter.setItems(items);
            }
        });
    }
    
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        outlineRecyclerView.setLayoutManager(layoutManager);
        
        outlineAdapter = new OutlineAdapter();
        outlineAdapter.setOnItemEditListener((item, position) -> {
            viewModel.updateOutlineItem(position, item);
        });
        outlineRecyclerView.setAdapter(outlineAdapter);
        
        viewModel.loadOutline(pptId);
    }
    
    private void navigateToTemplateSelection() {
        Intent intent = new Intent(this, PptTemplateSelectionActivity.class);
        intent.putExtra("ppt_id", pptId);
        startActivity(intent);
    }
    
    private static class OutlineAdapter extends RecyclerView.Adapter<OutlineAdapter.ViewHolder> {
        private List<OutlineItem> items = new ArrayList<>();
        private OnItemEditListener listener;
        
        interface OnItemEditListener {
            void onItemEdit(OutlineItem item, int position);
        }
        
        void setOnItemEditListener(OnItemEditListener listener) {
            this.listener = listener;
        }
        
        void setItems(List<OutlineItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ppt_outline, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OutlineItem item = items.get(position);
            holder.bind(item, position + 1);
            
            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemEdit(item, position);
                }
            });
            
            holder.contentEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && listener != null) {
                    item.setContent(holder.contentEditText.getText().toString());
                    listener.onItemEdit(item, position);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView numberText;
            TextView titleText;
            LinearLayout contentContainer;
            TextView contentText;
            EditText contentEditText;
            ImageButton editButton;
            ImageButton expandButton;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                numberText = itemView.findViewById(R.id.number_text);
                titleText = itemView.findViewById(R.id.title_text);
                contentContainer = itemView.findViewById(R.id.content_container);
                contentText = itemView.findViewById(R.id.content_text);
                contentEditText = itemView.findViewById(R.id.content_edit_text);
                editButton = itemView.findViewById(R.id.edit_button);
                expandButton = itemView.findViewById(R.id.expand_button);
            }
            
            void bind(OutlineItem item, int number) {
                numberText.setText(String.valueOf(number));
                titleText.setText(item.getTitle());
                
                if (item.getContent() != null && !item.getContent().isEmpty()) {
                    contentContainer.setVisibility(View.VISIBLE);
                    
                    if (item.isEditing()) {
                        contentText.setVisibility(View.GONE);
                        contentEditText.setVisibility(View.VISIBLE);
                        contentEditText.setText(item.getContent());
                        contentEditText.requestFocus();
                    } else {
                        contentText.setVisibility(View.VISIBLE);
                        contentEditText.setVisibility(View.GONE);
                        contentText.setText(item.getContent());
                    }
                } else {
                    contentContainer.setVisibility(View.GONE);
                }
                
                expandButton.setRotation(item.isExpanded() ? 180 : 0);
                expandButton.setOnClickListener(v -> {
                    item.setExpanded(!item.isExpanded());
                    bind(item, number);
                });
                
                editButton.setOnClickListener(v -> {
                    item.setEditing(!item.isEditing());
                    bind(item, number);
                });
            }
        }
    }
    
    public static class OutlineItem {
        private String title;
        private String content;
        private boolean expanded = true;
        private boolean editing = false;
        
        public OutlineItem(String title, String content) {
            this.title = title;
            this.content = content;
        }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public boolean isExpanded() { return expanded; }
        public void setExpanded(boolean expanded) { this.expanded = expanded; }
        public boolean isEditing() { return editing; }
        public void setEditing(boolean editing) { this.editing = editing; }
    }
}
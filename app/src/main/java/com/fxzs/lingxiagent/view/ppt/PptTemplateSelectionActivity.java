package com.fxzs.lingxiagent.view.ppt;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.viewmodel.ppt.VMPptTemplateSelection;

import java.util.ArrayList;
import java.util.List;

public class PptTemplateSelectionActivity extends BaseActivity<VMPptTemplateSelection> {
    
    private ImageButton backButton;
    private ImageButton closeButton;
    private View colorSelector;
    private View styleSelector;
    private TextView styleSelectorText;
    private RecyclerView templateRecyclerView;
    private Button refreshTemplatesButton;
    private Button generatePptButton;
    
    private TemplateAdapter templateAdapter;
    private String selectedTopic;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedTopic = getIntent().getStringExtra("topic");
        setupRecyclerView();
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_ppt_template_selection;
    }
    
    @Override
    protected Class<VMPptTemplateSelection> getViewModelClass() {
        return VMPptTemplateSelection.class;
    }
    
    @Override
    protected void initializeViews() {
        backButton = findViewById(R.id.back_button);
        closeButton = findViewById(R.id.close_button);
        colorSelector = findViewById(R.id.color_selector);
        // colorSelectorText = findViewById(R.id.color_selector_text); // 这个视图不存在
        styleSelector = findViewById(R.id.style_selector);
        styleSelectorText = findViewById(R.id.style_selector_text);
        templateRecyclerView = findViewById(R.id.template_recycler_view);
        refreshTemplatesButton = findViewById(R.id.refresh_templates_button);
        generatePptButton = findViewById(R.id.generate_ppt_button);
        
        backButton.setOnClickListener(v -> finish());
        closeButton.setOnClickListener(v -> finish());
        
        colorSelector.setOnClickListener(v -> showColorPicker());
        styleSelector.setOnClickListener(v -> showStylePicker());
        
        refreshTemplatesButton.setOnClickListener(v -> viewModel.refreshTemplates());
        generatePptButton.setOnClickListener(v -> generatePpt());
    }
    
    @Override
    protected void setupDataBinding() {
        viewModel.getSelectedStyle().observeForever(style -> 
            styleSelectorText.setText(style)
        );
        
        viewModel.getGenerateButtonEnabled().observeForever(enabled -> {
            generatePptButton.setEnabled(enabled);
            generatePptButton.setAlpha(enabled ? 1.0f : 0.5f);
        });
    }
    
    @Override
    protected void setupObservers() {
        viewModel.getTemplateList().observeForever(templates -> {
            if (templateAdapter != null) {
                templateAdapter.setTemplates(templates);
            }
        });
        
        viewModel.getSelectedTemplateId().observeForever(id -> {
            if (templateAdapter != null) {
                templateAdapter.setSelectedTemplateId(id);
            }
        });
    }
    
    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        templateRecyclerView.setLayoutManager(layoutManager);
        
        templateAdapter = new TemplateAdapter();
        templateAdapter.setOnTemplateClickListener(template -> 
            viewModel.selectTemplate(template.getId())
        );
        templateRecyclerView.setAdapter(templateAdapter);
        
        viewModel.loadTemplates();
    }
    
    private void showColorPicker() {
        ColorPickerDialog dialog = new ColorPickerDialog();
        dialog.setOnColorSelectedListener(color -> {
            viewModel.setSelectedColor(color);
            updateColorSelectorUI(color);
        });
        dialog.show(getSupportFragmentManager(), "color_picker");
    }
    
    private void showStylePicker() {
        PptStyleSelectionDialog dialog = new PptStyleSelectionDialog();
        dialog.setCurrentStyle(viewModel.getSelectedStyle().get());
        dialog.setOnStyleSelectedListener(style -> {
            viewModel.setSelectedStyle(style);
        });
        dialog.show(getSupportFragmentManager(), "style_picker");
    }
    
    private void updateColorSelectorUI(String color) {
        // Update color selector UI based on selected color
    }
    
    private void generatePpt() {
        PptGenerationProgressDialog progressDialog = new PptGenerationProgressDialog();
        progressDialog.show(getSupportFragmentManager(), "progress");
        
        viewModel.generatePpt(selectedTopic).observeForever(result -> {
            progressDialog.dismiss();
            if (result != null && result.isSuccess()) {
                Intent intent = new Intent(this, PptPreviewActivity.class);
                intent.putExtra("ppt_id", result.getPptId());
                startActivity(intent);
                finish();
            }
        });
    }
    
    private static class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {
        private List<PptTemplate> templates = new ArrayList<>();
        private String selectedTemplateId;
        private OnTemplateClickListener listener;
        
        interface OnTemplateClickListener {
            void onTemplateClick(PptTemplate template);
        }
        
        void setOnTemplateClickListener(OnTemplateClickListener listener) {
            this.listener = listener;
        }
        
        void setTemplates(List<PptTemplate> templates) {
            this.templates = templates;
            notifyDataSetChanged();
        }
        
        void setSelectedTemplateId(String id) {
            String oldId = selectedTemplateId;
            selectedTemplateId = id;
            
            int oldPos = findPositionById(oldId);
            int newPos = findPositionById(id);
            
            if (oldPos >= 0) notifyItemChanged(oldPos);
            if (newPos >= 0) notifyItemChanged(newPos);
        }
        
        private int findPositionById(String id) {
            if (id == null) return -1;
            for (int i = 0; i < templates.size(); i++) {
                if (id.equals(templates.get(i).getId())) {
                    return i;
                }
            }
            return -1;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ppt_template, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PptTemplate template = templates.get(position);
            holder.bind(template, template.getId().equals(selectedTemplateId));
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTemplateClick(template);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return templates.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            ImageView templateImage;
            TextView templateName;
            ImageView checkMark;
            View selectionOverlay;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_view);
                templateImage = itemView.findViewById(R.id.template_image);
                templateName = itemView.findViewById(R.id.template_name);
                checkMark = itemView.findViewById(R.id.check_mark);
                selectionOverlay = itemView.findViewById(R.id.selection_overlay);
            }
            
            void bind(PptTemplate template, boolean isSelected) {
                templateName.setText(template.getName());
                checkMark.setVisibility(isSelected ? View.VISIBLE : View.GONE);
                selectionOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
                
                if (isSelected) {
                    cardView.setCardElevation(8f);
                } else {
                    cardView.setCardElevation(2f);
                }
            }
        }
    }
    
    public static class PptTemplate {
        private String id;
        private String name;
        private String thumbnailUrl;
        
        public PptTemplate(String id, String name, String thumbnailUrl) {
            this.id = id;
            this.name = name;
            this.thumbnailUrl = thumbnailUrl;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getThumbnailUrl() { return thumbnailUrl; }
    }
}
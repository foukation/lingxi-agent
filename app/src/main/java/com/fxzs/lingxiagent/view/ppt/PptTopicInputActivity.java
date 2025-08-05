package com.fxzs.lingxiagent.view.ppt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.viewmodel.ppt.VMPptTopicInput;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class PptTopicInputActivity extends BaseActivity<VMPptTopicInput> {
    
    private ImageButton backButton;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private EditText topicInputEditText;
    private TextView charCountTextView;
    private ChipGroup suggestionChipGroup;
    private TextView efficiencyTipTextView;
    private ImageButton sendButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_ppt_topic_input;
    }
    
    @Override
    protected Class<VMPptTopicInput> getViewModelClass() {
        return VMPptTopicInput.class;
    }
    
    @Override
    protected void initializeViews() {
        backButton = findViewById(R.id.back_button);
        titleTextView = findViewById(R.id.title_text);
        subtitleTextView = findViewById(R.id.subtitle_text);
        topicInputEditText = findViewById(R.id.topic_input_edit_text);
        charCountTextView = findViewById(R.id.char_count_text);
        suggestionChipGroup = findViewById(R.id.suggestion_chip_group);
        efficiencyTipTextView = findViewById(R.id.efficiency_tip_text);
        sendButton = findViewById(R.id.send_button);
        
        backButton.setOnClickListener(v -> finish());
        sendButton.setOnClickListener(v -> submitTopic());
    }
    
    @Override
    protected void setupDataBinding() {
        topicInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.getTopicText().set(s.toString());
                updateCharCount(s.length());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    @Override
    protected void setupObservers() {
        viewModel.getSendButtonEnabled().observeForever(enabled -> {
            sendButton.setEnabled(enabled);
            sendButton.setAlpha(enabled ? 1.0f : 0.5f);
        });
    }
    
    private void setupUI() {
        setupTitleText();
        setupSuggestionChips();
        setupEfficiencyTip();
        updateCharCount(0);
    }
    
    private void setupTitleText() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        
        String ai = "AI";
        SpannableString aiSpan = new SpannableString(ai);
        aiSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), 0, ai.length(), 0);
        builder.append(aiSpan);
        
        String generate = "生成";
        SpannableString generateSpan = new SpannableString(generate);
        generateSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#E91E63")), 0, generate.length(), 0);
        builder.append(generateSpan);
        
        String ppt = "PPT";
        SpannableString pptSpan = new SpannableString(ppt);
        int[] colors = new int[]{Color.parseColor("#9C27B0"), Color.parseColor("#673AB7"), Color.parseColor("#3F51B5")};
        GradientColorSpan gradientSpan = new GradientColorSpan(colors);
        pptSpan.setSpan(gradientSpan, 0, ppt.length(), 0);
        builder.append(pptSpan);
        
        titleTextView.setText(builder);
    }
    
    private void setupSuggestionChips() {
        String[] suggestions = {
            "汽车销售活动运营及策划方案",
            "新人入职培训管理方案",
            "工程项目进度工作总结汇报",
            "共享自行车可行性的研究"
        };
        
        for (String suggestion : suggestions) {
            Chip chip = new Chip(this);
            chip.setText(suggestion);
            chip.setChipBackgroundColorResource(R.color.chip_background);
            chip.setTextColor(ContextCompat.getColor(this, R.color.chip_text));
            chip.setChipStrokeWidth(0);
            chip.setOnClickListener(v -> {
                topicInputEditText.setText(suggestion);
                topicInputEditText.setSelection(suggestion.length());
            });
            suggestionChipGroup.addView(chip);
        }
    }
    
    private void setupEfficiencyTip() {
        String tipText = "个人效率提升：掌握时间管理的秘籍";
        efficiencyTipTextView.setText(tipText);
    }
    
    private void updateCharCount(int count) {
        charCountTextView.setText(String.format("%d/40", count));
        charCountTextView.setTextColor(count > 40 ? Color.RED : Color.GRAY);
    }
    
    private void submitTopic() {
        String topic = topicInputEditText.getText().toString().trim();
        if (!topic.isEmpty() && topic.length() <= 40) {
            Intent intent = new Intent(this, PptTemplateSelectionActivity.class);
            intent.putExtra("topic", topic);
            startActivity(intent);
        }
    }
    
    private static class GradientColorSpan extends ForegroundColorSpan {
        private final int[] colors;
        
        public GradientColorSpan(int[] colors) {
            super(colors[0]);
            this.colors = colors;
        }
    }
}
package com.fxzs.lingxiagent.view.ppt;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.view.common.BaseActivity;
import com.fxzs.lingxiagent.viewmodel.ppt.VMPptPreview;

import java.util.ArrayList;
import java.util.List;

public class PptPreviewActivity extends BaseActivity<VMPptPreview> {
    
    private ImageButton backButton;
    private TextView titleText;
    private ViewPager2 slideViewPager;
    private LinearLayout pageIndicator;
    
    private SlideAdapter slideAdapter;
    private String pptId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pptId = getIntent().getStringExtra("ppt_id");
        setupViewPager();
    }
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_ppt_preview;
    }
    
    @Override
    protected Class<VMPptPreview> getViewModelClass() {
        return VMPptPreview.class;
    }
    
    @Override
    protected void initializeViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        slideViewPager = findViewById(R.id.slide_view_pager);
        pageIndicator = findViewById(R.id.page_indicator);
        
        backButton.setOnClickListener(v -> finish());
        
        findViewById(R.id.edit_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, PptOutlineEditActivity.class);
            intent.putExtra("ppt_id", pptId);
            startActivity(intent);
        });
    }
    
    @Override
    protected void setupDataBinding() {
        viewModel.getPptTitle().observeForever(title -> 
            titleText.setText(title)
        );
    }
    
    @Override
    protected void setupObservers() {
        viewModel.getSlides().observeForever(slides -> {
            if (slideAdapter != null) {
                slideAdapter.setSlides(slides);
                setupPageIndicator(slides.size());
            }
        });
    }
    
    private void setupViewPager() {
        slideAdapter = new SlideAdapter();
        slideViewPager.setAdapter(slideAdapter);
        
        slideViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updatePageIndicator(position);
            }
        });
        
        viewModel.loadPptData(pptId);
    }
    
    private void setupPageIndicator(int pageCount) {
        pageIndicator.removeAllViews();
        
        for (int i = 0; i < pageCount; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(8, 8);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.page_indicator_dot);
            pageIndicator.addView(dot);
        }
        
        if (pageCount > 0) {
            updatePageIndicator(0);
        }
    }
    
    private void updatePageIndicator(int position) {
        for (int i = 0; i < pageIndicator.getChildCount(); i++) {
            View dot = pageIndicator.getChildAt(i);
            dot.setSelected(i == position);
        }
    }
    
    private static class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {
        private List<PptSlide> slides = new ArrayList<>();
        
        void setSlides(List<PptSlide> slides) {
            this.slides = slides;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ppt_slide, parent, false);
            return new SlideViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
            holder.bind(slides.get(position));
        }
        
        @Override
        public int getItemCount() {
            return slides.size();
        }
        
        static class SlideViewHolder extends RecyclerView.ViewHolder {
            ImageView slideImage;
            TextView slideTitle;
            TextView slideContent;
            LinearLayout chartContainer;
            
            SlideViewHolder(@NonNull View itemView) {
                super(itemView);
                slideImage = itemView.findViewById(R.id.slide_image);
                slideTitle = itemView.findViewById(R.id.slide_title);
                slideContent = itemView.findViewById(R.id.slide_content);
                chartContainer = itemView.findViewById(R.id.chart_container);
            }
            
            void bind(PptSlide slide) {
                slideTitle.setText(slide.getTitle());
                
                if (slide.getContent() != null) {
                    slideContent.setVisibility(View.VISIBLE);
                    slideContent.setText(slide.getContent());
                } else {
                    slideContent.setVisibility(View.GONE);
                }
                
                if (slide.hasChart()) {
                    chartContainer.setVisibility(View.VISIBLE);
                    // Setup chart view
                } else {
                    chartContainer.setVisibility(View.GONE);
                }
                
                // Set background based on slide type
                switch (slide.getType()) {
                    case COVER:
                        itemView.setBackgroundResource(R.drawable.bg_slide_cover);
                        break;
                    case SECTION:
                        itemView.setBackgroundResource(R.drawable.bg_slide_section);
                        break;
                    case CONTENT:
                        itemView.setBackgroundResource(R.drawable.bg_slide_content);
                        break;
                }
            }
        }
    }
    
    public static class PptSlide {
        public enum SlideType {
            COVER, SECTION, CONTENT
        }
        
        private String title;
        private String content;
        private SlideType type;
        private boolean hasChart;
        
        public PptSlide(String title, String content, SlideType type, boolean hasChart) {
            this.title = title;
            this.content = content;
            this.type = type;
            this.hasChart = hasChart;
        }
        
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public SlideType getType() { return type; }
        public boolean hasChart() { return hasChart; }
    }
}
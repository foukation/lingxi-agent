package com.fxzs.lingxiagent.view.ppt;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxzs.lingxiagent.R;

import java.util.ArrayList;
import java.util.List;

public class PptStyleSelectionDialog extends DialogFragment {
    
    private RecyclerView styleRecyclerView;
    private StyleAdapter styleAdapter;
    private OnStyleSelectedListener listener;
    private String currentStyle = "推荐";
    
    public interface OnStyleSelectedListener {
        void onStyleSelected(String style);
    }
    
    public void setOnStyleSelectedListener(OnStyleSelectedListener listener) {
        this.listener = listener;
    }
    
    public void setCurrentStyle(String style) {
        this.currentStyle = style;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.StyleSelectionDialog);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_ppt_style_selection, container, false);
        
        initViews(view);
        setupRecyclerView();
        
        // Click outside to dismiss
        view.setOnClickListener(v -> dismiss());
        
        return view;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        // Set dialog position to right side
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.END | Gravity.TOP;
            params.x = 0;
            params.y = 200; // Adjust based on toolbar height
            window.setAttributes(params);
        }
        
        return dialog;
    }
    
    private void initViews(View view) {
        styleRecyclerView = view.findViewById(R.id.style_recycler_view);
        
        // Prevent clicks on the content from dismissing
        LinearLayout contentContainer = view.findViewById(R.id.content_container);
        contentContainer.setOnClickListener(v -> {});
    }
    
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        styleRecyclerView.setLayoutManager(layoutManager);
        
        List<StyleItem> styles = new ArrayList<>();
        styles.add(new StyleItem("推荐", true));
        styles.add(new StyleItem("简约商务", false));
        styles.add(new StyleItem("卡通插画", false));
        styles.add(new StyleItem("炫酷科技", false));
        styles.add(new StyleItem("中国风", false));
        styles.add(new StyleItem("水彩清新", false));
        styles.add(new StyleItem("党务政务", false));
        styles.add(new StyleItem("其他", false));
        
        // Set current selected style
        for (StyleItem item : styles) {
            if (item.getName().equals(currentStyle)) {
                item.setSelected(true);
            } else {
                item.setSelected(false);
            }
        }
        
        styleAdapter = new StyleAdapter(styles);
        styleAdapter.setOnItemClickListener(style -> {
            if (listener != null) {
                listener.onStyleSelected(style.getName());
            }
            dismiss();
        });
        styleRecyclerView.setAdapter(styleAdapter);
    }
    
    private static class StyleAdapter extends RecyclerView.Adapter<StyleAdapter.ViewHolder> {
        private List<StyleItem> styles;
        private OnItemClickListener listener;
        
        interface OnItemClickListener {
            void onItemClick(StyleItem style);
        }
        
        StyleAdapter(List<StyleItem> styles) {
            this.styles = styles;
        }
        
        void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ppt_style, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StyleItem style = styles.get(position);
            holder.bind(style);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(style);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return styles.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView styleText;
            ImageView checkIcon;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                styleText = itemView.findViewById(R.id.style_text);
                checkIcon = itemView.findViewById(R.id.check_icon);
            }
            
            void bind(StyleItem style) {
                styleText.setText(style.getName());
                checkIcon.setVisibility(style.isSelected() ? View.VISIBLE : View.GONE);
                
                if (style.isSelected()) {
                    styleText.setTextColor(Color.parseColor("#1976D2"));
                } else {
                    styleText.setTextColor(Color.parseColor("#333333"));
                }
            }
        }
    }
    
    private static class StyleItem {
        private String name;
        private boolean selected;
        
        StyleItem(String name, boolean selected) {
            this.name = name;
            this.selected = selected;
        }
        
        String getName() { return name; }
        boolean isSelected() { return selected; }
        void setSelected(boolean selected) { this.selected = selected; }
    }
}
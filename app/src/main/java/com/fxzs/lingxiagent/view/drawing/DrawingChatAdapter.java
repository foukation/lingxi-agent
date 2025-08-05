package com.fxzs.lingxiagent.view.drawing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingMessageDto;

import java.util.ArrayList;
import java.util.List;

/**
 * AI绘画对话适配器
 */
public class DrawingChatAdapter extends RecyclerView.Adapter<DrawingChatAdapter.MessageViewHolder> {
    
    private List<DrawingMessageDto> messages = new ArrayList<>();
    private OnMessageActionListener listener;
    
    public interface OnMessageActionListener {
        void onDownloadClick(DrawingMessageDto message);
        void onContinueEditClick(DrawingMessageDto message);
        void onImageClick(DrawingMessageDto message);
    }
    
    public void setOnMessageActionListener(OnMessageActionListener listener) {
        this.listener = listener;
    }
    
    public void setMessages(List<DrawingMessageDto> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }
    
    public void addMessage(DrawingMessageDto message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    public void updateLastMessage(DrawingMessageDto message) {
        if (!messages.isEmpty()) {
            messages.set(messages.size() - 1, message);
            notifyItemChanged(messages.size() - 1);
        }
    }
    
    public DrawingMessageDto getLastMessage() {
        if (!messages.isEmpty()) {
            return messages.get(messages.size() - 1);
        }
        return null;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drawing_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        DrawingMessageDto message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMessage;
        LinearLayout llAiMessage;
        TextView tvAiMessage;
        LinearLayout llProgress;
        TextView tvProgressPercentage;
        ProgressBar progressBar;
        CardView cvImage;
        ImageView ivGeneratedImage;
        LinearLayout llActions;
        ImageButton btnDownload;
        TextView tvContinueEdit;
        ImageView ivSparkleBig;
        ImageView ivSparkleSmall;
        
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            llAiMessage = itemView.findViewById(R.id.ll_ai_message);
            tvAiMessage = itemView.findViewById(R.id.tv_ai_message);
            llProgress = itemView.findViewById(R.id.ll_progress);
            tvProgressPercentage = itemView.findViewById(R.id.tv_progress_percentage);
            progressBar = itemView.findViewById(R.id.progress_bar);
            cvImage = itemView.findViewById(R.id.cv_image);
            ivGeneratedImage = itemView.findViewById(R.id.iv_generated_image);
            llActions = itemView.findViewById(R.id.ll_actions);
            btnDownload = itemView.findViewById(R.id.btn_download);
            tvContinueEdit = itemView.findViewById(R.id.tv_continue_edit);
            ivSparkleBig = itemView.findViewById(R.id.iv_sparkle_big);
            ivSparkleSmall = itemView.findViewById(R.id.iv_sparkle_small);
        }
        
        void bind(DrawingMessageDto message) {
            // 用户消息
            if (message.isUserMessage()) {
                tvUserMessage.setVisibility(View.VISIBLE);
                tvUserMessage.setText(message.getText());
                llAiMessage.setVisibility(View.GONE);
            } else {
                // AI消息
                tvUserMessage.setVisibility(View.GONE);
                llAiMessage.setVisibility(View.VISIBLE);
                
                // 文字回复
                if (message.getText() != null && !message.getText().isEmpty()) {
                    tvAiMessage.setVisibility(View.VISIBLE);
                    tvAiMessage.setText(message.getText());
                } else {
                    tvAiMessage.setVisibility(View.GONE);
                }
                
                // 进度显示
                if (message.isGenerating()) {
                    llProgress.setVisibility(View.VISIBLE);
                    tvProgressPercentage.setText(message.getProgress() + "%");
                    if (progressBar != null) {
                        progressBar.setProgress(message.getProgress());
                    }
                    // 可以在这里添加星星动画
                } else {
                    llProgress.setVisibility(View.GONE);
                }
                
                // 图片显示
                if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                    cvImage.setVisibility(View.VISIBLE);
                    llActions.setVisibility(View.VISIBLE);
                    
                    Glide.with(itemView.getContext())
                            .load(message.getImageUrl())
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_placeholder)
                            .into(ivGeneratedImage);
                    
                    // 设置点击事件
                    ivGeneratedImage.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onImageClick(message);
                        }
                    });
                    
                    btnDownload.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onDownloadClick(message);
                        }
                    });
                    
                    tvContinueEdit.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onContinueEditClick(message);
                        }
                    });
                } else {
                    cvImage.setVisibility(View.GONE);
                    llActions.setVisibility(View.GONE);
                }
            }
        }
    }
}
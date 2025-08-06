package com.fxzs.lingxiagent.view.chat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Rect;


import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.chat.callback.MsgActionCallback;
import com.fxzs.lingxiagent.model.chat.callback.OnFileItemClick;
import com.fxzs.lingxiagent.model.chat.dto.ChatMessage;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.util.ZUtil.ImageUtil;
import com.fxzs.lingxiagent.util.ZUtil.MarkdownRenderer;
import com.fxzs.lingxiagent.util.ZUtil.MarkdownUtils;
import com.fxzs.lingxiagent.util.ZUtil.AdvancedTableEntry;
import com.fxzs.lingxiagent.util.ZUtil.CodeBlockPlugin;
import com.fxzs.lingxiagent.util.ZUtil.TTSUtils;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.model.chat.dto.IconTextItem;
import com.fxzs.lingxiagent.util.ZDpUtils;
import com.fxzs.lingxiagent.R;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import io.noties.markwon.Markwon;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.table.TableEntry;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.node.FencedCodeBlock;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static final String TAG = "ChatAdapter";
    public static final  int TYPE_USER = 0;//用户-普通消息
    public static final  int TYPE_AI = 1;//ai-文字消息
    public static final  int TYPE_USER_HEAD_AGENT = 2;//用户-智能体头部（固定头部）
    public static final  int TYPE_AI_DRAWING = 3;//ai-绘画消息
    public static final  int TYPE_USER_FILE = 4;//用户-文件
    public static final  int TYPE_USER_FILE_IMAGE = 5;//用户-图片
    public static final  int TYPE_USER_HEAD_MEETING = 6;//用户-智能问答头部（固定头部）

    private static final int REFRESH_DELAY = 3000;
    private static final int POPUP_HEIGHT = 160;
    private static final int POPUP_WIDTH = 128;
    private static final int POPUP_DISTANCE = 16;

    private Markwon markwon = null;
    private MarkdownRenderer markdownRenderer;
    private List<ChatMessage> chatMessages;
    Context context;

    // 保持对renderer的引用以便清理
    private final List<MarkdownRenderer> activeRenderers = new ArrayList<>();

    // 缓存MarkwonAdapter实例以避免重复创建和闪屏
    private MarkwonAdapter cachedMarkwonAdapter;
    private Markwon cachedUnifiedMarkwon;

    // 为每个ViewHolder缓存适配器实例，避免流式更新时的闪屏
    private final Map<RecyclerView.ViewHolder, MarkwonAdapter> viewHolderAdapterCache = new HashMap<>();
    private final Map<RecyclerView.ViewHolder, String> lastRenderedContent = new HashMap<>();

    // 缓存已渲染的Spanned内容，用于增量更新
    private final Map<RecyclerView.ViewHolder, CharSequence> lastRenderedSpanned = new HashMap<>();

    MsgActionCallback callback;
    private ChatFileAdapter chatFileAdapter;
    private boolean isCopy = false;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        Log.d(TAG, "ChatAdapter: Constructor called");
        this.context = context;
        this.chatMessages = chatMessages;

        markwon = MarkdownUtils.createMarkwon(context);
        markdownRenderer = new MarkdownRenderer(context);
        activeRenderers.add(markdownRenderer);

        // 初始化缓存的Markwon实例，避免重复创建
        initializeCachedMarkwon();

        chatFileAdapter = new ChatFileAdapter(context,null,ChatFileAdapter.TYPE_IMAGE, new OnFileItemClick() {
            @Override
            public void onItemClick(int position) {
            }

            @Override
            public void onClose(int position) {

            }
        });
        chatFileAdapter.setShowClose(false);
    }

    public void setCallback(MsgActionCallback callback) {
        this.callback = callback;
    }

    public List<ChatMessage> getSelectMessages() {
        List<ChatMessage> list = new ArrayList<>();
        list.add(chatMessages.get(mSharePos));
        return chatMessages;
    }

    /**
     * 初始化缓存的Markwon实例，避免重复创建导致的闪屏
     */
    private void initializeCachedMarkwon() {
        try {
            // 创建缓存的 MarkwonAdapter
            cachedMarkwonAdapter = MarkwonAdapter.builder(
                    R.layout.item_default,
                    R.id.text_view
                )
                .include(TableBlock.class, CodeBlockPlugin.createAdvancedTableEntry(context,
                    R.layout.mobile_style_table_final,
                    R.id.mobile_style_table_final,
                    R.layout.mobile_table_cell
                ))
                .include(FencedCodeBlock.class, CodeBlockPlugin.createCodeBlockEntry(context))
                .build();

            // 创建缓存的 Markwon 实例
            cachedUnifiedMarkwon = MarkdownUtils.createMarkwon(context);

            Log.d(TAG, "initializeCachedMarkwon: Cached Markwon instances initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "initializeCachedMarkwon: Failed to initialize cached instances", e);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int res = 0;
        if(viewType == TYPE_USER){
            res =  R.layout.item_user_message;
        }else if(viewType == TYPE_AI){
            res =  R.layout.item_ai_message;
        }else if(viewType == TYPE_AI_DRAWING){
            res =  R.layout.item_ai_drawing;
        }else  if(viewType == TYPE_USER_FILE_IMAGE){
            res =  R.layout.item_chat_file_rv;
        }else  if(viewType == TYPE_USER_FILE){
            res =  R.layout.item_chat_file_rv;
        }else if(viewType == TYPE_USER_HEAD_MEETING){
            res = R.layout.item_meeting_head_message;
        }else {
            res = R.layout.item_agent_message;
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(res, parent, false);
        return new ChatViewHolder(view);
    }

//    public void notifyItemChanged(int position, Object payload) {
//        mObservers.notifyItemRangeChanged(position, 1, payload);
//    }
    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position, List<Object> payloads) {
        Log.d("ChatAdapter", "onBindViewHolder: position=" + position + ", payloads=" + payloads);

        if (!payloads.isEmpty()) {
            ChatMessage item = chatMessages.get(position);

            // 检查是否为流式更新
            if (isStreamingUpdate(payloads)) {
                boolean immediate = isImmediateUpdate(payloads);
                Log.d(TAG, "onBindViewHolder: Handling streaming update for position " + position + ", immediate=" + immediate);

                // 流式更新：只更新内容，不重新设置整个UI
                if (item.getMsgType() == ChatAdapter.TYPE_AI) {
                    if (immediate) {
                        // 立即更新：跳过防抖机制
                        renderUnifiedContentImmediate(holder, item.getMessage());
                    } else {
                        // 正常流式更新：使用防抖机制
                        renderUnifiedContent(holder, item.getMessage());
                    }
                } else {
                    setUI(holder, item, position);
                }
            } else {
                // 其他类型的payload更新
                setUI(holder, item, position);
            }
        } else {
            // 如果没有 payloads，还是返回默认的整个刷新
            onBindViewHolder(holder, position);
        }
// RecyclerView 点击事件在 setAction 方法中设置
    }

    private void setUI(ChatViewHolder holder, ChatMessage item, int position) {
        if (item.getMsgType() == ChatAdapter.TYPE_USER_HEAD_AGENT) {
            holder.tv_agent_hint.setText(item.getMessage());
            ImageUtil.netRadius(context,item.getAvatar(),holder.iv_agent);
        }else if(item.getMsgType() == ChatAdapter.TYPE_USER_HEAD_MEETING){
            holder.tv_agent_hint.setText(item.getMessage());
//            ImageUtil.load(context,item.getAvatarRes(),holder.iv_agent);
        }else if(item.getMsgType() == ChatAdapter.TYPE_AI){
            setAIMessage(holder,item,position);
        }else if(item.getMsgType() == ChatAdapter.TYPE_AI_DRAWING){
            setDrawingMessage(holder,item,position);
        }else if(item.getMsgType() == ChatAdapter.TYPE_USER_FILE_IMAGE){
            setUserImage(holder,item,position);
        }else if(item.getMsgType() == ChatAdapter.TYPE_USER_FILE){
            setUserFile(holder,item,position);
        }else {
            // 用户消息使用缓存渲染（用户消息布局使用的是TextView）
            if (item.getMsgType() == ChatAdapter.TYPE_USER) {
                // 用户消息仍然使用TextView，因为用户消息布局文件中是TextView
//                markdownRenderer.renderWithCache(item.getMessage(), (TextView) holder.itemView.findViewById(R.id.messageText));
                markdownRenderer.renderWithCache(item.getMessage(), (TextView) holder.messageText);
                holder.messageText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ZUtils.copy(context,item.getMessage());
                        return false;
                    }
                });
            } else {
                // 其他类型消息使用统一的RecyclerView渲染
                renderUnifiedContent(holder, item.getMessage());
            }
        }
    }

    private void setUserFile(ChatViewHolder holder, ChatMessage item, int position) {

        holder.rv_file.setLayoutManager(new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, true));


//        chatFileAdapter = new ChatFileAdapter(context,item.getChatFileBeanList(),ChatFileAdapter.TYPE_FILE, new OnFileItemClick() {
//            @Override
//            public void onItemClick(int position) {
//            }
//
//            @Override
//            public void onClose(int position) {
//
//            }
//        });
        holder.rv_file.setAdapter(null); // 清除旧适配器
        chatFileAdapter.setDataList(item.getChatFileBeanList());
        chatFileAdapter.setType(ChatFileAdapter.TYPE_FILE);
        holder.rv_file.setAdapter(chatFileAdapter);
        chatFileAdapter.setShowClose(false);
        holder.rv_file.setAdapter(chatFileAdapter);

    }

    private void setUserImage(ChatViewHolder holder, ChatMessage item, int position) {
        holder.rv_file.setNestedScrollingEnabled(false);
        holder.rv_file.setLayoutManager(new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, true));
        ZUtils.print("item.getChatFileBeanList() == " + item.getChatFileBeanList().size());
        ChatFileAdapter chatFileAdapter = new ChatFileAdapter(context, item.getChatFileBeanList(), ChatFileAdapter.TYPE_IMAGE, new OnFileItemClick() {
            @Override
            public void onItemClick(int position) {
            }

            @Override
            public void onClose(int position) {

            }
        });
        chatFileAdapter.setShowClose(false);
        holder.rv_file.setAdapter(chatFileAdapter);
//        holder.rv_file.setAdapter(null); // 清除旧适配器
//        chatFileAdapter.setDataList(item.getChatFileBeanList());
//        chatFileAdapter.setType(ChatFileAdapter.TYPE_IMAGE);
//        holder.rv_file.setAdapter(chatFileAdapter);


    }


    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: position=" + position);
        ChatMessage message = chatMessages.get(position);
        setUI(holder, message, position);
    }

    @Override
    public void onViewRecycled(@NonNull ChatViewHolder holder) {
        super.onViewRecycled(holder);
        // 清理ViewHolder缓存，避免内存泄漏
        clearViewHolderCache(holder);
        Log.d(TAG, "onViewRecycled: Cleared cache for ViewHolder");
    }

    private void setAction(ChatViewHolder holder, ChatMessage item, int position) {

        holder.iv_chat_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                    ZUtils.showToast("开发中");
                TTSUtils.getInstance().click2PlayTts(item.getMessage());
            }
        });
        holder.iv_chat_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCopy) return;
                isCopy = true;
                ZUtils.copy(context, item.getMessage());
                holder.iv_chat_copy.setBackgroundResource(R.mipmap.chat_copy_success);

                holder.iv_chat_copy.postDelayed(() -> {
                    isCopy = false;
                    holder.iv_chat_copy.setBackgroundResource(R.mipmap.chat_copy);
                }, REFRESH_DELAY);
            }
        });
        holder.iv_chat_share.setOnClickListener(v -> {
            if (mShareListener != null) {
                mSharePos = position;
                mShareListener.onShareIconClick();
            }
        });
        holder.iv_chat_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ZUtils.showToast("开发中");
                showPopup(view, chatMessages.get(position - 1).getMessage());
            }
        });

//                    if (callback != null) {
//                        String content = "";
//                        if(chatMessages.size() > position-1&& position-1>0){
//                            content = chatMessages.get(position - 1).getMessage();
//                            ZUtils.print("获取到发送的 === " + content);
//                        }
//                        callback.refresh(content);
//                    }
//                }
//            });

        holder.iv_chat_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZUtils.showToast("开发中");

            }
        });
        holder.recyclerViewAi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ZUtils.showToast("点击信息");
                if (callback != null) {
                    callback.msgClick();
                }

            }
        });
    }

    private void setActionDrawing(ChatViewHolder holder, ChatMessage item, int position) {

        holder.iv_drawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                    ZUtils.showToast("开发中");
                if (callback != null) {
                    callback.viewDrawing(item);
                }
            }
        });
        holder.iv_chat_draw_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                    ZUtils.showToast("开发中");
                if (callback != null) {
                    callback.downloadDrawing(item);
                }
            }
        });
        holder.iv_chat_draw_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                    ZUtils.showToast("开发中");
                if (callback != null) {
                    callback.continueDrawing(item);
                }

            }
        });
        holder.iv_chat_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) {
                    ChatMessage mItem = chatMessages.get(position - 1);
                    callback.regenerateDrawing(mItem);
                }
            }
        });
    }

    private void setAIMessage(ChatViewHolder holder, ChatMessage item, int position) {
        Log.d(TAG, "setAIMessage: position=" + position + ", message length=" +
                (item.getMessage() != null ? item.getMessage().length() : 0));

        if (item.getMsgType() == TYPE_AI) {
            holder.tv_thinking_title.setText(item.getThinkMessageTitle());
            holder.tv_thinking.setText(item.getThinkMessage());
            ImageUtil.load(context, R.mipmap.thinking_dash, holder.iv_thinking_dash);
//            holder.ll_actions.setVisibility(item.isEnd()?View.VISIBLE:View.GONE);

            holder.ll_actions.setVisibility(View.GONE);
            holder.iv_chat_think_start.setVisibility(View.GONE);
            holder.iv_thinking_arrow.setVisibility(View.VISIBLE);
            holder.iv_thinking_dash.setVisibility(View.VISIBLE);
            if (!"".equals(item.getThinkMessage())) {
                holder.ll_thinking.setVisibility(View.VISIBLE);
            } else {
                holder.ll_thinking.setVisibility(View.GONE);
            }
            if (item.isHideActionRefresh()) {
                holder.iv_chat_refresh.setVisibility(View.GONE);
            } else {
                holder.iv_chat_refresh.setVisibility(View.VISIBLE);
            }
            switch (item.getStatus()) {
                case Constant.ThinkState.START:
                    holder.tv_thinking_title.setText("正在思考中");
                    holder.iv_chat_think_start.setVisibility(View.VISIBLE);
                    holder.iv_thinking_arrow.setVisibility(View.GONE);

                    break;
                case Constant.ThinkState.THINKING:
                    holder.tv_thinking_title.setText("思考中");
                    holder.iv_thinking_arrow.setVisibility(View.GONE);

                    break;
                case Constant.ThinkState.END:
                    holder.tv_thinking_title.setText(item.getThinkMessageTitle());
                    holder.ll_actions.setVisibility(View.VISIBLE);
                    holder.iv_thinking_dash.setVisibility(View.GONE);
                    holder.iv_thinking_arrow.setVisibility(View.VISIBLE);
                    break;
            }
            setAction(holder, item, position);

            // 统一使用 MarkwonAdapter 渲染所有内容（包括表格和非表格）
            String message = item.getMessage();
            if (message != null && !message.isEmpty()) {
                Log.d(TAG, "setAIMessage: Starting unified markdown render for position " + position);
                renderUnifiedContent(holder, message);
            } else {
                Log.d(TAG, "setAIMessage: Message is null or empty for position " + position);
                // 清空RecyclerView
                holder.recyclerViewAi.setAdapter(null);
            }
        }

// 现在统一使用 RecyclerView 渲染，不再需要单独的 TextView

    }


    private void setDrawingMessage(ChatViewHolder holder, ChatMessage item, int position) {

        if (item.getProgress() == 100) {
            holder.rl_progress.setVisibility(View.GONE);
//            holder.ll_actions.setVisibility(View.VISIBLE);
            holder.ll_actions_drawing.setVisibility(View.VISIBLE);
            ImageUtil.netRadius(context, item.getUrl(), holder.iv_drawing);
        } else {
            holder.rl_progress.setVisibility(View.VISIBLE);
            holder.tv_progress.setText(item.getProgress() + "%");
//            holder.ll_actions.setVisibility(View.GONE);
            holder.ll_actions_drawing.setVisibility(View.GONE);
            // 清除原有图片内容，加载动图作为渲染背景
            holder.iv_drawing.setImageDrawable(null);
            ImageUtil.loadGif(context, R.drawable.bg_imagine_loading, holder.iv_drawing);
        }
        setActionDrawing(holder, item, position);
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if(chatMessages.get(position).getMsgType() == ChatAdapter.TYPE_USER_HEAD_AGENT){
//          return TYPE_USER_HEAD_AGENT;
//        }else {
//            return chatMessages.get(position).isUser() ? TYPE_USER : TYPE_AI;
//        }
        return chatMessages.get(position).getMsgType();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_thinking_title;
        public TextView tv_thinking;
        public TextView messageText;
        public LinearLayout root_view;
        public LinearLayout ll_thinking;
        public LinearLayout ll_actions;
        public ImageView iv_chat_play;
        public ImageView iv_chat_copy;
        public ImageView iv_chat_share;
        public ImageView iv_chat_refresh;
        public ImageView iv_chat_export;
        public ImageView iv_chat_think_start;
        public ImageView iv_thinking_arrow;


        public ImageView iv_agent;
        public TextView tv_agent_hint;
        public ImageView iv_thinking_dash;
        public ImageView iv_drawing;
        public TextView tv_progress;
        public View rl_progress;
        public View ll_actions_drawing;
        public View iv_chat_draw_continue;
        public View iv_chat_draw_download;


        public RecyclerView rv_file;
        public RecyclerView recyclerViewAi;

        // 用于延迟更新的Runnable，避免频繁更新
        public Runnable pendingUpdateRunnable;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_thinking_title = itemView.findViewById(R.id.tv_thinking_title);
            tv_thinking = itemView.findViewById(R.id.tv_thinking);
            messageText = itemView.findViewById(R.id.messageText);
            root_view = itemView.findViewById(R.id.root_view);
            ll_thinking = itemView.findViewById(R.id.ll_thinking);
            ll_actions = itemView.findViewById(R.id.ll_actions);
            iv_chat_play = itemView.findViewById(R.id.iv_chat_play);
            iv_chat_copy = itemView.findViewById(R.id.iv_chat_copy);
            iv_chat_share = itemView.findViewById(R.id.iv_chat_share);
            iv_chat_refresh = itemView.findViewById(R.id.iv_chat_refresh);
            iv_chat_export = itemView.findViewById(R.id.iv_chat_export);
            iv_chat_think_start = itemView.findViewById(R.id.iv_chat_think_start);
            iv_thinking_arrow = itemView.findViewById(R.id.iv_thinking_arrow);
            iv_agent = itemView.findViewById(R.id.iv_agent);
            tv_agent_hint = itemView.findViewById(R.id.tv_agent_hint);
            iv_thinking_dash = itemView.findViewById(R.id.iv_thinking_dash);
            iv_drawing = itemView.findViewById(R.id.iv_drawing);
            tv_progress = itemView.findViewById(R.id.tv_progress);
            rl_progress = itemView.findViewById(R.id.rl_progress);
            ll_actions_drawing = itemView.findViewById(R.id.ll_actions_drawing);
            iv_chat_draw_continue = itemView.findViewById(R.id.iv_chat_draw_continue);
            iv_chat_draw_download = itemView.findViewById(R.id.iv_chat_draw_download);
            rv_file = itemView.findViewById(R.id.rv_file);
            recyclerViewAi = itemView.findViewById(R.id.recycler_view_ai);
        }
    }

    /**
     * 立即渲染内容，跳过防抖机制（用于非常小的增量更新）
     */
    private void renderUnifiedContentImmediate(ChatViewHolder holder, String message) {
        try {
            // 使用缓存的实例
            if (cachedUnifiedMarkwon == null) {
                Log.w(TAG, "renderUnifiedContentImmediate: Cached markwon not available, falling back to normal render");
                renderUnifiedContent(holder, message);
                return;
            }

            // 获取或创建ViewHolder专用的适配器
            MarkwonAdapter adapter = viewHolderAdapterCache.get(holder);
            if (adapter == null) {
                Log.w(TAG, "renderUnifiedContentImmediate: No cached adapter, falling back to normal render");
                renderUnifiedContent(holder, message);
                return;
            }

            // 设置当前Markdown内容供表格提取使用
            AdvancedTableEntry.setCurrentMarkdownContent(message);

            // 立即更新，不使用防抖
            adapter.setMarkdown(cachedUnifiedMarkwon, message);
            adapter.notifyItemChanged(0);

            // 更新缓存
            lastRenderedContent.put(holder, message);

            Log.d(TAG, "renderUnifiedContentImmediate: Immediate render completed");

        } catch (Exception e) {
            Log.e(TAG, "renderUnifiedContentImmediate: Failed to render immediately", e);
            // 降级到正常渲染
            renderUnifiedContent(holder, message);
        }
    }

    /**
     * 检测消息是否包含表格内容
     */
    private boolean containsTable(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        // 检测Markdown表格格式
        return message.contains("|") && message.contains("---");
    }



    /**
     * 统一渲染内容到RecyclerView（支持表格和非表格内容）
     * 使用缓存实例避免重复创建，防止闪屏，优化流式更新
     */
    private void renderUnifiedContent(ChatViewHolder holder, String message) {
        try {
            // 检查是否为流式更新（内容相似但有增量）
            String lastContent = lastRenderedContent.get(holder);
            boolean isStreamingUpdate = lastContent != null &&
                                      message.startsWith(lastContent) &&
                                      message.length() > lastContent.length();

            // 使用缓存的实例，避免重复创建导致闪屏
            if (cachedMarkwonAdapter == null || cachedUnifiedMarkwon == null) {
                Log.w(TAG, "renderUnifiedContent: Cached instances not available, reinitializing");
                initializeCachedMarkwon();
            }

            // 防闪屏：先设置LayoutManager，避免RecyclerView重新测量
            if (holder.recyclerViewAi.getLayoutManager() == null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                layoutManager.setAutoMeasureEnabled(true);
                holder.recyclerViewAi.setLayoutManager(layoutManager);

                // 禁用RecyclerView动画，减少流式更新时的闪烁
                holder.recyclerViewAi.setItemAnimator(null);

                // 移除默认的item间距，确保样式与原messageText一致
                holder.recyclerViewAi.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(@NonNull Rect outRect, @NonNull android.view.View view,
                                             @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                        // 移除所有默认间距
                        outRect.set(0, 0, 0, 0);
                    }
                });
            }

            // 获取或创建ViewHolder专用的适配器
            MarkwonAdapter adapter = viewHolderAdapterCache.get(holder);
            if (adapter == null || !isStreamingUpdate) {
                // 创建新的适配器实例
                adapter = MarkwonAdapter.builder(
                        R.layout.item_default,
                        R.id.text_view
                    )
                    .include(TableBlock.class, CodeBlockPlugin.createAdvancedTableEntry(context,
                        R.layout.mobile_style_table_final,
                        R.id.mobile_style_table_final,
                        R.layout.mobile_table_cell
                    ))
                    .include(FencedCodeBlock.class, CodeBlockPlugin.createCodeBlockEntry(context))
                    .build();

                viewHolderAdapterCache.put(holder, adapter);
                Log.d(TAG, "renderUnifiedContent: Created new adapter for ViewHolder");
            } else {
                Log.d(TAG, "renderUnifiedContent: Reusing cached adapter for streaming update");
            }

            // 设置当前Markdown内容供表格提取使用
            AdvancedTableEntry.setCurrentMarkdownContent(message);

            // 将 Markdown 内容解析为适配器数据
            adapter.setMarkdown(cachedUnifiedMarkwon, message);

            // 缓存当前内容
            lastRenderedContent.put(holder, message);

            // 根据是否为流式更新选择不同的设置策略
            if (isStreamingUpdate && holder.recyclerViewAi.getAdapter() == adapter) {
                // 流式更新：使用优化的更新策略
                if (tryIncrementalTextUpdate(holder, message, lastContent)) {
                    Log.d(TAG, "renderUnifiedContent: Streaming update - using optimized update strategy");
                } else {
                    // 如果优化更新失败，回退到标准更新，但使用动画禁用
                    holder.recyclerViewAi.setItemAnimator(null); // 禁用动画减少闪烁
                    AdvancedTableEntry.setCurrentMarkdownContent(message);
                    adapter.setMarkdown(cachedUnifiedMarkwon, message);
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "renderUnifiedContent: Streaming update - fallback to standard update");
                }
            } else {
                // 首次渲染或非流式更新：设置适配器
                MarkwonAdapter finalAdapter = adapter;
                holder.recyclerViewAi.post(() -> {
                    try {
                        holder.recyclerViewAi.setAdapter(finalAdapter);

                        // 检测是否包含表格并记录日志
                        if (containsTable(message)) {
                            Log.d(TAG, "renderUnifiedContent: Table detected and rendered successfully");
                        } else {
                            Log.d(TAG, "renderUnifiedContent: Regular content rendered successfully");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "renderUnifiedContent: Failed to set adapter in post", e);
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "renderUnifiedContent: Failed to render content", e);
            // 降级处理：创建简单的文本适配器
            renderFallbackContent(holder, message);
        }
    }

    /**
     * 降级渲染方法
     */
    private void renderFallbackContent(ChatViewHolder holder, String message) {
        try {
            MarkwonAdapter fallbackAdapter = MarkwonAdapter.builder(R.layout.item_default, R.id.text_view).build();
            Markwon fallbackMarkwon = Markwon.create(context);
            fallbackAdapter.setMarkdown(fallbackMarkwon, message);

            if (holder.recyclerViewAi.getLayoutManager() == null) {
                holder.recyclerViewAi.setLayoutManager(new LinearLayoutManager(context));
            }

            holder.recyclerViewAi.post(() -> {
                holder.recyclerViewAi.setAdapter(fallbackAdapter);
                Log.d(TAG, "renderFallbackContent: Fallback rendering successful");
            });
        } catch (Exception fallbackError) {
            Log.e(TAG, "renderFallbackContent: Fallback rendering also failed", fallbackError);
            // 最后的降级：显示错误信息
            try {
                MarkwonAdapter errorAdapter = MarkwonAdapter.builder(R.layout.item_default, R.id.text_view).build();
                errorAdapter.setMarkdown(Markwon.create(context), "**渲染失败**\n\n" + message);
                holder.recyclerViewAi.post(() -> holder.recyclerViewAi.setAdapter(errorAdapter));
            } catch (Exception finalError) {
                Log.e(TAG, "renderFallbackContent: Final fallback also failed", finalError);
            }
        }
    }

    /**
     * 取消所有正在进行的Markdown渲染
     */
    public void cancelAllMarkdownRendering() {
        Log.d(TAG, "cancelAllMarkdownRendering: Cancelling all markdown rendering");

        // 取消主渲染器
        if (markdownRenderer != null) {
            markdownRenderer.cancel();
        }

        // 取消所有活动的渲染器
        for (MarkdownRenderer renderer : activeRenderers) {
            if (renderer != null) {
                renderer.cancel();
            }
        }
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        Log.d(TAG, "cleanup: Starting adapter cleanup");

        for (MarkdownRenderer renderer : activeRenderers) {
            if (renderer != null) {
                renderer.destroy();
            }
        }
        activeRenderers.clear();

        if (markdownRenderer != null) {
            markdownRenderer.destroy();
            markdownRenderer = null;
        }

        // 清理ViewHolder缓存，避免内存泄漏
        viewHolderAdapterCache.clear();
        lastRenderedContent.clear();
        lastRenderedSpanned.clear();

        Log.d(TAG, "cleanup: Adapter cleanup completed");
    }

    /**
     * 清理特定ViewHolder的缓存
     */
    public void clearViewHolderCache(ChatViewHolder holder) {
        viewHolderAdapterCache.remove(holder);
        lastRenderedContent.remove(holder);
        lastRenderedSpanned.remove(holder);
    }

    /**
     * 专门用于流式更新的优化方法
     * 避免重复创建适配器，减少闪屏
     */
    public void updateStreamingContent(int position, String newContent) {
        updateStreamingContent(position, newContent, false);
    }

    /**
     * 专门用于流式更新的优化方法（带强制更新选项）
     * @param position 消息位置
     * @param newContent 新内容
     * @param forceImmediate 是否强制立即更新，跳过防抖机制
     */
    public void updateStreamingContent(int position, String newContent, boolean forceImmediate) {
        if (position < 0 || position >= chatMessages.size()) {
            return;
        }

        ChatMessage message = chatMessages.get(position);
        String oldContent = message.getMessage();
        message.setMessage(newContent);

        // 如果强制立即更新，使用特殊的payload
        String payload = forceImmediate ? "streaming_update_immediate" : "streaming_update";

        // 使用payload通知特定位置更新，避免整个item重新绑定
        notifyItemChanged(position, payload);

        Log.d(TAG, "updateStreamingContent: Updated position " + position +
              ", increment=" + (newContent.length() - (oldContent != null ? oldContent.length() : 0)) +
              ", immediate=" + forceImmediate);
    }

    /**
     * 检查是否为流式更新的payload
     */
    private boolean isStreamingUpdate(List<Object> payloads) {
        return payloads != null && !payloads.isEmpty() &&
               (payloads.contains("streaming_update") || payloads.contains("streaming_update_immediate"));
    }

    /**
     * 检查是否为立即更新的payload
     */
    private boolean isImmediateUpdate(List<Object> payloads) {
        return payloads != null && !payloads.isEmpty() &&
               payloads.contains("streaming_update_immediate");
    }

    /**
     * 尝试增量文本更新，避免重新解析Markdown
     * 使用防抖机制减少频繁更新导致的闪烁
     */
    private boolean tryIncrementalTextUpdate(ChatViewHolder holder, String newContent, String lastContent) {
        try {
            // 检查是否包含表格，如果有表格则不能使用增量更新
            if (containsTable(newContent) || containsTable(lastContent)) {
                Log.d(TAG, "tryIncrementalTextUpdate: Contains table, skipping incremental update");
                return false;
            }

            // 检查是否为简单的文本追加（流式更新的典型特征）
            if (lastContent == null || !newContent.startsWith(lastContent)) {
                Log.d(TAG, "tryIncrementalTextUpdate: Not a simple append, skipping incremental update");
                return false;
            }

            // 检查增量内容的长度，如果增量太大，可能不是流式更新
            int incrementLength = newContent.length() - lastContent.length();
            if (incrementLength > 100) { // 进一步减少阈值，确保是真正的流式更新
                Log.d(TAG, "tryIncrementalTextUpdate: Increment too large (" + incrementLength + "), skipping incremental update");
                return false;
            }

            // 获取当前适配器
            MarkwonAdapter adapter = viewHolderAdapterCache.get(holder);
            if (adapter == null) {
                Log.d(TAG, "tryIncrementalTextUpdate: No cached adapter, skipping incremental update");
                return false;
            }

            // 取消之前的待处理更新
            if (holder.pendingUpdateRunnable != null) {
                holder.recyclerViewAi.removeCallbacks(holder.pendingUpdateRunnable);
            }

            // 对于非常小的增量，立即更新以保持最佳丝滑感
            if (incrementLength <= 5) {
                try {
                    // 设置当前Markdown内容供表格提取使用
                    AdvancedTableEntry.setCurrentMarkdownContent(newContent);

                    // 立即更新，不使用防抖
                    adapter.setMarkdown(cachedUnifiedMarkwon, newContent);
                    adapter.notifyItemChanged(0);
                    Log.d(TAG, "tryIncrementalTextUpdate: Immediate update for small increment (" + incrementLength + ")");
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "tryIncrementalTextUpdate: Failed immediate update", e);
                    // 如果立即更新失败，继续使用防抖机制
                }
            }

            // 根据增量大小选择不同的防抖延迟
            long delayMs;
            if (incrementLength <= 20) {
                // 小增量：短延迟，保持丝滑感
                delayMs = 15;
            } else if (incrementLength <= 50) {
                // 中等增量：中等延迟
                delayMs = 30;
            } else {
                // 大增量：较长延迟，避免频繁重绘
                delayMs = 60;
            }

            // 使用智能防抖机制
            holder.pendingUpdateRunnable = () -> {
                try {
                    // 设置当前Markdown内容供表格提取使用
                    AdvancedTableEntry.setCurrentMarkdownContent(newContent);

                    // 重新解析内容
                    adapter.setMarkdown(cachedUnifiedMarkwon, newContent);

                    // 使用最温和的通知方式，只通知内容变化，不重建视图
                    adapter.notifyItemChanged(0);

                    Log.d(TAG, "tryIncrementalTextUpdate: Successfully updated with smart debounce (delay=" + delayMs + "ms, increment=" + incrementLength + ")");
                } catch (Exception e) {
                    Log.e(TAG, "tryIncrementalTextUpdate: Failed to update with smart debounce", e);
                }
            };

            // 使用智能延迟
            holder.recyclerViewAi.postDelayed(holder.pendingUpdateRunnable, delayMs);

            return true;

        } catch (Exception e) {
            Log.e(TAG, "tryIncrementalTextUpdate: Exception occurred", e);
            return false;
        }
    }

    private static OnShareClickListener mShareListener;
    private static int mSharePos = -1;

    public interface OnShareClickListener {
        void onShareIconClick();
    }

    public void setShareListener(OnShareClickListener listener) {
        mShareListener = listener;
    }

    private void showPopup(View anchor, String content) {
        Context context = anchor.getContext();
        View popupView = LayoutInflater.from(context)
                .inflate(R.layout.popup_refresh_options, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setOutsideTouchable(true);

        RecyclerView rvOptions = popupView.findViewById(R.id.rvOptions);
        rvOptions.setLayoutManager(new LinearLayoutManager(context));

        String[] optionTitles = context.getResources()
                .getStringArray(R.array.refresh_options);
        List<IconTextItem> options = new ArrayList<>();
        options.add(new IconTextItem(R.mipmap.refresh_retry, optionTitles[0]));
        options.add(new IconTextItem(R.mipmap.refresh_simplify, optionTitles[1]));
        options.add(new IconTextItem(R.mipmap.refresh_details, optionTitles[2]));
        options.add(new IconTextItem(R.mipmap.refresh_conversation, optionTitles[3]));

        rvOptions.setAdapter(new IconTextAdapter(options,
                position -> {
                    popupWindow.dismiss();
                    handleOptionClick(position, content);
                }));

        int xOffset = ZDpUtils.dpToPx2(context, POPUP_DISTANCE + POPUP_WIDTH);
        int yOffset = ZDpUtils.dpToPx2(context, POPUP_HEIGHT);
        popupWindow.showAsDropDown(anchor, -xOffset, -yOffset);
    }

    private void handleOptionClick(int position, String content) {
        if (position == 0) {
            if (callback != null) {
                callback.refresh(content);
            }
        }
    }

}
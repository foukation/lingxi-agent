package com.fxzs.lingxiagent.view.aiwork;

import android.content.Context;
import android.graphics.Rect;
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

import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.model.chat.callback.MsgActionCallback;
import com.fxzs.lingxiagent.model.chat.callback.OnFileItemClick;
import com.fxzs.lingxiagent.model.chat.dto.ChatMessage;
import com.fxzs.lingxiagent.model.chat.dto.IconTextItem;
import com.fxzs.lingxiagent.network.ZNet.bean.getCatDetailListBean;
import com.fxzs.lingxiagent.util.ShadowUtils;
import com.fxzs.lingxiagent.util.ZDpUtils;
import com.fxzs.lingxiagent.util.ZUtil.AdvancedTableEntry;
import com.fxzs.lingxiagent.util.ZUtil.CodeBlockPlugin;
import com.fxzs.lingxiagent.util.ZUtil.Constant;
import com.fxzs.lingxiagent.util.ZUtil.ImageUtil;
import com.fxzs.lingxiagent.util.ZUtil.MarkdownRenderer;
import com.fxzs.lingxiagent.util.ZUtil.MarkdownUtils;
import com.fxzs.lingxiagent.util.ZUtil.TTSUtils;
import com.fxzs.lingxiagent.util.ZUtils;
import com.fxzs.lingxiagent.view.agent.AgentAdapter;
import com.fxzs.lingxiagent.view.chat.ChatFileAdapter;
import com.fxzs.lingxiagent.view.chat.IconTextAdapter;
import com.fxzs.lingxiagent.view.user.HistoryItem;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.node.FencedCodeBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.noties.markwon.Markwon;
import io.noties.markwon.recycler.MarkwonAdapter;


public class AiWorkAdapter extends RecyclerView.Adapter<AiWorkAdapter.ChatViewHolder> {
    private static final String TAG = "AiWorkAdapter";
    public static final  int TYPE_MEETING = 0;//会议
    public static final  int TYPE_PPT = 1;//ppt
    public static final  int TYPE_TRANSLATE = 2;//同声传译
    public static final  int TYPE_DRAWING = 3;//ai绘画


    private List<HistoryItem> list;
    Context context;
    private static final int POPUP_HEIGHT = 160;
    private static final int POPUP_WIDTH = 128;
    private static final int POPUP_DISTANCE = 16;

    int type = TYPE_DRAWING;


    public AiWorkAdapter(Context context, List<HistoryItem> datas) {
        Log.d(TAG, "AiWorkAdapter: Constructor called");
        this.context = context;
        this.list = datas;

    }

    public void setItems(List<HistoryItem> items) {
        this.list = items;
        notifyDataSetChanged();
    }


    public void setType(int type) {
        this.type = type;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int res = 0;
        if(viewType == TYPE_MEETING){
            res =  R.layout.item_work_meeting;
        }else if(viewType == TYPE_PPT){
            res =  R.layout.item_work_meeting;
        }else if(viewType == TYPE_DRAWING){
            res =  R.layout.item_work_drawing;
        }else if(viewType == TYPE_TRANSLATE){
            res =  R.layout.item_work_meeting;
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(res, parent, false);
        return new ChatViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: position=" + position);
        HistoryItem data = list.get(position);
//        setUI(holder, message, position);

                ((ChatViewHolder) holder).bind(data);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
//        return list.get(position).getType();
        return type;
    }

    public  class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_title;
        public TextView tv_content;
        public TextView tv_date;
        public ImageView iv_content;
        public View ll_card;


        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_content = itemView.findViewById(R.id.tv_content);
            tv_date = itemView.findViewById(R.id.tv_date);
            ll_card = itemView.findViewById(R.id.ll_card);
            iv_content = itemView.findViewById(R.id.iv_content);

        }


        void bind(HistoryItem data) {

            ShadowUtils.applyDefaultShadow(ll_card,context);
//        String url = data.getIcon();
//        ImageUtil.netRadius(context,url,iv_cover);
//        tv_title.setText(data.getModelName());
//        tv_des.setText(data.getDescription());
            tv_title.setText(data.getTitle());
            if(iv_content != null){
                ImageUtil.netRadius(context, data.getImageUrl(), iv_content);
            }
//            tv_content.setText(data.ge());

        }
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
//                    handleOptionClick(position, content);
                }));

        int xOffset = ZDpUtils.dpToPx2(context, POPUP_DISTANCE + POPUP_WIDTH);
        int yOffset = ZDpUtils.dpToPx2(context, POPUP_HEIGHT);
        popupWindow.showAsDropDown(anchor, -xOffset, -yOffset);
    }


}
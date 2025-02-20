package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;
import com.example.project.models.ChatMessage;
import com.example.project.utils.DateTimeUtils;
import java.util.List;

/**
 * 聊天界面的 RecyclerView 适配器
 * 用于显示用户与 AI 助手之间的对话消息
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    // 定义消息类型常量
    private static final int VIEW_TYPE_USER = 1;  // 用户发送的消息
    private static final int VIEW_TYPE_AI = 2;    // AI 助手的回复消息
    
    // 存储聊天消息列表
    private final List<ChatMessage> messages;

    /**
     * 构造函数
     * @param messages 聊天消息列表
     */
    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    /**
     * 根据消息发送者确定视图类型
     * @param position 消息在列表中的位置
     * @return 返回视图类型（用户消息或 AI 消息）
     */
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    /**
     * 创建 ViewHolder
     * 根据不同的消息类型加载不同的布局文件
     */
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 根据消息类型选择对应的布局文件
        int layoutId = viewType == VIEW_TYPE_USER ? 
                R.layout.item_chat_user : R.layout.item_chat_ai;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        return new ChatViewHolder(view);
    }

    /**
     * 绑定数据到 ViewHolder
     * 设置消息内容和发送时间
     */
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.messageText.setText(message.getContent());
        holder.timeText.setText(DateTimeUtils.formatTimestamp(message.getTimestamp()));
    }

    /**
     * 获取消息列表的大小
     * @return 消息数量
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * 聊天消息的 ViewHolder
     * 持有消息内容和时间的视图引用
     */
    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;  // 消息内容文本视图
        TextView timeText;     // 发送时间文本视图

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
        }
    }
} 
package com.example.projectv2.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectv2.R;
import com.example.projectv2.model.Message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private static final long TEN_MINUTES = 10 * 60 * 1000; // 10分钟的毫秒数
    private SimpleDateFormat fullDateFormat;
    private SimpleDateFormat timeOnlyFormat;
    private Calendar calendar;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
        this.fullDateFormat = new SimpleDateFormat("yyyy/MM/dd    HH:mm", Locale.CHINESE);
        this.timeOnlyFormat = new SimpleDateFormat("HH:mm", Locale.CHINESE);
        this.calendar = Calendar.getInstance();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageText.setText(message.getContent());

        // 设置消息的对齐方式和背景
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();
        if (message.isAi()) {
            params.gravity = Gravity.START;
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_ai);
        } else {
            params.gravity = Gravity.END;
            holder.messageText.setBackgroundResource(R.drawable.message_bubble_user);
        }
        holder.messageText.setLayoutParams(params);

        // 处理时间显示
        boolean shouldShowTime = false;
        String timeText = "";

        if (position == 0) {
            // 第一条消息总是显示完整日期
            shouldShowTime = true;
            timeText = fullDateFormat.format(new Date(message.getTimestamp()));
        } else {
            Message previousMessage = messages.get(position - 1);
            long timeDiff = message.getTimestamp() - previousMessage.getTimestamp();
            
            if (timeDiff >= TEN_MINUTES) {
                shouldShowTime = true;
                // 检查是否是同一天
                Calendar currentMessageTime = Calendar.getInstance();
                Calendar previousMessageTime = Calendar.getInstance();
                currentMessageTime.setTimeInMillis(message.getTimestamp());
                previousMessageTime.setTimeInMillis(previousMessage.getTimestamp());

                if (isSameDay(currentMessageTime, previousMessageTime)) {
                    timeText = timeOnlyFormat.format(new Date(message.getTimestamp()));
                } else {
                    timeText = fullDateFormat.format(new Date(message.getTimestamp()));
                }
            }
        }

        holder.timeText.setVisibility(shouldShowTime ? View.VISIBLE : View.GONE);
        holder.timeText.setText(timeText);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
} 
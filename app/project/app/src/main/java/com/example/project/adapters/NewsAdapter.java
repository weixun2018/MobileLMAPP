package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;
import com.example.project.models.NewsItem;
import java.util.List;

/**
 * 心理健康资讯列表的 RecyclerView 适配器
 * 用于显示心理学相关的新闻和文章列表
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    // 存储新闻列表数据
    private List<NewsItem> newsList;
    // 新闻项点击事件监听器
    private OnNewsItemClickListener listener;

    /**
     * 新闻项点击事件的接口定义
     * 用于处理新闻列表项的点击事件
     */
    public interface OnNewsItemClickListener {
        /**
         * 当新闻项被点击时调用
         * @param newsItem 被点击的新闻项数据
         */
        void onNewsItemClick(NewsItem newsItem);
    }

    /**
     * 构造函数
     * @param newsList 新闻数据列表
     * @param listener 点击事件监听器
     */
    public NewsAdapter(List<NewsItem> newsList, OnNewsItemClickListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    /**
     * 创建 ViewHolder
     * 加载新闻列表项的布局文件
     */
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    /**
     * 绑定数据到 ViewHolder
     * 设置新闻标题、日期，并处理点击事件
     */
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);
        // 设置新闻标题和日期
        holder.titleText.setText(newsItem.getTitle());
        holder.dateText.setText(newsItem.getDate());
        
        // 设置点击事件监听器
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewsItemClick(newsItem);
            }
        });
    }

    /**
     * 获取新闻列表的大小
     * @return 新闻数量
     */
    @Override
    public int getItemCount() {
        return newsList.size();
    }

    /**
     * 新闻列表项的 ViewHolder
     * 持有新闻标题和日期的视图引用
     */
    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;  // 新闻标题文本视图
        TextView dateText;   // 发布日期文本视图

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.news_title);
            dateText = itemView.findViewById(R.id.news_date);
        }
    }
} 
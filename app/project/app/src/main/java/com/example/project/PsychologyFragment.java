package com.example.project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.project.adapters.NewsAdapter;
import com.example.project.models.NewsItem;
import com.example.project.utils.ApiConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * 心理学资讯界面 Fragment
 * 负责展示心理学相关的新闻和文章列表
 * 包含下拉刷新和文章链接跳转功能
 */
public class PsychologyFragment extends Fragment implements NewsAdapter.OnNewsItemClickListener {
    // UI 组件
    private RecyclerView newsRecyclerView;       // 新闻列表视图
    private SwipeRefreshLayout swipeRefreshLayout; // 下拉刷新布局
    private NewsAdapter newsAdapter;              // 新闻列表适配器
    private List<NewsItem> newsList;             // 新闻数据列表

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_psychology, container, false);
        
        initViews(view);        // 初始化视图组件
        setupRecyclerView();    // 设置列表视图
        loadNews();            // 加载新闻数据
        
        return view;
    }

    /**
     * 初始化视图组件
     * 绑定 UI 元素并设置下拉刷新监听器
     */
    private void initViews(View view) {
        newsRecyclerView = view.findViewById(R.id.news_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        
        // 设置下拉刷新监听器，触发新闻刷新
        swipeRefreshLayout.setOnRefreshListener(this::loadNews);
    }

    /**
     * 设置新闻列表视图
     * 初始化适配器和布局管理器
     */
    private void setupRecyclerView() {
        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(newsList, this);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsRecyclerView.setAdapter(newsAdapter);
    }

    /**
     * 加载新闻数据
     * 从服务器获取最新的心理学资讯
     * 支持下拉刷新和错误重试
     */
    private void loadNews() {
        swipeRefreshLayout.setRefreshing(true);
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                ApiConfig.PSYCHOLOGY_URL + "news", null,
                response -> {
                    swipeRefreshLayout.setRefreshing(false);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray newsArray = response.getJSONArray("news");
                            newsList.clear();
                            
                            // 解析新闻数据并添加到列表
                            for (int i = 0; i < newsArray.length(); i++) {
                                JSONObject newsObj = newsArray.getJSONObject(i);
                                newsList.add(new NewsItem(
                                    newsObj.getString("title"),
                                    newsObj.getString("url"),
                                    newsObj.getString("date")
                                ));
                            }
                            
                            newsAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing news data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Error loading news", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * 处理新闻项点击事件
     * 使用系统浏览器打开新闻链接
     * 
     * @param newsItem 被点击的新闻项
     */
    @Override
    public void onNewsItemClick(NewsItem newsItem) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getUrl()));
        startActivity(intent);
    }
} 
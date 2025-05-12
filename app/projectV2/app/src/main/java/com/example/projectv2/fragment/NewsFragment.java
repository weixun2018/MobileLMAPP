package com.example.projectv2.fragment;

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

import com.example.projectv2.R;
import com.example.projectv2.adapter.NewsAdapter;
import com.example.projectv2.api.ApiClient;
import com.example.projectv2.model.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment {
    
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;

    public static NewsFragment newInstance() {
        return new NewsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化视图
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        newsRecyclerView = view.findViewById(R.id.newsRecyclerView);

        // 设置RecyclerView
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        newsAdapter = new NewsAdapter(new ArrayList<>());
        newsRecyclerView.setAdapter(newsAdapter);

        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(this::refreshNews);

        // 加载新闻数据
        loadNews();
    }

    private void loadNews() {
        ApiClient.getNewsApi().getLatestNews().enqueue(new Callback<List<News>>() {
            @Override
            public void onResponse(Call<List<News>> call, Response<List<News>> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        newsAdapter.updateNews(response.body());
                    } else {
                        showError("获取新闻失败");
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<List<News>> call, Throwable t) {
                if (isAdded()) {
                    showError("网络错误: " + t.getMessage());
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void refreshNews() {
        ApiClient.getNewsApi().refreshNews().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        loadNews();
                    } else {
                        showError("刷新新闻失败");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) {
                    showError("网络错误: " + t.getMessage());
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void showError(String message) {
        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
} 
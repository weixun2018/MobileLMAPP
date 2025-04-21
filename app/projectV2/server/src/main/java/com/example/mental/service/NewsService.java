package com.example.mental.service;

import com.example.mental.entity.News;
import java.util.List;

public interface NewsService {
    List<News> getLatestNews();
    void refreshNews();
} 
package com.example.bluecat.service;

import com.example.bluecat.entity.News;
import java.util.List;

public interface NewsService {
    List<News> getLatestNews();
    void refreshNews();
} 
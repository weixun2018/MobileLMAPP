package com.example.bluecat.service;

import com.example.bluecat.dto.NewsDTO;
import java.util.List;

public interface NewsService {
    List<NewsDTO> getLatestNews();
    void refreshNews();
} 
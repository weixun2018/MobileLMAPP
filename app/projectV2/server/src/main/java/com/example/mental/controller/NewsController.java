package com.example.mental.controller;

import com.example.mental.entity.News;
import com.example.mental.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<List<News>> getLatestNews() {
        return ResponseEntity.ok(newsService.getLatestNews());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshNews() {
        newsService.refreshNews();
        return ResponseEntity.ok().build();
    }
} 
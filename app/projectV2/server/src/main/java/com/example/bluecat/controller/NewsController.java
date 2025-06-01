package com.example.bluecat.controller;

import com.example.bluecat.dto.NewsDTO;
import com.example.bluecat.service.NewsService;
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
    public ResponseEntity<List<NewsDTO>> getLatestNews() {
        return ResponseEntity.ok(newsService.getLatestNews());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshNews() {
        newsService.refreshNews();
        return ResponseEntity.ok().build();
    }
} 
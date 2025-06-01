package com.example.bluecat.dto;

import lombok.Data;

@Data
public class NewsDTO {
    private Long id;
    private String title;
    private String url;
    private String publishDate;
    // 注意：排除了createdAt字段
} 
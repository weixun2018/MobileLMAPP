package com.example.project.models;

/**
 * 心理健康资讯实体类
 * 用于存储心理学相关的新闻和文章信息
 * 包含标题、链接和发布日期等基本信息
 */
public class NewsItem {
    private String title;  // 新闻标题
    private String url;    // 新闻链接地址
    private String date;   // 发布日期

    /**
     * 新闻项构造函数
     * @param title 新闻标题
     * @param url 新闻详情页的 URL 链接
     * @param date 新闻发布日期（格式：yyyy-MM-dd）
     */
    public NewsItem(String title, String url, String date) {
        this.title = title;
        this.url = url;
        this.date = date;
    }

    /**
     * 获取新闻标题
     * @return 新闻的标题文本
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取新闻链接
     * @return 新闻详情页的完整 URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取发布日期
     * @return 新闻的发布日期字符串
     */
    public String getDate() {
        return date;
    }
} 
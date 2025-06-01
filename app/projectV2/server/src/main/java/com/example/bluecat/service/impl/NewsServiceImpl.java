package com.example.bluecat.service.impl;

import com.example.bluecat.dto.NewsDTO;
import com.example.bluecat.entity.News;
import com.example.bluecat.mapper.NewsMapper;
import com.example.bluecat.service.NewsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {

    private final NewsMapper newsMapper;
    private final ObjectMapper objectMapper;
    
    // 本地开发环境路径
    private static final String LOCAL_PYTHON_SCRIPT_PATH = "src/main/python/news_crawler.py";
    // 服务器部署环境路径（项目组成员的修改）
    private static final String SERVER_PYTHON_SCRIPT_PATH = "/app/news_crawler.py";

    @Override
    public List<NewsDTO> getLatestNews() {
        List<News> newsList = newsMapper.findLatestNews(20);
        return newsList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void refreshNews() {
        try {
            log.info("开始刷新新闻数据");

            // 创建HTTP客户端
            RestTemplate restTemplate = new RestTemplate();

            // 设置超时时间（根据实际情况调整）
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(30000); // 30秒连接超时
            factory.setReadTimeout(300000);    // 5分钟读取超时（爬虫可能耗时较长）
            restTemplate.setRequestFactory(factory);

            // 设置请求头（如果需要）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // 创建请求实体（无请求体）
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // 发送POST请求到爬虫服务
            // 微服务python爬虫脚本容器
            String CRAWLER_SERVICE_URL = "http://crawler-service:5000/crawl";
            ResponseEntity<String> response = restTemplate.postForEntity(
                    CRAWLER_SERVICE_URL,
                    requestEntity,
                    String.class
            );

            // 检查响应状态
            if (response.getStatusCode() == HttpStatus.OK) {
                String jsonResponse = response.getBody();

                if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                    log.warn("爬虫服务返回空数据");
                    return;
                }

                try {
                    // 解析JSON数据
                    List<News> newsList = objectMapper.readValue(
                            jsonResponse,
                            new TypeReference<List<News>>() {}
                    );

                    // 清除旧数据并保存新数据
                    newsMapper.deleteOldNews(0); // 删除所有旧数据
                    for (News news : newsList) {
                        newsMapper.insert(news);
                    }

                    log.info("新闻数据刷新成功，共更新 {} 条新闻", newsList.size());
                } catch (JsonProcessingException e) {
                    log.error("解析爬虫服务返回的JSON数据失败: {}", e.getMessage());
                    log.error("原始响应内容: {}", jsonResponse);
                }
            } else {
                log.error("爬虫服务返回错误状态码: {}", response.getStatusCodeValue());
                log.error("错误响应: {}", response.getBody());
            }

        } catch (RestClientException e) {
            log.error("调用爬虫服务失败: {}", e.getMessage());
            log.error("异常详情:", e);
        } catch (Exception e) {
            log.error("刷新新闻数据时发生未知错误", e);
        }
    }
    /**
     * 将News实体转换为NewsDTO
     */
    private NewsDTO convertToDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setUrl(news.getUrl());
        dto.setPublishDate(news.getPublishDate());
        return dto;
    }
    
    /**
     * 检测运行环境并返回对应的Python脚本路径
     * 本地环境：src/main/python/news_crawler.py
     * 服务器环境：/app/news_crawler.py （项目组成员的修改）
     */
    private String detectEnvironmentAndGetScriptPath() {
        // 优先检查服务器路径（项目组成员的修改）
        File serverScript = new File(SERVER_PYTHON_SCRIPT_PATH);
        if (serverScript.exists()) {
            log.info("检测到服务器环境，使用服务器脚本路径");
            return SERVER_PYTHON_SCRIPT_PATH;
        }
        
        // 检查本地路径
        File localScript = new File(LOCAL_PYTHON_SCRIPT_PATH);
        if (localScript.exists()) {
            log.info("检测到本地环境，使用本地脚本路径");
            return LOCAL_PYTHON_SCRIPT_PATH;
        }
        
        // 默认返回本地路径（向后兼容）
        log.warn("未检测到Python脚本文件，使用默认本地路径");
        return LOCAL_PYTHON_SCRIPT_PATH;
    }
    
    /**
     * 检测可用的Python命令
     * 服务器环境通常使用python3（项目组成员的修改）
     * 本地环境可能使用python或python3
     */
    private String detectPythonCommand() {
        // 尝试python3命令（项目组成员认为服务器环境需要的）
        if (isPythonCommandAvailable("python3")) {
            log.info("检测到python3命令可用");
            return "python3";
        }
        
        // 尝试python命令（本地环境通常可用）
        if (isPythonCommandAvailable("python")) {
            log.info("检测到python命令可用");
            return "python";
        }
        
        // 默认使用python3（项目组成员的修改）
        log.warn("未检测到可用的Python命令，使用默认python3");
        return "python3";
    }
    
    /**
     * 检测指定的Python命令是否可用
     */
    private boolean isPythonCommandAvailable(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{command, "--version"});
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
} 

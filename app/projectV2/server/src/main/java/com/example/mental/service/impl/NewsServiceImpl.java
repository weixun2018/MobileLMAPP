package com.example.mental.service.impl;

import com.example.mental.entity.News;
import com.example.mental.repository.NewsRepository;
import com.example.mental.service.NewsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final ObjectMapper objectMapper;
    private static final String PYTHON_SCRIPT_PATH = "src/main/python/news_crawler.py";

    @Override
    public List<News> getLatestNews() {
        return newsRepository.findLatestNews();
    }

    @Override
    @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void refreshNews() {
        try {
            log.info("开始刷新新闻数据");
            
            // 执行Python脚本
            Process process = Runtime.getRuntime().exec("python " + PYTHON_SCRIPT_PATH);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder output = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // 读取错误输出
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            
            // 等待Python脚本执行完成
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // 解析JSON数据
                List<News> newsList = objectMapper.readValue(
                    output.toString(),
                    new TypeReference<List<News>>() {}
                );
                
                // 清除旧数据并保存新数据
                newsRepository.deleteAll();
                newsRepository.saveAll(newsList);
                
                log.info("新闻数据刷新成功，共更新 {} 条新闻", newsList.size());
            } else {
                log.error("Python脚本执行失败，退出码：{}，错误信息：{}", exitCode, errorOutput.toString());
            }
            
        } catch (Exception e) {
            log.error("刷新新闻数据时发生错误", e);
        }
    }
} 
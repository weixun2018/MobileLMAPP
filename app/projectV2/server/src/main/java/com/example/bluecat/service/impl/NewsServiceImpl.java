package com.example.bluecat.service.impl;

import com.example.bluecat.entity.News;
import com.example.bluecat.mapper.NewsMapper;
import com.example.bluecat.service.NewsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

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
    public List<News> getLatestNews() {
        return newsMapper.findLatestNews(20);
    }

    @Override
    @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void refreshNews() {
        try {
            log.info("开始刷新新闻数据");
            
            // 自动检测运行环境并选择合适的脚本路径和Python命令
            String pythonScriptPath = detectEnvironmentAndGetScriptPath();
            String pythonCommand = detectPythonCommand();
            
            log.info("使用Python脚本路径: {}", pythonScriptPath);
            log.info("使用Python命令: {}", pythonCommand);
            
            // 执行Python脚本
            Process process = Runtime.getRuntime().exec(new String[]{pythonCommand, pythonScriptPath});
            
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
                String jsonOutput = output.toString();
                if (jsonOutput.trim().isEmpty()) {
                    log.warn("Python脚本返回空数据");
                    return;
                }
                
                List<News> newsList = objectMapper.readValue(
                    jsonOutput,
                    new TypeReference<List<News>>() {}
                );
                
                // 清除旧数据并保存新数据
                newsMapper.deleteOldNews(0); // 删除所有旧数据
                for (News news : newsList) {
                    newsMapper.insert(news);
                }
                
                log.info("新闻数据刷新成功，共更新 {} 条新闻", newsList.size());
            } else {
                log.error("Python脚本执行失败，退出码：{}，错误信息：{}", exitCode, errorOutput.toString());
            }
            
        } catch (Exception e) {
            log.error("刷新新闻数据时发生错误", e);
        }
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

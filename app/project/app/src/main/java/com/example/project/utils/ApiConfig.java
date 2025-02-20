package com.example.project.utils;

/**
 * API 接口配置类
 * 存储所有后端 API 接口的基础 URL 和具体端点地址
 * 使用单例模式防止实例化，所有 URL 都是静态常量
 */
public class ApiConfig {
    /**
     * API 服务器基础地址
     * 10.0.2.2 是 Android 模拟器访问本机服务的特殊 IP
     * 5000 是后端服务器的默认端口
     */
    private static final String BASE_URL = "http://10.0.2.2:5000/api/";
    
    /**
     * 用户认证相关的 API 端点
     * 包括登录、注册、密码重置等功能
     */
    public static final String AUTH_URL = BASE_URL + "auth/";

    /**
     * 聊天功能相关的 API 端点
     * 用于与 AI 助手进行对话交互
     */
    public static final String CHAT_URL = BASE_URL + "chat/";

    /**
     * 用户资料相关的 API 端点
     * 用于管理用户个人信息
     */
    public static final String PROFILE_URL = BASE_URL + "profile/";

    /**
     * MBTI 测试相关的 API 端点
     * 包括获取题目、提交答案、查看结果等功能
     */
    public static final String MBTI_URL = BASE_URL + "mbti/";

    /**
     * 心理学资讯相关的 API 端点
     * 用于获取心理健康新闻和文章
     */
    public static final String PSYCHOLOGY_URL = BASE_URL + "psychology/";
    
    /**
     * 私有构造函数
     * 防止类被实例化，保持配置的统一性
     */
    private ApiConfig() {
        // Private constructor to prevent instantiation
    }
} 
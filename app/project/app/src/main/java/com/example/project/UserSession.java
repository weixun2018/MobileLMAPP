package com.example.project;

/**
 * 用户会话管理类
 * 使用单例模式管理用户登录状态和认证令牌
 * 确保整个应用程序使用同一个用户会话实例
 */
public class UserSession {
    // 单例实例
    private static UserSession instance;
    
    // 用户认证令牌
    private String token;

    /**
     * 私有构造函数
     * 防止外部直接创建实例，确保单例模式
     */
    private UserSession() {
        // 私有构造函数，不允许外部实例化
    }

    /**
     * 获取 UserSession 单例实例
     * 如果实例不存在则创建新实例
     * 
     * @return UserSession 的唯一实例
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * 用户登录
     * 保存用户认证令牌
     * 
     * @param token 用户认证令牌
     */
    public void login(String token) {
        this.token = token;
    }

    /**
     * 用户登出
     * 清除认证令牌
     */
    public void logout() {
        this.token = null;
    }

    /**
     * 获取认证令牌
     * 用于网络请求的认证
     * 
     * @return 当前用户的认证令牌，未登录时返回 null
     */
    public String getToken() {
        return token;
    }

    /**
     * 检查用户是否已登录
     * 
     * @return true 表示已登录，false 表示未登录
     */
    public boolean isLoggedIn() {
        return token != null;
    }
} 
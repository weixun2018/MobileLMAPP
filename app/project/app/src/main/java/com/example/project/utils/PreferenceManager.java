package com.example.project.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用户偏好设置管理类
 * 负责管理用户登录凭证的持久化存储
 * 使用 Android SharedPreferences 实现数据的本地存储
 */
public class PreferenceManager {
    // SharedPreferences 文件名
    private static final String PREF_NAME = "UserPrefs";
    
    // 存储键名常量
    private static final String KEY_REMEMBER_PASSWORD = "remember_password";  // 是否记住密码的标志
    private static final String KEY_USERNAME = "username";                    // 用户名
    private static final String KEY_PASSWORD = "password";                    // 密码

    // SharedPreferences 实例
    private final SharedPreferences preferences;

    /**
     * 构造函数
     * @param context 应用程序上下文，用于获取 SharedPreferences 实例
     */
    public PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存登录凭证
     * 根据 remember 参数决定是否持久化存储用户名和密码
     * 
     * @param username 用户名
     * @param password 密码
     * @param remember 是否记住密码
     */
    public void saveLoginCredentials(String username, String password, boolean remember) {
        SharedPreferences.Editor editor = preferences.edit();
        if (remember) {
            // 如果选择记住密码，则保存所有信息
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_REMEMBER_PASSWORD, true);
        } else {
            // 如果不记住密码，则清除所有存储的信息
            editor.clear();
        }
        editor.apply();
    }

    /**
     * 获取存储的用户名
     * @return 返回存储的用户名，如果没有则返回空字符串
     */
    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    /**
     * 获取存储的密码
     * @return 返回存储的密码，如果没有则返回空字符串
     */
    public String getPassword() {
        return preferences.getString(KEY_PASSWORD, "");
    }

    /**
     * 检查是否记住密码
     * @return 返回是否记住密码的标志
     */
    public boolean isRememberPassword() {
        return preferences.getBoolean(KEY_REMEMBER_PASSWORD, false);
    }

    /**
     * 清除所有存储的登录信息
     * 用于用户注销时清除本地存储的凭证
     */
    public void clear() {
        preferences.edit().clear().apply();
    }
} 
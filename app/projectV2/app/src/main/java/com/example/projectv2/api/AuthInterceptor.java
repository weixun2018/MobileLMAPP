package com.example.projectv2.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * JWT认证拦截器
 * 自动在所有API请求中添加JWT令牌
 */
public class AuthInterceptor implements Interceptor {
    
    private Context context;
    
    public AuthInterceptor(Context context) {
        this.context = context;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // 获取存储的JWT令牌
        SharedPreferences prefs = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        
        // 如果没有token或者是登录/注册请求，直接执行原始请求
        if (token == null || isAuthRequest(originalRequest.url().toString())) {
            return chain.proceed(originalRequest);
        }
        
        // 添加Authorization头部
        Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
                
        return chain.proceed(authenticatedRequest);
    }
    
    /**
     * 判断是否为认证相关的请求（登录、注册）
     */
    private boolean isAuthRequest(String url) {
        return url.contains("/api/user/login") || 
               url.contains("/api/user/register");
    }
} 
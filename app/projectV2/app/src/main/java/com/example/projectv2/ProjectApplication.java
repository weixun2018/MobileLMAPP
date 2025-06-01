package com.example.projectv2;

import android.app.Application;
import com.example.projectv2.api.ApiClient;

public class ProjectApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化ApiClient
        ApiClient.init(this);
    }
} 
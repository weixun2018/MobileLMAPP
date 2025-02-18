package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主界面活动
 * 管理底部导航栏和不同功能模块的 Fragment 切换
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化底部导航栏
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        /**
         * 设置底部导航栏的选项切换监听器
         * 根据用户选择加载对应的功能模块 Fragment
         */
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            
            // 根据选中的菜单项 ID 创建对应的 Fragment
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_ai_chat) {
                selectedFragment = new AIChatFragment();
            } else if (itemId == R.id.navigation_mbti) {
                selectedFragment = new MBTITestFragment();
            } else if (itemId == R.id.navigation_psychology) {
                selectedFragment = new PsychologyFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            // 如果成功创建了 Fragment，执行切换
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
                return true;
            }
            
            return false;
        });

        /**
         * 首次启动时显示默认 Fragment
         * 仅在活动首次创建时执行，避免配置变更时重复加载
         */
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AIChatFragment())
                .commit();
        }
    }
} 
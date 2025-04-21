package com.example.projectv2;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.projectv2.fragment.AiChatFragment;
import com.example.projectv2.fragment.MbtiFragment;
import com.example.projectv2.fragment.NewsFragment;
import com.example.projectv2.fragment.ProfileFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // 默认选中AI心理咨询页面
        if (savedInstanceState == null) {
            loadFragment(AiChatFragment.newInstance());
        }
        bottomNavigationView.setSelectedItemId(R.id.navigation_ai_chat);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_ai_chat) {
            fragment = AiChatFragment.newInstance();
        } else if (itemId == R.id.navigation_mbti) {
            fragment = MbtiFragment.newInstance();
        } else if (itemId == R.id.navigation_news) {
            fragment = NewsFragment.newInstance();
        } else if (itemId == R.id.navigation_profile) {
            fragment = ProfileFragment.newInstance();
        }
        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
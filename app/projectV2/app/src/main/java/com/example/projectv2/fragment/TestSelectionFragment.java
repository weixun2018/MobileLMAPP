package com.example.projectv2.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.projectv2.R;

public class TestSelectionFragment extends Fragment {
    
    private CardView mbtiCard, sclCard;
    private TextView mbtiTitle, mbtiDesc, sclTitle, sclDesc;

    public static TestSelectionFragment newInstance() {
        return new TestSelectionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化视图
        mbtiCard = view.findViewById(R.id.mbti_card);
        sclCard = view.findViewById(R.id.scl_card);
        mbtiTitle = view.findViewById(R.id.mbti_title);
        mbtiDesc = view.findViewById(R.id.mbti_description);
        sclTitle = view.findViewById(R.id.scl_title);
        sclDesc = view.findViewById(R.id.scl_description);

        // 设置MBTI卡片点击事件
        mbtiCard.setOnClickListener(v -> {
            // 打开MBTI测试Fragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, MbtiFragment.newInstance());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // 设置SCL-90卡片点击事件
        sclCard.setOnClickListener(v -> {
            // 打开SCL-90测试Fragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, SCL90Fragment.newInstance());
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
} 
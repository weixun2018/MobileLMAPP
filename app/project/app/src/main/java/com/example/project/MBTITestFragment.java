package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.project.utils.ApiConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.volley.DefaultRetryPolicy;

/**
 * MBTI 测试界面 Fragment
 * 负责管理 MBTI 人格测试的问题展示、答案收集和结果处理
 * 包含测试进度跟踪和已有结果的展示功能
 */
public class MBTITestFragment extends Fragment {
    // UI 组件
    private TextView progressText;     // 进度文本显示
    private ProgressBar progressBar;   // 进度条
    private TextView questionText;     // 问题文本
    private Button optionAButton;      // 选项A按钮
    private Button optionBButton;      // 选项B按钮
    
    // 测试数据
    private List<JSONObject> questions;           // 问题列表
    private List<Map<String, String>> answers;    // 用户答案列表
    private int currentQuestionIndex = 0;         // 当前问题索引

    /**
     * 创建 Fragment 视图
     * 初始化 UI 组件并检查是否存在历史测试结果
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mbti_test, container, false);
        
        initViews(view);
        checkExistingResult();  // 检查是否有历史测试结果
        
        return view;
    }

    /**
     * 初始化视图组件
     * 设置按钮点击监听器和进度显示
     */
    private void initViews(View view) {
        progressText = view.findViewById(R.id.progress_text);
        progressBar = view.findViewById(R.id.progress_bar);
        questionText = view.findViewById(R.id.question_text);
        optionAButton = view.findViewById(R.id.option_a_button);
        optionBButton = view.findViewById(R.id.option_b_button);

        optionAButton.setOnClickListener(v -> answerQuestion("A"));
        optionBButton.setOnClickListener(v -> answerQuestion("B"));
    }

    /**
     * 检查用户是否已有 MBTI 测试结果
     * 如果有结果则显示结果页面，否则加载新的测试问题
     */
    private void checkExistingResult() {
        String url = ApiConfig.MBTI_URL + "result";
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            if (response.getBoolean("hasResult")) {
                                // 显示已有的测试结果
                                JSONObject result = response.getJSONObject("result");
                                showExistingResult(
                                    result.getString("mbti_type"),
                                    result.getJSONObject("scores")
                                );
                            } else {
                                // 开始新的测试
                                loadQuestions();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error checking MBTI result", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Error checking MBTI result", Toast.LENGTH_SHORT).show();
                    loadQuestions();  // 即使检查失败也加载问题
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * 显示已有的测试结果
     * 隐藏测试相关的 UI 组件，显示结果和重测选项
     * 
     * @param mbtiType MBTI 人格类型
     * @param scores 各维度的得分
     */
    private void showExistingResult(String mbtiType, JSONObject scores) {
        // 隐藏测试相关视图
        questionText.setVisibility(View.GONE);
        optionAButton.setVisibility(View.GONE);
        optionBButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);

        // 显示结果和重测选项
        View resultView = getLayoutInflater().inflate(R.layout.layout_existing_mbti, null);
        
        // Create layout params for center positioning
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        
        // Add the view to the parent layout with proper params
        ViewGroup parentLayout = (ViewGroup) questionText.getParent();
        parentLayout.addView(resultView, params);

        TextView typeText = resultView.findViewById(R.id.existing_mbti_type);
        Button retakeButton = resultView.findViewById(R.id.retake_test_button);
        Button viewDetailsButton = resultView.findViewById(R.id.view_details_button);

        typeText.setText(getString(R.string.mbti_type_format, mbtiType));

        retakeButton.setOnClickListener(v -> {
            // Remove result view
            ((ViewGroup) resultView.getParent()).removeView(resultView);
            // Show test-related views
            questionText.setVisibility(View.VISIBLE);
            optionAButton.setVisibility(View.VISIBLE);
            optionBButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            // Load questions
            loadQuestions();
        });

        viewDetailsButton.setOnClickListener(v -> {
            // Show detailed results
            showResult(mbtiType, scores);
        });
    }

    /**
     * 加载测试问题
     * 从服务器获取问题列表并初始化测试状态
     */
    private void loadQuestions() {
        String url = ApiConfig.MBTI_URL + "questions";
        System.out.println("Loading MBTI questions from: " + url);
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null,
                response -> {
                    try {
                        System.out.println("Got response: " + response.toString());
                        if (response.getBoolean("success")) {
                            JSONArray questionsArray = response.getJSONArray("questions");
                            System.out.println("Questions array length: " + questionsArray.length());
                            questions = new ArrayList<>();
                            answers = new ArrayList<>();
                            
                            for (int i = 0; i < questionsArray.length(); i++) {
                                questions.add(questionsArray.getJSONObject(i));
                            }
                            
                            if (questions.isEmpty()) {
                                Toast.makeText(getContext(), "No questions available", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            progressBar.setMax(questions.size());
                            showQuestion(0);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing response: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error loading questions: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    System.err.println("Network error: " + error.getMessage());
                    error.printStackTrace();
                    Toast.makeText(getContext(), 
                        "Error loading questions: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
            10000,  // 10 seconds timeout
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * 显示指定索引的问题
     * 更新问题文本、选项按钮和进度显示
     * 
     * @param index 问题索引
     */
    private void showQuestion(int index) {
        try {
            JSONObject question = questions.get(index);
            questionText.setText(question.getString("question_text"));
            optionAButton.setText(question.getString("option_a"));
            optionBButton.setText(question.getString("option_b"));
            
            progressText.setText(getString(R.string.question_progress, index + 1, questions.size()));
            progressBar.setProgress(index + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理用户的答案选择
     * 记录答案并切换到下一个问题或提交结果
     * 
     * @param choice 用户选择的选项（"A" 或 "B"）
     */
    private void answerQuestion(String choice) {
        try {
            JSONObject question = questions.get(currentQuestionIndex);
            Map<String, String> answer = new HashMap<>();
            answer.put("dimension", question.getString("dimension"));
            answer.put("choice", choice);
            answers.add(answer);

            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                showQuestion(currentQuestionIndex);
            } else {
                submitAnswers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 提交测试答案
     * 将收集的答案发送到服务器并处理结果
     */
    private void submitAnswers() {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONArray answersArray = new JSONArray();
            
            for (Map<String, String> answer : answers) {
                JSONObject answerObj = new JSONObject();
                answerObj.put("dimension", answer.get("dimension"));
                answerObj.put("choice", answer.get("choice"));
                answersArray.put(answerObj);
            }
            
            jsonBody.put("answers", answersArray);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    ApiConfig.MBTI_URL + "submit", jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                String mbtiType = response.getString("mbti_type");
                                JSONObject scores = response.getJSONObject("scores");
                                
                                // Show result page
                                showResult(mbtiType, scores);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(getContext(), R.string.error_submitting_answers, Toast.LENGTH_SHORT).show()) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
                    return headers;
                }
            };

            Volley.newRequestQueue(requireContext()).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示测试结果
     * 创建结果页面并传递测试数据
     * 
     * @param mbtiType MBTI 人格类型
     * @param scores 各维度的得分
     */
    private void showResult(String mbtiType, JSONObject scores) {
        // Switch to result page
        MBTIResultFragment resultFragment = new MBTIResultFragment();
        Bundle args = new Bundle();
        args.putString("mbti_type", mbtiType);
        args.putString("scores", scores.toString());
        resultFragment.setArguments(args);
        
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, resultFragment)
                .addToBackStack(null)
                .commit();
    }
} 
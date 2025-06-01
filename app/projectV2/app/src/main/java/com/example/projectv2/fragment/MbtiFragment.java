package com.example.projectv2.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectv2.R;
import com.example.projectv2.api.ApiClient;
import com.example.projectv2.model.MbtiQuestion;
import com.example.projectv2.model.MbtiType;
import com.example.projectv2.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MbtiFragment extends Fragment {
    private List<MbtiQuestion> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private Map<String, Integer> dimensionScores = new HashMap<>();
    private Long userId;

    private LinearLayout questionLayout;
    private ScrollView resultLayout;
    private TextView questionProgress;
    private TextView questionText;
    private RadioGroup optionsGroup;
    private RadioButton optionA;
    private RadioButton optionB;
    private Button nextButton;
    private Button previousButton;
    private Button retestButton;
    private Button clearAnswersButton;
    private TextView mbtiTypeText;
    private TextView typeNameText;
    private TextView descriptionText;
    private TextView characteristicsText;
    private TextView strengthsText;
    private TextView weaknessesText;
    
    private static final String PREF_MBTI_ANSWERS = "mbti_answers";
    private static final String PREF_MBTI_CURRENT_INDEX = "mbti_current_index";
    private static final String PREF_MBTI_IN_PROGRESS = "mbti_in_progress";
    private Map<Integer, Boolean> answers = new HashMap<>();
    private boolean isTestCompleted = false;

    public static MbtiFragment newInstance() {
        return new MbtiFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mbti, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        
        questionLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.GONE);
        
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);
        
        if (userId != -1) {
            checkUserMbtiType();
        } else {
            Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUserMbtiType() {
        ApiClient.getUserApi().getUserInfo(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String mbtiType = user.getMbtiType();
                    
                    SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                    boolean isInProgress = prefs.getBoolean(PREF_MBTI_IN_PROGRESS + "_" + userId, false);
                    
                    if (mbtiType != null && !mbtiType.isEmpty()) {
                        loadMbtiTypeResult(mbtiType);
                    } else if (isInProgress) {
                        loadQuestions();
                    } else {
                        questionLayout.setVisibility(View.VISIBLE);
                        loadQuestions();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getContext(), "获取用户信息失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                boolean isInProgress = prefs.getBoolean(PREF_MBTI_IN_PROGRESS + "_" + userId, false);
                
                if (isInProgress) {
                    loadQuestions();
                } else {
                    questionLayout.setVisibility(View.VISIBLE);
                    loadQuestions();
                }
            }
        });
    }

    private void loadMbtiTypeResult(String mbtiType) {
        ApiClient.getUserApi().getMbtiType(mbtiType).enqueue(new Callback<MbtiType>() {
            @Override
            public void onResponse(Call<MbtiType> call, Response<MbtiType> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showResult(response.body());
                    Toast.makeText(getContext(), "已显示您上次的测试结果，可点击下方\"重新测试\"按钮进行新测试", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MbtiType> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "获取MBTI类型信息失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initViews(View view) {
        questionLayout = view.findViewById(R.id.questionLayout);
        resultLayout = view.findViewById(R.id.resultLayout);
        questionProgress = view.findViewById(R.id.questionProgress);
        questionText = view.findViewById(R.id.questionText);
        optionsGroup = view.findViewById(R.id.optionsGroup);
        optionA = view.findViewById(R.id.optionA);
        optionB = view.findViewById(R.id.optionB);
        nextButton = view.findViewById(R.id.nextButton);
        previousButton = view.findViewById(R.id.previousButton);
        retestButton = view.findViewById(R.id.retestButton);
        clearAnswersButton = view.findViewById(R.id.clearAnswersButton);
        mbtiTypeText = view.findViewById(R.id.mbtiTypeText);
        typeNameText = view.findViewById(R.id.typeNameText);
        descriptionText = view.findViewById(R.id.descriptionText);
        characteristicsText = view.findViewById(R.id.characteristicsText);
        strengthsText = view.findViewById(R.id.strengthsText);
        weaknessesText = view.findViewById(R.id.weaknessesText);

        nextButton.setOnClickListener(v -> handleNextQuestion());
        if (previousButton != null) {
            previousButton.setOnClickListener(v -> handlePreviousQuestion());
        }
        retestButton.setOnClickListener(v -> restartTest());

        if (clearAnswersButton != null) {
            clearAnswersButton.setOnClickListener(v -> clearAllAnswers());
        }

        dimensionScores.put("EI", 0);
        dimensionScores.put("SN", 0);
        dimensionScores.put("TF", 0);
        dimensionScores.put("JP", 0);
    }
    
    private void clearAllAnswers() {
        answers.clear();
        currentQuestionIndex = 0;
        dimensionScores.put("EI", 0);
        dimensionScores.put("SN", 0);
        dimensionScores.put("TF", 0);
        dimensionScores.put("JP", 0);
        
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        prefs.edit()
            .remove(PREF_MBTI_ANSWERS + "_" + userId)
            .remove(PREF_MBTI_CURRENT_INDEX + "_" + userId)
            .remove(PREF_MBTI_IN_PROGRESS + "_" + userId)
            .apply();
        
        if (!questions.isEmpty()) {
            showQuestion(0);
        }
        Toast.makeText(getContext(), "已清空所有作答", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (!questions.isEmpty() && userId != -1) {
            restoreAnswersFromLocal();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (answers.size() > 0 && currentQuestionIndex < questions.size() && !isTestCompleted) {
            saveAnswersToLocal();
            Toast.makeText(getContext(), "测试进度已保存，下次可继续", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveAnswersToLocal() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        Gson gson = new Gson();
        String answersJson = gson.toJson(answers);
        
        editor.putString(PREF_MBTI_ANSWERS + "_" + userId, answersJson);
        editor.putInt(PREF_MBTI_CURRENT_INDEX + "_" + userId, currentQuestionIndex);
        editor.putBoolean(PREF_MBTI_IN_PROGRESS + "_" + userId, questionLayout.getVisibility() == View.VISIBLE);
        editor.apply();
    }
    
    private void restoreAnswersFromLocal() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        
        boolean isInProgress = prefs.getBoolean(PREF_MBTI_IN_PROGRESS + "_" + userId, false);
        
        if (isInProgress) {
            if (questions.isEmpty()) {
                loadQuestions();
                Toast.makeText(getContext(), "正在加载问题，请稍后...", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String answersJson = prefs.getString(PREF_MBTI_ANSWERS + "_" + userId, "");
            if (!answersJson.isEmpty()) {
                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<Integer, Boolean>>(){}.getType();
                answers = gson.fromJson(answersJson, type);
                
                recalculateDimensionScores();
                
                System.out.println("恢复答案: " + answersJson);
                System.out.println("恢复的答案数量: " + answers.size());
            }
            
            currentQuestionIndex = prefs.getInt(PREF_MBTI_CURRENT_INDEX + "_" + userId, 0);
            System.out.println("恢复的问题索引: " + currentQuestionIndex);
            
            if (currentQuestionIndex >= questions.size()) {
                currentQuestionIndex = questions.size() - 1;
            }
            
            questionLayout.setVisibility(View.VISIBLE);
            resultLayout.setVisibility(View.GONE);
            isTestCompleted = false;
            
            showQuestion(currentQuestionIndex);
            
            Toast.makeText(getContext(), "已恢复到第 " + (currentQuestionIndex + 1) + " 题", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void recalculateDimensionScores() {
        dimensionScores.put("EI", 0);
        dimensionScores.put("SN", 0);
        dimensionScores.put("TF", 0);
        dimensionScores.put("JP", 0);
        
        for (Map.Entry<Integer, Boolean> entry : answers.entrySet()) {
            int questionIndex = entry.getKey();
            boolean choseOptionA = entry.getValue();
            
            if (questionIndex < questions.size()) {
                MbtiQuestion question = questions.get(questionIndex);
                String dimension = question.getDimension();
                int currentScore = dimensionScores.get(dimension);
                dimensionScores.put(dimension, choseOptionA ? currentScore + 1 : currentScore);
            }
        }
    }

    private void loadQuestions() {
        ApiClient.getUserApi().getMbtiQuestions().enqueue(new Callback<List<MbtiQuestion>>() {
            @Override
            public void onResponse(Call<List<MbtiQuestion>> call, Response<List<MbtiQuestion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    questions = response.body();
                    
                    SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                    boolean isInProgress = prefs.getBoolean(PREF_MBTI_IN_PROGRESS + "_" + userId, false);
                    
                    if (isInProgress) {
                        restoreAnswersFromLocal();
                    } else {
                    showQuestion(0);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MbtiQuestion>> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "加载问题失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showQuestion(int index) {
        if (index < questions.size()) {
            MbtiQuestion question = questions.get(index);
            questionProgress.setText((index + 1) + "/" + questions.size());
            questionText.setText(question.getQuestionText());
            optionA.setText(question.getOptionA());
            optionB.setText(question.getOptionB());
            
            // 打印日志
            System.out.println("显示问题: " + index + ", 问题内容: " + question.getQuestionText());
            
            // 清除之前的选择
            optionsGroup.clearCheck();
            
            // 如果该问题已作答，恢复选择状态
            if (answers.containsKey(index)) {
                Boolean previousAnswer = answers.get(index);
                System.out.println("问题 " + index + " 的答案: " + previousAnswer);
                
                if (previousAnswer != null) {
                    if (previousAnswer) {
                        optionA.setChecked(true);
                        System.out.println("选中选项A");
                    } else {
                        optionB.setChecked(true);
                        System.out.println("选中选项B");
                    }
                }
            }
            
            if (previousButton != null) {
                previousButton.setEnabled(index > 0);
            }
        } else {
            System.out.println("问题索引超出范围: " + index + ", 问题总数: " + questions.size());
        }
    }

    private void handleNextQuestion() {
        if (optionsGroup.getCheckedRadioButtonId() == -1) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "请选择一个选项", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        MbtiQuestion currentQuestion = questions.get(currentQuestionIndex);
        boolean choseOptionA = optionA.isChecked();
        String dimension = currentQuestion.getDimension();
        
        int currentScore = dimensionScores.get(dimension);
        dimensionScores.put(dimension, choseOptionA ? currentScore + 1 : currentScore);
        
        answers.put(currentQuestionIndex, choseOptionA);
        
        saveAnswersToLocal();

        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            showQuestion(currentQuestionIndex);
        } else {
            calculateAndShowResult();
        }
    }
    
    private void handlePreviousQuestion() {
        if (currentQuestionIndex > 0) {
            if (optionsGroup.getCheckedRadioButtonId() != -1) {
                boolean choseOptionA = optionA.isChecked();
                MbtiQuestion currentQuestion = questions.get(currentQuestionIndex);
                String dimension = currentQuestion.getDimension();
                
                int currentScore = dimensionScores.get(dimension);
                if (answers.containsKey(currentQuestionIndex)) {
                    boolean previousAnswer = answers.get(currentQuestionIndex);
                    if (previousAnswer) {
                        dimensionScores.put(dimension, currentScore - 1);
                    }
                }
                
                answers.put(currentQuestionIndex, choseOptionA);
                if (choseOptionA) {
                    dimensionScores.put(dimension, dimensionScores.get(dimension) + 1);
                }
                
                saveAnswersToLocal();
            }
            
            currentQuestionIndex--;
            showQuestion(currentQuestionIndex);
        }
    }

    private void calculateAndShowResult() {
        final String mbtiType = ""
                + (dimensionScores.get("EI") >= 3 ? "E" : "I")
                + (dimensionScores.get("SN") >= 3 ? "S" : "N")
                + (dimensionScores.get("TF") >= 3 ? "T" : "F")
                + (dimensionScores.get("JP") >= 3 ? "J" : "P");

        ApiClient.getUserApi().getMbtiType(mbtiType).enqueue(new Callback<MbtiType>() {
            @Override
            public void onResponse(Call<MbtiType> call, Response<MbtiType> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showResult(response.body());
                    updateUserMbtiType(mbtiType);
                    
                    SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                    prefs.edit()
                        .remove(PREF_MBTI_ANSWERS + "_" + userId)
                        .remove(PREF_MBTI_CURRENT_INDEX + "_" + userId)
                        .remove(PREF_MBTI_IN_PROGRESS + "_" + userId)
                        .apply();
                }
            }

            @Override
            public void onFailure(Call<MbtiType> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "获取结果失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showResult(MbtiType mbtiType) {
        if (getActivity() == null || !isAdded()) return;
        
        getActivity().runOnUiThread(() -> {
            questionLayout.setVisibility(View.GONE);
            resultLayout.setVisibility(View.VISIBLE);
            isTestCompleted = true;

            mbtiTypeText.setText(mbtiType.getTypeCode());
            typeNameText.setText(mbtiType.getTypeName());
            descriptionText.setText(mbtiType.getDescription());
            characteristicsText.setText(mbtiType.getCharacteristics());
            strengthsText.setText(mbtiType.getStrengths());
            weaknessesText.setText(mbtiType.getWeaknesses());
        });
    }

    private void updateUserMbtiType(String mbtiType) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("mbtiType", mbtiType);
        
        ApiClient.getUserApi().updateUserMbtiType(userId, requestBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && getContext() != null) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "MBTI类型更新成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "更新MBTI类型失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "更新MBTI类型失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void restartTest() {
        if (isTestCompleted) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("确认重新测试")
                .setMessage("您已经完成了MBTI测试，确定要重新开始测试吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    checkForPartialAnswers();
                })
                .setNegativeButton("取消", null)
                .show();
        } else {
            checkForPartialAnswers();
        }
    }
    
    private void checkForPartialAnswers() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String answersJson = prefs.getString(PREF_MBTI_ANSWERS + "_" + userId, "");
        boolean hasPartialAnswers = !answersJson.isEmpty();

        if (hasPartialAnswers) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("发现未完成的测试")
                .setMessage("检测到您有未完成的MBTI测试记录，是否恢复之前的作答？")
                .setPositiveButton("恢复作答", (dialog, which) -> {
                    if (questions.isEmpty()) {
                        loadQuestionsAndRestore();
                    } else {
                        restoreAnswersFromLocal();
                    }
                })
                .setNegativeButton("重新开始", (dialog, which) -> {
                    clearAndRestartTest();
                })
                .setCancelable(false)
                .show();
        } else {
            clearAndRestartTest();
        }
    }
    
    private void loadQuestionsAndRestore() {
        Toast.makeText(getContext(), "正在加载问题...", Toast.LENGTH_SHORT).show();
        
        ApiClient.getUserApi().getMbtiQuestions().enqueue(new Callback<List<MbtiQuestion>>() {
            @Override
            public void onResponse(Call<List<MbtiQuestion>> call, Response<List<MbtiQuestion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    questions = response.body();
                    restoreAnswersFromLocal();
                } else {
                    Toast.makeText(getContext(), "加载问题失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MbtiQuestion>> call, Throwable t) {
                Toast.makeText(getContext(), "加载问题失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void clearAndRestartTest() {
        currentQuestionIndex = 0;
        answers.clear();
        dimensionScores.put("EI", 0);
        dimensionScores.put("SN", 0);
        dimensionScores.put("TF", 0);
        dimensionScores.put("JP", 0);
        isTestCompleted = false;
        
        questionLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        prefs.edit()
            .remove(PREF_MBTI_ANSWERS + "_" + userId)
            .remove(PREF_MBTI_CURRENT_INDEX + "_" + userId)
            .remove(PREF_MBTI_IN_PROGRESS + "_" + userId)
            .apply();
        
        prefs.edit().putBoolean(PREF_MBTI_IN_PROGRESS + "_" + userId, true).apply();
        
        if (!questions.isEmpty()) {
            showQuestion(0);
        } else {
        loadQuestions();
        }
        
        Toast.makeText(getContext(), "已开始新测试", Toast.LENGTH_SHORT).show();
    }
} 
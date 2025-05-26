package com.example.projectv2.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.projectv2.model.SCL90Question;
import com.example.projectv2.model.SCL90Result;
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

public class SCL90Fragment extends Fragment {
    
    private List<SCL90Question> questions;
    private int currentQuestionIndex = 0;
    private Map<Integer, Integer> answers = new HashMap<>(); // 问题ID -> 分数
    
    private Long userId;
    
    private LinearLayout questionLayout;
    private ScrollView resultLayout;
    private TextView questionProgress;
    private TextView questionText;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4, option5;
    private Button nextButton;
    private Button previousButton;
    private Button retestButton;
    private Button clearAnswersButton; // 新增清空作答按钮
    private ProgressBar progressBar;
    
    // 结果视图
    private TextView totalScoreText;
    private TextView positiveItemsText;
    private TextView factorScoresText;
    private LinearLayout factorScoresLayout;
    
    // 用于存储作答进度的键名
    private static final String PREF_SCL90_ANSWERS = "scl90_answers";
    private static final String PREF_SCL90_CURRENT_INDEX = "scl90_current_index";
    private static final String PREF_SCL90_IN_PROGRESS = "scl90_in_progress";

    public static SCL90Fragment newInstance() {
        return new SCL90Fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scl90, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        
        // 初始时隐藏所有布局
        questionLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        
        // 从SharedPreferences获取用户ID
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);
        
        if (userId != -1) {
            // 获取SCL-90问题列表
            loadSCL90Questions();
        } else {
            Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }
    
    private void initViews(View view) {
        questionLayout = view.findViewById(R.id.questionLayout);
        resultLayout = view.findViewById(R.id.resultLayout);
        questionProgress = view.findViewById(R.id.questionProgress);
        questionText = view.findViewById(R.id.questionText);
        optionsGroup = view.findViewById(R.id.optionsGroup);
        option1 = view.findViewById(R.id.option1);
        option2 = view.findViewById(R.id.option2);
        option3 = view.findViewById(R.id.option3);
        option4 = view.findViewById(R.id.option4);
        option5 = view.findViewById(R.id.option5);
        nextButton = view.findViewById(R.id.nextButton);
        previousButton = view.findViewById(R.id.previousButton);
        retestButton = view.findViewById(R.id.retestButton);
        clearAnswersButton = view.findViewById(R.id.clearAnswersButton); // 初始化清空作答按钮
        progressBar = view.findViewById(R.id.progressBar);
        
        totalScoreText = view.findViewById(R.id.totalScoreText);
        positiveItemsText = view.findViewById(R.id.positiveItemsText);
        factorScoresText = view.findViewById(R.id.factorScoresText);
        factorScoresLayout = view.findViewById(R.id.factorScoresLayout);
        
        nextButton.setOnClickListener(v -> handleNextQuestion());
        previousButton.setOnClickListener(v -> handlePreviousQuestion());
        retestButton.setOnClickListener(v -> restartTest());
        
        // 设置清空作答按钮点击事件
        if (clearAnswersButton != null) {
            clearAnswersButton.setOnClickListener(v -> clearAllAnswers());
        }
        
        // 初始化空的问题列表
        questions = new ArrayList<>();
    }
    
    // 清空所有作答
    private void clearAllAnswers() {
        // 清空内存中的作答
        answers.clear();
        currentQuestionIndex = 0;
        
        // 清空本地存储
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        prefs.edit()
            .remove(PREF_SCL90_ANSWERS + "_" + userId)
            .remove(PREF_SCL90_CURRENT_INDEX + "_" + userId)
            .remove(PREF_SCL90_IN_PROGRESS + "_" + userId)
            .apply();
        
        // 显示第一题并提示用户
        showQuestion(0);
        Toast.makeText(getContext(), "已清空所有作答", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // 只有当问题列表已加载完毕时才恢复作答状态
        if (questions != null && !questions.isEmpty() && userId != -1) {
            restoreAnswersFromLocal();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        // 保存当前作答状态到本地
        if (userId != -1 && !answers.isEmpty()) {
            saveAnswersToLocal();
        }
    }
    
    // 保存当前作答到本地
    private void saveAnswersToLocal() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // 使用Gson将Map转换为JSON字符串
        Gson gson = new Gson();
        String answersJson = gson.toJson(answers);
        
        // 保存当前作答状态
        editor.putString(PREF_SCL90_ANSWERS + "_" + userId, answersJson);
        editor.putInt(PREF_SCL90_CURRENT_INDEX + "_" + userId, currentQuestionIndex);
        editor.putBoolean(PREF_SCL90_IN_PROGRESS + "_" + userId, questionLayout.getVisibility() == View.VISIBLE);
        editor.apply();
    }
    
    // 从本地恢复作答状态
    private void restoreAnswersFromLocal() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        
        // 检查是否有进行中的测试
        boolean isInProgress = prefs.getBoolean(PREF_SCL90_IN_PROGRESS + "_" + userId, false);
        
        if (isInProgress) {
            // 恢复答案Map
            String answersJson = prefs.getString(PREF_SCL90_ANSWERS + "_" + userId, "");
            if (!answersJson.isEmpty()) {
                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<Integer, Integer>>(){}.getType();
                answers = gson.fromJson(answersJson, type);
            }
            
            // 恢复当前问题索引
            currentQuestionIndex = prefs.getInt(PREF_SCL90_CURRENT_INDEX + "_" + userId, 0);
            
            // 显示问题布局并加载当前问题
            questionLayout.setVisibility(View.VISIBLE);
            resultLayout.setVisibility(View.GONE);
            showQuestion(currentQuestionIndex);
        }
    }
    
    private void loadSCL90Questions() {
        ApiClient.getUserApi().getSCL90Questions().enqueue(new Callback<List<SCL90Question>>() {
            @Override
            public void onResponse(Call<List<SCL90Question>> call, Response<List<SCL90Question>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    questions = response.body();
                    // 问题加载成功后，检查用户是否已完成SCL-90测试
                    checkUserSCL90Result();
                } else {
                    // 如果无法从服务器获取问题，使用备用方法获取问题
                    loadLocalSCL90Questions();
                    checkUserSCL90Result();
                    Toast.makeText(getContext(), "无法从服务器获取问题列表，使用本地问题", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SCL90Question>> call, Throwable t) {
                // 如果请求失败，使用备用方法获取问题
                loadLocalSCL90Questions();
                checkUserSCL90Result();
                Toast.makeText(getContext(), "获取问题列表失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadLocalSCL90Questions() {
        // 如果无法从服务器获取问题，使用本地问题列表
        questions = new ArrayList<>();
        for (int i = 1; i <= 90; i++) {
            SCL90Question question = new SCL90Question();
            question.setId(i);
            question.setQuestionText(getQuestionText(i));
            question.setFactor(getFactorForQuestion(i));
            questions.add(question);
        }
    }
    
    private String getQuestionText(int questionId) {
        // 备用问题文本，当无法从服务器获取时使用
        switch (questionId) {
            case 1: return "1. 头痛";
            case 2: return "2. 神经过敏，心中不踏实";
            case 3: return "3. 头脑中有不必要的想法或字句盘旋";
            case 4: return "4. 头昏或昏倒";
            case 5: return "5. 对异性的兴趣减退";
            case 6: return "6. 对旁人责备求全";
            case 7: return "7. 感到别人能控制你的思想";
            case 8: return "8. 责怪别人制造麻烦";
            case 9: return "9. 忘记性大";
            case 10: return "10. 担心自己的衣饰整齐及仪态的端正";
            case 11: return "11. 容易烦恼和激动";
            case 12: return "12. 胸痛";
            case 13: return "13. 害怕空旷的场所或街道";
            case 14: return "14. 感到自己的精力下降，活动减慢";
            case 15: return "15. 想结束自己的生命";
            case 16: return "16. 听到旁人听不到的声音";
            case 17: return "17. 发抖";
            case 18: return "18. 感到大多数人都不可信任";
            case 19: return "19. 胃口不好";
            case 20: return "20. 容易哭泣";
            case 21: return "21. 同异性相处时感到害羞不自在";
            case 22: return "22. 感到受骗，中了圈套或有人想抓您";
            case 23: return "23. 无缘无故地突然感到害怕";
            case 24: return "24. 自己不能控制地大发脾气";
            case 25: return "25. 怕单独出门";
            case 26: return "26. 经常责怪自己";
            case 27: return "27. 腰痛";
            case 28: return "28. 感到难以完成任务";
            case 29: return "29. 感到孤独";
            case 30: return "30. 感到苦闷";
            case 31: return "31. 过分担忧";
            case 32: return "32. 对事物不感兴趣";
            case 33: return "33. 感到害怕";
            case 34: return "34. 我的感情容易受到伤害";
            case 35: return "35. 旁人能知道您的私下想法";
            case 36: return "36. 感到别人不理解您不同情您";
            case 37: return "37. 感到人们对你不友好，不喜欢你";
            case 38: return "38. 做事必须做得很慢以保证做得正确";
            case 39: return "39. 心跳得很厉害";
            case 40: return "40. 恶心或胃部不舒服";
            case 41: return "41. 感到比不上他人";
            case 42: return "42. 肌肉酸痛";
            case 43: return "43. 感到有人在监视您谈论您";
            case 44: return "44. 难以入睡";
            case 45: return "45. 做事必须反复检查";
            case 46: return "46. 难以作出决定";
            case 47: return "47. 怕乘电车、公共汽车、地铁或火车";
            case 48: return "48. 呼吸有困难";
            case 49: return "49. 一阵阵发冷或发热";
            case 50: return "50. 因为感到害怕而避开某些东西，场合或活动";
            case 51: return "51. 脑子变空了";
            case 52: return "52. 身体发麻或刺痛";
            case 53: return "53. 喉咙有梗塞感";
            case 54: return "54. 感到对前途没有希望";
            case 55: return "55. 不能集中注意力";
            case 56: return "56. 感到身体的某一部分软弱无力";
            case 57: return "57. 感到紧张或容易紧张";
            case 58: return "58. 感到手或脚发沉";
            case 59: return "59. 想到有关死亡的事";
            case 60: return "60. 吃得太多";
            case 61: return "61. 当别人看着您或谈论您时感到不自在";
            case 62: return "62. 有一些不属于您自己的想法";
            case 63: return "63. 有想打人或伤害他人的冲动";
            case 64: return "64. 醒得太早";
            case 65: return "65. 必须反复洗手、点数目或触摸某些东西";
            case 66: return "66. 睡得不稳不深";
            case 67: return "67. 有想摔坏或破坏东西的冲动";
            case 68: return "68. 有一些别人没有的想法或念头";
            case 69: return "69. 感到对别人神经过敏";
            case 70: return "70. 在商店或电影院等人多的地方感到不自在";
            case 71: return "71. 感到任何事情都很难做";
            case 72: return "72. 一阵阵恐惧或惊恐";
            case 73: return "73. 感到在公共场合吃东西很不舒服";
            case 74: return "74. 经常与人争论";
            case 75: return "75. 单独一人时神经很紧张";
            case 76: return "76. 别人对您的成绩没有作出恰当的评价";
            case 77: return "77. 即使和别人在一起也感到孤单";
            case 78: return "78. 感到坐立不安心神不宁";
            case 79: return "79. 感到自己没有什么价值";
            case 80: return "80. 感到熟悉的东西变成陌生或不象是真的";
            case 81: return "81. 大叫或摔东西";
            case 82: return "82. 害怕会在公共场合昏倒";
            case 83: return "83. 感到别人想占您的便宜";
            case 84: return "84. 为一些有关\"性\"的想法而很苦恼";
            case 85: return "85. 认为应该因为自己的过错而受到惩罚";
            case 86: return "86. 感到要赶快把事情做完";
            case 87: return "87. 感到自己的身体有严重问题";
            case 88: return "88. 从未感到和其他人很亲近";
            case 89: return "89. 感到自己有罪";
            case 90: return "90. 感到自己的脑子有毛病";
            default: return questionId + ". SCL-90问题" + questionId;
        }
    }
    
    private String getFactorForQuestion(int questionId) {
        // 根据SCL-90的因子划分返回对应因子
        if (questionId == 1 || questionId == 4 || questionId == 12 || questionId == 27 || 
            questionId == 40 || questionId == 42 || questionId == 48 || questionId == 49 || 
            questionId == 52 || questionId == 53 || questionId == 56 || questionId == 58) {
            return "躯体化";
        } else if (questionId == 3 || questionId == 9 || questionId == 10 || questionId == 28 || 
                   questionId == 38 || questionId == 45 || questionId == 46 || questionId == 51 || 
                   questionId == 55 || questionId == 65) {
            return "强迫症状";
        } else if (questionId == 6 || questionId == 21 || questionId == 34 || questionId == 36 || 
                   questionId == 37 || questionId == 41 || questionId == 61 || questionId == 69 || 
                   questionId == 73) {
            return "人际关系敏感";
        } else if (questionId == 5 || questionId == 14 || questionId == 15 || questionId == 20 || 
                   questionId == 22 || questionId == 26 || questionId == 29 || questionId == 30 || 
                   questionId == 31 || questionId == 32 || questionId == 54 || questionId == 71 || 
                   questionId == 79) {
            return "抑郁";
        } else if (questionId == 2 || questionId == 17 || questionId == 23 || questionId == 33 || 
                   questionId == 39 || questionId == 57 || questionId == 72 || questionId == 78 || 
                   questionId == 80 || questionId == 86) {
            return "焦虑";
        } else if (questionId == 11 || questionId == 24 || questionId == 63 || questionId == 67 || 
                   questionId == 74 || questionId == 81) {
            return "敌对";
        } else if (questionId == 13 || questionId == 25 || questionId == 47 || questionId == 50 || 
                   questionId == 70 || questionId == 75 || questionId == 82) {
            return "恐怖";
        } else if (questionId == 8 || questionId == 18 || questionId == 43 || questionId == 68 || 
                   questionId == 76 || questionId == 83) {
            return "偏执";
        } else if (questionId == 7 || questionId == 16 || questionId == 35 || questionId == 62 || 
                   questionId == 77 || questionId == 84 || questionId == 85 || questionId == 87 || 
                   questionId == 88 || questionId == 90) {
            return "精神病性";
        } else if (questionId == 19 || questionId == 44 || questionId == 59 || questionId == 60 || 
                   questionId == 64 || questionId == 66 || questionId == 89) {
            return "其他";
        } else {
            return "未分类";
        }
    }
    
    private void checkUserSCL90Result() {
        ApiClient.getUserApi().getSCL90Result(userId).enqueue(new Callback<SCL90Result>() {
            @Override
            public void onResponse(Call<SCL90Result> call, Response<SCL90Result> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    // 用户已完成测试，显示结果
                    SCL90Result result = response.body();
                    displaySavedResult(result);
                } else {
                    // 检查是否有本地保存的进行中的测试
                    SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                    boolean isInProgress = prefs.getBoolean(PREF_SCL90_IN_PROGRESS + "_" + userId, false);
                    
                    if (isInProgress) {
                        // 恢复上次的作答
                        restoreAnswersFromLocal();
                    } else {
                        // 用户未完成测试，也没有本地保存的进度，显示新测试界面
                        questionLayout.setVisibility(View.VISIBLE);
                        showQuestion(0);
                    }
                }
            }

            @Override
            public void onFailure(Call<SCL90Result> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                
                // 检查是否有本地保存的进行中的测试
                SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                boolean isInProgress = prefs.getBoolean(PREF_SCL90_IN_PROGRESS + "_" + userId, false);
                
                if (isInProgress) {
                    // 恢复上次的作答
                    restoreAnswersFromLocal();
                } else {
                    // 显示新测试
                    questionLayout.setVisibility(View.VISIBLE);
                    showQuestion(0);
                }
                
                Toast.makeText(getContext(), "获取测试结果失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showQuestion(int index) {
        if (index < questions.size()) {
            SCL90Question question = questions.get(index);
            questionProgress.setText((index + 1) + "/" + questions.size());
            questionText.setText(question.getQuestionText());
            
            // 如果已经回答过这个问题，显示之前的选择
            if (answers.containsKey(question.getId())) {
                int previousAnswer = answers.get(question.getId());
                switch (previousAnswer) {
                    case 1:
                        option1.setChecked(true);
                        break;
                    case 2:
                        option2.setChecked(true);
                        break;
                    case 3:
                        option3.setChecked(true);
                        break;
                    case 4:
                        option4.setChecked(true);
                        break;
                    case 5:
                        option5.setChecked(true);
                        break;
                    default:
                        optionsGroup.clearCheck();
                        break;
                }
            } else {
                optionsGroup.clearCheck();
            }
            
            // 处理上一题按钮的启用状态
            previousButton.setEnabled(index > 0);
        }
    }
    
    private void handleNextQuestion() {
        if (optionsGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "请选择一个选项", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 记录当前问题的回答
        int score = getSelectedScore();
        SCL90Question currentQuestion = questions.get(currentQuestionIndex);
        answers.put(currentQuestion.getId(), score);
        
        // 立即保存当前进度到本地
        saveAnswersToLocal();
        
        currentQuestionIndex++;
        
        if (currentQuestionIndex < questions.size()) {
            // 显示下一个问题
            showQuestion(currentQuestionIndex);
        } else {
            // 测试完成，计算结果
            calculateResults();
        }
    }
    
    private int getSelectedScore() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.option1) return 1;
        if (selectedId == R.id.option2) return 2;
        if (selectedId == R.id.option3) return 3;
        if (selectedId == R.id.option4) return 4;
        if (selectedId == R.id.option5) return 5;
        return 1; // 默认值
    }
    
    private void calculateResults() {
        // 计算总分
        int totalScore = 0;
        int positiveItems = 0; // 阳性项目数（分数>=2）
        Map<String, List<Integer>> factorScores = new HashMap<>(); // 因子 -> 得分列表
        
        for (SCL90Question question : questions) {
            int score = answers.getOrDefault(question.getId(), 1);
            totalScore += score;
            
            if (score >= 2) {
                positiveItems++;
            }
            
            String factor = question.getFactor();
            if (!factorScores.containsKey(factor)) {
                factorScores.put(factor, new ArrayList<>());
            }
            factorScores.get(factor).add(score);
        }
        
        // 计算各因子的平均分
        Map<String, Double> factorAverages = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : factorScores.entrySet()) {
            double sum = 0;
            for (int score : entry.getValue()) {
                sum += score;
            }
            factorAverages.put(entry.getKey(), sum / entry.getValue().size());
        }
        
        // 显示结果
        displayResults(totalScore, positiveItems, factorAverages);
        
        // 将结果保存到服务器
        saveResultToServer(totalScore, positiveItems, factorAverages);
    }
    
    private void displayResults(int totalScore, int positiveItems, Map<String, Double> factorAverages) {
        // 隐藏问题布局，显示结果布局
        questionLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        
        // 设置结果文本
        double totalAverage = (double) totalScore / 90;
        // 总分格式更改为单行格式
        totalScoreText.setText(String.format("总分: %d | 总均分: %.2f", totalScore, totalAverage));
        
        // 阳性项目数的计算和显示
        double positiveAverage = positiveItems > 0 ? 
                (double)(totalScore - (90 - positiveItems)) / positiveItems : 0;
        positiveItemsText.setText(String.format("阳性项目数: %d\n阳性症状均分: %.2f", positiveItems, positiveAverage));
        
        // 清空因子分数布局
        factorScoresLayout.removeAllViews();
        
        // 添加各因子的分数
        for (Map.Entry<String, Double> entry : factorAverages.entrySet()) {
            TextView factorText = new TextView(getContext());
            factorText.setPadding(0, 8, 0, 8);
            factorText.setTextColor(getResources().getColor(R.color.telegram_text_primary));
            
            // 评估因子分数
            String evaluation = "";
            if (entry.getValue() >= 3.0) {
                evaluation = " (重度)";
            } else if (entry.getValue() >= 2.5) {
                evaluation = " (中重度)";
            } else if (entry.getValue() >= 2.0) {
                evaluation = " (中度)";
            } else if (entry.getValue() >= 1.5) {
                evaluation = " (轻度)";
            } else {
                evaluation = " (正常)";
            }
            
            factorText.setText(String.format("%s: %.2f%s", entry.getKey(), entry.getValue(), evaluation));
            factorScoresLayout.addView(factorText);
        }
    }
    
    private void saveResultToServer(int totalScore, int positiveItems, Map<String, Double> factorAverages) {
        // 计算阳性均分
        double positiveAverage = positiveItems > 0 ? 
                (double)(totalScore - (90 - positiveItems)) / positiveItems : 0;
        
        // 创建结果对象
        SCL90Result result = new SCL90Result();
        result.setUserId(userId);
        result.setTotalScore(totalScore);
        result.setTotalAverage((double) totalScore / 90);
        result.setPositiveItems(positiveItems);
        result.setPositiveAverage(positiveAverage);
        result.setFactorScores(factorAverages);
        
        // 发送到服务器
        ApiClient.getUserApi().saveSCL90Result(result).enqueue(new Callback<SCL90Result>() {
            @Override
            public void onResponse(Call<SCL90Result> call, Response<SCL90Result> response) {
                if (response.isSuccessful()) {
                    // 测试结果已成功保存到服务器，清除本地临时保存状态
                    SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                    prefs.edit()
                        .remove(PREF_SCL90_ANSWERS + "_" + userId)
                        .remove(PREF_SCL90_CURRENT_INDEX + "_" + userId)
                        .remove(PREF_SCL90_IN_PROGRESS + "_" + userId)
                        .apply();
                        
                    Toast.makeText(getContext(), "测试结果已保存", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "保存结果失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SCL90Result> call, Throwable t) {
                Toast.makeText(getContext(), "保存结果失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displaySavedResult(SCL90Result result) {
        questionLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        
        // 设置结果文本，改为单行格式
        totalScoreText.setText(String.format("总分: %d | 总均分: %.2f", 
                result.getTotalScore(), result.getTotalAverage()));
        
        positiveItemsText.setText(String.format("阳性项目数: %d\n阳性症状均分: %.2f", 
                result.getPositiveItems(), result.getPositiveAverage()));
        
        // 清空因子分数布局
        factorScoresLayout.removeAllViews();
        
        // 添加各因子的分数
        if (result.getFactorScores() != null) {
            for (Map.Entry<String, Double> entry : result.getFactorScores().entrySet()) {
                TextView factorText = new TextView(getContext());
                factorText.setPadding(0, 8, 0, 8);
                factorText.setTextColor(getResources().getColor(R.color.telegram_text_primary));
                
                // 评估因子分数
                String evaluation = "";
                if (entry.getValue() >= 3.0) {
                    evaluation = " (重度)";
                } else if (entry.getValue() >= 2.5) {
                    evaluation = " (中重度)";
                } else if (entry.getValue() >= 2.0) {
                    evaluation = " (中度)";
                } else if (entry.getValue() >= 1.5) {
                    evaluation = " (轻度)";
                } else {
                    evaluation = " (正常)";
                }
                
                factorText.setText(String.format("%s: %.2f%s", entry.getKey(), entry.getValue(), evaluation));
                factorScoresLayout.addView(factorText);
            }
        } else {
            // 如果因子分数为null，显示提示信息
            TextView noDataText = new TextView(getContext());
            noDataText.setPadding(0, 8, 0, 8);
            noDataText.setTextColor(getResources().getColor(R.color.telegram_text_primary));
            noDataText.setText("暂无因子分析数据");
            factorScoresLayout.addView(noDataText);
        }
    }
    
    private void restartTest() {
        // 检查本地是否有未完成的作答
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String answersJson = prefs.getString(PREF_SCL90_ANSWERS + "_" + userId, "");
        boolean hasPartialAnswers = !answersJson.isEmpty();

        if (hasPartialAnswers) {
            // 显示对话框询问用户是否要恢复未完成的作答
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("发现未完成的测试")
                .setMessage("检测到您有未完成的SCL-90测试记录，是否恢复之前的作答？")
                .setPositiveButton("恢复作答", (dialog, which) -> {
                    // 恢复之前的作答
                    restoreAnswersFromLocal();
                    Toast.makeText(getContext(), "已恢复之前的作答", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("重新开始", (dialog, which) -> {
                    // 清除之前的作答并重新开始
                    clearAndRestartTest();
                })
                .setCancelable(false)
                .show();
        } else {
            // 没有未完成的作答，直接重新开始
            clearAndRestartTest();
        }
    }

    // 新增方法：清除作答并重新开始测试
    private void clearAndRestartTest() {
        // 重置测试
        currentQuestionIndex = 0;
        answers.clear();
        questionLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        showQuestion(0);
        
        // 清除本地保存的作答
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        prefs.edit()
            .remove(PREF_SCL90_ANSWERS + "_" + userId)
            .remove(PREF_SCL90_CURRENT_INDEX + "_" + userId)
            .remove(PREF_SCL90_IN_PROGRESS + "_" + userId)
            .apply();
        
        // 设置测试状态为进行中
        prefs.edit().putBoolean(PREF_SCL90_IN_PROGRESS + "_" + userId, true).apply();
        
        Toast.makeText(getContext(), "已开始新测试", Toast.LENGTH_SHORT).show();
    }

    private void handlePreviousQuestion() {
        if (currentQuestionIndex > 0) {
            // 记录当前问题的回答（如果有选择）
            if (optionsGroup.getCheckedRadioButtonId() != -1) {
                int score = getSelectedScore();
                SCL90Question currentQuestion = questions.get(currentQuestionIndex);
                answers.put(currentQuestion.getId(), score);
                
                // 保存当前进度到本地
                saveAnswersToLocal();
            }
            
            // 返回上一题
            currentQuestionIndex--;
            showQuestion(currentQuestionIndex);
        }
    }
} 
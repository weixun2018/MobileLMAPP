package com.example.projectv2.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class SCL90Result {
    private Long id;
    private Long userId;
    private int totalScore;
    private double totalAverage;
    private int positiveItems;
    private double positiveAverage;
    
    @SerializedName("factorScores")
    private Object factorScoresRaw; // 用于处理服务器返回的字符串或Map
    
    private transient Map<String, Double> factorScores; // 实际使用的Map
    private static final Gson gson = new Gson();

    public SCL90Result() {
    }

    public SCL90Result(Long id, Long userId, int totalScore, double totalAverage,
                       int positiveItems, double positiveAverage, Map<String, Double> factorScores) {
        this.id = id;
        this.userId = userId;
        this.totalScore = totalScore;
        this.totalAverage = totalAverage;
        this.positiveItems = positiveItems;
        this.positiveAverage = positiveAverage;
        this.factorScores = factorScores;
        this.factorScoresRaw = factorScores; // 设置原始值用于序列化
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public double getTotalAverage() {
        return totalAverage;
    }

    public void setTotalAverage(double totalAverage) {
        this.totalAverage = totalAverage;
    }

    public int getPositiveItems() {
        return positiveItems;
    }

    public void setPositiveItems(int positiveItems) {
        this.positiveItems = positiveItems;
    }

    public double getPositiveAverage() {
        return positiveAverage;
    }

    public void setPositiveAverage(double positiveAverage) {
        this.positiveAverage = positiveAverage;
    }

    public Map<String, Double> getFactorScores() {
        if (factorScores == null && factorScoresRaw != null) {
            try {
                if (factorScoresRaw instanceof String) {
                    // 服务器返回的是JSON字符串，需要解析
                    String jsonString = (String) factorScoresRaw;
                    if (!jsonString.trim().isEmpty()) {
                        Type type = new TypeToken<Map<String, Double>>(){}.getType();
                        factorScores = gson.fromJson(jsonString, type);
                    }
                } else if (factorScoresRaw instanceof Map) {
                    // 已经是Map类型，直接使用
                    factorScores = (Map<String, Double>) factorScoresRaw;
                }
            } catch (Exception e) {
                System.err.println("Failed to parse factorScores: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return factorScores;
    }

    public void setFactorScores(Map<String, Double> factorScores) {
        this.factorScores = factorScores;
        this.factorScoresRaw = factorScores; // 存储Map用于发送到服务器
    }
} 
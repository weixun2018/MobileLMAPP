package com.example.projectv2.model;

import java.util.Map;

public class SCL90Result {
    private Long id;
    private Long userId;
    private int totalScore;
    private double totalAverage;
    private int positiveItems;
    private double positiveAverage;
    private Map<String, Double> factorScores; // 因子名称 -> 因子平均分

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
        return factorScores;
    }

    public void setFactorScores(Map<String, Double> factorScores) {
        this.factorScores = factorScores;
    }
} 
package com.example.projectv2.model;

public class SCL90Question {
    private int id;
    private String questionText;
    private String factor; // 该问题所属因子，如"躯体化"、"抑郁"等

    public SCL90Question() {
    }

    public SCL90Question(int id, String questionText, String factor) {
        this.id = id;
        this.questionText = questionText;
        this.factor = factor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }
} 
package com.example.bluecat.dto;

public class SCL90QuestionDTO {
    private int id;
    private String questionText;
    private String factor;
    
    public SCL90QuestionDTO() {
    }
    
    public SCL90QuestionDTO(int id, String questionText, String factor) {
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
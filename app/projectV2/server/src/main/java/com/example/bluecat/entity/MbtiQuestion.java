package com.example.bluecat.entity;

import lombok.Data;

@Data
public class MbtiQuestion {
    private Long id;
    private String question;
    private String optionA;
    private String optionB;
    private String dimension;
} 
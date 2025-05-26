package com.example.bluecat.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SCL90Result {
    private Long id;
    private Long userId;
    private Double totalScore;
    private String symptomLevel;
    private String dimensions;
    private String suggestions;
    private LocalDateTime createdAt;
    private Boolean hasSymptoms;
} 
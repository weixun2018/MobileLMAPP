package com.example.bluecat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MbtiQuestionDTO {
    private Long id;
    private String questionText;
    
    @JsonProperty("optionA")
    private String optionA;
    
    @JsonProperty("optionB")
    private String optionB;
    
    private String dimension;
} 
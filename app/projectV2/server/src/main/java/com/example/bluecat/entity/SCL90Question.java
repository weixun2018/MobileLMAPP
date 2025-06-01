package com.example.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scl_questions")
public class SCL90Question {
    private Integer id;
    private String questionText;
    private String factor;
    private Integer factorId;
} 
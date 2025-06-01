package com.example.bluecat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scl90_results")
public class SCL90Result {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer totalScore;
    private Double totalAverage;
    private Integer positiveItems;
    private Double positiveAverage;
    private String factorScores;
} 
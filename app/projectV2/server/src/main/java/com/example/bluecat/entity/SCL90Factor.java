package com.example.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scl_factors")
public class SCL90Factor {
    private Integer id;
    private String factorName;
    private String description;
} 
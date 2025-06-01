package com.example.projectv2.model;

/**
 * SCL90心理因子模型类
 * 对应服务器端的SCL90Factor实体
 */
public class SCL90Factor {
    private Integer id;
    private String factorName; // 因子名称，如"躯体化"、"强迫症状"等
    private String description; // 因子描述

    public SCL90Factor() {
    }

    public SCL90Factor(Integer id, String factorName, String description) {
        this.id = id;
        this.factorName = factorName;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFactorName() {
        return factorName;
    }

    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "SCL90Factor{" +
                "id=" + id +
                ", factorName='" + factorName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
} 
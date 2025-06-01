package com.example.bluecat.dto;

public class SCL90FactorDTO {
    private Integer id;
    private String factorName;
    private String description;
    
    public SCL90FactorDTO() {
    }
    
    public SCL90FactorDTO(Integer id, String factorName, String description) {
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
} 
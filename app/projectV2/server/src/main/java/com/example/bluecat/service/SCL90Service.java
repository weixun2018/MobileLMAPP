package com.example.bluecat.service;

import com.example.bluecat.dto.SCL90FactorDTO;
import com.example.bluecat.dto.SCL90QuestionDTO;
import com.example.bluecat.dto.SCL90ResultDTO;
import com.example.bluecat.entity.SCL90Result;

import java.util.List;
import java.util.Optional;

public interface SCL90Service {
    SCL90Result saveResult(SCL90ResultDTO resultDTO);
    Optional<SCL90Result> getResultByUserId(Long userId);
    void deleteResultByUserId(Long userId);
    
    // 新增：获取所有问题
    List<SCL90QuestionDTO> getAllQuestions();
    
    // 新增：因子相关方法
    List<SCL90FactorDTO> getAllFactors();
    SCL90FactorDTO getFactorById(Integer id);
    SCL90FactorDTO getFactorByName(String factorName);
} 
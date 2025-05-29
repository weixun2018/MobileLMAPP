package com.example.bluecat.service;

import com.example.bluecat.dto.SCL90ResultDTO;
import com.example.bluecat.entity.SCL90Result;

import java.util.Optional;

public interface SCL90Service {
    SCL90Result saveResult(SCL90ResultDTO resultDTO);
    Optional<SCL90Result> getResultByUserId(Long userId);
    void deleteResultByUserId(Long userId);
} 
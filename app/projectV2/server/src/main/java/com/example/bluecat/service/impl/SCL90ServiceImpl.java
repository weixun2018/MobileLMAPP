package com.example.bluecat.service.impl;

import com.example.bluecat.dto.SCL90ResultDTO;
import com.example.bluecat.entity.SCL90Result;
import com.example.bluecat.mapper.SCL90Mapper;
import com.example.bluecat.service.SCL90Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SCL90ServiceImpl implements SCL90Service {

    @Autowired
    private SCL90Mapper scl90Mapper;

    @Override
    @Transactional
    public SCL90Result saveResult(SCL90ResultDTO resultDTO) {
        // 查找是否已有结果
        SCL90Result existingResult = scl90Mapper.findByUserId(resultDTO.getUserId());
        
        SCL90Result result;
        if (existingResult != null) {
            result = existingResult;
            // 更新结果
            result.setTotalScore(Double.valueOf(resultDTO.getTotalScore()));
            scl90Mapper.updateById(result);
        } else {
            result = new SCL90Result();
            result.setUserId(resultDTO.getUserId());
            result.setTotalScore(Double.valueOf(resultDTO.getTotalScore()));
            scl90Mapper.insert(result);
        }
        
        return result;
    }

    @Override
    public Optional<SCL90Result> getResultByUserId(Long userId) {
        SCL90Result result = scl90Mapper.findByUserId(userId);
        return Optional.ofNullable(result);
    }

    @Override
    @Transactional
    public void deleteResultByUserId(Long userId) {
        scl90Mapper.deleteByUserId(userId);
    }
} 
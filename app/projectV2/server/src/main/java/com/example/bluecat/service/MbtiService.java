package com.example.bluecat.service;

import com.example.bluecat.dto.MbtiQuestionDTO;
import com.example.bluecat.dto.MbtiTypeDTO;

import java.util.List;

public interface MbtiService {
    List<MbtiQuestionDTO> getQuestions();
    MbtiTypeDTO getType(String typeCode);
    void updateUserMbtiType(Long userId, String mbtiType);
} 
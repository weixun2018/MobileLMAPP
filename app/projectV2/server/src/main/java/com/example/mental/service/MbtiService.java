package com.example.mental.service;

import com.example.mental.dto.MbtiQuestionDTO;
import com.example.mental.dto.MbtiTypeDTO;

import java.util.List;

public interface MbtiService {
    List<MbtiQuestionDTO> getQuestions();
    MbtiTypeDTO getType(String typeCode);
    void updateUserMbtiType(Long userId, String mbtiType);
} 
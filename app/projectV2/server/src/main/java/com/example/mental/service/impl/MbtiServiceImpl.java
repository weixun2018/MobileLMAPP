package com.example.mental.service.impl;

import com.example.mental.dto.MbtiQuestionDTO;
import com.example.mental.dto.MbtiTypeDTO;
import com.example.mental.entity.MbtiQuestion;
import com.example.mental.entity.MbtiType;
import com.example.mental.entity.User;
import com.example.mental.repository.MbtiQuestionRepository;
import com.example.mental.repository.MbtiTypeRepository;
import com.example.mental.repository.UserRepository;
import com.example.mental.service.MbtiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MbtiServiceImpl implements MbtiService {

    private final MbtiQuestionRepository questionRepository;
    private final MbtiTypeRepository typeRepository;
    private final UserRepository userRepository;

    @Override
    public List<MbtiQuestionDTO> getQuestions() {
        return questionRepository.findAll().stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MbtiTypeDTO getType(String typeCode) {
        MbtiType mbtiType = typeRepository.findById(typeCode)
                .orElseThrow(() -> new EntityNotFoundException("MBTI类型不存在: " + typeCode));
        return convertToTypeDTO(mbtiType);
    }

    @Override
    public void updateUserMbtiType(Long userId, String mbtiType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));
        user.setMbtiType(mbtiType);
        userRepository.save(user);
    }

    private MbtiQuestionDTO convertToQuestionDTO(MbtiQuestion question) {
        MbtiQuestionDTO dto = new MbtiQuestionDTO();
        BeanUtils.copyProperties(question, dto);
        return dto;
    }

    private MbtiTypeDTO convertToTypeDTO(MbtiType type) {
        MbtiTypeDTO dto = new MbtiTypeDTO();
        BeanUtils.copyProperties(type, dto);
        return dto;
    }
} 
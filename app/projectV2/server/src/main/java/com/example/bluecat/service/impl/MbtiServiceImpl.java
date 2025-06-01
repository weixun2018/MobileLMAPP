package com.example.bluecat.service.impl;

import com.example.bluecat.dto.MbtiQuestionDTO;
import com.example.bluecat.dto.MbtiTypeDTO;
import com.example.bluecat.entity.MbtiQuestion;
import com.example.bluecat.entity.MbtiType;
import com.example.bluecat.entity.User;
import com.example.bluecat.mapper.MbtiQuestionMapper;
import com.example.bluecat.mapper.MbtiTypeMapper;
import com.example.bluecat.mapper.UserMapper;
import com.example.bluecat.service.MbtiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MbtiServiceImpl implements MbtiService {

    private final MbtiQuestionMapper mbtiQuestionMapper;
    private final MbtiTypeMapper mbtiTypeMapper;
    private final UserMapper userMapper;

    @Override
    public List<MbtiQuestionDTO> getQuestions() {
        return mbtiQuestionMapper.findAllQuestions().stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MbtiTypeDTO getType(String typeCode) {
        MbtiType mbtiType = mbtiTypeMapper.findTypeByCode(typeCode);
        if (mbtiType == null) {
            throw new RuntimeException("MBTI类型不存在: " + typeCode);
        }
        return convertToTypeDTO(mbtiType);
    }

    @Override
    public void updateUserMbtiType(Long userId, String mbtiType) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        userMapper.updateMbtiType(userId, mbtiType);
    }

    /**
     * 手动映射：MbtiQuestion Entity -> MbtiQuestionDTO
     */
    private MbtiQuestionDTO convertToQuestionDTO(MbtiQuestion question) {
        MbtiQuestionDTO dto = new MbtiQuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestion());  // Entity的question字段映射到DTO的questionText
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setDimension(question.getDimension());
        return dto;
    }

    /**
     * 手动映射：MbtiType Entity -> MbtiTypeDTO
     */
    private MbtiTypeDTO convertToTypeDTO(MbtiType type) {
        MbtiTypeDTO dto = new MbtiTypeDTO();
        dto.setTypeCode(type.getType());              // Entity的type字段映射到DTO的typeCode
        dto.setTypeName(type.getName());              // Entity的name字段映射到DTO的typeName
        dto.setDescription(type.getDescription());
        dto.setCharacteristics(type.getCharacteristics());
        dto.setStrengths(type.getSuitable());         // Entity的suitable字段映射到DTO的strengths
        dto.setWeaknesses(type.getNotSuitable());     // Entity的notSuitable字段映射到DTO的weaknesses
        return dto;
    }
} 
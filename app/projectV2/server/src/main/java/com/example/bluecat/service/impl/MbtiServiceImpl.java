package com.example.bluecat.service.impl;

import com.example.bluecat.dto.MbtiQuestionDTO;
import com.example.bluecat.dto.MbtiTypeDTO;
import com.example.bluecat.entity.MbtiQuestion;
import com.example.bluecat.entity.MbtiType;
import com.example.bluecat.entity.User;
import com.example.bluecat.mapper.MbtiMapper;
import com.example.bluecat.mapper.UserMapper;
import com.example.bluecat.service.MbtiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MbtiServiceImpl implements MbtiService {

    private final MbtiMapper mbtiMapper;
    private final UserMapper userMapper;

    @Override
    public List<MbtiQuestionDTO> getQuestions() {
        return mbtiMapper.findAllQuestions().stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MbtiTypeDTO getType(String typeCode) {
        MbtiType mbtiType = mbtiMapper.findTypeByCode(typeCode);
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
        user.setMbtiType(mbtiType);
        userMapper.updateById(user);
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
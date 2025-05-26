package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.MbtiQuestion;
import com.example.bluecat.entity.MbtiType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MbtiMapper extends BaseMapper<MbtiQuestion> {
    
    // MBTI问题相关
    List<MbtiQuestion> findAllQuestions();
    MbtiQuestion findQuestionById(@Param("id") Long id);
    
    // MBTI类型相关
    List<MbtiType> findAllTypes();
    MbtiType findTypeByCode(@Param("type") String type);
    
    // 插入/更新操作
    int insertQuestion(MbtiQuestion question);
    int insertType(MbtiType type);
    int updateType(MbtiType type);
} 
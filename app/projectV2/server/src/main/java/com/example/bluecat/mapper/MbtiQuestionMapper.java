package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.MbtiQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MBTI问题数据访问层
 * 继承BaseMapper<MbtiQuestion>，自动获得基础的CRUD操作方法
 */
@Mapper
public interface MbtiQuestionMapper extends BaseMapper<MbtiQuestion> {
    
    // ==================== 自定义查询方法 ====================
    
    /**
     * 查询所有MBTI问题
     * @return MBTI问题列表
     */
    List<MbtiQuestion> findAllQuestions();
    
    /**
     * 根据ID查询MBTI问题
     * @param id 问题ID
     * @return MBTI问题
     */
    MbtiQuestion findQuestionById(@Param("id") Long id);
    
    /**
     * 插入MBTI问题
     * @param question MBTI问题对象
     * @return 受影响的行数
     */
    int insertQuestion(MbtiQuestion question);
    
    // ==================== BaseMapper提供的方法说明 ====================
    // 以下方法由MyBatis-Plus的BaseMapper自动提供，无需手动实现
    
    /**
     * 根据ID查询MBTI问题
     * 使用方法：mbtiQuestionMapper.selectById(questionId)
     * @param id 问题ID
     * @return MBTI问题信息，如果不存在返回null
     */
    // MbtiQuestion selectById(Serializable id);
    
    /**
     * 插入新的MBTI问题
     * 使用方法：mbtiQuestionMapper.insert(question)
     * @param entity MBTI问题实体对象
     * @return 影响的行数
     */
    // int insert(MbtiQuestion entity);
    
    /**
     * 根据ID更新MBTI问题信息
     * 使用方法：mbtiQuestionMapper.updateById(question)
     * @param entity MBTI问题实体对象
     * @return 影响的行数
     */
    // int updateById(MbtiQuestion entity);
    
    /**
     * 根据ID删除MBTI问题
     * 使用方法：mbtiQuestionMapper.deleteById(questionId)
     * @param id 问题ID
     * @return 影响的行数
     */
    // int deleteById(Serializable id);
    
    /**
     * 查询所有MBTI问题列表
     * 使用方法：mbtiQuestionMapper.selectList(null)
     * @param queryWrapper 查询条件包装器，传null查询所有
     * @return MBTI问题列表
     */
    // List<MbtiQuestion> selectList(Wrapper<MbtiQuestion> queryWrapper);
} 
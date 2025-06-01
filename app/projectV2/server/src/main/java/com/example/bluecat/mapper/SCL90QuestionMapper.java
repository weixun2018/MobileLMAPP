package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.SCL90Question;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SCL90问题数据访问层
 * 继承BaseMapper<SCL90Question>，自动获得基础的CRUD操作方法
 */
@Mapper
public interface SCL90QuestionMapper extends BaseMapper<SCL90Question> {
    
    // ==================== 自定义查询方法 ====================
    
    /**
     * 获取所有问题
     * @return SCL90问题列表
     */
    List<SCL90Question> findAllQuestions();
    
    /**
     * 根据因子获取问题
     * @param factor 心理因子名称
     * @return 指定因子的问题列表
     */
    List<SCL90Question> findQuestionsByFactor(String factor);
    
    // ==================== BaseMapper提供的方法说明 ====================
    // 以下方法由MyBatis-Plus的BaseMapper自动提供，无需手动实现
    
    /**
     * 根据ID查询SCL90问题
     * 使用方法：scl90QuestionMapper.selectById(questionId)
     * @param id 问题ID
     * @return SCL90问题信息，如果不存在返回null
     */
    // SCL90Question selectById(Serializable id);
    
    /**
     * 插入新的SCL90问题
     * 使用方法：scl90QuestionMapper.insert(question)
     * @param entity SCL90问题实体对象
     * @return 影响的行数
     */
    // int insert(SCL90Question entity);
    
    /**
     * 根据ID更新SCL90问题信息
     * 使用方法：scl90QuestionMapper.updateById(question)
     * @param entity SCL90问题实体对象
     * @return 影响的行数
     */
    // int updateById(SCL90Question entity);
    
    /**
     * 根据ID删除SCL90问题
     * 使用方法：scl90QuestionMapper.deleteById(questionId)
     * @param id 问题ID
     * @return 影响的行数
     */
    // int deleteById(Serializable id);
    
    /**
     * 查询所有SCL90问题列表
     * 使用方法：scl90QuestionMapper.selectList(null)
     * 推荐使用此方法替代自定义的findAllQuestions方法
     * @param queryWrapper 查询条件包装器，传null查询所有
     * @return SCL90问题列表
     */
    // List<SCL90Question> selectList(Wrapper<SCL90Question> queryWrapper);
} 
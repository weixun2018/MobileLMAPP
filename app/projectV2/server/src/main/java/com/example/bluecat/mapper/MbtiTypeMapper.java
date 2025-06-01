package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.MbtiType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MBTI类型数据访问层
 * 继承BaseMapper<MbtiType>，自动获得基础的CRUD操作方法
 */
@Mapper
public interface MbtiTypeMapper extends BaseMapper<MbtiType> {
    
    // ==================== 自定义查询方法 ====================
    
    /**
     * 查询所有MBTI类型
     * @return MBTI类型列表
     */
    List<MbtiType> findAllTypes();
    
    /**
     * 根据类型代码查询MBTI类型
     * @param type 类型代码（如INTJ, ENFP等）
     * @return MBTI类型
     */
    MbtiType findTypeByCode(@Param("type") String type);
    
    /**
     * 插入MBTI类型
     * @param type MBTI类型对象
     * @return 受影响的行数
     */
    int insertType(MbtiType type);
    
    /**
     * 更新MBTI类型
     * @param type MBTI类型对象
     * @return 受影响的行数
     */
    int updateType(MbtiType type);
    
    // ==================== BaseMapper提供的方法说明 ====================
    // 以下方法由MyBatis-Plus的BaseMapper自动提供，无需手动实现
    
    /**
     * 根据主键查询MBTI类型
     * 使用方法：mbtiTypeMapper.selectById(typeCode)
     * @param id 类型代码主键
     * @return MBTI类型信息，如果不存在返回null
     */
    // MbtiType selectById(Serializable id);
    
    /**
     * 插入新的MBTI类型
     * 使用方法：mbtiTypeMapper.insert(mbtiType)
     * @param entity MBTI类型实体对象
     * @return 影响的行数
     */
    // int insert(MbtiType entity);
    
    /**
     * 根据主键更新MBTI类型信息
     * 使用方法：mbtiTypeMapper.updateById(mbtiType)
     * @param entity MBTI类型实体对象
     * @return 影响的行数
     */
    // int updateById(MbtiType entity);
    
    /**
     * 根据主键删除MBTI类型
     * 使用方法：mbtiTypeMapper.deleteById(typeCode)
     * @param id 类型代码主键
     * @return 影响的行数
     */
    // int deleteById(Serializable id);
    
    /**
     * 查询所有MBTI类型列表
     * 使用方法：mbtiTypeMapper.selectList(null)
     * @param queryWrapper 查询条件包装器，传null查询所有
     * @return MBTI类型列表
     */
    // List<MbtiType> selectList(Wrapper<MbtiType> queryWrapper);
} 
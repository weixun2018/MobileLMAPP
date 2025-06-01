package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.SCL90Result;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SCL90测试结果数据访问层
 * 继承BaseMapper<SCL90Result>，自动获得基础的CRUD操作方法
 */
@Mapper
public interface SCL90Mapper extends BaseMapper<SCL90Result> {
    
    // ==================== 自定义查询方法 ====================
    
    /**
     * 根据用户ID查找结果
     * @param userId 用户ID
     * @return SCL90测试结果
     */
    SCL90Result findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID删除结果
     * @param userId 用户ID
     * @return 受影响的行数
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有结果
     * @return SCL90测试结果列表
     */
    List<SCL90Result> findAll();
    
    // ==================== BaseMapper提供的方法说明 ====================
    // 以下方法由MyBatis-Plus的BaseMapper自动提供，无需手动实现
    
    /**
     * 根据ID查询SCL90测试结果
     * 使用方法：scl90Mapper.selectById(resultId)
     * @param id 结果ID
     * @return SCL90测试结果，如果不存在返回null
     */
    // SCL90Result selectById(Serializable id);
    
    /**
     * 插入新的SCL90测试结果（BaseMapper版本）
     * 使用方法：scl90Mapper.insert(result)
     * @param entity SCL90测试结果实体对象
     * @return 影响的行数
     */
    // int insert(SCL90Result entity);
    
    /**
     * 根据ID更新SCL90测试结果（BaseMapper版本）
     * 使用方法：scl90Mapper.updateById(result)
     * @param entity SCL90测试结果实体对象
     * @return 影响的行数
     */
    // int updateById(SCL90Result entity);
    
    /**
     * 根据ID删除SCL90测试结果
     * 使用方法：scl90Mapper.deleteById(resultId)
     * @param id 结果ID
     * @return 影响的行数
     */
    // int deleteById(Serializable id);
    
    /**
     * 查询所有SCL90测试结果列表
     * 使用方法：scl90Mapper.selectList(null)
     * 推荐使用此方法替代上面的自定义findAll方法
     * @param queryWrapper 查询条件包装器，传null查询所有
     * @return SCL90测试结果列表
     */
    // List<SCL90Result> selectList(Wrapper<SCL90Result> queryWrapper);
} 
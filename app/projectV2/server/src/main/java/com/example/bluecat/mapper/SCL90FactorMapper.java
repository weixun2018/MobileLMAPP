package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.SCL90Factor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SCL90心理因子数据访问层
 * 继承BaseMapper<SCL90Factor>，自动获得基础的CRUD操作方法
 */
@Mapper
public interface SCL90FactorMapper extends BaseMapper<SCL90Factor> {
    
    // ==================== 自定义查询方法 ====================
    
    /**
     * 获取所有因子
     * @return SCL90心理因子列表
     */
    List<SCL90Factor> findAllFactors();
    
    /**
     * 根据因子名称获取因子
     * @param factorName 因子名称
     * @return SCL90心理因子信息
     */
    SCL90Factor findByFactorName(String factorName);
    
    /**
     * 根据ID获取因子
     * @param id 因子ID
     * @return SCL90心理因子信息
     */
    SCL90Factor findById(Integer id);
    
    // ==================== BaseMapper提供的方法说明 ====================
    // 以下方法由MyBatis-Plus的BaseMapper自动提供，无需手动实现
    // 注意：上面的findById方法与BaseMapper的selectById重复，建议使用BaseMapper版本
    
    /**
     * 根据ID查询SCL90心理因子（BaseMapper版本）
     * 使用方法：scl90FactorMapper.selectById(factorId)
     * 推荐使用此方法替代上面的自定义findById方法
     * @param id 因子ID
     * @return SCL90心理因子信息，如果不存在返回null
     */
    // SCL90Factor selectById(Serializable id);
    
    /**
     * 插入新的SCL90心理因子
     * 使用方法：scl90FactorMapper.insert(factor)
     * @param entity SCL90心理因子实体对象
     * @return 影响的行数
     */
    // int insert(SCL90Factor entity);
    
    /**
     * 根据ID更新SCL90心理因子信息
     * 使用方法：scl90FactorMapper.updateById(factor)
     * @param entity SCL90心理因子实体对象
     * @return 影响的行数
     */
    // int updateById(SCL90Factor entity);
    
    /**
     * 根据ID删除SCL90心理因子
     * 使用方法：scl90FactorMapper.deleteById(factorId)
     * @param id 因子ID
     * @return 影响的行数
     */
    // int deleteById(Serializable id);
    
    /**
     * 查询所有SCL90心理因子列表
     * 使用方法：scl90FactorMapper.selectList(null)
     * 推荐使用此方法替代自定义的findAllFactors方法
     * @param queryWrapper 查询条件包装器，传null查询所有
     * @return SCL90心理因子列表
     */
    // List<SCL90Factor> selectList(Wrapper<SCL90Factor> queryWrapper);
} 
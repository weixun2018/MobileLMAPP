package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.SCL90Result;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SCL90Mapper extends BaseMapper<SCL90Result> {
    
    // 根据用户ID查找结果
    SCL90Result findByUserId(@Param("userId") Long userId);
    
    // 插入新结果
    int insert(SCL90Result result);
    
    // 更新结果
    int updateById(SCL90Result result);
    
    // 根据用户ID删除结果
    int deleteByUserId(@Param("userId") Long userId);
    
    // 查询所有结果
    List<SCL90Result> findAll();
} 
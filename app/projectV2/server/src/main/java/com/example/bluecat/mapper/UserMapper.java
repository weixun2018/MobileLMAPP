package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    // 根据用户名查找用户
    User findByUsername(@Param("username") String username);
    
    // 根据邮箱查找用户
    User findByEmail(@Param("email") String email);
    
    // 根据手机号查找用户
    User findByPhone(@Param("phone") String phone);
    
    // 检查用户名是否存在
    int countByUsername(@Param("username") String username);
    
    // 检查邮箱是否存在
    int countByEmail(@Param("email") String email);
    
    // 检查手机号是否存在
    int countByPhone(@Param("phone") String phone);
    
    // 注意：以下方法由BaseMapper提供，无需重复定义
    // selectById(Long id) - 替代 findById
    // insert(User user) - 已提供
    // updateById(User user) - 已提供
    // deleteById(Long id) - 已提供
    // selectList(Wrapper) - 可替代 findAll
} 
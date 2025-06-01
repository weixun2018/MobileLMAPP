package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问层
 * 继承BaseMapper<User>，自动获得基础的CRUD操作方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    // ==================== 自定义查询方法 ====================
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息，如果不存在返回null
     */
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 用户信息，如果不存在返回null
     */
    User findByEmail(@Param("email") String email);
    
    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户信息，如果不存在返回null
     */
    User findByPhone(@Param("phone") String phone);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 存在的用户数量（0或1）
     */
    int countByUsername(@Param("username") String username);
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱地址
     * @return 存在的用户数量（0或1）
     */
    int countByEmail(@Param("email") String email);
    
    /**
     * 检查手机号是否存在
     * @param phone 手机号
     * @return 存在的用户数量（0或1）
     */
    int countByPhone(@Param("phone") String phone);
    
    /**
     * 更新用户MBTI类型
     * @param userId 用户ID
     * @param mbtiType MBTI类型代码
     */
    void updateMbtiType(@Param("userId") Long userId, @Param("mbtiType") String mbtiType);

    // 注意：以下方法由BaseMapper提供，无需重复定义
    // selectById(Long id) - 替代 findById
    // insert(User user) - 已提供
    // updateById(User user) - 已提供
    // deleteById(Long id) - 已提供
    // selectList(Wrapper) - 可替代 findAll
} 
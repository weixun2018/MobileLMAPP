package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 新闻资讯数据访问层
 * 继承BaseMapper<News>，自动获得基础的CRUD操作方法
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {
    
    // ==================== 自定义查询方法 ====================
    
    /**
     * 获取最新的新闻列表
     * @param limit 限制条数
     * @return 最新新闻列表
     */
    List<News> findLatestNews(@Param("limit") int limit);
    
    /**
     * 根据标题查找新闻（避免重复）
     * @param title 新闻标题
     * @return 新闻信息，如果不存在返回null
     */
    News findByTitle(@Param("title") String title);
    
    /**
     * 删除旧新闻（保留最新的N条）
     * @param keepCount 保留的新闻数量
     * @return 删除的新闻数量
     */
    int deleteOldNews(@Param("keepCount") int keepCount);
    
    // ==================== BaseMapper提供的方法说明 ====================
    // 以下方法由MyBatis-Plus的BaseMapper自动提供，无需手动实现
    
    /**
     * 根据ID查询新闻
     * 使用方法：newsMapper.selectById(newsId)
     * @param id 新闻ID
     * @return 新闻信息，如果不存在返回null
     */
    // News selectById(Serializable id);
    
    /**
     * 插入新的新闻
     * 使用方法：newsMapper.insert(news)
     * @param entity 新闻实体对象
     * @return 影响的行数
     */
    // int insert(News entity);
    
    /**
     * 根据ID更新新闻信息
     * 使用方法：newsMapper.updateById(news)
     * @param entity 新闻实体对象
     * @return 影响的行数
     */
    // int updateById(News entity);
    
    /**
     * 根据ID删除新闻
     * 使用方法：newsMapper.deleteById(newsId)
     * @param id 新闻ID
     * @return 影响的行数
     */
    // int deleteById(Serializable id);
    
    /**
     * 查询所有新闻列表
     * 使用方法：newsMapper.selectList(null)
     * @param queryWrapper 查询条件包装器，传null查询所有
     * @return 新闻列表
     */
    // List<News> selectList(Wrapper<News> queryWrapper);
    
    /**
     * 根据条件查询新闻数量
     * 使用方法：newsMapper.selectCount(queryWrapper)
     * @param queryWrapper 查询条件包装器
     * @return 新闻数量
     */
    // Integer selectCount(Wrapper<News> queryWrapper);
} 
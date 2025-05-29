package com.example.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bluecat.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsMapper extends BaseMapper<News> {
    
    // 获取最新的新闻列表
    List<News> findLatestNews(@Param("limit") int limit);
    
    // 根据标题查找新闻（避免重复）
    News findByTitle(@Param("title") String title);
    
    // 删除旧新闻（保留最新的N条）
    int deleteOldNews(@Param("keepCount") int keepCount);
    
    // 注意：以下方法由BaseMapper提供
    // insert(News news) - 已提供
    // selectById(Long id) - 替代 findById
    // selectList(Wrapper) - 可替代 findAll
} 
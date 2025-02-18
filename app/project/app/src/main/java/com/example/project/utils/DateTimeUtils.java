package com.example.project.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * 日期时间工具类
 * 用于处理时间戳的格式化和转换
 * 支持 ISO 8601 格式转换为用户友好的中文时间格式
 */
public class DateTimeUtils {
    /**
     * 输出格式化器
     * 格式：年/月/日 上午/下午 时:分
     * 使用中文语言环境显示上下午
     */
    private static final SimpleDateFormat outputFormat = 
        new SimpleDateFormat("yyyy/M/d a h:mm", Locale.CHINESE);

    /**
     * ISO 8601 格式解析器
     * 用于解析标准格式的时间字符串
     */
    private static final DateTimeFormatter isoFormatter = 
        DateTimeFormatter.ISO_DATE_TIME;

    /**
     * 中国时区
     * 用于确保时间显示符合中国用户习惯
     */
    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 将 ISO 8601 格式的时间戳转换为用户友好的格式
     * 
     * @param timestamp ISO 8601 格式的时间字符串
     * @return 格式化后的时间字符串，格式如："2024/3/15 下午 2:30"
     *         如果解析失败则返回原始字符串
     */
    public static String formatTimestamp(String timestamp) {
        try {
            // 解析ISO 8601格式的时间字符串
            Instant instant = isoFormatter.parse(timestamp, Instant::from);
            
            // 转换为中国时区的时间
            Date date = Date.from(instant.atZone(CHINA_ZONE).toInstant());
            
            // 格式化为指定格式
            return outputFormat.format(date);
        } catch (Exception e) {
            // 如果解析失败，返回原始字符串
            return timestamp;
        }
    }
}

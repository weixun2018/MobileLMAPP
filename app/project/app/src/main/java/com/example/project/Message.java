package com.example.project;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 聊天消息实体类
 * 用于表示聊天界面中的单条消息
 * 包含消息内容、类型和时间戳信息
 */
public class Message {
    // 消息类型常量
    public static final int TYPE_SENT = 1;      // 发送的消息
    public static final int TYPE_RECEIVED = 2;   // 接收的消息

    // 消息属性
    private String content;    // 消息内容
    private int type;         // 消息类型（发送/接收）
    private long timestamp;   // 消息时间戳

    /**
     * 构造函数
     * 创建新消息时自动记录当前时间戳
     * 
     * @param content 消息内容
     * @param type 消息类型（TYPE_SENT 或 TYPE_RECEIVED）
     */
    public Message(String content, int type) {
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();  // 使用系统当前时间作为时间戳
    }

    /**
     * 获取消息内容
     * @return 消息文本内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取消息类型
     * @return 消息类型（TYPE_SENT 或 TYPE_RECEIVED）
     */
    public int getType() {
        return type;
    }

    /**
     * 获取格式化的消息时间
     * 将时间戳转换为 HH:mm 格式
     * @return 格式化后的时间字符串
     */
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
} 
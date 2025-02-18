package com.example.project.models;

/**
 * 聊天消息实体类
 * 用于存储用户与 AI 助手之间的对话消息数据
 * 包含消息 ID、内容、发送者类型和时间戳等信息
 */
public class ChatMessage {
    private long id;           // 消息唯一标识符
    private String content;    // 消息内容
    private boolean isUser;    // 消息发送者类型（true: 用户, false: AI助手）
    private String timestamp;  // 消息发送时间戳

    /**
     * 聊天消息构造函数
     * @param id 消息唯一标识符
     * @param content 消息内容
     * @param isUser 是否为用户发送的消息
     * @param timestamp 消息发送时间戳
     */
    public ChatMessage(long id, String content, boolean isUser, String timestamp) {
        this.id = id;
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    /**
     * 获取消息 ID
     * @return 消息的唯一标识符
     */
    public long getId() {
        return id;
    }

    /**
     * 获取消息内容
     * @return 消息的文本内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 判断消息发送者类型
     * @return true 表示用户发送的消息，false 表示 AI 助手的回复
     */
    public boolean isUser() {
        return isUser;
    }

    /**
     * 获取消息发送时间
     * @return 消息的时间戳字符串
     */
    public String getTimestamp() {
        return timestamp;
    }
} 
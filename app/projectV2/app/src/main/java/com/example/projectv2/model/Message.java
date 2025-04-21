package com.example.projectv2.model;

public class Message {
    private long id;
    private String content;
    private long timestamp;
    private boolean isAi; // true表示AI消息，false表示用户消息

    public Message() {
    }

    public Message(String content, boolean isAi) {
        this.content = content;
        this.isAi = isAi;
        this.timestamp = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAi() {
        return isAi;
    }

    public void setAi(boolean ai) {
        isAi = ai;
    }
} 
// 导入必要的依赖
// DataTypes: 用于定义数据类型
// sequelize: 数据库连接实例
const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/db');

// 定义聊天消息模型
// 用于存储用户与 AI 的对话记录
// 包含消息内容和消息类型（用户/AI）
const ChatMessage = sequelize.define('ChatMessage', {
    // 消息内容字段
    // - 使用 TEXT 类型支持长文本
    // - 设置为必填字段
    content: {
        type: DataTypes.TEXT,
        allowNull: false
    },

    // 消息类型字段
    // - 使用布尔值区分用户和 AI 消息
    // - true 表示用户消息，false 表示 AI 消息
    // - 通过 getter 方法转换为数字类型（1/0）
    isUser: {
        type: DataTypes.BOOLEAN,
        allowNull: false,
        get() {
            return this.getDataValue('isUser') ? 1 : 0;
        }
    }
}, {
    // 表配置
    tableName: 'chat_messages',    // 指定表名
    timestamps: true,              // 启用时间戳
    indexes: [
        {
            fields: ['userId']     // 为用户 ID 创建索引提高查询性能
        }
    ]
});

// 导出模型
// 供其他模块使用
module.exports = ChatMessage; 
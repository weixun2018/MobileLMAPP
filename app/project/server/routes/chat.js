// 导入必要的依赖
// - express: Web 框架
// - protect: 认证中间件
// - ChatMessage: 聊天消息模型
const express = require('express');
const router = express.Router();
const { protect } = require('../middleware/auth');
const ChatMessage = require('../models/ChatMessage');

// 获取聊天历史记录
// - 需要用户认证
// - 支持分页查询
// - 按时间倒序排列
router.get('/history', protect, async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = 10;  // 每页显示条数
        const offset = (page - 1) * limit;  // 计算偏移量

        // 验证用户身份
        // 确保只能访问自己的聊天记录
        if (!req.user) {
            return res.status(401).json({
                success: false,
                message: 'User not found'
            });
        }

        // 查询聊天记录
        // - 按用户ID筛选
        // - 按时间倒序排序
        // - 使用分页参数
        const messages = await ChatMessage.findAll({
            where: { userId: req.user.id },
            order: [['createdAt', 'DESC']],
            limit,
            offset,
            raw: true  // 获取原始数据提高性能
        });

        // 格式化响应数据
        // - 提取必要字段
        // - 统一时间格式
        res.json({
            success: true,
            messages: messages.map(msg => ({
                id: msg.id,
                content: msg.content,
                isUser: msg.isUser,
                timestamp: msg.createdAt.toISOString()
            }))
        });
    } catch (error) {
        // 错误处理
        // 记录错误信息并返回友好提示
        console.error('Error fetching chat history:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching chat history',
            error: error.message
        });
    }
});

// 发送消息
// - 需要用户认证
// - 保存用户消息
// - 生成 AI 响应
router.post('/send', protect, async (req, res) => {
    try {
        const { content } = req.body;
        
        // 验证消息内容
        // 确保不为空且去除首尾空格
        if (!content || !content.trim()) {
            return res.status(400).json({
                success: false,
                message: 'Message content is required'
            });
        }

        // 保存用户消息
        // 记录发送者身份和消息内容
        const userMessage = await ChatMessage.create({
            content: content.trim(),
            isUser: true,
            userId: req.user.id
        });
        
        // TODO: 调用 AI API 获取响应
        // 目前使用模拟响应
        const aiResponse = "This is a simulated AI response";
        
        // 保存 AI 响应
        // 记录为系统消息
        const aiMessage = await ChatMessage.create({
            content: aiResponse,
            isUser: false,
            userId: req.user.id
        });
        
        // 返回完整对话
        // 包含用户消息和 AI 响应
        res.json({
            success: true,
            messages: [
                {
                    id: userMessage.id,
                    content: userMessage.content,
                    isUser: userMessage.isUser,
                    timestamp: userMessage.createdAt.toISOString()
                },
                {
                    id: aiMessage.id,
                    content: aiMessage.content,
                    isUser: aiMessage.isUser,
                    timestamp: aiMessage.createdAt.toISOString()
                }
            ]
        });
    } catch (error) {
        // 错误处理
        // 记录错误信息并返回友好提示
        console.error('Error sending message:', error);
        res.status(500).json({
            success: false,
            message: 'Error sending message',
            error: error.message
        });
    }
});

// 导出路由
module.exports = router; 
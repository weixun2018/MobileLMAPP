// 导入必要的依赖
// - express: Web 框架
// - jwt: 用于生成认证令牌
// - bcrypt: 密码加密工具
// - sequelize: 数据库操作
const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const { sequelize } = require('../config/db');
const bcrypt = require('bcryptjs');
const { Op } = require('sequelize');

// 测试路由
// 用于验证认证路由是否正常工作
router.get('/test', (req, res) => {
    res.json({ message: 'Auth routes are working' });
});

// 用户注册路由
// - 验证用户信息唯一性
// - 加密密码
// - 创建用户记录
// - 生成认证令牌
router.post('/register', async (req, res) => {
    try {
        const { username, email, password } = req.body;

        // 检查用户是否已存在
        // 使用 OR 条件同时检查用户名和邮箱
        // 避免重复注册
        const existingUser = await User.findOne({
            where: {
                [Op.or]: [
                    { username: username },
                    { email: email }
                ]
            }
        });

        if (existingUser) {
            return res.status(400).json({
                success: false,
                message: 'Username or email already exists'
            });
        }

        // 密码加密处理
        // 使用 bcrypt 生成安全的密码哈希
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // 创建新用户
        // 包含基本信息和默认头像
        const user = await User.create({
            username,
            email,
            password: hashedPassword,
            avatar: 'default_avatar.png'
        });

        // 生成 JWT 令牌
        // 包含用户 ID 和过期时间
        const token = jwt.sign(
            { id: user.id },
            process.env.JWT_SECRET,
            { expiresIn: '30d' }
        );

        // 返回成功响应
        // 包含令牌和用户基本信息
        res.status(201).json({
            success: true,
            token,
            user: {
                id: user.id,
                username: user.username,
                email: user.email,
                avatar: user.avatar
            }
        });
    } catch (error) {
        // 错误处理
        // 记录详细错误信息并返回友好提示
        console.error('Registration error:', error);
        res.status(500).json({
            success: false,
            message: 'Error registering user',
            error: error.message
        });
    }
});

// 用户登录路由
// - 验证用户存在性
// - 验证密码正确性
// - 生成新的认证令牌
router.post('/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        
        // 调试日志
        // 记录登录尝试的详细信息
        console.log('Login attempt:', { 
            username,
            password,
            bodyContent: JSON.stringify(req.body)
        });

        // 查找用户
        // 仅获取必要的字段提高性能
        const user = await User.findOne({
            where: { username },
            attributes: ['id', 'username', 'password']
        });

        // 用户不存在处理
        if (!user) {
            console.log('User not found:', username);
            return res.status(401).json({
                success: false,
                message: 'Invalid credentials'
            });
        }

        // 调试日志
        // 记录找到的用户信息
        console.log('Found user:', {
            id: user.id,
            username: user.username,
            passwordHash: user.password
        });

        // 验证密码
        // 使用 bcrypt 比较密码哈希
        const isMatch = await user.matchPassword(password);
        console.log('Password match:', isMatch);

        // 密码不匹配处理
        if (!isMatch) {
            console.log('Password mismatch:', {
                provided: password,
                storedHash: user.password
            });
            return res.status(401).json({
                success: false,
                message: 'Invalid credentials'
            });
        }

        // 生成新的 JWT 令牌
        const token = jwt.sign({ id: user.id }, process.env.JWT_SECRET, {
            expiresIn: '30d'
        });

        // 返回成功响应
        // 包含令牌和用户基本信息
        res.json({
            success: true,
            token,
            user: {
                id: user.id,
                username: user.username
            }
        });
    } catch (error) {
        // 错误处理
        // 记录详细错误信息
        console.error('Login error:', error);
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// 导出路由
module.exports = router; 
// 导入必要的依赖
// - express: Web 框架
// - User: 用户模型
// - protect: 认证中间件
// - upload: 文件上传工具
// - bcrypt: 密码加密
// - fs: 文件系统操作
const express = require('express');
const router = express.Router();
const User = require('../models/User');
const { protect } = require('../middleware/auth');
const upload = require('../utils/fileUpload');
const bcrypt = require('bcryptjs');
const fs = require('fs').promises;
const path = require('path');

// 获取用户档案
// - 需要用户认证
// - 排除密码等敏感信息
router.get('/', protect, async (req, res) => {
    try {
        const user = await User.findByPk(req.user.id, {
            attributes: { exclude: ['password'] }  // 安全考虑：排除密码字段
        });
        
        res.json({
            success: true,
            user
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// 更新用户档案
// - 需要用户认证
// - 支持部分字段更新
// - 返回更新后的完整信息
router.put('/', protect, async (req, res) => {
    try {
        const { age, gender, grade, bio } = req.body;
        
        const user = await User.findByPk(req.user.id);
        
        // 更新字段
        // 仅更新提供的字段，保持其他字段不变
        if (age !== undefined) user.age = age;
        if (gender !== undefined) user.gender = gender;
        if (grade !== undefined) user.grade = grade;
        if (bio !== undefined) user.bio = bio;
        
        await user.save();
        
        // 返回更新后的用户信息
        // 排除敏感字段
        res.json({
            success: true,
            user: {
                id: user.id,
                username: user.username,
                email: user.email,
                avatar: user.avatar,
                age: user.age,
                gender: user.gender,
                grade: user.grade,
                bio: user.bio
            }
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// 上传头像
// - 需要用户认证
// - 处理文件上传
// - 管理旧头像文件
router.post('/avatar', protect, upload.single('avatar'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: 'Please upload a file'
            });
        }

        const user = await User.findByPk(req.user.id);
        
        // 删除旧头像
        // 仅删除非默认头像
        // 处理文件删除失败的情况
        if (user.avatar !== 'default_avatar.png') {
            try {
                await fs.unlink(path.join('uploads/avatars', user.avatar));
            } catch (error) {
                console.error('Error deleting old avatar:', error);
            }
        }

        // 更新数据库中的头像信息
        user.avatar = req.file.filename;
        await user.save();

        res.json({
            success: true,
            avatar: req.file.filename
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// 修改密码
// - 需要用户认证
// - 验证当前密码
// - 安全加密新密码
router.put('/password', protect, async (req, res) => {
    try {
        const { currentPassword, newPassword } = req.body;
        
        const user = await User.findByPk(req.user.id);
        
        // 验证当前密码
        // 确保用户知道原密码
        const isMatch = await user.matchPassword(currentPassword);
        if (!isMatch) {
            return res.status(401).json({
                success: false,
                message: 'Current password is incorrect'
            });
        }
        
        // 加密新密码
        // 使用 bcrypt 进行安全哈希
        const salt = await bcrypt.genSalt(10);
        user.password = await bcrypt.hash(newPassword, salt);
        await user.save();
        
        res.json({
            success: true,
            message: 'Password updated successfully'
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

module.exports = router; 
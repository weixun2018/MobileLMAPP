// 导入必要的依赖
// DataTypes: 用于定义数据类型
// bcrypt: 用于密码加密
// sequelize: 数据库连接实例
const { DataTypes } = require('sequelize');
const bcrypt = require('bcryptjs');
const { sequelize } = require('../config/db');

// 定义用户模型
// 用于存储用户基本信息和个性化设置
// 包含认证、个人资料等核心数据
const User = sequelize.define('User', {
    // 用户名
    // - 唯一标识符
    // - 必填且不可重复
    // - 用于用户登录
    username: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true
    },

    // 电子邮箱
    // - 用于通知和找回密码
    // - 必填且不可重复
    // - 确保格式有效
    email: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true,
        validate: {
            isEmail: true
        }
    },

    // 密码
    // - 存储加密后的哈希值
    // - 必填字段
    // - 实际长度由加密算法决定
    password: {
        type: DataTypes.STRING,
        allowNull: false
    },

    // 头像
    // - 存储头像文件路径
    // - 提供默认值
    avatar: {
        type: DataTypes.STRING,
        defaultValue: 'default_avatar.png'
    },

    // MBTI 类型
    // - 存储用户的性格类型
    // - 可选字段
    // - 固定长度为 4 字符
    mbti_type: {
        type: DataTypes.STRING(4),
        allowNull: true
    },

    // 年龄
    // - 用于个性化推荐
    // - 可选字段
    age: {
        type: DataTypes.INTEGER,
        allowNull: true
    },

    // 性别
    // - 用于个性化推荐
    // - 可选字段
    gender: {
        type: DataTypes.STRING,
        allowNull: true
    },

    // 年级
    // - 用于学习相关功能
    // - 可选字段
    grade: {
        type: DataTypes.STRING,
        allowNull: true
    },

    // 个人简介
    // - 支持长文本
    // - 可选字段
    bio: {
        type: DataTypes.TEXT,
        allowNull: true
    }
});

// 密码验证方法
// - 使用 bcrypt 比较密码
// - 返回布尔值表示验证结果
// - 用于用户登录验证
User.prototype.matchPassword = async function(enteredPassword) {
    return await bcrypt.compare(enteredPassword, this.password);
};

// 导出模型
// 供其他模块使用
module.exports = User;
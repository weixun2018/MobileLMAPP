// 导入必要的依赖
// DataTypes: 用于定义数据类型
// sequelize: 数据库连接实例
const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/db');

// 定义 MBTI 结果模型
// 用于存储用户的 MBTI 测试结果
// 包含最终类型和各维度的具体得分
const MBTIResult = sequelize.define('MBTIResult', {
    // 用户 ID
    // - 关联到用户表的外键
    // - 必填字段确保结果归属
    userId: {
        type: DataTypes.INTEGER,
        allowNull: false,
    },

    // MBTI 类型结果
    // - 使用固定长度字符串存储
    // - 必填字段记录最终类型
    // - 如：INTJ, ENFP 等
    mbti_type: {
        type: DataTypes.STRING(4),
        allowNull: false
    },

    // 外向维度得分
    // - 记录 E 倾向的得分
    // - 必填字段用于结果分析
    E_score: {
        type: DataTypes.INTEGER,
        allowNull: false
    },

    // 内向维度得分
    // - 记录 I 倾向的得分
    // - 必填字段用于结果分析
    I_score: {
        type: DataTypes.INTEGER,
        allowNull: false
    },

    // 感觉维度得分
    // - 记录 S 倾向的得分
    // - 必填字段用于结果分析
    S_score: {
        type: DataTypes.INTEGER,
        allowNull: false
    },

    // 直觉维度得分
    // - 记录 N 倾向的得分
    // - 必填字段用于结果分析
    N_score: {
        type: DataTypes.INTEGER,
        allowNull: false
    },

    // 思考维度得分
    // - 记录 T 倾向的得分
    // - 必填字段用于结果分析
    T_score: {
        type: DataTypes.INTEGER,
        allowNull: false
    },

    // 情感维度得分
    // - 记录 F 倾向的得分
    // - 必填字段用于结果分析
    F_score: {
        type: DataTypes.INTEGER,
        allowNull: false
    },

    // 判断维度得分
    // - 记录 J 倾向的得分
    // - 必填字段用于结果分析
    J_score: {
        type: DataTypes.INTEGER,
        allowNull: false
    },

    // 知觉维度得分
    // - 记录 P 倾向的得分
    // - 必填字段用于结果分析
    P_score: {
        type: DataTypes.INTEGER,
        allowNull: false
    }
}, {
    // 表配置
    tableName: 'mbti_results'  // 指定表名
});

// 导出模型
// 供其他模块使用
module.exports = MBTIResult; 
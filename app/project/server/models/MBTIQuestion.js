// 导入必要的依赖
// DataTypes: 用于定义数据类型
// sequelize: 数据库连接实例
const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/db');

// 定义 MBTI 问题模型
// 用于存储 MBTI 测试的问题内容和选项
// 每个问题对应一个 MBTI 维度
const MBTIQuestion = sequelize.define('MBTIQuestion', {
    // 问题文本
    // - 使用 TEXT 类型支持长文本
    // - 必填字段确保问题完整性
    question_text: {
        type: DataTypes.TEXT,
        allowNull: false
    },

    // 选项 A 文本
    // - 使用 TEXT 类型支持长文本
    // - 必填字段确保选项完整性
    option_a: {
        type: DataTypes.TEXT,
        allowNull: false
    },

    // 选项 B 文本
    // - 使用 TEXT 类型支持长文本
    // - 必填字段确保选项完整性
    option_b: {
        type: DataTypes.TEXT,
        allowNull: false
    },

    // MBTI 维度
    // - 使用枚举类型限制有效值
    // - EI: 外向/内向
    // - SN: 感觉/直觉
    // - TF: 思考/情感
    // - JP: 判断/知觉
    dimension: {
        type: DataTypes.ENUM('EI', 'SN', 'TF', 'JP'),
        allowNull: false
    }
});

// 导出模型
// 供其他模块使用
module.exports = MBTIQuestion; 
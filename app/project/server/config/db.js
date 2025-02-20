// 导入必要的依赖
// Sequelize: ORM 框架，用于数据库操作
// config: 数据库配置信息
const { Sequelize } = require('sequelize');
const config = require('./config');

// 打印数据库配置信息用于调试
// 注意：出于安全考虑不打印密码
// 帮助开发者快速定位配置问题
console.log('Database Configuration:', {
    host: config.database.host,
    port: config.database.port,
    database: config.database.database,
    username: config.database.username,
    // 密码信息已省略
});

// 创建 Sequelize 实例
// - 使用 MySQL 作为数据库引擎
// - 配置从 config 文件读取
// - 关闭 SQL 查询日志减少控制台输出
const sequelize = new Sequelize({
    dialect: 'mysql',
    host: config.database.host,
    port: config.database.port,
    database: config.database.database,
    username: config.database.username,
    password: config.database.password,
    logging: false
});

// 数据库连接函数
// - 验证数据库连接
// - 初始化数据模型
// - 同步数据库结构
// - 错误时优雅退出
const connectDB = async () => {
    try {
        // 测试数据库连接
        await sequelize.authenticate();
        console.log('Database connected successfully');
        
        // 导入并初始化所有模型
        // 确保在同步前加载所有模型定义
        require('../models');
        
        // 同步数据库结构
        // 根据模型定义创建或更新表结构
        await sequelize.sync();
        console.log('Database synchronized');
    } catch (error) {
        // 连接失败时记录错误并退出程序
        // 确保应用不会在数据库连接失败的情况下继续运行
        console.error('Failed to connect to the database:', error);
        process.exit(1);
    }
};

// 导出数据库实例和连接函数
// 供其他模块使用
module.exports = { sequelize, connectDB }; 
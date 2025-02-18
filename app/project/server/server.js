// 导入必要的依赖
// - express: Web 服务器框架
// - cors: 跨域资源共享
// - dotenv: 环境变量管理
// - path: 路径处理
// - models: 数据模型
const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const path = require('path');
const models = require('./models');

// 加载环境变量
// 从 .env 文件读取配置
dotenv.config();

// 导入数据库配置
// 包含连接实例和连接函数
const { sequelize, connectDB } = require('./config/db');

// 连接数据库
// 使用异步操作确保数据库就绪
connectDB().then(() => {
    const app = express();

    // 配置中间件
    // - cors: 允许跨域请求
    // - 支持所有来源
    // - 限制允许的方法和头部
    app.use(cors({
        origin: '*',
        methods: ['GET', 'POST', 'PUT', 'DELETE'],
        allowedHeaders: ['Content-Type', 'Authorization']
    }));
    app.use(express.json());

    // 静态文件服务
    // 用于提供上传的文件访问
    app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

    // 健康检查路由
    // 用于验证服务器运行状态
    app.get('/', (req, res) => {
        res.json({ message: 'Server is running' });
    });

    // 注册 API 路由
    // 按功能模块划分路由
    app.use('/api/auth', require('./routes/auth'));
    app.use('/api/chat', require('./routes/chat'));
    app.use('/api/profile', require('./routes/profile'));
    app.use('/api/mbti', require('./routes/mbti'));
    app.use('/api/psychology', require('./routes/psychology'));

    // 全局错误处理
    // 捕获未处理的异常
    // 返回统一的错误响应
    app.use((err, req, res, next) => {
        console.error(err.stack);
        res.status(500).json({
            success: false,
            message: 'Server error'
        });
    });

    // 服务器端口配置
    // 优先使用环境变量中的端口
    const PORT = process.env.PORT || 5000;

    // 启动服务器
    // 监听所有网络接口
    app.listen(PORT, '0.0.0.0', () => {
        console.log(`Server running on port ${PORT}`);
    });
}).catch(err => {
    // 数据库连接错误处理
    // 记录错误并终止程序
    console.error('Failed to connect to the database:', err);
    process.exit(1);
}); 
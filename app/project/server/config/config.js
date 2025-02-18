// 加载环境变量配置
// 使用 dotenv 包支持从 .env 文件读取配置
// 便于在不同环境下灵活配置
require('dotenv').config();

// 导出配置对象
// 包含数据库、JWT 和服务器相关配置
// 支持通过环境变量覆盖默认值
module.exports = {
    // 数据库配置
    // - 支持配置数据库连接参数
    // - 提供开发环境的默认值
    // - 通过环境变量支持不同环境配置
    database: {
        host: process.env.DB_HOST || 'localhost',      // 数据库主机地址
        port: process.env.DB_PORT || '3306',          // 数据库端口
        database: process.env.DB_NAME || 'ai_chat',    // 数据库名称
        username: process.env.DB_USER || 'root',       // 数据库用户名
        password: process.env.DB_PASSWORD || ''        // 数据库密码
    },

    // JWT 配置
    // - 用于用户认证和授权
    // - 配置密钥和过期时间
    // - 建议在生产环境中修改密钥
    jwt: {
        secret: process.env.JWT_SECRET || 'your-secret-key',  // JWT 签名密钥
        expiresIn: '30d'                                      // Token 过期时间
    },

    // 服务器配置
    // - 设置服务器监听端口
    // - 支持通过环境变量指定端口
    // - 默认使用 5000 端口
    server: {
        port: process.env.PORT || 5000
    }
}; 
// 导入必要的依赖
// jwt: 用于处理 JSON Web Token
// User: 用户模型，用于数据库查询
const jwt = require('jsonwebtoken');
const User = require('../models/User');

// 认证中间件
// 用于保护需要登录才能访问的路由
// 验证请求中的 JWT token 并加载对应用户信息
const protect = async (req, res, next) => {
    let token;

    // 从请求头中获取 token
    // 检查 Authorization 头是否存在且格式为 "Bearer <token>"
    // 这是 JWT 认证的标准格式
    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
        try {
            // 提取 token
            // 分割 "Bearer <token>" 字符串并获取第二部分
            token = req.headers.authorization.split(' ')[1];

            // 验证 token 的有效性
            // 使用 JWT_SECRET 解密 token
            // 如果 token 无效或过期会抛出异常
            const decoded = jwt.verify(token, process.env.JWT_SECRET);

            // 根据 token 中的用户 ID 获取用户信息
            // 排除密码字段以提高安全性
            // 将用户信息添加到请求对象中供后续中间件使用
            req.user = await User.findByPk(decoded.id, {
                attributes: { exclude: ['password'] }
            });

            // 继续处理请求
            next();
        } catch (error) {
            // 处理 token 验证失败的情况
            // 记录错误信息便于调试
            // 返回 401 未授权状态码
            console.error('Auth middleware error:', error);
            res.status(401).json({
                success: false,
                message: 'Not authorized'
            });
        }
    }

    // 处理没有提供 token 的情况
    // 返回 401 状态码和明确的错误信息
    if (!token) {
        res.status(401).json({
            success: false,
            message: 'Not authorized, no token'
        });
    }
};

// 导出中间件函数
// 供路由配置使用
module.exports = {
    protect
}; 
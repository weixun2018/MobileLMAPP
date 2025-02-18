// 导入必要的依赖
// - bcrypt: 密码加密
// - User: 用户模型
// - dotenv: 环境变量管理
// - path: 路径处理
const bcrypt = require('bcryptjs');
const { User } = require('../models/User');
const dotenv = require('dotenv');
const path = require('path');

// 加载环境变量
// 使用相对路径确保在任何目录下执行都能找到配置文件
dotenv.config({ path: path.join(__dirname, '../.env') });

// 创建管理员账户
// - 设置默认密码
// - 加密密码
// - 创建用户记录
async function createAdmin() {
    try {
        // 设置默认密码
        // 注意：生产环境应使用更强的密码
        // 建议通过环境变量配置
        const password = 'admin123';
        
        // 生成密码哈希
        // 使用 bcrypt 加密确保安全性
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);
        
        console.log('Creating admin user...');
        
        // 创建管理员用户
        // 使用预设的管理员信息
        // 实际应用中应通过配置文件设置
        const admin = await User.create({
            username: 'admin',
            email: 'admin@example.com',
            password: hashedPassword
        });
        
        // 输出创建结果
        // 仅显示非敏感信息
        console.log('Admin user created successfully:', {
            id: admin.id,
            username: admin.username,
            email: admin.email
        });
        
        // 正常退出
        process.exit(0);
    } catch (error) {
        // 错误处理
        // 记录错误并异常退出
        console.error('Error creating admin:', error);
        process.exit(1);
    }
}

// 执行创建过程
createAdmin(); 
// 导入必要的依赖
// - bcrypt: 密码加密工具
const bcrypt = require('bcryptjs');

// 密码测试函数
// - 测试已存储的哈希值
// - 生成新的哈希值
// - 验证密码匹配
async function testPassword() {
    // 测试密码和已存储的哈希值
    // 用于验证密码系统的正确性
    const password = 'admin123';
    const storedHash = '$2a$10$6kBwfnTsWrTcvRrSWPsJXOPx9GtlEe3W8KvvA/0tVzFp0LtPuKFXu';

    // 输出测试参数
    // 便于调试和验证
    console.log('Testing password:', {
        password,
        storedHash
    });

    // 生成新的哈希值
    // 验证哈希生成的一致性
    const salt = await bcrypt.genSalt(10);
    const newHash = await bcrypt.hash(password, salt);
    console.log('New hash generated:', newHash);

    // 验证与存储哈希的匹配
    // 确保存储的哈希值正确
    const isMatch = await bcrypt.compare(password, storedHash);
    console.log('Password match with stored hash:', isMatch);

    // 验证与新哈希的匹配
    // 确保哈希生成过程正确
    const isNewMatch = await bcrypt.compare(password, newHash);
    console.log('Password match with new hash:', isNewMatch);
}

// 执行测试
// 捕获并记录可能的错误
testPassword().catch(console.error); 
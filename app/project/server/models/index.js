// 导入数据模型
// 包含用户、聊天消息、MBTI 问题和结果等核心模型
const User = require('./User');
const ChatMessage = require('./ChatMessage');
const MBTIQuestion = require('./MBTIQuestion');
const MBTIResult = require('./MBTIResult');

// 设置模型关联关系
// 建立用户与其他实体之间的一对多关系

// 用户-聊天消息关联
// - 一个用户可以有多条聊天记录
// - 每条聊天记录必须属于一个用户
// - 使用 userId 作为外键
User.hasMany(ChatMessage, {
    foreignKey: {
        name: 'userId',
        allowNull: false
    },
    as: 'chatMessages'  // 定义关联别名，用于查询
});
ChatMessage.belongsTo(User, {
    foreignKey: {
        name: 'userId',
        allowNull: false
    }
});

// 用户-MBTI结果关联
// - 一个用户可以有多个 MBTI 测试结果
// - 每个测试结果必须属于一个用户
// - 使用 userId 作为外键
User.hasMany(MBTIResult, {
    foreignKey: {
        name: 'userId',
        allowNull: false
    },
    as: 'mbtiResults'  // 定义关联别名，用于查询
});
MBTIResult.belongsTo(User, {
    foreignKey: {
        name: 'userId',
        allowNull: false
    }
});

// 导出所有模型
// 便于其他模块统一引入
module.exports = {
    User,
    ChatMessage,
    MBTIQuestion,
    MBTIResult
}; 
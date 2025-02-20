// 导入必要的依赖
// - express: Web 框架
// - protect: 认证中间件
// - 数据模型: MBTI问题和结果
const express = require('express');
const router = express.Router();
const { protect } = require('../middleware/auth');
const MBTIQuestion = require('../models/MBTIQuestion');
const MBTIResult = require('../models/MBTIResult');
const User = require('../models/User');

// 获取所有MBTI问题
// - 需要用户认证
// - 用于展示测试题目
router.get('/questions', protect, async (req, res) => {
    try {
        console.log('Fetching MBTI questions...');
        const questions = await MBTIQuestion.findAll();
        console.log(`Found ${questions.length} questions`);
        res.json({
            success: true,
            questions
        });
    } catch (error) {
        // 错误处理
        // 记录错误信息并返回友好提示
        console.error('Error fetching MBTI questions:', error);
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// 提交MBTI测试答案并计算结果
// - 需要用户认证
// - 计算各维度得分
// - 确定MBTI类型
// - 更新用户档案
router.post('/submit', protect, async (req, res) => {
    try {
        const { answers } = req.body;
        
        // 初始化各维度得分
        // E/I: 外向/内向
        // S/N: 感觉/直觉
        // T/F: 思考/情感
        // J/P: 判断/知觉
        let scores = {
            E: 0, I: 0,
            S: 0, N: 0,
            T: 0, F: 0,
            J: 0, P: 0
        };

        // 计算每个维度的分数
        // 根据答案选择累加相应维度的分数
        answers.forEach(answer => {
            const dimension = answer.dimension;
            if (answer.choice === 'A') {
                scores[dimension[0]]++;
            } else {
                scores[dimension[1]]++;
            }
        });

        // 确定MBTI类型
        // 通过比较各维度分数确定最终类型
        const mbti_type = 
            (scores.E > scores.I ? 'E' : 'I') +
            (scores.S > scores.N ? 'S' : 'N') +
            (scores.T > scores.F ? 'T' : 'F') +
            (scores.J > scores.P ? 'J' : 'P');

        console.log('Updating user MBTI type:', {
            userId: req.user.id,
            mbti_type: mbti_type
        });

        // 保存测试结果
        // 记录详细得分和最终类型
        await MBTIResult.create({
            userId: req.user.id,
            mbti_type,
            E_score: scores.E,
            I_score: scores.I,
            S_score: scores.S,
            N_score: scores.N,
            T_score: scores.T,
            F_score: scores.F,
            J_score: scores.J,
            P_score: scores.P
        });

        // 更新用户的MBTI类型
        // 在用户档案中记录最新的测试结果
        const user = await User.findByPk(req.user.id);
        if (user) {
            user.mbti_type = mbti_type;
            await user.save();
            console.log('User MBTI type updated successfully');
        } else {
            console.error('User not found');
        }

        // 返回测试结果
        // 包含类型和详细得分
        res.json({
            success: true,
            mbti_type,
            scores
        });
    } catch (error) {
        // 错误处理
        // 记录错误信息并返回友好提示
        console.error('Error in MBTI submit:', error);
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// 获取用户最新的MBTI结果
// - 需要用户认证
// - 返回最近一次的测试结果
router.get('/result', protect, async (req, res) => {
    try {
        // 查询最新的测试结果
        // 按时间倒序排序获取第一条
        const result = await MBTIResult.findOne({
            where: { userId: req.user.id },
            order: [['createdAt', 'DESC']]
        });
        
        if (result) {
            // 格式化返回数据
            // 包含类型和详细得分
            res.json({
                success: true,
                hasResult: true,
                result: {
                    mbti_type: result.mbti_type,
                    scores: {
                        E: result.E_score,
                        I: result.I_score,
                        S: result.S_score,
                        N: result.N_score,
                        T: result.T_score,
                        F: result.F_score,
                        J: result.J_score,
                        P: result.P_score
                    }
                }
            });
        } else {
            // 处理无测试结果的情况
            res.json({
                success: true,
                hasResult: false
            });
        }
    } catch (error) {
        // 错误处理
        // 记录错误信息并返回友好提示
        console.error('Error fetching MBTI result:', error);
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// 导出路由
module.exports = router; 
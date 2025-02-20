// 导入必要的依赖
// - express: Web 框架
// - spawn: 子进程管理
// - path: 路径处理
const express = require('express');
const router = express.Router();
const { spawn } = require('child_process');
const path = require('path');

// 获取心理学新闻
// - 调用 Python 爬虫脚本
// - 处理跨平台兼容性
// - 解析爬虫返回数据
router.get('/news', async (req, res) => {
    try {
        console.log('Starting Python crawler...');
        // 根据操作系统选择正确的 Python 命令
        // Windows 使用 'python'，其他系统使用 'python3'
        const pythonCommand = process.platform === 'win32' ? 'python' : 'python3';

        // 启动 Python 爬虫进程
        // 使用绝对路径确保脚本位置正确
        const pythonProcess = spawn(pythonCommand, [
            path.join(__dirname, '../scripts/news_crawler.py')
        ]);

        // 初始化数据缓冲区
        // 用于收集爬虫输出的数据
        let dataString = '';
        let errorString = '';

        // 处理标准输出
        // 收集爬虫返回的数据
        pythonProcess.stdout.on('data', (data) => {
            console.log('Python stdout:', data.toString());
            dataString += data.toString();
        });

        // 处理标准错误
        // 收集错误信息用于调试
        pythonProcess.stderr.on('data', (data) => {
            console.error('Python stderr:', data.toString());
            errorString += data.toString();
        });

        // 处理进程结束
        // 解析数据并返回响应
        pythonProcess.on('close', (code) => {
            console.log('Python process exited with code:', code);
            if (code !== 0) {
                return res.status(500).json({
                    success: false,
                    message: 'Error fetching news: ' + errorString
                });
            }
            try {
                // 解析爬虫返回的 JSON 数据
                console.log('Raw Python output:', dataString);
                const newsData = JSON.parse(dataString);
                res.json({
                    success: true,
                    news: newsData
                });
            } catch (error) {
                // 处理 JSON 解析错误
                console.error('Error parsing news data:', error);
                res.status(500).json({
                    success: false,
                    message: 'Error parsing news data'
                });
            }
        });
    } catch (error) {
        // 处理服务器错误
        // 记录详细错误信息
        console.error('Server error:', error);
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// 导出路由
module.exports = router; 
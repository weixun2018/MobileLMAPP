// 导入必要的依赖
// - multer: 文件上传中间件
// - path: 路径处理
// - fs: 文件系统操作
const multer = require('multer');
const path = require('path');
const fs = require('fs');

// 创建上传目录
// - 确保目录存在
// - 支持递归创建
const uploadDir = 'uploads/avatars';
if (!fs.existsSync(uploadDir)){
    fs.mkdirSync(uploadDir, { recursive: true });
}

// 配置存储选项
// - 指定存储位置
// - 自定义文件名生成规则
const storage = multer.diskStorage({
    // 设置文件存储目录
    // 使用预定义的上传路径
    destination: (req, file, cb) => {
        cb(null, uploadDir);
    },
    // 生成唯一文件名
    // 防止文件名冲突
    // 保留原始扩展名
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, 'avatar-' + uniqueSuffix + path.extname(file.originalname));
    }
});

// 文件过滤器
// - 限制文件类型
// - 只允许图片文件
const fileFilter = (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
        cb(null, true);
    } else {
        cb(new Error('Not an image! Please upload an image.'), false);
    }
};

// 创建上传中间件
// - 配置存储选项
// - 设置文件过滤器
// - 限制文件大小
const upload = multer({
    storage: storage,
    fileFilter: fileFilter,
    limits: {
        fileSize: 1024 * 1024 * 5 // 5MB 文件大小限制
    }
});

// 导出上传中间件
// 供路由使用
module.exports = upload; 
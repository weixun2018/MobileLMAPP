- # 基于端侧语言模型的大学生心理健康助手App

  ## 项目概述

  本项目是一个面向大学生的心理健康服务应用，采用端侧语言模型进行本地化部署，提供智能心理咨询、MBTI 性格测试和心理学资讯等功能。项目注重用户隐私保护，通过本地化部署AI模型确保用户数据安全。

  ## 主要功能

  - 🤖 **智能心理咨询**
    - 基于端侧语言模型的实时对话
    - 本地化部署保护隐私
    - 聊天历史记录管理
    - 情绪分析与建议

  - 📊 **MBTI 性格测试**
    - 专业的 MBTI 测试题库
    - 16种性格类型详细解析
    - 四维度得分可视化
    - 个性化发展建议

  - 📰 **心理健康资讯**
    - 校园心理新闻推送
    - 心理健康知识科普
    - 专业文章阅读

  - 👤 **用户系统**
    - 安全的账号管理
    - 个人档案维护
    - 测试结果记录

  ## 技术架构

  ### 前端 (Android)

  - Java
  - Android SDK
  - 端侧AI模型集成
  - Fragment 页面管理
  - Volley 网络请求

  ### 后端 (Node.js)

  - Express.js 框架
  - MySQL 数据库
  - Sequelize ORM
  - JWT 身份认证
  - Python 爬虫

  ## 项目结构

  ```
  project-code/
  ├── app/                                # Android 客户端
  │   └── src/
  │       └── main/
  │           ├── java/                   # Java 源代码
  │           │   └── com/example/project/
  │           │       ├── adapters/       # 适配器类
  │           │       │   ├── ChatAdapter.java
  │           │       │   └── NewsAdapter.java
  │           │       ├── models/         # 数据模型类
  │           │       │   ├── ChatMessage.java
  │           │       │   └── NewsItem.java
  │           │       ├── utils/          # 工具类
  │           │       │   ├── ApiConfig.java
  │           │       │   ├── DateTimeUtils.java
  │           │       │   ├── PreferenceManager.java
  │           │       │   └── VolleyMultipartRequest.java
  │           │       ├── AIChatFragment.java     # AI聊天界面
  │           │       ├── LoginActivity.java      # 登录界面
  │           │       ├── MainActivity.java       # 主界面
  │           │       ├── MBTIResultFragment.java # MBTI结果界面
  │           │       ├── MBTITestFragment.java   # MBTI测试界面
  │           │       ├── Message.java            # 消息实体类
  │           │       ├── ProfileFragment.java    # 个人资料界面
  │           │       ├── PsychologyFragment.java # 心理资讯界面
  │           │       ├── RegisterActivity.java   # 注册界面
  │           │       └── UserSession.java        # 用户会话管理
  │           └── res/                     # 资源文件
  │               ├── color/               # 颜色资源
  │               │   └── bottom_nav_color.xml
  │               ├── drawable/            # 图形资源
  │               │   ├── bg_chat_ai.xml
  │               │   └── bg_chat_user.xml
  │               ├── layout/             # 布局文件
  │               └── values/             # 值资源
  │                   └── strings.xml
  └── server/                            # 服务端
      ├── config/                        # 配置文件
      │   ├── config.js
      │   └── db.js
      ├── middleware/                    # 中间件
      │   └── auth.js
      ├── models/                        # 数据模型
      │   ├── ChatMessage.js
      │   ├── MBTIQuestion.js
      │   ├── MBTIResult.js
      │   ├── User.js
      │   └── index.js
      ├── routes/                        # API路由
      │   ├── auth.js
      │   ├── chat.js
      │   ├── mbti.js
      │   ├── profile.js
      │   └── psychology.js
      ├── scripts/                       # 工具脚本
      │   └── news_crawler.py
      ├── package.json                   # 项目依赖配置
      └── server.js                      # 服务器入口文件
  ```

  ## 安装说明

  ### 前端部署

  1. 克隆项目

  ```bash
  git clone [repository-url]
  ```

  2. 使用 Android Studio 打开项目
  3. 配置 gradle 依赖
  4. 运行应用

  ### 后端部署

  1. 安装依赖

  ```bash
  cd server
  npm install
  ```

  2. 配置环境变量

  ```bash
  cp .env.example .env
  # 编辑 .env 文件
  ```

  3. 启动服务

  ```bash
  npm start
  ```

  ## 数据库配置

  1. 创建 MySQL 数据库
  2. 修改 .env 文件中的数据库配置
  3. 运行数据库迁移

  ```bash
  npm run migrate
  ```

  ## API 文档

  主要 API 端点：

  - `/api/auth/*` - 用户认证
  - `/api/mbti/*` - MBTI 测试
  - `/api/chat/*` - AI 对话
  - `/api/news/*` - 心理资讯

  详细 API 文档见 [API.md](./API.md)

  ## 开发团队

  - 开发者：王泽宇
  - 指导教师：魏勋
  - 所属院校：江西理工大学软件工程学院

  ## 注意事项

  1. 本项目代码仅包含核心业务逻辑
  2. 端侧模型文件需要单独下载配置
  3. 首次运行需要完成数据库初始化
  4. 建议在 Android 8.0 及以上版本运行

  ## License

  MIT License - 详见 [LICENSE](./LICENSE) 文件

  ## 联系方式

  如有问题或建议，请通过以下方式联系：

  - Email: wzy13319381001@163.com
  - GitHub: https://github.com/Wagon000
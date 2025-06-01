# 基于端侧语言模型的大学生心理健康助手APP

![项目状态](https://img.shields.io/badge/状态-已完成-brightgreen.svg)
![技术栈](https://img.shields.io/badge/技术栈-Android%20%7C%20Spring%20Boot%20%7C%20LLaMA-blue.svg)
![开发语言](https://img.shields.io/badge/语言-Java%20%7C%20C%2B%2B%20%7C%20Python-orange.svg)
![数据库](https://img.shields.io/badge/ORM-MyBatis--Plus%203.5.3.1-red.svg)
![架构迁移](https://img.shields.io/badge/迁移-JPA→MyBatis--Plus-yellow.svg)

## 📖 项目简介

本项目是一个基于端侧语言模型的大学生心理健康助手Android应用（又称小蓝猫App），旨在为大学生提供便捷、私密的心理健康服务。通过集成LLaMA大语言模型，实现本地化的AI心理咨询，保护用户隐私的同时提供专业的心理健康指导。

### 🎯 项目背景

随着现代社会压力的增加，大学生心理健康问题日益突出。传统的心理咨询服务存在预约困难、费用高昂、隐私担忧等问题。本项目利用先进的端侧AI技术，为大学生提供24/7可用的心理健康助手。

## ✨ 核心功能

### 🔐 1. 用户认证模块
- **用户注册**：支持邮箱/用户名注册，密码安全加密
- **用户登录**：JWT Token认证，保持登录状态
- **密码安全**：BCrypt加密存储，支持在个人中心修改密码

### 🤖 2. AI心理咨询模块
- **端侧推理**：集成LLaMA模型，本地运行保护隐私
- **智能对话**：专业心理咨询对话，支持多轮会话和上下文记忆
- **实时响应**：流式输出，实时显示AI回复过程
- **聊天记录**：本地SQLite存储对话历史，支持清除功能
- **会话管理**：支持重置聊天会话，清空历史上下文

### 📋 3. 心理测试模块
- **SCL-90量表**：权威心理健康评估工具
- **MBTI性格测试**：16型人格测试，了解性格特质
- **测试报告**：详细的测试结果分析和建议
- **结果存储**：测试结果保存至数据库，可重复查看

### 📰 4. 心理资讯模块
- **实时资讯**：从后端API获取心理健康相关新闻
- **下拉刷新**：支持手动刷新获取最新资讯
- **链接跳转**：点击新闻标题直接跳转到原始新闻链接
- **简洁展示**：显示新闻标题和发布日期

### 👤 5. 个人中心模块
- **个人信息**：编辑用户名、个人简介、年级、性别、年龄
- **头像管理**：上传和更换用户头像，支持图片压缩
- **密码管理**：安全修改登录密码
- **MBTI类型**：显示用户的MBTI人格类型
- **模型管理**：LLaMA模型下载、加载、卸载功能
- **账户安全**：安全登出功能

## 🛠️ 技术栈

### 前端 (Android App)
- **开发语言**：Java (主要) + Kotlin (构建脚本)
- **编译版本**：compileSdk 32, targetSdk 33, minSdk 28
- **UI框架**：Android SDK, Material Design
- **网络请求**：Retrofit2 2.9.0 + OkHttp3 4.9.0
- **图片加载**：Glide 4.15.1
- **JSON解析**：Gson 2.8.9
- **本地存储**：SQLite
- **AI推理**：LLaMA C++ Native库 (CMake 3.22.1)
- **异步处理**：Kotlin Coroutines 1.7.3
- **其他组件**：
  - androidx.fragment:fragment:1.5.5
  - androidx.appcompat:appcompat:1.3.0
  - androidx.constraintlayout:constraintlayout:2.0.4
  - androidx.recyclerview:recyclerview:1.2.1
  - androidx.swiperefreshlayout:swiperefreshlayout:1.1.0
  - com.google.android.material:material:1.4.0

### 后端 (Spring Boot)
- **开发语言**：Java 8
- **框架**：Spring Boot 2.7.0
- **数据库**：MySQL 8.0 (mysql-connector-java)
- **ORM**：MyBatis-Plus 3.5.3.1 (继承BaseMapper，自动CRUD)
- **安全认证**：Spring Security + JWT (jjwt 0.9.1)
- **JWT组件**：JwtAuthenticationFilter、JwtAuthenticationEntryPoint、JwtTokenUtil
- **XML绑定**：JAXB API 2.3.1 + JAXB Runtime 2.3.1 (Java 11+兼容性)
- **API架构**：RESTful API
- **爬虫模块**：Python + BeautifulSoup + Flask (独立服务)
- **工具库**：Lombok (简化代码)
- **依赖管理**：Maven
- **项目名称**：blue-cat-server (artifactId)
- **核心依赖**：
  - spring-boot-starter-web (Web MVC支持)
  - mybatis-plus-boot-starter (数据访问层)
  - spring-boot-starter-security (安全框架)
  - javax.xml.bind:jaxb-api (XML处理)
  - org.glassfish.jaxb:jaxb-runtime (JAXB运行时)

### 其他技术
- **版本控制**：Git
- **构建工具**：Gradle 7.x (前端) + Maven (后端)
- **IDE支持**：Android Studio (推荐)
- **C++标准**：C++17 (用于LLaMA集成)
- **NDK支持**：CMake 3.22.1
- **数据爬取**：Python爬虫脚本
- **模型部署**：LLaMA端侧部署 (GGUF格式)
- **云服务**：阿里云ECS + RDS MySQL
- **文件存储**：本地文件系统 (uploads目录)

## 📁 项目结构

```
projectV2/                        # 项目根目录
├── README.md                     # 项目说明文档
├── ai_chat_v2.sql               # MySQL数据库初始化脚本
├── build.gradle                 # 根项目Gradle构建配置
├── settings.gradle              # Gradle项目设置文件
├── gradle.properties            # Gradle属性配置文件
│
├── app/                          # Android前端应用模块
│   ├── src/main/
│   │   ├── java/com/example/projectv2/
│   │   │   ├── ProjectApplication.java     # 应用程序入口类
│   │   │   ├── MainActivity.java           # 主界面Activity
│   │   │   ├── LoginActivity.java          # 登录Activity
│   │   │   ├── RegisterActivity.java       # 注册Activity
│   │   │   ├── SplashActivity.java         # 启动页Activity
│   │   │   ├── LLamaAPI.java              # LLaMA模型接口
│   │   │   ├── ModelManager.java           # 模型管理器
│   │   │   ├── ModelDownloadService.java   # 模型下载服务
│   │   │   ├── fragment/                   # Fragment组件
│   │   │   │   ├── AiChatFragment.java     # AI聊天界面
│   │   │   │   ├── SCL90Fragment.java      # SCL-90测试界面
│   │   │   │   ├── MbtiFragment.java       # MBTI测试界面
│   │   │   │   ├── NewsFragment.java       # 资讯界面
│   │   │   │   ├── ProfileFragment.java    # 个人中心界面
│   │   │   │   └── TestSelectionFragment.java # 测试选择界面
│   │   │   ├── api/                        # API接口层
│   │   │   │   ├── ApiClient.java          # Retrofit客户端配置
│   │   │   │   ├── AuthInterceptor.java    # 认证拦截器
│   │   │   │   ├── UserApi.java            # 用户相关API
│   │   │   │   └── NewsApi.java            # 新闻相关API
│   │   │   ├── model/                      # 数据模型层
│   │   │   │   ├── User.java               # 用户实体
│   │   │   │   ├── News.java               # 新闻实体
│   │   │   │   ├── SCL90Question.java      # SCL-90题目
│   │   │   │   ├── SCL90Result.java        # SCL-90结果
│   │   │   │   ├── SCL90Factor.java        # SCL-90因子
│   │   │   │   ├── MbtiQuestion.java       # MBTI题目
│   │   │   │   ├── MbtiType.java           # MBTI类型
│   │   │   │   └── Message.java            # 聊天消息
│   │   │   ├── adapter/                    # RecyclerView适配器
│   │   │   │   ├── MessageAdapter.java     # 聊天消息适配器
│   │   │   │   └── NewsAdapter.java        # 新闻列表适配器
│   │   │   └── db/                         # 本地数据库
│   │   │       └── ChatDbHelper.java       # 聊天记录SQLite数据库
│   │   ├── cpp/                           # C++本地代码 (JNI)
│   │   │   ├── llama-android.cpp          # LLaMA Android JNI实现
│   │   │   └── CMakeLists.txt             # CMake构建配置
│   │   ├── res/                           # Android资源文件
│   │   │   ├── layout/                     # 布局文件
│   │   │   ├── drawable/                   # 图片资源
│   │   │   ├── drawable-v24/               # API 24+图片资源
│   │   │   ├── mipmap-*/                   # 应用图标 (多分辨率)
│   │   │   ├── values/                     # 默认值资源
│   │   │   ├── values-night/               # 夜间模式资源
│   │   │   ├── menu/                       # 菜单资源
│   │   │   └── xml/                        # XML配置文件
│   │   └── AndroidManifest.xml            # 应用清单文件
│   ├── build.gradle                       # Android模块构建配置
│   └── proguard-rules.pro                 # 代码混淆规则
│
└── server/                       # Spring Boot后端服务
    ├── src/main/
    │   ├── java/com/example/bluecat/       # Java源码包 (重命名自mental)
    │   │   ├── BlueCatServerApplication.java     # Spring Boot启动类
    │   │   ├── controller/                 # Web控制器层
    │   │   │   ├── UserController.java     # 用户管理API (/api/user)
    │   │   │   ├── SCL90Controller.java    # SCL-90测试API (/api/scl90)
    │   │   │   ├── MbtiController.java     # MBTI测试API (/api/mbti)
    │   │   │   ├── NewsController.java     # 资讯管理API (/api/news)
    │   │   │   └── FileUploadController.java # 文件上传API (/api/upload)
    │   │   ├── service/                    # 业务逻辑层
    │   │   │   ├── UserService.java        # 用户服务接口
    │   │   │   ├── SCL90Service.java       # SCL-90服务接口
    │   │   │   ├── MbtiService.java        # MBTI服务接口
    │   │   │   ├── NewsService.java        # 新闻服务接口
    │   │   │   └── impl/                   # 服务实现类
    │   │   │       ├── UserServiceImpl.java
    │   │   │       ├── SCL90ServiceImpl.java
    │   │   │       ├── MbtiServiceImpl.java
    │   │   │       └── NewsServiceImpl.java
    │   │   ├── mapper/                     # MyBatis-Plus数据访问层
    │   │   │   ├── UserMapper.java         # 用户数据映射器
    │   │   │   ├── NewsMapper.java         # 新闻数据映射器
    │   │   │   ├── MbtiQuestionMapper.java # MBTI题目映射器
    │   │   │   ├── MbtiTypeMapper.java     # MBTI类型映射器
    │   │   │   ├── SCL90Mapper.java        # SCL-90映射器 (整合)
    │   │   │   ├── SCL90FactorMapper.java  # SCL-90因子映射器
    │   │   │   └── SCL90QuestionMapper.java # SCL-90题目映射器
    │   │   ├── entity/                     # JPA实体类
    │   │   │   ├── User.java               # 用户实体 (@TableName)
    │   │   │   ├── News.java               # 新闻实体 (@TableName)
    │   │   │   ├── SCL90Result.java        # SCL-90结果实体
    │   │   │   ├── MbtiQuestion.java       # MBTI题目实体
    │   │   │   ├── MbtiType.java           # MBTI类型实体
    │   │   │   ├── SCL90Factor.java        # SCL-90因子实体
    │   │   │   └── SCL90Question.java      # SCL-90题目实体
    │   │   ├── dto/                        # 数据传输对象
    │   │   │   ├── UserDTO.java            # 用户DTO
    │   │   │   ├── SCL90QuestionDTO.java   # SCL-90题目DTO
    │   │   │   ├── SCL90ResultDTO.java     # SCL-90结果DTO
    │   │   │   ├── SCL90FactorDTO.java     # SCL-90因子DTO
    │   │   │   ├── MbtiQuestionDTO.java    # MBTI题目DTO
    │   │   │   ├── MbtiTypeDTO.java        # MBTI类型DTO
    │   │   │   └── NewsDTO.java            # 新闻DTO
    │   │   ├── config/                     # Spring配置类
    │   │   │   ├── SecurityConfig.java     # Spring Security配置
    │   │   │   ├── FileUploadConfig.java   # 文件上传配置
    │   │   │   └── PasswordEncoderConfig.java # 密码编码配置
    │   │   └── security/                   # 安全认证组件
    │   │       ├── JwtTokenUtil.java       # JWT工具类
    │   │       ├── JwtAuthenticationFilter.java # JWT认证过滤器
    │   │       └── JwtAuthenticationEntryPoint.java # JWT认证入口点
    │   ├── python/                        # Python微服务
    │   │   ├── news_crawler.py            # 新闻爬虫 (BeautifulSoup)
    │   │   └── app.py                     # Flask API服务
    │   └── resources/                     # 配置资源
    │       ├── application.yml.example    # 配置文件模板
    │       └── mapper/                    # MyBatis XML映射
    │           ├── UserMapper.xml          # 用户SQL映射
    │           ├── NewsMapper.xml          # 新闻SQL映射
    │           ├── MbtiQuestionMapper.xml  # MBTI题目SQL映射
    │           ├── MbtiTypeMapper.xml      # MBTI类型SQL映射
    │           ├── SCL90Mapper.xml         # SCL-90SQL映射
    │           ├── SCL90FactorMapper.xml   # SCL-90因子SQL映射
    │           └── SCL90QuestionMapper.xml # SCL-90题目SQL映射
    ├── uploads/                           # 文件上传目录
    │   └── avatars/                       # 用户头像存储
    └── pom.xml                            # Maven项目配置
```

### 📋 目录说明

#### 🤖 Android应用 (`app/`)
- **架构模式**: MVP模式，Fragment+Activity架构
- **UI框架**: Material Design + ConstraintLayout
- **网络层**: Retrofit2 + OkHttp3 + Gson
- **本地存储**: SQLite (聊天记录) + SharedPreferences (用户设置)
- **原生集成**: JNI + CMake (LLaMA C++集成)

#### 🚀 后端服务 (`server/`)
- **架构模式**: 分层架构 (Controller-Service-Mapper)
- **数据访问**: MyBatis-Plus + MySQL
- **安全认证**: Spring Security + JWT
- **API设计**: RESTful API + JSON数据传输
- **微服务**: Python Flask (新闻爬虫独立服务)

### 🏗️ 技术架构特色

#### 📱 移动端技术亮点
- **端侧AI推理**: LLaMA模型通过JNI集成，本地运行保护隐私
- **流式对话**: 实时显示AI回复过程，提升用户体验
- **离线功能**: 聊天记录本地存储，无网络也可查看历史
- **模型管理**: 支持模型下载、加载、卸载的完整生命周期管理
- **多分辨率适配**: mipmap资源支持不同屏幕密度

#### 🌐 后端技术亮点
- **分层解耦**: Controller-Service-Mapper三层架构，职责清晰
- **ORM优化**: MyBatis-Plus自动CRUD，减少样板代码
- **安全认证**: JWT无状态认证 + Spring Security权限控制
- **数据传输**: DTO模式隔离内部实体，保护数据安全
- **微服务架构**: Python爬虫服务独立部署，技术栈灵活

#### 🔧 开发规范
- **包名规范**: 
  - Android: `com.example.projectv2.*`
  - 后端: `com.example.bluecat.*` 
- **命名约定**:
  - Activity: `*Activity.java` (如 `MainActivity.java`)
  - Fragment: `*Fragment.java` (如 `AiChatFragment.java`)
  - API接口: `*Api.java` (如 `UserApi.java`)
  - 服务类: `*Service.java` / `*ServiceImpl.java`
  - 映射器: `*Mapper.java` + `*Mapper.xml`
- **资源组织**:
  - 布局文件: `res/layout/activity_*.xml`, `res/layout/fragment_*.xml`
  - 字符串: `res/values/strings.xml`
  - 颜色: `res/values/colors.xml`
  - 主题: `res/values/themes.xml`

## 🚀 快速开始

### 🌐 在线服务

**后端服务部署说明**
- **服务器地址**: `http://YOUR_SERVER_IP:8080`
- **API基础路径**: `http://YOUR_SERVER_IP:8080/api`
- **数据库**: MySQL 8.0

### ⚙️ 部署前配置

**重要提醒**: 在部署前，请确保修改以下配置文件中的IP地址和数据库信息：

1. **Android端API地址**：
   - 文件: `app/src/main/java/com/example/projectv2/api/ApiClient.java`
   - 修改: `BASE_URL` 为您的服务器地址

2. **服务端数据库配置**：
   - 文件: `server/src/main/resources/application.yml.example`
   - 复制为: `server/src/main/resources/application.yml`
   - 修改: 数据库连接URL、用户名、密码、JWT密钥

### 环境要求

- **JDK**: 1.8+ (服务器部署)
- **Android Studio**: 4.0+ (本地开发)
- **MySQL**: 8.0+ (已部署至阿里云)
- **Python**: 3.7+ (用于爬虫模块)
- **CMake**: 3.22.1+ (用于C++编译)

### 🏗️ 本地开发环境配置

#### Android应用配置

1. **修改API基础地址**：
   在Android项目中配置您的服务器地址：
   ```java
   // 在 app/src/main/java/com/example/projectv2/api/ApiClient.java 中
   public static final String BASE_URL = "http://YOUR_SERVER_IP:8080/";
   ```

2. **网络权限配置**：
   `app/src/main/AndroidManifest.xml` 中已包含必要的网络权限：
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   <uses-permission android:name="android.permission.usesCleartextTraffic" />
   ```

#### 数据库配置 (仅供参考)

数据库配置示例 (`application.yml`)：
```yaml
server:
  port: 8080
  address: 0.0.0.0

spring:
  datasource:
    url: jdbc:mysql://YOUR_DB_HOST:3306/ai_chat_v2?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.bluecat.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: your-secret-key
  expiration: 86400000 # 24小时
```

### 🚀 快速部署

#### 1. Android应用构建

1. 使用Android Studio打开`projectV2`根目录（包含settings.gradle的目录）
2. **重要**: 修改API地址配置
   - 编辑 `app/src/main/java/com/example/projectv2/api/ApiClient.java`
   - 将 `BASE_URL` 修改为您的服务器地址
   - 示例: `http://YOUR_SERVER_IP:8080/`
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击运行按钮构建并安装应用

#### 2. 数据库初始化

在部署后端服务前，需要初始化数据库：

1. **创建数据库**：
```sql
CREATE DATABASE ai_chat_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **导入数据库结构**：
```bash
mysql -u username -p ai_chat_v2 < ai_chat_v2.sql
```

**数据库表结构**：
- `users` - 用户信息表
- `news` - 新闻资讯表
- `mbti_questions` - MBTI测试题目表
- `mbti_types` - MBTI人格类型表
- `scl90_results` - SCL-90测试结果表
- `scl_factors` - SCL-90因子定义表
- `scl_questions` - SCL-90测试题目表

#### 3. 服务器端 (已部署至阿里云)

**✅ 后端服务状态**: 已部署运行
- **部署平台**: 云服务器ECS
- **服务端口**: 8080
- **数据库**: MySQL 8.0
- **访问地址**: http://YOUR_SERVER_IP:8080

**API接口测试**:
```bash
# 测试用户注册
curl -X POST http://YOUR_SERVER_IP:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","email":"test@example.com"}'

# 测试用户登录
curl -X POST http://YOUR_SERVER_IP:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 测试新闻接口
curl -X GET http://YOUR_SERVER_IP:8080/api/news
```

#### 4. Python爬虫服务 (可选)

如果需要启用新闻爬虫功能：

1. 进入Python目录：
```bash
cd server/src/main/python
```

2. 安装Python依赖：
```bash
pip install requests beautifulsoup4 flask
```

3. 启动Flask爬虫服务：
```bash
python app.py
```

**爬虫服务配置**：
- **服务端口**: 5000
- **API接口**: `http://localhost:5000/api/news`
- **目标网站**: 中国心理学网 (psy.china.com.cn)
- **技术栈**: BeautifulSoup + Flask + Requests

#### 5. 本地后端开发 (可选)

如果需要本地开发调试，可以按以下步骤配置：

1. **进入后端目录**：
```bash
cd server
```

2. **配置数据库连接**：
```bash
# 复制配置模板
cp src/main/resources/application.yml.example src/main/resources/application.yml

# 编辑配置文件，修改数据库连接信息
# 将数据库地址改为 localhost
```

3. **编译并运行**：
```bash
mvn clean install
mvn spring-boot:run
```

**本地开发配置要点**：
- 确保MySQL服务已启动
- 数据库`ai_chat_v2`已创建并导入数据
- 修改`application.yml`中的数据库连接参数
- JWT密钥可以使用默认值进行本地测试

### 📊 数据库表结构

项目使用MySQL 8.0数据库，包含以下7个核心表：

| 表名 | 功能 | 主要字段 | 备注 |
|------|------|----------|------|
| `users` | 用户信息 | id, username, password, email, phone, avatar_url, mbti_type, age, bio, gender, grade, created_at, updated_at | 支持唯一约束 |
| `news` | 新闻资讯 | id, title, url, publish_date, created_at | 爬虫数据存储 |
| `mbti_questions` | MBTI测试题目 | id, question_text, option_a, option_b, dimension | 20道测试题目 |
| `mbti_types` | MBTI人格类型 | type_code(PK), type_name, description, characteristics, strengths, weaknesses | 16种人格类型 |
| `scl90_results` | SCL-90测试结果 | id, user_id, factor_scores(JSON), positive_average, positive_items, total_average, total_score | 存储测试评分 |
| `scl_factors` | SCL-90因子定义 | id(PK), factor_name, description | 9个心理因子 |
| `scl_questions` | SCL-90测试题目 | id(PK), question_text, factor, factor_id | 90道测试题目 |

**数据库特性**：
- **字符集**: UTF8MB4 (支持Emoji等特殊字符)
- **排序规则**: utf8mb4_unicode_ci
- **引擎**: InnoDB (支持事务)
- **主键策略**: AUTO_INCREMENT
- **索引优化**: 用户表的username、email、phone字段建立唯一索引

### 🔧 LLaMA模型配置

1. 下载LLaMA模型文件（建议使用GGUF格式）
2. 将模型文件放置在Android设备的指定目录：
   ```
   /storage/emulated/0/Android/data/com.example.projectv2/files/models/
   ```
3. 在应用中配置模型路径

### 📡 API接口详情

#### 用户认证相关
- **POST** `/api/user/register` - 用户注册
- **POST** `/api/user/login` - 用户登录
- **GET** `/api/user/{userId}` - 获取用户信息
- **PUT** `/api/user/{userId}/field` - 更新用户字段
- **PUT** `/api/user/{userId}/password` - 修改密码

#### 心理测试相关
- **GET** `/api/scl90/questions` - 获取SCL-90题目
- **POST** `/api/scl90/results` - 提交SCL-90测试结果
- **GET** `/api/scl90/results/{userId}` - 获取用户测试结果
- **GET** `/api/mbti/questions` - 获取MBTI题目
- **GET** `/api/mbti/types/{typeCode}` - 获取MBTI类型详情
- **PUT** `/api/mbti/user/{userId}` - 更新用户MBTI类型

#### 新闻资讯相关
- **GET** `/api/news` - 获取最新资讯
- **POST** `/api/news/refresh` - 刷新资讯

#### 文件上传相关
- **POST** `/api/upload/avatar` - 上传用户头像

### 🔐 安全配置说明

#### 防火墙设置
确保阿里云服务器安全组已开放以下端口：
- **8080**: Spring Boot应用端口
- **3306**: MySQL数据库端口 (如需外部访问)

#### API访问示例
```javascript
// 前端请求示例
const API_BASE_URL = 'http://YOUR_SERVER_IP:8080/api';

// 用户登录
fetch(`${API_BASE_URL}/user/login`, {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        username: 'your_username',
        password: 'your_password'
    })
});
```

## 📱 应用截图

### 主要界面
- 登录注册界面
- AI聊天界面
- 心理测试界面
- 资讯浏览界面
- 个人中心界面

## 🔧 核心特性

### 🏠 端侧推理
- **隐私保护**：模型在本地运行，用户数据不上传
- **离线可用**：无需网络连接即可使用AI功能
- **响应迅速**：本地推理，减少网络延迟

### 🛡️ 安全性
- **数据加密**：用户密码BCrypt加密存储
- **JWT认证**：安全的用户身份验证机制
- **权限控制**：细粒度的功能权限管理

### 📊 专业性
- **权威量表**：使用SCL-90等专业心理评估工具
- **科学算法**：基于心理学理论的评分算法
- **个性化建议**：根据测试结果提供针对性建议

## 🔮 未来规划

### 功能扩展
- [ ] 支持更多心理测试量表
- [ ] 增加语音交互功能
- [ ] 集成更多AI模型选择
- [ ] 添加社区功能模块
- [ ] 支持数据云端同步
- [ ] 增加心理健康数据分析


## 📄 开源协议

本项目采用 MIT 协议 

## 👥 开发团队

- **项目开发人员**:  Questiony, qwqcoder
- **指导教师**: will Wei

## 📞 联系方式

- **邮箱**: 2640289029@qq.com(Questiony), qwqcoder@163.com(qwqcoder)
- **GitHub**: https://github.com/Questiony2002, https://github.com/qwqcoder

---

*本项目为毕业设计作品，旨在探索AI技术在心理健康领域的应用，为大学生群体提供便捷的心理健康服务。* 
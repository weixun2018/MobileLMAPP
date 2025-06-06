# CogniRAG

一个基于大语言模型的智能对话系统，具有记忆能力和用户画像分析功能。系统通过多轮对话不断学习和适应用户特征，提供个性化的交互体验。

## 功能特点

### 1. 智能对话能力
- 基于大语言模型的自然语言处理
- 支持多轮对话和上下文理解
- 自动提取用户意图和关键信息
- 生成自然、连贯的回复

### 2. 记忆系统
- 基于向量数据库的语义检索
- 支持对话历史的智能检索
- 智能线索提取和记忆关联
- 工作记忆和长期记忆管理

### 3. 用户画像分析
- 自动提取用户特征和偏好
- 结构化存储用户信息
- 动态更新用户画像
- 智能分析用户输入

### 4. 性能优化
- 显存管理和自动清理
- 嵌入向量缓存机制
- 批量处理优化
- 相似度阈值过滤

## 项目结构

```
CogniRAG/
├── src/
│   ├── config/                 # 配置管理
│   │   ├── __init__.py        # 配置包初始化文件
│   │   └── config.py          # 系统配置参数
│   │
│   ├── models/                # 模型接口
│   │   ├── __init__.py        # 模型包初始化文件
│   │   └── model_interface.py # 语言模型交互封装
│   │
│   ├── managers/              # 功能管理器
│   │   ├── __init__.py        # 管理器包初始化文件
│   │   ├── message_history.py # 对话历史管理
│   │   ├── user_profile.py    # 用户画像管理
│   │   └── memory.py         # 记忆系统管理
│   │
│   ├── __init__.py           # 项目主包初始化文件
│   └── app.py                # 主程序入口
│
├── test/                      # 测试目录
│   ├── evaluate_simple.py     # 记忆能力评估脚本
│   └── evaluate_example.py    # 评估示例脚本
│
├── data/                      # 数据目录
│   ├── user/                  # 用户数据存储
│   └── memory/                # 记忆数据存储
│
├── requirements.txt          # 项目依赖
└── README.md                # 项目说明
```

## 核心模块说明

### 1. 配置管理 (config/)
- **config.py**: 定义系统所有配置参数，包括角色提示模板、模型配置、对话配置、记忆配置和性能配置
- **__init__.py**: 配置包初始化文件，导出Config类，方便其他模块直接导入

### 2. 模型接口 (models/)
- **model_interface.py**: 封装与语言模型的交互，包括模型初始化、文本生成、嵌入向量计算、显存管理和批处理优化
- **__init__.py**: 模型包初始化文件，提供包级导入支持

### 3. 功能管理器 (managers/)
- **message_history.py**: 管理对话历史，维护上下文信息，处理消息格式化和历史记录清理
- **user_profile.py**: 负责用户画像管理，提取和存储用户信息，更新和合并用户数据
- **memory.py**: 实现记忆系统，包括记忆存储、检索、语义相似度计算和缓存管理
- **__init__.py**: 管理器包初始化文件，提供包级导入支持

### 4. 主程序 (src/)
- **app.py**: 系统主程序入口，整合所有组件并提供统一接口，实现用户输入处理流程
- **__init__.py**: 项目主包初始化文件，方便模块导入

### 5. 测试模块 (test/)
- **evaluate_simple.py**: 记忆能力评估脚本，通过一系列测试用例评估系统的记忆能力，包括基础信息记忆、时间序列记忆、项目信息记忆等
- **evaluate_example.py**: 评估示例脚本，提供评估方法的示例实现

## 脚本功能详解

### src/app.py
主程序模块，整合所有组件并提供统一的接口。实现了完整的用户输入处理流程：
- 初始化各个组件（模型接口、对话历史、用户画像、记忆系统）
- 定义用户输入处理流程（分析用户信息、提取线索、检索记忆、生成回复）
- 提供命令行交互界面
- 记录处理时间和性能指标

### src/config/config.py
配置管理模块，包含所有系统配置信息：
- 定义项目目录结构和文件路径
- 设置角色提示模板（用户画像分析、线索提取、回复生成）
- 配置模型参数（模型名称、嵌入模型、生成参数）
- 设置对话和记忆相关参数
- 优化性能的配置项

### src/models/model_interface.py
模型接口模块，负责与语言模型的交互：
- 封装模型初始化和加载过程
- 实现文本生成和响应处理
- 提供嵌入向量计算功能
- 管理显存和缓存
- 优化批量处理性能
- 自动清理内存机制

### src/managers/message_history.py
消息历史管理模块，负责处理对话历史：
- 维护当前对话状态和历史
- 处理系统、用户和AI消息
- 提供上下文信息获取功能
- 实现历史记录的清理和优化

### src/managers/user_profile.py
用户画像管理模块，负责用户信息的提取和管理：
- 从对话中提取用户特征
- 结构化存储用户信息
- 更新和合并用户数据
- 处理格式转换和数据清理
- 提供持久化存储功能

### src/managers/memory.py
记忆管理模块，负责对话历史的语义检索：
- 基于ChromaDB实现向量存储
- 提供记忆添加和检索功能
- 实现基于线索的语义搜索
- 管理工作记忆和长期记忆
- 优化记忆检索性能
- 提供记忆相似度计算和过滤

### test/evaluate_simple.py
对话评估测试脚本，评估系统的记忆能力：
- 提供多种记忆测试用例（基础信息记忆、时间序列记忆等）
- 实现测试用例执行和评分机制
- 生成评估报告和统计数据
- 支持过滤和选择测试用例
- 记录和保存测试结果

### test/evaluate_example.py
评估示例脚本，提供评估方法的示例实现和参考代码。

## 使用方法

1. 安装依赖：
```bash
pip install -r requirements.txt
```

2. 运行系统：
```bash
python src/app.py
```

3. 开始对话：
- 直接输入问题与AI助手对话
- 系统会自动学习用户特征
- 输入"退出"结束对话

4. 运行测试评估：
```bash
# 列出所有测试用例
python test/evaluate_simple.py --list

# 运行所有测试
python test/evaluate_simple.py

# 运行特定测试用例
python test/evaluate_simple.py --only "基础信息记忆测试"

# 跳过特定测试用例
python test/evaluate_simple.py --skip "长期记忆衰减测试"

# 指定结果输出目录
python test/evaluate_simple.py --output "my_results"
```

## 配置说明

主要配置参数在 `src/config/config.py` 中：

### 模型配置
- `MODEL_NAME`: 使用的语言模型
- `EMBEDDING_MODEL_NAME`: 使用的嵌入模型
- `MAX_NEW_TOKENS`: 生成回复的最大长度
- `TEMPERATURE`: 生成温度参数
- `TOP_P`: 生成采样参数

### 对话配置
- `MAX_HISTORY_ROUNDS`: 保存的对话轮数
- `SYSTEM_MESSAGE`: 系统提示消息

### 记忆配置
- `WORKING_MEMORY_SIZE`: 工作记忆容量
- `MEMORY_RETRIEVAL_TOP_K`: 检索记忆数量
- `SIMILARITY_THRESHOLD`: 相似度阈值

### 性能配置
- `EMBEDDING_BATCH_SIZE`: 批处理大小
- `EMBEDDING_CACHE_SIZE`: 缓存大小
- `MEMORY_CLEAR_FREQUENCY`: 显存清理频率
- `MEMORY_CLEAR_ENABLED`: 是否启用显存清理

## 注意事项

1. 系统要求：
   - Python 3.8+
   - CUDA 支持（推荐）
   - 足够的显存（建议至少 8GB）

2. 首次运行：
   - 会自动下载模型文件
   - 需要稳定的网络连接
   - 下载时间可能较长

3. 性能优化：
   - 可通过配置文件调整参数
   - 建议使用 GPU 运行
   - 注意显存使用情况
   - 适当调整批处理大小和缓存设置

4. 测试评估：
   - 评估结果保存在 evaluation_results 目录
   - 可以通过命令行参数自定义测试
   - 测试用例可以独立运行和扩展

## 许可证

MIT License 
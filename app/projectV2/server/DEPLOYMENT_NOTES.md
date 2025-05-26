# Blue Cat Server 部署说明

## 新闻爬虫服务环境兼容性

### 项目组成员的修改（已保留并兼容）

为了解决阿里云服务器中爬虫无法运行的问题，项目组成员进行了以下修改：

1. **Python脚本路径修改**：
   - 原路径：`src/main/python/news_crawler.py`
   - 服务器路径：`/app/news_crawler.py`

2. **Python命令修改**：
   - 原命令：`python`
   - 服务器命令：`python3`

### 当前兼容性解决方案

现在的代码支持自动环境检测，兼容本地开发和服务器部署：

#### 路径检测逻辑：
1. 优先检查服务器路径 `/app/news_crawler.py`（项目组修改）
2. 如果不存在，检查本地路径 `src/main/python/news_crawler.py`
3. 默认使用本地路径（向后兼容）

#### Python命令检测逻辑：
1. 优先尝试 `python3` 命令（服务器环境）
2. 如果不可用，尝试 `python` 命令（本地环境）
3. 默认使用 `python3`（服务器兼容）

### 部署指南

#### 本地开发环境：
- 确保 `src/main/python/news_crawler.py` 文件存在
- 安装Python依赖：`pip install requests beautifulsoup4`
- 应用会自动检测并使用本地配置

#### 阿里云服务器环境：
- 将爬虫脚本部署到 `/app/news_crawler.py`
- 确保 `python3` 命令可用
- 应用会自动检测并使用服务器配置

### 技术实现

在 `NewsServiceImpl.java` 中新增了以下方法：
- `detectEnvironmentAndGetScriptPath()`: 自动检测脚本路径
- `detectPythonCommand()`: 自动检测Python命令
- `isPythonCommandAvailable()`: 验证Python命令可用性

这种设计确保了代码在不同环境下的自动适配，无需手动修改配置。 
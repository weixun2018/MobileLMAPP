# 阶段一：LLM模型记忆功能实现

## 1. 项目背景
本项目实现了基于 MiniCPM-2B 模型的对话记忆功能。在实践过程中发现，虽然 MiniCPM-2B 模型的 `.chat()` 方法返回 history，但实际并不支持直接使用 history 参数来维持对话上下文，因此我们实现了一个自定义的记忆管理方案。

## 2. 核心特性

- 自定义记忆管理：独立于模型的记忆存储方案

- 上下文构建：智能的对话历史组织机制

- 实时响应：高效的记忆检索和更新

- 灵活配置：可调节的系统参数


## 3. 系统架构
### 3.1 应用场景

- 连续对话系统

- 上下文相关问答

- 个性化交互

- 知识追踪


```

[用户交互层]

    ↓

[记忆管理层] → 历史记录处理 → 上下文构建

    ↓

[模型交互层] → 输入整合 → 响应生成

    ↓

[存储层] → 内存存储

```

#### 2.2.2 记忆管理模块

- 数据结构：

  - 对话历史列表

  - 角色标记

  - 时间戳

- 管理机制：

  - 实时更新

  - 顺序维护

  - 容量控制



#### 2.2.3 上下文处理模块

- 构建策略：

  - 历史融合

  - 提示词优化

  - 长度控制

- 优化机制：

  - 动态裁剪

  - 重要信息保留

  - 格式规范化

## 3. 技术实现

### 3.1 基础模型调用
```python
from transformers import AutoModelForCausalLM, AutoTokenizer
import torch

path = 'openbmb/MiniCPM-2B-sft-bf16'
tokenizer = AutoTokenizer.from_pretrained(path)
MiniCPM = AutoModelForCausalLM.from_pretrained(path, 
                                              torch_dtype=torch.bfloat16, 
                                              device_map='cuda', 
                                              trust_remote_code=True)
```

### 3.2 记忆功能实现
我们实现了两种方案：

#### 方案一：直接使用模型返回的 history（不可行）
```python
def chat_with_memory_v1(question):
    response, history = MiniCPM.chat(tokenizer, question, 
                                   history=chat_history,  # 这种方式实际上不起作用
                                   temperature=0.8, 
                                   top_p=0.8)
    chat_history.extend(history)
    return response
```

#### 方案二：自定义记忆管理（推荐方案）
```python
chat_history = []

def chat_with_memory(question):
    # 将用户问题添加到历史记录
    chat_history.append({"role": "user", "content": question})
    
    # 构建包含历史上下文的问题
    context_question = (
        f"以下是我们之前的对话记录:\\n{chat_history}\\n"
        f"现在我的问题是: {question}"
    ) if chat_history else question
    
    # 获取回复并保存到历史记录
    response, history = MiniCPM.chat(tokenizer, context_question, 
                                   temperature=0.8, 
                                   top_p=0.8)
    chat_history.extend(history)
    
    return response
```

### 3.3 参数说明
- `tokenizer`: 用于文本分词的分词器
- `prompt`: 输入的问题文本
- `temperature`: 采样温度，控制输出的随机性（0-1之间）
- `top_p`: 控制采样概率的阈值（0-1之间）
- `chat_history`: 存储对话历史的列表，格式为 `[{"role": "user/assistant", "content": "消息内容"}, ...]`

### 3.4 使用示例
```python
# 初始化对话
question1 = "1+1=?"
response1 = chat_with_memory(question1)
print(f"用户: {question1}")
print(f"助手: {response1}\n")

# 测试记忆能力
question2 = "我刚刚问了你什么？"
response2 = chat_with_memory(question2)
print(f"用户: {question2}")
print(f"助手: {response2}")
```

## 4. 实现细节

### 4.1 对话历史格式
对话历史采用列表存储，每条记录包含两个字段：
- `role`: 表示发言者角色（"user" 或 "assistant"）
- `content`: 具体的对话内容

### 4.2 上下文构建
- 每次提问时，将完整的对话历史作为上下文
- 采用特定的格式化字符串构建提示词
- 确保模型能够理解对话的连续性

### 4.3 记忆管理
- 使用列表动态存储对话历史
- 支持实时更新和扩展对话记录
- 保持对话的时序性和完整性

## 5. 当前限制
1. 由于需要在每次提问时附带完整的对话历史，会消耗较多的 token
2. 对话历史仅保存在内存中，程序重启后历史记录会丢失
3. 没有实现对话历史的清理机制，可能导致 token 数量过多
4. 对话历史的存储结构较为简单，不支持复杂的检索需求

## 6. 后续优化方向

### 6.1 存储优化
1. 实现对话历史的持久化存储
   - 支持数据库存储（如 SQLite、MongoDB）
   - 支持文件系统存储（如 JSON、CSV）
2. 添加对话历史的清理机制
   - 基于时间的清理策略
   - 基于 token 数量的清理策略

### 6.2 功能优化
1. 优化上下文构建方式
   - 实现滑动窗口机制
   - 添加重要信息提取
2. 实现更智能的记忆检索机制
   - 添加语义相似度检索
   - 支持多维度的记忆检索

### 6.3 性能优化
1. 添加缓存机制
2. 优化 token 使用效率
3. 实现异步处理机制



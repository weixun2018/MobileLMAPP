# 本地部署并实现接口访问

+ 安装依赖

  > 匹配 `CUDA` 版本 12.4

  ```bash
  # CUDA 12.4
  conda install pytorch==2.5.1 torchvision==0.20.1 torchaudio==2.5.1 pytorch-cuda=12.4 -c pytorch -c nvidia
  ```
  
  

# Sglang推理

+ 源码安装 **sglang**

```bash
git clone https://github.com/sgl-project/sglang.git
cd sglang

pip install --upgrade pip
pip install -e "python[all]"
```

+ 源码安装 `flashinfer` 依赖

```bash
git clone https://github.com/flashinfer-ai/flashinfer.git --recursive
cd flashinfer
pip install -e .
```

+ 命令行执行调用

> `--model` 可替换为本地路径，这里是仓库 id
>
> 执行后再 `--port` 参数指定端口，监听请求

```bash
python -m sglang.launch_server --model openbmb/MiniCPM3-4B --trust-remote-code --port 30000 --chat-template chatml
```

+ Python 访问指定端口

```python
import openai
client = openai.Client(
    base_url="http://127.0.0.1:30000/v1", api_key="EMPTY")

# 对话
response = client.chat.completions.create(
    model="default",
    messages=[
        {"role": "system", "content": "You are a helpful AI assistant"},
        {"role": "user", "content": "你叫什么名字？"},
    ],
    temperature=0,
    max_tokens=64,
)
print(response.choices[0].message.content)
```

+ 支持流式访问

```python
stream = client.chat.completions.create(
    model="default",
    messages=[{"role": "user", "content": "Say this is a test"}],
    stream=True,
)
for chunk in stream:
    if chunk.choices[0].delta.content is not None:
        print(chunk.choices[0].delta.content, end="")
```

+ 常用参数

```python
response = client.chat.completions.create(
    model="meta-llama/Meta-Llama-3.1-8B-Instruct",
    messages=[
        {
            "role": "system",
            "content": "你是一位知识渊博的历史学家，提供简洁的回答。",
        },
        {"role": "user", "content": "给我讲讲古罗马"},
        {
            "role": "assistant",
            "content": "古罗马是一个以意大利为中心的文明。",
        },
        {"role": "user", "content": "他们的主要成就是什么？"},
    ],
    temperature=0.3,  # 较低的温度用于更专注的回答
    max_tokens=128,  # 合理的长度用于简洁的回答
    top_p=0.95,  # 稍高的值用于更好的流畅性
    presence_penalty=0.2,  # 轻微的惩罚以避免重复
    frequency_penalty=0.2,  # 轻微的惩罚以使语言更自然
    n=1,  # 单一回应通常更稳定
    seed=42,  # 保持可重复性
)
```


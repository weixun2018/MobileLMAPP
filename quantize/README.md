# 量化仓库

[MiniCPM3-4B_Q4_K_M](https://huggingface.co/qwqcoder/MiniCPM3-4B_Q4_K_M)

[MiniCPM3-4B-GPTQ](https://huggingface.co/openbmb/MiniCPM3-4B-GPTQ-Int4/tree/main)

[MiniCPM3-4B-AWQ](https://huggingface.co/qwqcoder/MiniCPM3-4B-AWQ)

# 量化模型

## llama.cpp

**示例代码**

+ 量化

  ```bash
  ./llama-quantize ./models/Minicpm/ggml-model-f16.gguf ./models/Minicpm/ggml-model-Q4_K_M.gguf Q4_K_M
  ```

+ 模型格式转换

  llama.cpp 项目路径下就有一个 `py` 脚本，用于模型转换

  ```bash
  python convert-hf-to-gguf.py models/Minicpm/
  ```




## AutoAWQ量化

+ 安装 AutoAWQ 工具

```bash
git clone https://github.com/LDLINGLINGLING/AutoAWQ.git
cd AutoAWQ
checkout minicpm3
pip install e .
```

+ 获取模型

```bash
git clone https://huggingface.co/openbmb/MiniCPM3-4B
```

+ 执行同级目录下 `awq_quant.py` 脚本文件

+ 使用 `transformers` 方法推理量化模型

```python
from transformers import AutoTokenizer, AutoModelForCausalLM
import time

# 加载模型和分词器
tokenizer = AutoTokenizer.from_pretrained("/content/quantize/awq-MiniCPM3-4B")  # 传入 awq_quant.py 脚本中保存模型的路径
model = AutoModelForCausalLM.from_pretrained("/content/quantize/awq-MiniCPM3-4B", trust_remote_code=True).cuda() # 传入 awq_quant.py 脚本中保存模型的路径

# 初始化对话历史
history = []

print("欢迎使用本模型，输入 quit 结束对话")

# 使用 for 循环实现对话循环
for i in range(1000):  # 假设最多 1000 轮对话
    query = input("user: ")
    if query == "quit":
        break  # 输入 quit 退出循环
    
    # 生成模型回复
    start = time.time()
    response, history = model.chat(tokenizer, query=query, history=history)
    elapsed_time = time.time() - start
    print(f"生成回答耗费时间: {elapsed_time:.2f}秒\n")
    print(f"model: {response}\n")
```


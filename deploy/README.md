# llama.cpp

## CPU

+ 安装 llama.cpp

  ```bash
  git clone https://github.com/ggerganov/llama.cpp
  # 进入 llama.cpp 路径
  ```

+ cmake 编译，（旧版本使用 make 编译即可）

  ```bash
  cmake -B build_cpu
  ```

  > 此处使用 `cmake version 3.31.4`
  >
  > 编译器使用 `Visual Studio 2019 MSVC v142 2019 c++ x64/x86` 生成工具，`mingw` 也可

  ```bash
  cmake --build build_cpu --config Release
  ```

## GPU

+ 安装 llama.cpp

  ```bash
  git clone https://github.com/ggerganov/llama.cpp
  # 进入 llama.cpp 路径
  ```

+ 确定 CUDA 版本，并安装

  > 项目采用 GTX 1060 6G 显卡
  >
  > 因为并非是新版本显卡，建议安装 CUDA 11 版本
  >
  > 最好是 CUDA 11.8 兼容性最佳
  >
  > 再安装好 cuDNN v8.9.7 for CUDA 11.x

+ cmake 编译

  ==建议利用内联参数明确 CUDA 地址和 CUDA 编译器==

  ```bash
  cmake -B build_gpu -DGGML_CUDA=1 \
  -DCUDAToolkit_ROOT='/c/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v11.0' \
  -DCudaToolkitDir='/c/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v11.0' \
  -DCMAKE_CUDA_COMPILER='/c/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v11.0/bin/nvcc.exe'
  ```

  ```bash
  cmake --build build_gpu --config Release
  ```




# 量化

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

  
# ollama

## 基础命令

ollama 类似使用 docker，大部分命令都是相似的

+ 拉取模型

  ```bash
  ollama pull model
  ```

+ 运行模型

  ```bash
  ollama run model
  ```

+ 停止模型

  ```bash
  ollama stop model
  ```

+ 删除模型映像

  ```bash
  ollama rm modelName
  ```

+ 创造模型映像

  ```bash
  ollama create [model_name] -f /path/to/Modefile
  # modelfile 是一个模型的配置文件，包含若干参数
  ```

## 运行端口修改

ollama 默认使用 11434 端口

+ windows 环境下，可以在环境变量中配置 `OLLAMA_PORT` 修改默认运行端口
+ docker 环境下配置 

## Modefile

+ 示例代码

  ```bash
  FROM ./DeepSeek-R1-Distill-Qwen-7B-Q4_K_M.gguf
  
  SYSTEM """You are Qwen, created by Alibaba Cloud. You are a helpful assistant."""
  
  PARAMETER temperature 1 # 规定模型创造性，越大创造性越强
  PARAMETER num_ctx 4096  # 设定上下文窗口大小
  
  
  TEMPLATE """{{- if .System }}{{ .System }}{{ end }}
  {{- range $i, $_ := .Messages }}
  {{- $last := eq (len (slice $.Messages $i)) 1}}
  {{- if eq .Role "user" }}<｜User｜>{{ .Content }}
  {{- else if eq .Role "assistant" }}<｜Assistant｜>{{ .Content }}{{- if not $last }}<｜end▁of▁sentence｜>{{- end }}
  {{- end }}
  {{- if and $last (ne .Role "assistant") }}<｜Assistant｜>{{- end }}
  {{- end }}
  """
  ```

+ FORM 

  说明模型的具体路径

+ SYSTEM

  指定模板中药使用的系统消息

+ PARAMETER

  定义一个在模型运行时的参数

  | 参数名称         | 描述                                                         | 值类型 | 示例用法               |
  | ---------------- | ------------------------------------------------------------ | ------ | ---------------------- |
  | `mirostat`       | 启用 Mirostat 采样以控制困惑度。默认值为 0，0 = 禁用，1 = Mirostat，2 = Mirostat 2.0 | int    | `mirostat 0`           |
  | `mirostat_eta`   | 影响算法对生成文本的反馈响应速度。较低的学习率会导致调整更慢，而较高的学习率会使算法更敏感。默认值为 0.1 | float  | `mirostat_eta 0.1`     |
  | `mirostat_tau`   | 控制输出的连贯性与多样性之间的平衡。较低的值会使文本更聚焦和连贯。默认值为 5.0 | float  | `mirostat_tau 5.0`     |
  | `num_ctx`        | 设置用于生成下一个 token 的上下文窗口大小。默认值为 2048     | int    | `num_ctx 4096`         |
  | `repeat_last_n`  | 设置模型回溯多远以防止重复。默认值为 64，0 = 禁用，-1 = 等于 `num_ctx` | int    | `repeat_last_n 64`     |
  | `repeat_penalty` | 设置对重复内容的惩罚强度。较高的值（例如 1.5）会更严格地惩罚重复内容，而较低的值（例如 0.9）会更宽松。默认值为 1.1 | float  | `repeat_penalty 1.1`   |
  | `temperature`    | 模型的温度。提高温度会使模型的回答更具创造性。默认值为 0.8   | float  | `temperature 0.7`      |
  | `seed`           | 设置用于生成的随机数种子。设置为特定数字会使模型对相同的提示生成相同的文本。默认值为 0 | int    | `seed 42`              |
  | `stop`           | 设置停止序列。当遇到此模式时，LLM 将停止生成文本并返回结果。可以通过在 `Modelfile` 中指定多个独立的 `stop` 参数来设置多个停止序列 | string | `stop "AI assistant:"` |
  | `tfs_z`          | 尾部自由采样用于减少输出中不太可能的 token 的影响。较高的值（例如 2.0）会减少这种影响，而值为 1.0 时禁用此设置。默认值为 1 | float  | `tfs_z 1`              |
  | `num_predict`    | 设置生成文本时的最大 token 数量。默认值为 128，-1 = 无限生成，-2 = 填充上下文 | int    | `num_predict 42`       |
  | `top_k`          | 减少生成无意义内容的概率。较高的值（例如 100）会生成更多样化的回答，而较低的值（例如 10）会更保守。默认值为 40 | int    | `top_k 40`             |
  | `top_p`          | 与 top-k 配合使用。较高的值（例如 0.95）会生成更多样化的文本，而较低的值（例如 0.5）会生成更聚焦和保守的文本。默认值为 0.9 | float  | `top_p 0.9`            |
  | `min_p`          | 作为 top_p 的替代，旨在确保质量和多样性的平衡。参数 p 表示相对于最可能 token 的概率，一个 token 被考虑的最小概率。例如，当 p=0.05 且最可能的 token 的概率为 0.9 时，概率小于 0.045 的 token 将被过滤掉。默认值为 0.0 | float  | `min_p 0.05`           |

+ TEMPLATE

  采用 Go 语音的模板语法，具体示例看官方
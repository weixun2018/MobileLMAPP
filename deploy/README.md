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

  

  
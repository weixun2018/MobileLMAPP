import torch
# 验证 torch cuda 模块
print(torch.__version__)          # 输出 PyTorch 版本（应为 2.0.1）
print(torch.cuda.is_available())  # 输出 True 表示 GPU 可用
print(torch.version.cuda)         # 输出 CUDA 版本（应为 11.8）
print(torch.cuda.current_device())  # 应输出 0
print(torch.cuda.get_device_name(0))  # 应显示 "GeForce GTX 1060 6GB"
import sys
# print(sys.path)
# 模型加载测试
# # test_load.py
# from transformers import AutoTokenizer, AutoModelForCausalLM

# model = AutoModelForCausalLM.from_pretrained(
#     r'./models/DeepSeek-V2/DeepSeek-V2-Lite',
#     trust_remote_code=True
# )
# print("Model loaded successfully!")
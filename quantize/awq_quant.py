from datasets import load_dataset	
from awq import AutoAWQForCausalLM	
from transformers import AutoTokenizer	
import torch
import os	


# 在MiniCPM/quantize/awq_quantize.py 文件中根据注释修改配置参数：
# 使用自定义数据集，按照示例补充custom_data即可

model_path = 'D:\work_space\MobileLMAPP\models\MiniCPM\MiniCPM-2B-dpo-bf16' # model_path or model_id	
quant_path = 'D:/work_space/quantize/minicpm_2B_awq_4q' # quant_save_path	
quant_config = { "zero_point": True, "q_group_size": 128, "w_bit": 4, "version": "GEMM" } # "w_bit":4 or 8	
quant_samples=512 # how many samples to use for calibration	

# custom_data = [
#     [  # first custom data
#         {"role": "system", "content": "You are a playful and friendly assistant."},
#         {"role": "user", "content": "给我讲讲古罗马"},
#         {"role": "assistant", "content": "古罗马是一个以意大利为中心的文明。"},
#     ],
#     [  # second custom data
#         {"role": "system", "content": "You are a playful and friendly assistant."},
#         {"role": "user", "content": "给我讲讲古罗马"},
#         {"role": "assistant", "content": "古罗马是一个以意大利为中心的文明。"},
#     ],
# ]

def parse_text_to_custom_data(file_path):
    # 从文件中读取文本
    with open(file_path, 'r', encoding='utf-8') as file:
        text = file.read()
    
    # 分割文本为不同的问题和回答
    sections = text.split("----")
    custom_data = []
    
    for section in sections:
        if not section.strip():
            continue
        
        # 提取问题和回答
        lines = section.strip().split("\n")
        question = None
        answer = None
        
        for line in lines:
            if line.startswith("问题"):
                question = line.split("：")[1].strip()
            elif line.startswith("回答"):
                answer = line.split("：")[1].strip()
        
        if question and answer:
            # 构建对话数据
            dialogue = [
                {"role": "system", "content": "You are a helpful and empathetic assistant."},
                {"role": "user", "content": question},
                {"role": "assistant", "content": answer}
            ]
            custom_data.append(dialogue)
    
    return custom_data

# 文件路径
file_path = r"quantize/responses.txt"

# 处理文本并生成 custom_data
custom_data = parse_text_to_custom_data(file_path)

# # 打印生成的 custom_data
# for i, dialogue in enumerate(custom_data):
#     print(f"Dialogue {i+1}:")
#     for turn in dialogue:
#         print(turn)
#     print("\n")

def get_device_map() -> str:
    return 'cuda' if torch.cuda.is_available() else 'cpu'

device = get_device_map()  # 'cpu'

# Load model	
model = AutoAWQForCausalLM.from_pretrained(model_path,safetensors=False)	
tokenizer = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True,device_map=device)	

def load_cust_data(custom_data):
    quant_data=[tokenizer.decode(tokenizer.apply_chat_template(i)) for i in custom_data]	
    return quant_data[:quant_samples]	

#使用自定义数据集进行量化
model.quantize(tokenizer, quant_config=quant_config, calib_data=load_cust_data(custom_data=custom_data))

# 保存模型
model.save_quantized(quant_path)	
tokenizer.save_pretrained(quant_path)	

print(f'Model is quantized and saved at "{quant_path}"')
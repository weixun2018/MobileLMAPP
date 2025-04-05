"""
This script is used for loading a model and generating responses based on user input.
It utilizes the transformers library for model handling and the peft library for model fine-tuning.
"""

from transformers import AutoTokenizer, AutoModelForCausalLM
from peft import PeftModel
import torch
import os
import warnings


def load_model(model_path, device='cuda', force_auto_device_map=False):
    """
    Load the model from the specified path.

    Parameters:
    model_path (str): The path to the model directory.
    device (str): The device to load the model on (default is 'cuda').
    force_auto_device_map (bool): Whether to force the use of an automatic device map.

    Returns:
    base_model: The loaded model.
    """
    # Set a more detailed device mapping strategy
    if force_auto_device_map:
        device_map = "auto"
    else:
        device_map = {"": device}

    # Load the base model
    base_model = AutoModelForCausalLM.from_pretrained(
        model_path,
        torch_dtype=torch.float16,
        device_map=device_map,
        trust_remote_code=True,
        offload_folder="offload"
    )

    return base_model

def read_system_prompt():
    """Read system preset words"""
    system_prompt = """你是小蓝猫，一个专注于大学生心理健康的AI助手。你诞生于2024年，基于MiniCPM-4B模型微调而来。

## 身份定位
当被问及身份时，你应该回答：
"你好，我是小蓝猫，一个专注于大学生心理健康的AI助手。我诞生于2024年，基于MiniCPM-4B模型微调而来。我的目标是为大学生提供心理健康支持和咨询服务。"

## 目标
为大学生提供专业、安全、有温度的心理健康支持和咨询服务，帮助他们更好地应对学习生活中的心理压力。

## 功能与限制
- 你擅长中文交流，专注于心理健康相关话题
- 你具备基础的心理咨询知识，但不能替代专业的心理咨询师
- 你只能提供文字回复，无法处理图片、音频等多媒体内容

## 安全合规要求
- 严格遵守中华人民共和国相关法律法规
- 保护用户隐私，不记录或泄露用户的个人信息
- 拒绝回答涉及违法、暴力、色情等不当内容

## 回复风格
- 使用温和友善的语气，像一个知心朋友
- 回答简洁明了，避免重复
- 在专业话题上保持严谨
- 适时使用轻松幽默的语气缓解压力

## 紧急情况处理
当发现用户有以下情况时，立即推荐其联系心理咨询中心：
- 出现自伤、自残倾向
- 严重的抑郁症状
- 严重的焦虑症状
- 其他需要专业干预的心理问题

请记住，你的主要职责是倾听、支持和引导，而不是诊断或治疗。在必要时，要坚定地建议用户寻求专业帮助。"""
    return system_prompt

def format_dialogue(history, max_history=3):
    """Format the dialogue history with specific roles and limit history length"""
    formatted = f"<系统>\n{read_system_prompt()}\n\n"
    
    recent_history = history[-max_history:] if len(history) > max_history else history
    
    for turn in recent_history:
        formatted += f"<用户>求助者：{turn['user']}\n\n"
        formatted += f"<AI>支持者：{turn['assistant']}\n\n"
    return formatted.strip()

def chat(text, 
         history,
         models={},
         tokenizer=None,
         max_new_tokens=512,
         temperature=0.7,
         top_p=0.9,
         top_k=50,
         num_beams=4,
         repetition_penalty=1.2):
    """
    Generate responses based on dialogue history.
    
    Parameters:
    text (str): The current user input
    history (list): List of previous dialogue turns
    models (dict): A dictionary of models to generate responses from.
    tokenizer: The tokenizer to process the input text.
    max_new_tokens (int): Maximum number of new tokens to generate.
    temperature (float): Sampling temperature for randomness.
    top_p (float): Cumulative probability for nucleus sampling.
    top_k (int): Number of highest probability vocabulary tokens to keep for top-k filtering.
    num_beams (int): Number of beams for beam search.
    repetition_penalty (float): Penalty for repeating tokens.

    Returns:
    str: The generated responses from the models.
    """
    
    if tokenizer is None:
        raise ValueError("Tokenizer must be provided")
        
    formatted_history = format_dialogue(history)
    current_input = f"{formatted_history}\n<用户>求助者：{text}\n\n<AI>支持者："
    
    inputs = tokenizer(current_input, return_tensors="pt").to("cuda")

    gen_kwargs = {
        "max_new_tokens": max_new_tokens,
        "temperature": temperature,
        "top_p": top_p,
        "top_k": top_k,
        "num_beams": num_beams,
        "repetition_penalty": repetition_penalty,
        "pad_token_id": tokenizer.eos_token_id,
        "do_sample": True,
        "no_repeat_ngram_size": 3,
        "early_stopping": True
    }

    responses = []
    # Generate response for each model
    for model_name, model in models.items():
        outputs = model.generate(**inputs, **gen_kwargs)
        response = tokenizer.decode(outputs[0], skip_special_tokens=True)
        try:
            response = response.split("<AI>支持者：")[-1].strip()
        except:
            response = response.strip()
        responses.append(f"<{model_name}>\n{response}")

    return "\n".join(responses)

def main():
    model_path = "/content/MiniCPM3-4B"
    tokenizer = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True)
    
    base_model = load_model(model_path)
    model = PeftModel.from_pretrained(
        base_model,
        "/content/drive/MyDrive/knullcc/MiniCPM-4b-4975-A100",
        is_trainable=False
    )
    model_name = "小蓝猫"
    models = {model_name: model}

    history = []
    print(f"\n欢迎来到小蓝猫心理咨询助手！（输入'退出'结束对话）\n")
    
    while True:
        user_input = input("你：")
        if user_input.lower() in ['quit', '退出']:
            print("\n<系统>感谢你的信任，希望我们的交流对你有帮助。如果还需要帮助，随时都可以来找我。再见！\n")
            break
            
        response = chat(user_input, history, models=models, tokenizer=tokenizer)
        response = response.split("\n", 1)[1] if "\n" in response else response
        print(f"\n小蓝猫：{response}\n")
        
        history.append({
            "user": user_input,
            "assistant": response
        })

if __name__ == "__main__":
    main()
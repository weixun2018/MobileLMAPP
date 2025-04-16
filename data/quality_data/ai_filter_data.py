"""
Author: knull-cc
Date: 2025-03-15
Description: Unified data processing script for evaluating and filtering single-turn and multi-turn psychological counseling data.
This script can automatically detect data formats:
Input format for single_turn_data:
[
    {
        "user": "user message", 
        "assistant": "assistant reply"
    }, 
    ...
]
Input Format for multi_turn_data:
[
    {
        "messages": [
            {
                "role": "user", 
                "content": "user message"
            }, 
            {   
                "role": "assistant", 
                "content": "assistant reply"
            }, 
            ...
        ]
    }, 
    ...
]
"""

import json
import requests
import time
import concurrent.futures
import os
from typing import List, Dict, Any, Union, Optional

# Configurable constants
INPUT_FILE = "multi_turn_data_19k.json"
MAX_WORKERS = 300  # Number of parallel processing threads
API_BASE_URL = "https://proxy.com"  # API base URL
API_KEY = "$API_KEY"  # API key



"""

"""
# System prompts
SYSTEM_PROMPT_SINGLE = """你是一位专业的心理咨询数据审核专家，负责评估对话质量。评估标准如下：

1. 内容相关性（符合任一类型即可）：
- 话题范围：心理咨询、心理健康、情绪管理
- 具体类型：
    * 情绪类：焦虑、抑郁、压力、烦躁、自卑、情绪困扰等
    * 关系类：恋爱、婚姻、家庭、社交、人际交往
    * 成长类：自我认知、目标规划、习惯养成、性格改变等
    * 生活类：学业压力、职场困惑、选择困难等

    
2. 回答质量要求（至少满足3项）：
- 建设性：提供具体建议或解决思路
- 同理心：理解和关心咨询者的处境
- 逻辑性：回答结构清晰，有条理
- 实用性：建议具有可操作性
- 深度：不是简单敷衍的回答

3. 禁止内容（严格执行）：
- 政治敏感、暴力、色情内容
- 歧视性言论、违法犯罪内容
- 明显的广告或营销内容
- 自残、自杀等危险倾向
- 具体药物推荐

4. 回复格式：
- 符合要求：回复'采纳'
- 不符合要求：回复'不采纳'
- 仅输出结论，无需解释原因"""

SYSTEM_PROMPT_MULTI = """你是一位专业的大学生心理咨询数据审核专家，负责评估对话质量。请按照以下标准进行评估：

1. 必要条件（必须同时满足）：
- 咨询者身份：必须是在校大学生或研究生
- 核心话题（必须严格符合以下类型之一）：
  * 学业类：学习压力、考试焦虑、学业倦怠、专业选择等
  * 情感类：校园恋爱、异地恋困扰、失恋适应等
  * 人际类：室友矛盾、社交障碍、校园人际关系等
  * 生涯类：专业规划、就业焦虑、职业选择等
  * 情绪类：学业焦虑、考试抑郁、自我怀疑等
  * 适应类：新生适应、独立生活、学校归属感等

2. 参考条件（满足其中部分即可）：
- 回答展现出基本的专业性和共情性
- 提供具体可行的建议
- 保持积极正向的指导方向

3. 基本要求：
- 咨询问题：不少于50字
- 回答内容：不少于100字

4. 禁止内容：
- 非大学生群体的问题
- 涉及自伤自残的内容
- 违法或严重违规内容

5. 回复格式：
- 符合必要条件且基本要求：回复'采纳'
- 不符合必要条件或基本要求：回复'不采纳'
- 仅输出结论，无需解释原因"""

def call_openai_api(prompt_content: str, system_prompt: str, input_data: Union[str, List[Dict[str, str]]]) -> bool:
    """
    Call OpenAI API for data quality evaluation
    
    Parameters:
    prompt_content (str): Prompt content submitted to API
    system_prompt (str): System prompt defining evaluation criteria
    input_data (str or list): Input data, can be string or message list
    
    Returns:
    bool: Whether passed evaluation
    """
    url = f"{API_BASE_URL}/v1/chat/completions"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}"
    }
    
    data = {
        "messages": [
            {
                "role": "system",
                "content": system_prompt
            },
            {
                "role": "user",
                "content": prompt_content
            }
        ],
        "model": "gpt-4o-mini",
        "max_tokens": 10,
        "temperature": 0.1
    }
    
    try:
        response = requests.post(url, headers=headers, json=data)
        response.raise_for_status()
        result = response.json()
        content = result["choices"][0]["message"]["content"]
        return content.strip() == "采纳"
    except Exception as e:
        print(f"API call error: {str(e)}")
        return False

def process_single_turn_data(item: Dict[str, str], system_prompt: str) -> bool:
    """
    Process single-turn data and evaluate
    
    Parameters:
    item (dict): Single-turn data item {"user": "...", "assistant": "..."}
    system_prompt (str): System prompt defining evaluation criteria
    
    Returns:
    bool: Whether passed evaluation
    """
    user_message = item.get('user', '')
    assistant_message = item.get('assistant', '')
    
    # Skip empty messages
    if not user_message or not assistant_message:
        return False
    
    prompt_content = f"请求者：{user_message} 支持者：{assistant_message}"
    return call_openai_api(prompt_content, system_prompt, item)

def process_multi_turn_data(item: Dict[str, List[Dict[str, str]]], system_prompt: str) -> bool:
    """
    Process multi-turn data and evaluate
    
    Parameters:
    item (dict): Multi-turn data item {"messages": [...]}
    system_prompt (str): System prompt defining evaluation criteria
    
    Returns:
    bool: Whether passed evaluation
    """
    messages = item.get('messages', [])
    
    # Skip empty message list
    if len(messages) < 2:
        return False
    
    conversation = "\n".join([f"{msg['role']}: {msg['content']}" for msg in messages])
    prompt_content = f"对话内容：\n{conversation}"
    return call_openai_api(prompt_content, system_prompt, messages)

def process_data(input_file: str, output_file: str) -> List[Dict]:
    """
    Unified processing of single-turn or multi-turn data and save results
    
    Parameters:
    input_file (str): Input file path
    output_file (str): Output file path
    
    Returns:
    list: List of processed results
    """
    try:
        # Read data
        with open(input_file, 'r', encoding='utf-8') as file:
            all_data = json.load(file)
        
        # Auto detect data type
        data_type = 'multi' if all_data and len(all_data) > 0 and 'messages' in all_data[0] else 'single'
        system_prompt = SYSTEM_PROMPT_MULTI if data_type == 'multi' else SYSTEM_PROMPT_SINGLE
                
        print(f"Detected data type: {data_type}")
        print(f"Will process {len(all_data)} records")
        
        results = []
        processed_count = 0
        
        # Save interval
        save_interval = 100
        temp_output_file = f"{output_file}.temp"
        
        try:
            # Use parallel processing
            with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
                if data_type == 'single':
                    future_to_item = {executor.submit(process_single_turn_data, item, system_prompt): item 
                                    for item in all_data}
                else:  # multi
                    future_to_item = {executor.submit(process_multi_turn_data, item, system_prompt): item 
                                    for item in all_data}
                
                for future in concurrent.futures.as_completed(future_to_item):
                    item = future_to_item[future]
                    processed_count += 1
                    
                    try:
                        if future.result():
                            results.append(item)
                            print(f"Progress: {processed_count}/{len(all_data)} - Evaluation passed")
                        else:
                            print(f"Progress: {processed_count}/{len(all_data)} - Evaluation failed")
                    except Exception as e:
                        print(f"Error processing data: {str(e)}")
                    
                    # Periodically save intermediate results
                    if processed_count % save_interval == 0:
                        with open(temp_output_file, 'w', encoding='utf-8') as f:
                            json.dump(results, f, ensure_ascii=False, indent=4)
                        print(f"Saved intermediate results ({processed_count}/{len(all_data)}) to {temp_output_file}")
        
        except KeyboardInterrupt:
            print("\nProcessing interrupted by user! Saving processed data...")
        except Exception as e:
            print(f"\nError occurred during processing: {str(e)}, saving processed data...")
        finally:
            # Save results
            if results:
                with open(output_file, 'w', encoding='utf-8') as f:
                    json.dump(results, f, ensure_ascii=False, indent=4)
                print(f"\nProcessed {processed_count}/{len(all_data)} records")
                print(f"Among them {len(results)} passed AI evaluation and saved to {output_file}")
            else:
                print("\nNo qualifying records found")
        
        return results
            
    except FileNotFoundError:
        print(f"Error: File not found {input_file}")
    except json.JSONDecodeError:
        print("Error: Invalid JSON format")
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return []

def find_json_files(directory='.'):
    """
    Find all JSON files in specified directory
    
    Parameters:
    directory (str): Directory path, defaults to current directory
    
    Returns:
    list: List of JSON file paths
    """
    json_files = []
    for file in os.listdir(directory):
        if file.endswith('.json') and not file.endswith('_output.json'):
            json_files.append(os.path.join(directory, file))
    return json_files

# Main program
if __name__ == "__main__":
    try:
        base_name = os.path.splitext(INPUT_FILE)[0]
        output_file = f"{base_name}_output.json"
        print(f"\nProcessing file: {INPUT_FILE}")
        process_data(INPUT_FILE, output_file)
    except FileNotFoundError:
        print(f"Error: File not found {INPUT_FILE}")
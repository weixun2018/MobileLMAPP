"""
Author: knull-cc
Date: 2025-03-15
Description: Convert single-turn dialogue data to multi-turn dialogue format.
Input format:
[
    {
        "user": "user message", 
        "assistant": "assistant reply"
    }, 
    ...
]
Output format:
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
import concurrent.futures
from threading import Lock
import os

# Configurable constants
INPUT_FILE = "评估集_原始单轮.json"  # Input file
MAX_WORKERS = 100 # Number of parallel processing threads
API_BASE_URL = "https://proxy.com/v1/chat/completions"  # API base URL
API_KEY = "$API_KEY"

# System prompt
SYSTEM_PROMPT = """你是一位经验丰富的心理咨询师，擅长将单轮对话扩展为简短而有效的多轮心理咨询对话。请基于提供的单轮对话内容，生成5轮精炼的心理咨询对话。

1. 对话格式：
   第X轮对话：
   求助者：<简短内容>
   支持者：<简短内容>

2. 对话特点：
   - 每轮对话要简短精炼，直击要点
   - 保持自然流畅的对话节奏
   - 运用专业咨询技巧（倾听、提问、共情等）
   - 避免冗长说教，保持对话简洁
   - 确保每句话都有价值，避免废话

3. 内容要求：
   - 第一轮：快速建立信任，了解核心诉求
   - 第二轮：初步探索问题根源
   - 第三轮：深入分析具体困扰
   - 第四轮：引导觉察和反思
   - 第五轮：提供简短具体的建议

请确保每轮对话简明扼要，突出重点，避免过多铺垫和修饰性语言。"""

def parse_multi_turn_dialog(api_response):
    """
    Parse API response, extract multi-turn dialogue content and convert to specified format
    
    Parameters:
    api_response (dict): Original API response
    
    Returns:
    list: List containing multi-turn dialogue messages
    """
    try:
        content = api_response['choices'][0]['message']['content']
        messages = []
        
        # Split dialogue rounds
        rounds = content.split('\n\n')
        for dialog_round in rounds:
            if not dialog_round.strip():
                continue
                
            # Split seeker and supporter dialogue in each round
            lines = dialog_round.split('\n')
            for line in lines:
                if '求助者：' in line:
                    user_content = line.replace('求助者：', '').strip()
                    messages.append({"role": "user", "content": user_content})
                elif '支持者：' in line:
                    assistant_content = line.replace('支持者：', '').strip()
                    messages.append({"role": "assistant", "content": assistant_content})
        
        return messages
    except Exception as e:
        print(f"Error parsing dialogue content: {str(e)}")
        return None

def call_openai_api(question, answer):
    """
    Call OpenAI API to convert single-turn dialogue to multi-turn dialogue
    
    Parameters:
    question (str): Original question
    answer (str): Original answer
    
    Returns:
    dict: Generated API response
    """
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}"
    }
    
    data = {
        "messages": [
            {
                "role": "system",
                "content": SYSTEM_PROMPT
            },
            {
                "role": "user",
                "content": f"请求者：{question}\n支持者：{answer}"
            }
        ],
        "model": "gpt-4o-mini",
        "max_tokens": 350,
        "temperature": 0.2
    }
    
    try:
        response = requests.post(API_BASE_URL, headers=headers, json=data)
        response.raise_for_status()
        return response.json()
    except Exception as e:
        print(f"API call failed: {str(e)}")
        return None

def process_dataset(input_file, output_file, max_workers=MAX_WORKERS):
    """
    Process dataset, convert single-turn dialogues to multi-turn dialogues
    
    Parameters:
    input_file (str): Input file path
    output_file (str): Output file path
    max_workers (int): Maximum number of concurrent worker threads
    """
    print_lock = Lock()
    results = []
    results_lock = Lock()
    
    # Save interval
    save_interval = 100
    temp_output_file = f"{output_file}.temp"
    
    # Try to recover from temporary file
    if os.path.exists(temp_output_file):
        try:
            with open(temp_output_file, 'r', encoding='utf-8') as f:
                results = json.load(f)
            print(f"Recovered {len(results)} processed dialogues from temporary file")
        except Exception as e:
            print(f"Error trying to recover temporary file: {str(e)}")
    
    def save_temp_results():
        """Save current results to temporary file"""
        try:
            with open(temp_output_file, 'w', encoding='utf-8') as f:
                json.dump(results, f, ensure_ascii=False, indent=4)
            print(f"Saved {len(results)} dialogues to temporary file")
        except Exception as e:
            print(f"Error saving temporary file: {str(e)}")
    
    def process_item(idx, item):
        question = item.get('user', '')
        answer = item.get('assistant', '')
        
        with print_lock:
            print(f"\nProcessing dialogue {idx+1}:\n{question[:100]}...")
            
        api_response = call_openai_api(question, answer)
        
        if api_response:
            messages = parse_multi_turn_dialog(api_response)
            if messages:
                result = {
                    "messages": messages
                }
                
                with results_lock:
                    results.append(result)
                    # Periodic saving
                    if len(results) % save_interval == 0:
                        save_temp_results()
                    
                with print_lock:
                    print(f"Dialogue {idx+1} processing completed")
            else:
                with print_lock:
                    print(f"Dialogue {idx+1} parsing failed")
        else:
            with print_lock:
                print(f"Dialogue {idx+1} processing failed")
    
    try:
        # Read input file
        with open(input_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        print(f"Read {len(data)} dialogue entries")
        
        # Get number of processed items to avoid reprocessing
        processed_count = len(results)
        
        # Use thread pool for concurrent processing
        with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
            futures = [executor.submit(process_item, idx, item) 
                      for idx, item in enumerate(data[processed_count:])]
            concurrent.futures.wait(futures)
        
        # Save final results
        if results:
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(results, f, ensure_ascii=False, indent=4)
            print(f"\nSaved {len(results)} multi-turn dialogues to {output_file}")
            
            # Delete temporary file after completion
            if os.path.exists(temp_output_file):
                os.remove(temp_output_file)
                print("Deleted temporary file")
        else:
            print("\nNo valid multi-turn dialogues generated")
            
    except Exception as e:
        print(f"Error occurred during processing: {str(e)}")
        # Try to save temporary results when error occurs
        if results:
            save_temp_results()

if __name__ == "__main__":
    # If input/output files not specified, can modify here
    input_file = INPUT_FILE
    
    # Generate output filename based on input filename
    base_name = os.path.splitext(input_file)[0]
    output_file = f"{base_name}_multi.json"
    
    process_dataset(input_file, output_file, MAX_WORKERS)

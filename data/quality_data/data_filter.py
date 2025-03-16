"""
This script processes a JSON file containing question and answer data,
searches for a specific keyword in the questions, and saves the results
to a CSV file.
"""

import json
import pandas as pd
import requests
import time
import concurrent.futures
from queue import Queue
from threading import Lock

def get_longest_answer(answers):
    """
    Select an answer from the answer list based on the following priorities:
    1. Prioritize recommended answers
    2. Sort recommended answers by likes
    3. If likes are equal, choose the longest answer
    4. If no recommended answers, apply the same rules to non-recommended answers
    
    Parameters:
    answers (list): List of answer dictionaries

    Returns:
    str: Content of the selected answer
    """
    if not answers:
        return ""
    
    # Split answers into recommended and non-recommended groups
    recommended = [a for a in answers if a.get('recommend_flag') == "推荐"]
    not_recommended = [a for a in answers if a.get('recommend_flag') != "推荐"]
    
    # First try to select from recommended group
    if recommended:
        selected = max(recommended,
                      key=lambda x: (int(x.get('zan', 0)), len(x.get('content', ''))))
    # If no recommended answers, select from non-recommended group
    elif not_recommended:
        selected = max(not_recommended,
                      key=lambda x: (int(x.get('zan', 0)), len(x.get('content', ''))))
    else:
        return ""
    
    return selected['content']

def call_openai_api(question, answer):
    """
    Call OpenAI API for data quality evaluation
    
    Parameters:
    question (str): Question content
    answer (str): Answer content
    
    Returns:
    bool: Whether to accept this data
    """
    url = "https://proxy/v1/chat/completions"
    headers = {
        "Content-Type": "application/json",
        "Authorization": "Bearer $API_KEY"
    }
    
    data = {
        "messages": [
            {
                "role": "system",
                "content": "你是一位专业的心理咨询数据审核专家，负责评估对话质量。评估标准如下：\n\n1. 内容相关性（符合任一类型即可）：\n- 话题范围：心理咨询、心理健康、情绪管理\n- 具体类型：\n  * 情绪类：焦虑、抑郁、压力、烦躁、自卑、情绪困扰等\n  * 关系类：恋爱、婚姻、家庭、社交、人际交往等\n  * 成长类：自我认知、目标规划、习惯养成、性格改变等\n  * 生活类：学业压力、职场困惑、选择困难等\n\n2. 回答质量要求（至少满足3项）：\n- 建设性：提供具体建议或解决思路\n- 同理心：理解和关心咨询者的处境\n- 逻辑性：回答结构清晰，有条理\n- 实用性：建议具有可操作性\n- 深度：不是简单敷衍的回答\n\n3. 禁止内容（严格执行）：\n- 政治敏感、暴力、色情内容\n- 歧视性言论、违法犯罪内容\n- 明显的广告或营销内容\n- 自残、自杀等危险倾向\n- 具体药物推荐\n\n4. 回复格式：\n- 符合要求：回复'采纳'\n- 不符合要求：回复'不采纳'\n- 仅输出结论，无需解释原因"
            },
            {
                "role": "user",
                "content": f"请求者：{question} 支持者：{answer}"
            }
        ],
        "model": "gpt-4o-mini",
        "max_tokens": 10,
        "temperature": 0.3,
        "frequency_penalty": 0.3,
        "presence_penalty": 0
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

def search_and_save_to_json(file_path, start_hash_id, batch_size, output_file):
    """
    Read specified amount of data starting from the given hash_id,
    filter using AI concurrently and save to JSON file.
    """
    results = []
    found_start = False
    processed_count = 0
    total_processed = 0
    print_lock = Lock()  # Add print lock
    results_lock = Lock()  # Add results lock
    
    def process_item(item_data):
        nonlocal total_processed
        ques_id = item_data.get('ques_id', '')
        ques_info = item_data.get('ques_info', {})
        title = ques_info.get('title', '')
        content = ques_info.get('content', '')
        content = f"{title}\n{content}"
        
        # Get the longest answer
        answers = item_data.get('answers_info', [])
        longest_answer = get_longest_answer(answers)
        
        if len(longest_answer) < 20:
            return None
            
        with print_lock:
            print(f"\nProcessing data #{total_processed + 1}:")
            print(f"Original ID: {ques_id}")
        
        # AI evaluation
        if call_openai_api(content, longest_answer):
            result = {
                "id": "single_turn_data_xxx",
                "question": content,
                "answer": longest_answer,
                "type": "filtered_dataset"
            }
            
            with print_lock:
                print(f"Original ID: {ques_id}")
                print("AI Evaluation Result: Accepted")
                print(f"请求者: {content}")
                print(f"支持者: {longest_answer}")
            return result
        else:
            with print_lock:
                print(f"AI Evaluation Result: Rejected")
                print(f"请求者: {content}")
                print(f"支持者: {longest_answer}")
            return None
    
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            data = json.load(file)
        
        items_to_process = []
        for item in data:
            if not start_hash_id or item.get('ques_id', '') == start_hash_id:
                found_start = True
            
            if not found_start:
                continue
                
            if processed_count >= batch_size:
                break
                
            staic_info = item.get('staic_info', {})
            if staic_info.get('reply', '0 answers') != '0 answers':
                items_to_process.append(item)
                processed_count += 1
        
        # Use thread pool for concurrent processing
        with concurrent.futures.ThreadPoolExecutor(max_workers=100) as executor:
            futures = [executor.submit(process_item, item) for item in items_to_process]
            
            for future in concurrent.futures.as_completed(futures):
                total_processed += 1
                result = future.result()
                if result:
                    with results_lock:
                        results.append(result)
        
        # Update final statistics
        print(f"\nProcessed {total_processed} items in total")
        print(f"Among them, {len(results)} passed AI evaluation and were saved")
        print(f"{total_processed - len(results)} items did not pass AI evaluation")
        
        # Save as JSON format
        if results:
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(results, f, ensure_ascii=False, indent=4)
            print(f"\nSaved {len(results)} records to {output_file}")
        else:
            print("\nNo matching records found")
            
    except FileNotFoundError:
        print(f"Error: File not found {file_path}")
    except json.JSONDecodeError:
        print("Error: Invalid JSON format")
    except Exception as e:
        print(f"An error occurred: {str(e)}")

# Main program
if __name__ == "__main__":
    file_path = "ques_ans1.json"
    start_hash_id = "c81e728d9d4c2f636f067f89cc14862c"  # 起始ID
    batch_size = 100000
    output_file = "single_turn_data_18k.json"
    
    search_and_save_to_json(file_path, start_hash_id, batch_size, output_file)
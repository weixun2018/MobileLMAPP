import json
import requests
import concurrent.futures
from threading import Lock

def parse_multi_turn_dialog(api_response):
    """
    Parse API response to extract multi-turn dialogue content and convert to specified format
    
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
            if len(lines) >= 2:  # 修改条件，因为有些行可能包含"第X轮对话："
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
    str: Generated multi-turn dialogue content
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
                "content": "你是一位专业的心理咨询师，需要基于提供的单轮对话内容，生成5轮完整的高质量心理咨询对话。要求：\n\n1. 对话格式：\n   第X轮对话：\n   求助者：<内容>\n   支持者：<内容>\n\n2. 对话要求：\n- 保持对话的连贯性和进展性\n- 每轮对话都要围绕原始问题展开\n- 体现专业的心理咨询技巧和同理心\n- 提供具体、可行的建议\n- 使用简洁的语言\n\n3. 数据质量要求：\n- 改写原始内容，避免直接复制\n- 删除重复、冗余的表达\n- 保持每轮对话内容的独特性\n- 确保用语专业且自然\n- 避免过度口语化或网络用语\n- 控制每轮对话的长度适中\n\n4. 输出要求：\n- 直接输出5轮对话内容\n- 不要包含任何额外解释\n- 每轮对话之间用换行分隔\n- 确保每轮对话都有实质性进展"
            },
            {
                "role": "user",
                "content": f"请求者：{question}\n支持者：{answer}"
            }
        ],
        "model": "gpt-4o-mini",
        "max_tokens": 1024,
        "temperature": 0.3,
        "frequency_penalty": 0.3,
        "presence_penalty": 0
    }
    
    try:
        response = requests.post(url, headers=headers, json=data)
        response.raise_for_status()
        return response.json()
    except Exception as e:
        print(f"API call failed: {str(e)}")
        return None

def process_dataset(input_file, output_file, max_workers=10):
    """
    Process dataset to convert single-turn dialogues to multi-turn dialogues
    
    Parameters:
    input_file (str): Input file path
    output_file (str): Output file path
    max_workers (int): Maximum number of concurrent workers
    """
    print_lock = Lock()
    results = []
    results_lock = Lock()
    
    def process_item(item):
        question = item.get('question', '')
        answer = item.get('answer', '')
        original_id = item.get('id', '')
        
        with print_lock:
            print(f"\nProcessing dialogue {original_id}:\n{question[:100]}...")
            
        api_response = call_openai_api(question, answer)
        
        if api_response:
            messages = parse_multi_turn_dialog(api_response)
            if messages:
                result = {
                    "id": f"Multi_turn_data_{original_id.split('_')[-1]}",
                    "messages": messages
                }
                
                with results_lock:
                    results.append(result)
                    
                with print_lock:
                    print(f"Dialogue {original_id} processing completed")
            else:
                with print_lock:
                    print(f"Dialogue {original_id} parsing failed")
        else:
            with print_lock:
                print(f"Dialogue {original_id} processing failed")
    
    try:
        # Read input file
        with open(input_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # Use thread pool for concurrent processing
        with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
            futures = [executor.submit(process_item, item) for item in data]
            concurrent.futures.wait(futures)
        
        # Save results
        if results:
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(results, f, ensure_ascii=False, indent=4)
            print(f"\nSaved {len(results)} multi-turn dialogues to {output_file}")
        else:
            print("\nNo valid multi-turn dialogues generated")
            
    except Exception as e:
        print(f"Error occurred during processing: {str(e)}")

if __name__ == "__main__":
    input_file = "single_turn_data_18k.json"
    output_file = "multi_turn_data_18k.json"

    max_workers = 100
    
    process_dataset(input_file, output_file, max_workers)

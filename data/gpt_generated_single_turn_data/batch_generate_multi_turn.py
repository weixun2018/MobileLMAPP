"""
Author: knull-cc
Date: 2024-10-20
Description: This script processes an Excel file containing single-turn dialogues,
generates payloads for a chat model, and sends requests to a specified URL.
It handles responses and saves them in JSON format.
"""

import pandas as pd
import requests
import json
import os
import concurrent.futures
import time
import re

def read_excel_and_generate_payload(excel_path, start_id=1, end_id=6000):
    """
    Reads an Excel file and filters rows based on ID range.

    Parameters:
    excel_path (str): The path to the Excel file.
    start_id (int): The starting ID for filtering (default is 1).
    end_id (int): The ending ID for filtering (default is 6000).

    Returns:
    DataFrame: Filtered DataFrame containing the relevant rows.
    """
    # Read Excel file
    df = pd.read_excel(excel_path)
    
    # Ensure Excel has three columns
    if len(df.columns) < 3:
        raise ValueError("The Excel file must contain at least 3 columns：ID, content, response")
    
    # Rename columns
    df.columns = ['ID', 'content', 'response'][:len(df.columns)]
    
    # Filter ID range
    df_filtered = df[(df['ID'] >= start_id) & (df['ID'] <= end_id)]
    
    return df_filtered

def parse_response_to_json(response_text):
    """
    Parses the response text into a structured JSON format.

    Parameters:
    response_text (str): The response text to be parsed.

    Returns:
    dict: A dictionary containing the structured messages.
    """
    try:
        # Replace escaped newlines
        text = response_text.replace('\\n', '\n')
        
        # Regular expression to match conversation rounds
        pattern = r'第(\d+)轮对话：\n求助者：(.*?)\n支持者：(.*?)(?=\n\n第|$)'
        matches = re.findall(pattern, text, re.DOTALL | re.MULTILINE)
        
        print(f"Number of matched rounds: {len(matches)}")
        
        messages = []
        for match in matches:
            # Remove newlines and extra spaces
            user_content = re.sub(r'\s+', ' ', match[1]).strip()
            assistant_content = re.sub(r'\s+', ' ', match[2]).strip()
            
            messages.append({"role": "user", "content": user_content})
            messages.append({"role": "assistant", "content": assistant_content})
        
        return {"messages": messages}
    
    except Exception as e:
        print(f"Error occurred while parsing response text: {e}")
        return {"messages": []}

def create_payload(row):
    """
    Creates a payload for the POST request based on the row data.

    Parameters:
    row (Series): A row from the DataFrame containing 'content' and 'response'.

    Returns:
    dict: The payload to be sent in the POST request.
    """
    # Build POST request payload
    payload = {
        "model": "gpt-4o-mini",
        "messages": [
            {
                "role": "system", 
                "content": "你是一个经验丰富的心理咨询师，我想让你担任大学生心理疏导及建议咨询师。您需要提供一个寻求指导和建议给大学生，以管理他们的情绪、压力、焦虑和其他心理健康问题。您应该利用您的认知行为疗法、冥想技巧、正念练习和其他治疗方法的知识来制定个人可以实施的策略，以改善他们的整体健康状况。请基于提供的单轮对话信息，根据单轮对话信息长度生成5～10轮完整的对话内容。求助者从求助者开始到支持者，每一轮对话的内容都是基于单轮对话，围绕用户的问题提供具体的建议，展现对用户问题的理解、同理心以及建设性的建议。【格式举例】第1轮对话：\n求助者：内容\n支持者：内容\n\n"
            },
            {
                "role": "user", 
                "content": f"下面是用户提供的对话：求助者：{row['content']} 支持者：{row['response']}"
            }
        ]
    }
    return payload

def send_request(payload, url, headers=None):
    """
    Sends a POST request to the specified URL with the given payload.

    Parameters:
    payload (dict): The payload to be sent in the request.
    url (str): The URL to send the request to.
    headers (dict, optional): Additional headers for the request.

    Returns:
    dict or None: The JSON response from the server or None if an error occurred.
    """
    if headers is None:
        headers = {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer api-key'
        }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        response.raise_for_status()  # Check for HTTP errors
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Request error occurred: {e}")
        return None

def process_row(row, url, output_dir):
    """
    Processes a single row, sends a request, and saves the response.

    Parameters:
    row (Series): A row from the DataFrame.
    url (str): The URL to send the request to.
    output_dir (str): The directory to save the response JSON files.

    Returns:
    dict or None: The response from the server or None if an error occurred.
    """
    try:
        payload = create_payload(row)  # Create payload for the request
        response = send_request(payload, url)  # Send the request
        
        if response and 'choices' in response and response['choices']:
            # Get content from the response
            content = response['choices'][0]['message']['content']
            
            # Convert response text to JSON format
            conversation_json = parse_response_to_json(content)
            
            # Create separate JSON file for each response
            response_file = os.path.join(output_dir, f'{row["ID"]}.json')
            with open(response_file, 'w', encoding='utf-8') as f:
                json.dump(conversation_json, f, ensure_ascii=False, indent=4)
            
            print(f"Processed data for ID {row['ID']}")
            return response
        else:
            print(f"Failed to process data for ID {row['ID']}")
            return None
    except Exception as e:
        print(f"Error occurred while processing ID {row['ID']}: {e}")
        return None

def process_excel_and_send_requests(excel_path, url, output_dir='data2', max_workers=10):
    """
    Processes the Excel file and sends requests concurrently.

    Parameters:
    excel_path (str): The path to the Excel file.
    url (str): The URL to send the requests to.
    output_dir (str): The directory to save the response JSON files (default is 'data2').
    max_workers (int): The maximum number of threads to use (default is 10).

    Returns:
    list: A list of all responses received.
    """
    # Create output directory
    os.makedirs(output_dir, exist_ok=True)
    
    # Read Excel data
    df = read_excel_and_generate_payload(excel_path)
    
    # List to store responses
    all_responses = []
    
    # Use thread pool for concurrent processing
    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        # Create a Future for each row
        futures = [
            executor.submit(process_row, row, url, output_dir) 
            for _, row in df.iterrows()
        ]
        
        # Wait for all tasks to complete and collect results
        for future in concurrent.futures.as_completed(futures):
            response = future.result()
            if response:
                all_responses.append(response)
            
            # Add small delay to avoid too rapid requests
            time.sleep(0.1)
    
    return all_responses

def main():
    """
    Main function to execute the script.
    It sets the Excel file path and the URL, processes the requests, and prints the total responses.
    """
    excel_path = 'single_turn_data.xlsx'  # Excel file path
    url = 'http://proxy.com/v1/chat/completions'
    
    responses = process_excel_and_send_requests(excel_path, url)
    print(f"Processed a total of {len(responses)} responses")

if __name__ == "__main__":
    main()
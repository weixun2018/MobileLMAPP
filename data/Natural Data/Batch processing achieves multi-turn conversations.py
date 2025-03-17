"""
This script processes a CSV file containing single-turn dialogues,
generates payloads for a chat model, and sends requests to an API
to obtain multi-turn conversation responses.
"""

import pandas as pd
import requests
import json
import os
import concurrent.futures
import time
import re

def read_excel_and_generate_payload(excel_path):
    """
    Reads a CSV file and generates a cleaned DataFrame for processing.

    Parameters:
    excel_path (str): The path to the CSV file.

    Returns:
    DataFrame: A cleaned DataFrame containing the relevant data.
    """
    # Read CSV file with strict parameters to handle multi-line content
    df = pd.read_csv(
        excel_path,
        encoding='utf-8',
        quoting=1,  # Use QUOTE_ALL mode
        engine='python',
        sep=',',
        dtype={'id': str},
        on_bad_lines='skip'  # Skip problematic rows
    )
    
    # Clean data
    df = df.dropna(subset=['id'])  # Delete rows with empty id
    
    # Clean data in each column
    for col in ['id', 'question', 'response']:
        if col in df.columns:
            df[col] = df[col].astype(str).apply(lambda x: x.strip())
            if col in ['question', 'response']:
                df[col] = df[col].apply(lambda x: ' '.join(x.replace('\r', ' ').replace('\n', ' ').split()))
    
    # Only keep rows where id is a 32-bit hash value
    df = df[df['id'].str.match(r'^[a-f0-9]{32}$', na=False)]
    
    print("Number of rows after cleaning:", len(df))
    print("\nPreview of first few rows:")
    print(df.head())
    
    return df

def parse_response_to_json(response_text):
    """
    Parses the response text from the API into a structured JSON format.

    Parameters:
    response_text (str): The raw response text from the API.

    Returns:
    dict: A dictionary containing the structured messages.
    """
    try:
        # Replace escaped newline characters
        text = response_text.replace('\\n', '\n')
        
        # Regular expression for matching dialogue turns
        pattern = r'第(\d+)轮对话：\n求助者：(.*?)\n支持者：(.*?)(?=\n\n第|$)'
        matches = re.findall(pattern, text, re.DOTALL | re.MULTILINE)
        
        print(f"Number of matched conversation turns: {len(matches)}")
        
        # If no dialogue is found, try other formats
        if len(matches) == 0:
            print("No standard format dialogue found, trying alternative formats...")
            
            # Try matching dialogues without "第X轮对话：" format
            alternative_pattern = r'求助者：(.*?)\n支持者：(.*?)(?=\n求助者：|$)'
            alt_matches = re.findall(alternative_pattern, text, re.DOTALL | re.MULTILINE)
            
            if alt_matches:
                print(f"Found {len(alt_matches)} turns of conversation using alternative format")
                
            # If still no match is found, record the original text for debugging
            print("Warning: Unable to parse dialogue content, original text:")
            print("-" * 50)
            print(text)
            print("-" * 50)
        
        messages = []
        for i, match in enumerate(matches, 1):
            user_content = re.sub(r'\s+', ' ', match[1]).strip()  # Clean user content
            assistant_content = re.sub(r'\s+', ' ', match[2]).strip()  # Clean assistant content
            
            if user_content and assistant_content:  # Ensure content is not empty
                messages.append({"role": "user", "content": user_content})
                messages.append({"role": "assistant", "content": assistant_content})
        
        return {"messages": messages}
    
    except Exception as e:
        print(f"Error occurred while parsing response text: {e}")
        print("Problem text:")
        print(response_text[:500] + "..." if len(response_text) > 500 else response_text)
        return {"messages": []}

def create_payload(row):
    """
    Creates a payload for the API request based on the row data.

    Parameters:
    row (Series): A row from the DataFrame containing 'question' and 'response'.

    Returns:
    dict: The payload to be sent to the API.
    """
    try:
        # Ensure correct column names 'question' and 'response'
        question = str(row['question']).strip()
        response = str(row['response']).strip()
        
        payload = {
            "model": "gpt-4o-mini",
            "messages": [
                {
                    "role": "system", 
                    "content": "你是一个经验丰富的心理咨询师，我想让你担任大学生心理疏导及建议咨询师。您需要提供一个寻求指导和建议给大学生，以管理他们的情绪、压力、焦虑和其他心理健康问题。您应该利用您的认知行为疗法、冥想技巧、正念练习和其他治疗方法的知识来制定个人可以实施的策略，以改善他们的整体健康状况。请基于提供的单轮对话信息，根据单轮对话信息长度生成10轮完整的对话内容。求助者从求助者开始到支持者，每一轮对话的内容都是基于单轮对话，围绕用户的问题提供具体的建议，展现对用户问题的理解、同理心以及建设性的建议。【格式举例】第1轮对话：\n求助者：内容\n支持者：内容\n\n"
                },
                {
                    "role": "user", 
                    "content": f"下面是用户提供的对话：求助者：{question} 支持者：{response}"
                }
            ]
        }
        return payload
    except Exception as e:
        print(f"Error occurred while creating payload: {e}")
        return None

def send_request(payload, url, headers=None):
    """
    Sends a POST request to the specified URL with the given payload.

    Parameters:
    payload (dict): The payload to send in the request.
    url (str): The URL to send the request to.
    headers (dict, optional): Additional headers for the request.

    Returns:
    dict: The JSON response from the API, or None if an error occurred.
    """
    if headers is None:
        headers = {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer api-key'
        }
    
    try:
        response = requests.post(url, json=payload, headers=headers)  # Send the request
        response.raise_for_status()  # Raise an error for bad responses
        return response.json()  # Return the JSON response
    except requests.exceptions.RequestException as e:
        print(f"Request error occurred: {e}")
        return None

def process_row(row, url, output_dir):
    """
    Processes a single row of data, sending a request and saving the response.

    Parameters:
    row (Series): A row from the DataFrame.
    url (str): The URL to send the request to.
    output_dir (str): The directory to save the response files.

    Returns:
    dict: The response from the API, or None if an error occurred.
    """
    try:
        # First verify ID format
        if not re.match(r'^[a-f0-9]{32}$', str(row['id'])):
            print(f"Skipping invalid ID: {row['id']}")
            return None
        
        # Check for necessary columns
        if 'question' not in row or 'response' not in row:
            print(f"Missing necessary columns, ID: {row['id']}")
            return None
            
        payload = create_payload(row)  # Create the payload
        if payload is None:
            print(f"Failed to create payload, ID: {row['id']}")
            return None
            
        response = send_request(payload, url)  # Send the request
        
        if response and 'choices' in response and response['choices']:
            content = response['choices'][0]['message']['content']  # Extract content from response
            conversation_json = parse_response_to_json(content)  # Parse the response content
            
            hash_id = str(row['id']).strip()
            response_file = os.path.join(output_dir, f'{hash_id}.json')  # Define the output file path
            
            with open(response_file, 'w', encoding='utf-8') as f:
                json.dump(conversation_json, f, ensure_ascii=False, indent=4)  # Save the response as JSON
            
            print(f"Processed ID: {hash_id}")
            return response
        else:
            print(f"Failed to process data, ID: {row['id']}")
            return None
    except Exception as e:
        print(f"Error occurred while processing, ID: {row['id']}, Error: {e}")
        return None

def process_excel_and_send_requests(excel_path, url, output_dir='data', max_workers=15):
    """
    Processes the Excel file and sends requests concurrently.

    Parameters:
    excel_path (str): The path to the Excel file.
    url (str): The URL to send the requests to.
    output_dir (str): The directory to save the response files.
    max_workers (int): The maximum number of threads to use for processing.

    Returns:
    list: A list of responses from the API.
    """
    os.makedirs(output_dir, exist_ok=True)  # Create output directory if it doesn't exist
    
    df = read_excel_and_generate_payload(excel_path)  # Read and clean the data
    all_responses = []
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        futures = [
            executor.submit(process_row, row, url, output_dir)  # Submit each row for processing
            for _, row in df.iterrows()
        ]
        
        for future in concurrent.futures.as_completed(futures):
            response = future.result()  # Get the result of the future
            if response:
                all_responses.append(response)  # Collect successful responses
            time.sleep(0.1)  # Sleep to avoid overwhelming the server
    
    return all_responses

def main():
    """
    Main function to execute the script.
    """
    excel_path = 'Single-turn dialogue data.csv'
    url = 'http://proxy/v1/chat/completions'
    output_dir = 'data'
    
    # Ensure output directory exists
    os.makedirs(output_dir, exist_ok=True)
    
    # Read data and print information
    print("Starting to read data file...")
    df = read_excel_and_generate_payload(excel_path)
    
    # Check for correct columns
    if 'id' not in df.columns:
        print("Error: id column not found!")
        print("Available columns:", df.columns.tolist())
        return
    
    responses = process_excel_and_send_requests(excel_path, url, output_dir)  # Process the data and send requests
    print(f"Processed {len(responses)} responses in total")

if __name__ == "__main__":
    main()  # Execute the main function
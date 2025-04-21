"""
Author: knull-cc
Date: 2024-10-20
Description: This script processes a batch of questions from an Excel file,
sends them to the OpenAI API for responses, and saves the results
to a CSV file. It also logs any errors encountered during processing.
"""

import pandas as pd
import requests
import os
from concurrent.futures import ThreadPoolExecutor

API_URL = 'http://proxy.com/v1/chat/completions'
API_KEY = 'api-key'

data_file = 'question_data.xlsx'  # Excel file containing questions
output_file = 'single_turn_data.csv'  # CSV file to save responses
error_log_file = 'error_log.txt'  # Log file for errors

# Create output file
if not os.path.exists(output_file):
    with open(output_file, 'w') as f:
        f.write('ID,content,response\n')

# Create error log file
if not os.path.exists(error_log_file):
    with open(error_log_file, 'w') as f:
        f.write('ID,Error\n')

# Load data
df = pd.read_excel(data_file)

# Set ID range
start_id = 1
end_id = 9122
# Filter DataFrame by ID range
df = df[(df['ID'] >= start_id) & (df['ID'] <= end_id)]  


# Define processing function
def fetch_response(row):
    """
    Fetch response from OpenAI API for a given row of data.

    Parameters:
    row (DataFrame row): A row from the DataFrame containing 'ID' and 'content'.
    """
    question_id = row['ID']
    content = row['content'] 
    payload = {
        "model": "gpt-4o-mini",
        "messages": [
            {
                "role": "system",
                "content": "现在你是世界上最优秀的心理咨询师，你具备心理学扎实的专业知识和强烈的同理心。你需要用简洁明了的语言帮助咨询者，每次回答限制在 50-100 字，不使用换行符或多段落。",
            },
            {
                "role": "user",
                "content": f"我是一名大学生，我咨询的问题内容为：{content}"  # User's question
            }
        ],
        "max_tokens": 150,  
        "temperature": 0.8
    }
    headers = {
        "Authorization": f"Bearer {API_KEY}"  # Add API key to headers
    }
    try:
        response = requests.post(API_URL, json=payload, headers=headers)  # Send request to API
        response.raise_for_status()  # Raise an error for bad responses
        answer = response.json()['choices'][0]['message']['content']  # Extract the response content
        
        # Clean line breaks from the answer
        cleaned_answer = answer.replace('\n', '').replace('\r', '')  # Remove line breaks from answer
        cleaned_content = content.replace('\n', '').replace('\r', '')  # Remove line breaks from content
        
        # Write to output file
        with open(output_file, 'a') as f:
            f.write(f"{question_id},{cleaned_content},{cleaned_answer}\n")  # Append results to CSV
        print(f"Processed ID: {question_id}")  # Log processed ID
    except requests.exceptions.HTTPError as e:
        error_message = f"HTTPError {response.status_code} for ID {question_id}: {str(e)}"  # Log HTTP errors
        print(error_message)
        # Write to error log
        with open(error_log_file, 'a') as f:
            f.write(f"{question_id},{error_message}\n")  # Log error to file
    except Exception as e:
        error_message = f"Unexpected error for ID {question_id}: {str(e)}"  # Log unexpected errors
        print(error_message)
        # Write to error log
        with open(error_log_file, 'a') as f:
            f.write(f"{question_id},{error_message}\n")  # Log error to file

# Use thread pool for concurrent requests
with ThreadPoolExecutor(max_workers=15) as executor:
    futures = [executor.submit(fetch_response, row) for _, row in df.iterrows()]  # Submit tasks to executor
    # Wait for all threads to complete
    for future in futures:
        future.result()

print(f"Processing complete, results saved to {output_file}, error log recorded in {error_log_file}")  # Final log message
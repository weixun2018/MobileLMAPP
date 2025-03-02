"""
This script processes a batch of questions from an Excel file,
sends them to the OpenAI API for responses, and saves the results
to a CSV file. It also logs any errors encountered during processing.
"""

import pandas as pd
import requests
import os
from concurrent.futures import ThreadPoolExecutor

# Set up OpenAI API proxy URL and key
API_URL = 'http://proxy/v1/chat/completions'  # API endpoint for OpenAI
API_KEY = 'api-key'

# Input file path
data_file = 'QuestionData.xlsx'  # Excel file containing questions
# Output file path
output_file = 'Single-turn dialogue.csv'  # CSV file to save responses
# Log file path
error_log_file = 'error_log.txt'  # Log file for errors

# Create output file (if it doesn't exist)
if not os.path.exists(output_file):
    with open(output_file, 'w') as f:
        f.write('ID,content,response\n')  # Add header for CSV

# Create error log file (if it doesn't exist)
if not os.path.exists(error_log_file):
    with open(error_log_file, 'w') as f:
        f.write('ID,Error\n')  # Add header for error log

# Load data
df = pd.read_excel(data_file)  # Read questions from Excel file

# Set ID range
start_id = 1  # Starting ID
end_id = 9122  # Ending ID
df = df[(df['ID'] >= start_id) & (df['ID'] <= end_id)]  # Filter DataFrame by ID range

# Define processing function
def fetch_response(row):
    """
    Fetch response from OpenAI API for a given row of data.

    Parameters:
    row (DataFrame row): A row from the DataFrame containing 'ID' and 'content'.
    """
    question_id = row['ID']  # Extract question ID
    content = row['content']  # Extract question content
    payload = {
        "model": "gpt-4o-mini",  # Specify the model to use
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
        "max_tokens": 150,  # Maximum tokens for the response
        "temperature": 0.8  # Temperature for response variability
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
        future.result()  # Ensure all futures are completed

print(f"Processing complete, results saved to {output_file}, error log recorded in {error_log_file}")  # Final log message
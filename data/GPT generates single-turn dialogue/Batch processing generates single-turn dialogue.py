import pandas as pd
import requests
import os
from concurrent.futures import ThreadPoolExecutor

# Set up OpenAI API proxy URL and key
API_URL = 'http://proxy/v1/chat/completions'
API_KEY = 'api-key'  # Replace with your proxy API key

# Input file path
data_file = 'QuestionData.xlsx'
# Output file path
output_file = 'Single-turn dialogue.csv'
# Log file path
error_log_file = 'error_log.txt'

# Create output file (if it doesn't exist)
if not os.path.exists(output_file):
    with open(output_file, 'w') as f:
        f.write('ID,content,response\n')  # Add header

# Create error log file (if it doesn't exist)
if not os.path.exists(error_log_file):
    with open(error_log_file, 'w') as f:
        f.write('ID,Error\n')  # Add header

# Load data
df = pd.read_excel(data_file)

# Set ID range
start_id = 1  # Starting ID
end_id = 9122  # Ending ID
df = df[(df['ID'] >= start_id) & (df['ID'] <= end_id)]

# Define processing function
def fetch_response(row):
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
                "content": f"我是一名大学生，我咨询的问题内容为：{content}"
            }
        ],
        "max_tokens": 150,
        "temperature": 0.8
    }
    headers = {
        "Authorization": f"Bearer {API_KEY}"  # Add API key
    }
    try:
        response = requests.post(API_URL, json=payload, headers=headers)
        response.raise_for_status()
        answer = response.json()['choices'][0]['message']['content']
        # Clean line breaks from the answer
        cleaned_answer = answer.replace('\n', '').replace('\r', '')
        cleaned_content = content.replace('\n', '').replace('\r', '')
        # Write to output file
        with open(output_file, 'a') as f:
            f.write(f"{question_id},{cleaned_content},{cleaned_answer}\n")
        print(f"Processed ID: {question_id}")
    except requests.exceptions.HTTPError as e:
        error_message = f"HTTPError {response.status_code} for ID {question_id}: {str(e)}"
        print(error_message)
        # Write to error log
        with open(error_log_file, 'a') as f:
            f.write(f"{question_id},{error_message}\n")
    except Exception as e:
        error_message = f"Unexpected error for ID {question_id}: {str(e)}"
        print(error_message)
        # Write to error log
        with open(error_log_file, 'a') as f:
            f.write(f"{question_id},{error_message}\n")

# Use thread pool for concurrent requests
with ThreadPoolExecutor(max_workers=15) as executor:
    futures = [executor.submit(fetch_response, row) for _, row in df.iterrows()]
    # Wait for all threads to complete
    for future in futures:
        future.result()

print(f"Processing complete, results saved to {output_file}, error log recorded in {error_log_file}")
"""
Author: knull-cc
Date: 2024-10-05
Description: This script crawls content from links provided in 
an Excel file and saves the results to a new Excel file.
"""

import pandas as pd
import requests
import os

# Read Excel file
df = pd.read_excel('res_unique.xlsx')

# Create an empty DataFrame to store results
results = pd.DataFrame(columns=['ID', 'link', 'title', 'content', 'created'])

# Set ID range
start_id = 1
end_id = 10000
output_file = 'results.xlsx'

# Iterate through each row, filtered by ID range
for index, row in df.iterrows():
    if start_id <= row['ID'] <= end_id:
        link = row['link']
        id_ = link.split('/')[-1]  
        
        try:
            # Request API
            api_url = f'https://yiapp.xinli001.com/qaQuestion/detail?id={id_}'
            response = requests.get(api_url) 
            response.raise_for_status()  
            
            data = response.json() 

            # Check returned data format
            if data['code'] == 0: 
                item = data['data'] 
                if item:
                    # Add data to results
                    results = pd.concat([results, pd.DataFrame([{
                        'ID': row['ID'],
                        'link': link,
                        'title': item['title'], 
                        'content': item['content'],
                        'created': item['created'] 
                    }])], ignore_index=True)
                    # Print processing status
                    print(f"Processed ID: {row['ID']}, Title: {item['title']}")
                else:
                    print(f"No data returned - ID: {row['ID']}")
            else:
                print(f"Request failed: {data['message']} - ID: {row['ID']}")
        
        except Exception as e:
            print(f"Error occurred while processing ID: {row['ID']}: {e}")

# Save results
if not results.empty:
    results.to_excel(output_file, index=False, engine='openpyxl')
    print("Results saved to results.xlsx")
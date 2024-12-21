import pandas as pd
import requests
import os

# Read Excel file
df = pd.read_excel('res_unique.xlsx')

# Create an empty DataFrame to store results
results = pd.DataFrame(columns=['ID', 'link', 'title', 'content', 'created'])

# Set ID range
start_id = 1  # Start ID
end_id = 10000  # End ID
output_file = 'results.xlsx'

# Iterate through each row, filtered by ID range
for index, row in df.iterrows():
    # Only process IDs within range
    if start_id <= row['ID'] <= end_id:
        link = row['link']  # Make sure to use correct column name
        # Extract ID from link
        id_ = link.split('/')[-1]  # Get the last part of the link as ID
        
        try:
            # Request API
            api_url = f'https://yiapp.xinli001.com/qaQuestion/detail?id={id_}'
            response = requests.get(api_url)
            response.raise_for_status()  # Check if request was successful
            
            data = response.json()
            # Check returned data format
            if data['code'] == 0:
                item = data['data']
                if item:  # Ensure item is not None
                    # Add data to results
                    results = pd.concat([results, pd.DataFrame([{
                        'ID': row['ID'],  # Use ID from table
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
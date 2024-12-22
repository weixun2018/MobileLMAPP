import json
import pandas as pd

def get_longest_answer(answers):
    """
    Get the longest answer from the answer list
    """
    if not answers:
        return ""
    # Sort by content length and return the longest
    return max(answers, key=lambda x: len(x.get('content', '')))['content']

def search_and_save_to_csv(file_path, keyword, output_file):
    # List to store results
    results = []
    
    try:
        # Open and read JSON file
        with open(file_path, 'r', encoding='utf-8') as file:
            data = json.load(file)
        
        # Traverse JSON data
        count = 0
        for item in data:
            # Get question information
            ques_id = item.get('ques_id', '')
            ques_info = item.get('ques_info', {})
            title = ques_info.get('title', '')
            content = ques_info.get('content', '')
            
            # Check if title or content contains the keyword
            if keyword in title or keyword in content:
                # Check if there are any answers
                staic_info = item.get('staic_info', {})
                if staic_info.get('reply', '0 answers') == '0 answers':
                    continue
                    
                # Get the longest answer
                answers = item.get('answers_info', [])
                longest_answer = get_longest_answer(answers)
                
                # Check if answer length is greater than 20 characters
                if len(longest_answer) < 20:
                    continue
                
                count += 1
                print(f"\nFound record {count} containing '{keyword}':")
                print(f"ID: {ques_id}")
                print(f"Title: {title}")
                print(f"Content: {content[:100]}...")  # Only print first 100 characters of content
                
                # Add results to list
                results.append({
                    'id': ques_id,
                    'question': content,  # Use complete question content
                    'response': longest_answer
                })
        
        # Create DataFrame and save to CSV
        if results:
            df = pd.DataFrame(results)
            df.to_csv(output_file, index=False, encoding='utf-8')
            print(f"\nSaved {count} records to {output_file}")
        else:
            print(f"\nNo records found containing '{keyword}'")
        
    except FileNotFoundError:
        print(f"Error: File not found {file_path}")
    except json.JSONDecodeError:
        print("Error: Invalid JSON format")
    except Exception as e:
        print(f"Error occurred: {str(e)}")

# Main program
if __name__ == "__main__":
    # Set file path, search keyword and output filename
    file_path = "data source/ques_ans1.json"
    keyword = "middle school"
    output_file = "data.csv"
    
    # Execute search and save results
    search_and_save_to_csv(file_path, keyword, output_file)
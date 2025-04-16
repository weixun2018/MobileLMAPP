"""
Author: knull-cc
Date: 2025-03-11
Description: This script reads a multi-turn dialogue dataset from a JSON file,
extracts relevant information, and formats it for further processing.
"""

import json

# Read raw data
with open('original_multi_standard_dataset.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# Set the number of dialogue turns to extract
DIALOGUE_TURNS = 3

# Extract and save each message
count = 1
# Used to store all dialogue results
results = []
for entry in data:
    # Extract complete multi-turn dialogue
    conversation = entry['conversation']
    # Only save the first 200 entries
    if count > 200: 
        break
    
    # Get the complete dialogue rounds and ensure there are at least DIALOGUE_TURNS dialogues
    if len(conversation) >= DIALOGUE_TURNS:  
        # print(f"Dialogue content: {conversation}")
        
        # Extract the first DIALOGUE_TURNS dialogue turns
        dialogue_history = []
        for i in range(min(DIALOGUE_TURNS, len(conversation))):
            # Add user input
            user_msg = {
                "role": "user",
                "content": conversation[i]["input"]
            }
            dialogue_history.append(user_msg)
            
            # Add assistant response
            assistant_msg = {
                "role": "assistant",
                "content": conversation[i]["output"]
            }
            dialogue_history.append(assistant_msg)
        
        if len(conversation) > DIALOGUE_TURNS:
            question = conversation[DIALOGUE_TURNS]['input']
            answer = conversation[DIALOGUE_TURNS]['output']
        else:
            question = ""
            answer = ""
    else:
        question = ""
        answer = ""
        dialogue_history = conversation
    
    # Construct new data format
    formatted_entry = {
        "id": "ques_"+str(count),
        "dialogue_history": dialogue_history,
        "question": question,
        "answer": answer,
        "type": "qa"
    }
    
    results.append(formatted_entry)
    count += 1

# Save all dialogues to a JSON file
output_file_path = 'standard_multi_dataset.json'
with open(output_file_path, 'w', encoding='utf-8') as outfile:
    json.dump(results, outfile, ensure_ascii=False, indent=4) 
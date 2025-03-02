"""
This script reads a multi-turn dialogue dataset from a JSON file,
extracts relevant information, and formats it for further processing.
"""

import json

# Read raw data
with open('multi_turn_dataset_1.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# Extract and save each message
count = 1
# Used to store all dialogue results
results = [] 
for entry in data:
    # Extract complete multi-turn dialogue
    conversation = entry['conversation']  # Get the conversation from the entry
    # Only save the first 200 entries
    if count > 200: 
        break
    
    # Get the complete dialogue rounds and ensure there are at least 3 dialogues
    if len(conversation) >= 3:  
        print(f"Dialogue content: {conversation}")  # Print the conversation content
        
        # Extract the first three dialogue turns
        dialogue_history = conversation[:3]  # Extract the first three messages
        question = conversation[0]['input']  # Get input from the first message
        answer = conversation[0]['output']  # Get output from the first message
        
        # Ensure the last user's content is not the same as the question
        if question == dialogue_history[-1]['input']:
            question = "" 
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
output_file_path = 'test1_standard_dataset.json'  # Path for the output file
with open(output_file_path, 'w', encoding='utf-8') as outfile:
    json.dump(results, outfile, ensure_ascii=False, indent=4)  # Write results to JSON file 
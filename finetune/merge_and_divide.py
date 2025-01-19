import json
import os
from pathlib import Path
import random

def merge_json_files(data_dir):
    # Create an empty list to store all conversation data
    merged_data = []
    
    # Get all json files in the data directory
    data_path = Path(data_dir)
    json_files = list(data_path.glob('*.json'))
    
    # Iterate over all json files
    for json_file in json_files:
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
                # If the data is in the original format (with outer messages key), add it directly to the list
                merged_data.append(data)
        except Exception as e:
            print(f"Error processing file {json_file}: {str(e)}")
    
    # Write the merged data to a new json file
    output_file = 'merged_conversations.json'
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(merged_data, f, ensure_ascii=False, indent=4)
        print(f"Successfully merged {len(json_files)} files into {output_file}")
        
        # Add dataset splitting logic
        print("Starting to split the dataset into training and validation sets...")

        os.makedirs(divide_directory, exist_ok=True)
        
        # Set random seed to ensure reproducibility
        random.seed(42)
        
        # Shuffle the data randomly
        random.shuffle(merged_data)
        
        # Calculate the split point
        total_samples = len(merged_data)
        train_size = int(total_samples * 0.9)
        
        # Split the data
        train_data = merged_data[:train_size]
        dev_data = merged_data[train_size:]
        
        # Save the training set
        train_file = os.path.join(divide_directory, 'train.json')
        with open(train_file, 'w', encoding='utf-8') as f:
            json.dump(train_data, f, ensure_ascii=False, indent=2)
            
        # Save the validation set
        dev_file = os.path.join(divide_directory, 'dev.json')
        with open(dev_file, 'w', encoding='utf-8') as f:
            json.dump(dev_data, f, ensure_ascii=False, indent=2)
            
        print(f"Dataset split completed!")
        print(f"Training set size: {len(train_data)}, saved to: {train_file}")
        print(f"Validation set size: {len(dev_data)}, saved to: {dev_file}")
        print(f"Split ratio: {len(train_data)}/{len(dev_data)} = {len(train_data)/total_samples:.2%}/{len(dev_data)/total_samples:.2%}")
        
    except Exception as e:
        print(f"Error writing to file: {str(e)}")

if __name__ == "__main__":
    # Specify the data folder path
    data_directory = "data"
    divide_directory = "divide"
    merge_json_files(data_directory)
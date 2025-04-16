"""
Author: knull-cc
Date: 2025-04-02
Description: This script checks for duplicate or similar entries between 
training and evaluation datasets using MinHash and LSH. 
It helps detect data overlap by comparing user-assistant dialogue pairs 
and outputs summary statistics and matched results.
"""

import json
import numpy as np
from datasketch import MinHash, MinHashLSH
from typing import List, Dict
import re
from tqdm import tqdm

# Configuration parameters
INPUT_TRAIN_FILE = "training_data.json"
INPUT_EVAL_FILE = "evaluation_data.json"
OUTPUT_FILE = "analysis-result.json"
SIMILARITY_THRESHOLD = 0.7

def clean_text(text: str) -> str:
    """Clean text by removing special characters and extra whitespace"""
    text = re.sub(r'\s+', ' ', text)
    text = text.strip().lower()
    return text

def get_document_signature(text: str, num_perm: int = 128) -> MinHash:
    """Generate MinHash signature for document"""
    minhash = MinHash(num_perm=num_perm)
    # Split text into 3-grams
    words = text.split()
    for i in range(len(words) - 2):
        ngram = ' '.join(words[i:i+3])
        minhash.update(ngram.encode('utf-8'))
    return minhash

def calculate_similarity(data1: List[Dict], data2: List[Dict], threshold: float = 0.7) -> Dict:
    """Calculate similarity between two datasets"""
    # Initialize LSH
    lsh = MinHashLSH(threshold=threshold, num_perm=128)
    
    # Store results
    results = {
        "duplicates": [],          # Duplicate data
        "duplicate_pairs": [],     # Duplicate data pairs
        "duplicate_indices": set() # Indices of duplicate data
    }
    
    print("Building LSH index for dataset 1...")
    # Add dataset 1 to LSH
    for idx1, item1 in enumerate(tqdm(data1)):
        full_text1 = clean_text(item1['user'] + ' ' + item1['assistant'])
        # full_text1 = item1['text']
        signature1 = get_document_signature(full_text1)
        lsh.insert(f"data1_{idx1}", signature1)
    
    print("Checking duplicates in dataset 2...")
    # Check each item in dataset 2
    for idx2, item2 in enumerate(tqdm(data2)):
        full_text2 = clean_text(item2['user'] + ' ' + item2['assistant'])
        # full_text2 = item2['text']
        signature2 = get_document_signature(full_text2)
        
        # Query similar documents
        similar_docs = lsh.query(signature2)
        if similar_docs:
            results["duplicates"].append(item2)
            results["duplicate_indices"].add(idx2)
            
            # Record duplicate pairs
            for doc_id in similar_docs:
                idx1 = int(doc_id.split('_')[1])
                results["duplicate_pairs"].append({
                    "eval_data": item2,
                    "train_data": data1[idx1]
                })
    
    return results

def print_statistics(data1: List[Dict], data2: List[Dict], results: Dict):
    """Print statistics"""
    duplicate_count = len(results["duplicates"])
    total_count = len(data2)
    overlap_rate = (duplicate_count / total_count) * 100
    
    print("\n=== Dataset Duplication Analysis Report ===")
    print(f"Training set size: {len(data1)} items")
    print(f"Evaluation set size: {len(data2)} items")
    print(f"Duplicate count: {duplicate_count} items")
    print(f"Duplication rate: {overlap_rate:.2f}%")
    print("========================")

def main():
    # Read data
    print(f"Reading training data: {INPUT_TRAIN_FILE}")
    with open(INPUT_TRAIN_FILE, 'r', encoding='utf-8') as f:
        train_data = json.load(f)
    
    print(f"Reading evaluation data: {INPUT_EVAL_FILE}")
    with open(INPUT_EVAL_FILE, 'r', encoding='utf-8') as f:
        eval_data = json.load(f)
    
    # Calculate similarity
    results = calculate_similarity(train_data, eval_data, SIMILARITY_THRESHOLD)
    
    # Print statistics
    print_statistics(train_data, eval_data, results)
    
    # Save analysis results
    output = {
        "statistics": {
            "train_size": len(train_data),
            "eval_size": len(eval_data),
            "duplicate_count": len(results["duplicates"]),
            "overlap_rate": (len(results["duplicates"]) / len(eval_data)) * 100
        },
        "duplicate_samples": results["duplicates"],
        "duplicate_pairs": results["duplicate_pairs"]
    }
    
    print(f"\nSaving analysis results to: {OUTPUT_FILE}")
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    
    print("Analysis complete!")

if __name__ == "__main__":
    main()
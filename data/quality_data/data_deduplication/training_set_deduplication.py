"""
Author: knull-cc
Date: 2025-04-02
Description: Removes duplicate or highly similar user-assistant dialogues 
from a dataset using MinHash and LSH.
"""

import json
import numpy as np
from datasketch import MinHash, MinHashLSH
from typing import List, Dict
import re

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

def dedup_conversations(data: List[Dict], threshold: float = 0.9) -> List[Dict]:
    """Deduplicate conversation data"""
    # Initialize LSH
    lsh = MinHashLSH(threshold=threshold, num_perm=128)
    
    unique_data = []
    seen_indices = set()
    
    print("Processing data...")
    
    # First pass to build LSH index
    for idx, item in enumerate(data):
        if idx % 1000 == 0:
            print(f"Processing item {idx}...")
            
        # Combine user and assistant text
        full_text = clean_text(item['user'] + ' ' + item['assistant'])
        # full_text = item['text']
        
        # Generate MinHash signature
        signature = get_document_signature(full_text)
        
        # Check for similar documents
        result = lsh.query(signature)
        
        if not result:  # If no similar documents found
            lsh.insert(str(idx), signature)
            unique_data.append(item)
            seen_indices.add(idx)
    
    print(f"Original data count: {len(data)}")
    print(f"Deduplicated data count: {len(unique_data)}")
    print(f"Removed duplicates count: {len(data) - len(unique_data)}")
    
    return unique_data

def main():
    # Read data
    print("Reading data...")
    with open('data.json', 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Perform deduplication
    deduped_data = dedup_conversations(data)
    
    # Save deduplicated data
    print("Saving deduplicated data...")
    with open('data_deduplicated.json', 'w', encoding='utf-8') as f:
        json.dump(deduped_data, f, ensure_ascii=False, indent=2)
    
    print("Processing complete!")

if __name__ == "__main__":
    main()
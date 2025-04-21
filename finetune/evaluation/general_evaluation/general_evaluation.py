"""
Author: knull-cc
Date: 2025-04-05
Description: Referenced the evaluation method of EmoLLM.
This script performs automatic evaluation of generated responses using BLEU scores.
It loads a test dataset, generates responses using specified models, and evaluates the results.
"""

import json
import os
import torch
from tqdm import tqdm
from collections import defaultdict
from transformers import AutoTokenizer, AutoModelForCausalLM
from peft import PeftModel, PeftConfig
from nltk.translate.bleu_score import sentence_bleu, SmoothingFunction
from nltk.tokenize import word_tokenize
import nltk
import jieba
from rouge import Rouge
import numpy as np
from peft import LoraConfig, get_peft_model

try:
    nltk.data.find('tokenizers/punkt')
except LookupError:
    nltk.download('punkt')

def load_testset(test_path):
    """Load test dataset
    Args:
        test_path (str): Path to the test dataset file
    Returns:
        list: Processed test data
    """
    processed_data = []
    with open(test_path, 'r', encoding='utf-8') as f:
        data = json.load(f) 
        for idx, item in enumerate(data):
            processed_item = {
                'id': idx,
                'question': item['instruction'],
                'answer': item['output'],
                'type': 'qa'
            }
            processed_data.append(processed_item)
    return processed_data

def batch_generate(models, tokenizer, test_data, batch_size=20):
    """Batch generate responses to optimize speed
    Args:
        models (dict): Dictionary of model names and their instances
        tokenizer: Tokenizer for encoding input queries
        test_data (list): List of test data containing questions and answers
        batch_size (int): Number of queries to process per batch
    Returns:
        list: List containing generated responses and reference answers
    """
    results = []
    for i in tqdm(range(0, len(test_data), batch_size)):
        batch = test_data[i:i+batch_size]

        # Build inputs
        formatted_queries = []
        for item in batch:
            query = item['question']
            # 确保输入以"<AI>支持者："结尾
            if not query.endswith("<AI>支持者："):
                query = query.rstrip() + "\n<AI>支持者："
            formatted_queries.append(query)

        # Batch generation
        batch_responses = {}

        for model_name, model in models.items():
            model.eval()
            with torch.no_grad():
                # Real batch processing
                inputs = tokenizer(
                    formatted_queries,
                    return_tensors="pt",
                    padding=True,
                    truncation=True,
                    max_length=512
                ).to("cuda")
                
                # Batch generation
                outputs = model.generate(
                    **inputs,
                    return_dict_in_generate=False,
                    max_new_tokens=128,
                    do_sample=True,
                    temperature=0.2,
                    top_p=0.7,
                    pad_token_id=73440,
                    eos_token_id=73440
                )
                
                # Batch decoding and cleaning
                cleaned_responses = []
                for j, output in enumerate(outputs):
                    # Decode only generated part, excluding input
                    input_length = len(inputs.input_ids[j])
                    generated_tokens = output[input_length:]
                    response_only = tokenizer.decode(generated_tokens, skip_special_tokens=True)
                    
                    # Clean, keep only first round reply
                    cleaned = clean_generated_response(response_only)
                    cleaned_responses.append(cleaned)
                
                # Print brief info for first question of first batch (for debugging)
                if i == 0 and j == 0:
                    print(f"\nModel: {model_name} - Sample Output")
                    print(f"Input: {formatted_queries[0][:50]}...")
                    print(f"Output: {cleaned_responses[0][:50]}...")
                
                batch_responses[model_name] = cleaned_responses

        # Assemble results
        for idx in range(len(formatted_queries)):
            result = {
                "id": batch[idx]['id'],
                "question": formatted_queries[idx],
                "reference": batch[idx]['answer'],
                "responses": {k: v[idx] for k,v in batch_responses.items()},
                "type": batch[idx].get('type', 'unknown')
            }
            results.append(result)

    return results

def calculate_bleu(prediction, reference):
    """Calculate BLEU and Rouge scores
    Args:
        prediction (str): The generated response to evaluate.
        reference (str): The reference answer for comparison.
    Returns:
        dict: A dictionary containing BLEU and Rouge scores.
    """
    prediction = str(prediction).strip().replace(" ", "")
    reference = str(reference).strip().replace(" ", "")

    # Use jieba for word segmentation and join with spaces
    prediction_text = " ".join(jieba.cut(prediction))
    reference_text = " ".join(jieba.cut(reference))

    try:
        # Calculate BLEU
        weights = [
            (1.0, 0.0, 0.0, 0.0),      # BLEU-1
            (1./2., 1./2., 0.0, 0.0),  # BLEU-2
            (1./3., 1./3., 1./3., 0.0),# BLEU-3
            (1./4., 1./4., 1./4., 1./4.)# BLEU-4
        ]

        scores = {}
        for n, weight in enumerate(weights, start=1):
            score = sentence_bleu(
                references=[reference_text.split()],
                hypothesis=prediction_text.split(),
                weights=weight,
                smoothing_function=SmoothingFunction().method1
            ) * 100
            scores[f'bleu-{n}'] = score

        # Calculate ROUGE
        rouge = Rouge()
        rouge_scores = rouge.get_scores(prediction_text, reference_text, avg=True)

        # Add ROUGE scores
        for key, value in rouge_scores.items():
            scores[key] = value['f'] * 100

        return scores

    except Exception as e:
        print(f"Error calculating scores: {str(e)}")
        return {
            **{f'bleu-{n}': 0.0 for n in range(1, 5)},
            **{'rouge-1': 0.0, 'rouge-2': 0.0, 'rouge-l': 0.0}
        }

def evaluate_results(results, eval_type):
    """Evaluate generated results
    Args:
        results (list): The list of generated results to evaluate.
        eval_type (str): The type of evaluation to perform (e.g., 'qa').
    Returns:
        dict: A dictionary containing average scores for each model.
    """
    metrics = defaultdict(lambda: defaultdict(float))
    valid_count = 0

    for item in results:
        if item['type'] != eval_type:
            continue

        valid_count += 1
        reference = item['reference']

        for model, response in item['responses'].items():
            response_text = response.split('\n', 1)[1] if '\n' in response else response
            scores = calculate_bleu(response_text, reference)
            for metric, value in scores.items():
                metrics[model][metric] += value

    # Calculate average
    if valid_count > 0:
        for model in metrics:
            total_score = 0.0
            for metric in metrics[model]:
                metrics[model][metric] /= valid_count
                # Add to total score (weighted average of BLEU and Rouge scores)
                if 'bleu' in metric:
                    total_score += metrics[model][metric] * 0.125  # 4 BLEU scores * 0.125 = 0.5 total weight
                else:
                    total_score += metrics[model][metric] * 0.167  # 3 Rouge scores * 0.167 ≈ 0.5 total weight
            metrics[model]['total_score'] = total_score

    print(f"\nEvaluation type: {eval_type}")
    print(f"Valid sample count: {valid_count}")
    print("Detailed metrics:", dict(metrics))

    return dict(metrics)

def load_model(model_path):
    """Load base model
    Args:
        model_path (str): The path to the pre-trained model.
    Returns:
        model: The loaded model instance.
    """
    # Load base model
    base_model = AutoModelForCausalLM.from_pretrained(
        model_path,
        torch_dtype=torch.float16,
        device_map="auto",
        trust_remote_code=True
    )
    return base_model

def clean_generated_response(response):
    """Clean generated response, keep only first round reply"""
    # Find dialogue markers
    cut_markers = ["<user>", "<system>", "<AI>"]
    positions = []
    for marker in cut_markers:
        pos = response.find(marker)
        if pos != -1:
            positions.append(pos)
    
    # If dialogue markers found, truncate at first marker
    if positions:
        first_cut = min(positions)
        return response[:first_cut].strip()
    
    return response

def main():
    """Main function to execute the evaluation process."""
    # Initialize model and tokenizer
    model_path = "/content/MiniCPM3-4B"
    tokenizer = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True)

    # Set padding token
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token

    # Load base model
    base_model = load_model(model_path)
    print("Base model loaded")

    # Define all fine-tuned model paths to test
    adapter_paths = [
        # "/content/drive/MyDrive/knullcc/MiniCPM-4b-3765-A100"
        # "/content/drive/MyDrive/knullcc/MiniCPM-4b-4975-A100",
        # "/content/drive/MyDrive/knullcc/MiniCPM-4b-6300-A100",
        # "/content/drive/MyDrive/knullcc/MiniCPM-4b-6558-A100",
    ]

    # Configure test set path
    test_path = "/content/MobileLMAPP_tools/eval_data/eval_set_short.json"
    test_data = load_testset(test_path)
    
    # Create result storage dictionary
    all_results = []
    all_metrics = {}
    
    # Evaluate base model first
    print("Evaluating base model...")
    models = {"MiniCPM-4b-base": base_model}
    base_results = batch_generate(models, tokenizer, test_data)
    all_results.extend(base_results)
    base_metrics = evaluate_results(base_results, 'qa')
    all_metrics.update(base_metrics)
    
    # Clear model dictionary to free memory
    models.clear()
    
    # Evaluate fine-tuned models one by one
    for adapter_path in adapter_paths:
        model_name = adapter_path.split('/')[-1]
        print(f"Evaluating model {model_name}...")
        
        # Load config from adapter_config.json
        peft_config = PeftConfig.from_pretrained(adapter_path)
        
        # Load fine-tuned model
        tuned_model = PeftModel.from_pretrained(
            base_model,
            adapter_path,
            device_map="auto"
        )
        
        # Create temporary model dictionary
        temp_models = {model_name: tuned_model}
        
        # Generate and evaluate results
        model_results = batch_generate(temp_models, tokenizer, test_data)
        all_results.extend(model_results)
        model_metrics = evaluate_results(model_results, 'qa')
        all_metrics.update(model_metrics)
        
        # Free model memory
        del tuned_model
        temp_models.clear()
        torch.cuda.empty_cache()  # Clear GPU cache
        print(f"{model_name} evaluation completed and released")

    # Print evaluation results comparison
    print("\n=== Evaluation Results Comparison ===")
    print("\nBase Model vs Fine-tuned Models:")
    metrics_list = ['bleu-1', 'bleu-2', 'bleu-3', 'bleu-4', 'rouge-1', 'rouge-2', 'rouge-l', 'total_score']

    # Print comparison of evaluation results for all models
    base_scores = all_metrics['MiniCPM-4b-base']

    for metric in metrics_list:
        print(f"\n{metric}:")
        print(f"   Base Model: {base_scores[metric]:.2f}")

        # Record best improvement
        best_improvement = float('-inf')
        best_model = None

        for model_name in all_metrics:
            if model_name != 'MiniCPM-4b-base':
                score = all_metrics[model_name][metric]
                diff = score - base_scores[metric]
                print(f"  {model_name}: {score:.2f} (Difference: {diff:.2f}, {diff/base_scores[metric]*100:.2f}%)")

                if diff > best_improvement:
                    best_improvement = diff
                    best_model = model_name

        print(f"\n   Best Model: {best_model}")
        print(f"   Maximum Improvement: {best_improvement:.2f} ({best_improvement/base_scores[metric]*100:.2f}%)")

    # Save detailed results
    with open('eval_results_comparison.json', 'w', encoding='utf-8') as f:
        json.dump(all_results, f, ensure_ascii=False, indent=2)

if __name__ == "__main__":
    main()
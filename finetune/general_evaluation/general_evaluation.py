"""
Referenced the evaluation method of EmoLLM.
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
        for line in f:
            item = json.loads(line)
            # Extract the question part from the instruction
            dialogue = item['instruction'].split('<用户>来访者：')
            last_question = dialogue[-1].split('<AI>医生：')[0].strip()

            processed_item = {
                'id': len(processed_data), 
                'question': item['instruction'],  # Keep the complete instruction
                'answer': item['output'],
                'type': 'qa'
            }
            processed_data.append(processed_item)
    return processed_data

def batch_generate(models, tokenizer, test_data, batch_size=20):
    """Batch generate responses
    Args:
        models (dict): A dictionary of model names and their corresponding model instances
        tokenizer: The tokenizer used for encoding the input queries
        test_data (list): The list of test data containing questions and answers
        batch_size (int): The number of queries to process in each batch
    Returns:
        list: A list of results containing generated responses and references
    """
    results = []
    for i in tqdm(range(0, len(test_data), batch_size)):
        batch = test_data[i:i+batch_size]

        # Construct input
        formatted_queries = []
        for item in batch:
            # Use standardized prompt template
            # query = f"<系统>现在你是一个心理专家，我有一些心理问题，请你用专业的知识帮我解决。请只回答医生的部分，不要生成用户的回复。<用户>来访者：{item['question']}<AI>医生："
            query = item['question']
            formatted_queries.append(query)

        # Batch generation
        batch_responses = {}

        def clean_response(response):
            """Clear the generated responses, removing all user replies and system tags."""
            # Define all markers that need to be processed
            cut_markers = ["<用户>", "<系统>", "<用户", "<系统"]
            
            # Find the position of the first occurrence of the marker
            positions = []
            for marker in cut_markers:
                pos = response.find(marker)
                if pos != -1:  # If marker is found
                    positions.append(pos)
            
            # If any markers are found, truncate at the earliest marker
            if positions:
                first_cut = min(positions)
                response = response[:first_cut].strip()
                
            # Remove the possible "来访者：" section
            if "来访者：" in response:
                response = response.split("来访者：")[0].strip()
                
            return response

        for model_name, model in models.items():
            # Enable evaluation mode
            model.eval()
            with torch.no_grad():  # No gradient calculation needed during inference
                inputs = tokenizer(
                    formatted_queries,
                    return_tensors="pt",
                    padding=True,
                    truncation=True,
                    max_length=512
                ).to("cuda")

                outputs = model.generate(
                    **inputs,
                    return_dict_in_generate=False,
                    max_new_tokens=256,
                    do_sample=True,
                    temperature=0.2,
                    pad_token_id=73440,
                    eos_token_id=73440
                    # eos_token_id=tokenizer.eos_token_id
                )

            # Decode output
            decoded_outputs = [tokenizer.decode(o, skip_special_tokens=True) for o in outputs]
            cleaned_responses = []
            for response in decoded_outputs:
                if "医生：" in response:
                    response = response.split("医生：")[-1].strip()
                    response = clean_response(response)
                cleaned_responses.append(response)
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
    print(results)
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
        "/content/drive/MyDrive/knullcc/MiniCPM-4b-4975-A100"
    ]

    # Create model dictionary
    models = {"MiniCPM-4b-base": base_model}
    
    # Load all fine-tuned models
    for adapter_path in adapter_paths:
        model_name = adapter_path.split('/')[-1]  # Get model name
        
        # Create LoRA config same as training
        lora_config = LoraConfig(
            r=64,
            lora_alpha=16,
            target_modules=["q_proj", "k_proj", "v_proj", "o_proj"],
            lora_dropout=0.05,
            bias="none",
            task_type="CAUSAL_LM"
        )

        # Load fine-tuned model
        tuned_model = PeftModel.from_pretrained(
            base_model,
            adapter_path,
            device_map="auto",
            config=lora_config
        )
        models[model_name] = tuned_model
        print(f"{model_name} loaded")

    # Configure test set path
    test_path = "/content/MobileLMAPP_tools/eval_data/test/converted_200.json"
    test_data = load_testset(test_path)

    # Generate responses in batch
    results = batch_generate(models, tokenizer, test_data)

    # Evaluate results
    qa_metrics = evaluate_results(results, 'qa')

    # Print evaluation results comparison
    print("\n=== Evaluation Results Comparison ===")
    print("\nBase Model vs Fine-tuned Models:")
    metrics_list = ['bleu-1', 'bleu-2', 'bleu-3', 'bleu-4', 'rouge-1', 'rouge-2', 'rouge-l', 'total_score']

    # Print all model evaluation results comparison
    base_scores = qa_metrics['MiniCPM-4b-base']
    
    for metric in metrics_list:
        print(f"\n{metric}:")
        print(f"   Base Model: {base_scores[metric]:.2f}")
        
        # Record best improvement
        best_improvement = float('-inf')
        best_model = None
        
        for model_name in qa_metrics:
            if model_name != 'MiniCPM-4b-base':
                score = qa_metrics[model_name][metric]
                diff = score - base_scores[metric]
                print(f"  {model_name}: {score:.2f} (Difference: {diff:.2f}, {diff/base_scores[metric]*100:.2f}%)")
                
                if diff > best_improvement:
                    best_improvement = diff
                    best_model = model_name
        
        print(f"\n   Best Model: {best_model}")
        print(f"   Maximum Improvement: {best_improvement:.2f} ({best_improvement/base_scores[metric]*100:.2f}%)")

    # Save detailed results
    with open('eval_results_comparison.json', 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

if __name__ == "__main__":
    main()
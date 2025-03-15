"""
This script performs automatic evaluation of generated responses using BLEU scores.
It loads a test dataset, generates responses using specified models, and evaluates the results.
"""

import json
import os
import torch
from tqdm import tqdm
from collections import defaultdict
from transformers import AutoTokenizer, AutoModelForCausalLM
from peft import PeftModel
from nltk.translate.bleu_score import sentence_bleu
from nltk.tokenize import word_tokenize
import nltk
import jieba

try:
    nltk.data.find('tokenizers/punkt')
except LookupError:
    nltk.download('punkt')

def load_testset(test_path):
    """Load standardized test set
    Args:
        test_path (str): The path to the test dataset file.
    Returns:
        list: The loaded test data.
    """
    with open(test_path, 'r', encoding='utf-8') as f:
        test_data = json.load(f)
    return test_data

def batch_generate(models, tokenizer, test_data, batch_size=10):
    """Batch generate responses
    Args:
        models (dict): A dictionary of model names and their corresponding model instances.
        tokenizer: The tokenizer used for encoding the input queries.
        test_data (list): The list of test data containing questions and answers.
        batch_size (int): The number of queries to process in each batch.
    Returns:
        list: A list of results containing generated responses and references.
    """
    results = []
    for i in tqdm(range(0, len(test_data), batch_size)):
        batch = test_data[i:i+batch_size]
        
        # Construct input with dialogue history
        formatted_queries = []
        for item in batch:
            dialogue_history = item.get('dialogue_history', [])
            formatted_dialogue = ""
            
            # Construct historical dialogue
            for turn in dialogue_history:
                if turn['role'] == 'user':
                    formatted_dialogue += f"求助者：{turn['content']}\n"
                elif turn['role'] == 'assistant':
                    formatted_dialogue += f"支持者：{turn['content']}\n"
            
            # Add the current question
            if formatted_dialogue:
                formatted_dialogue += "\n历史记录：\n\n"
            formatted_dialogue += f"当前用户提问：\n{item['question']}"
            
            formatted_queries.append(formatted_dialogue)

        # Batch generation
        batch_responses = {}
        for model_name, model in models.items():
            inputs = tokenizer(formatted_queries, return_tensors="pt", padding=True, truncation=True).to("cuda")
            outputs = model.generate(**inputs, max_length=512)
            batch_responses[model_name] = [tokenizer.decode(o, skip_special_tokens=True) for o in outputs]

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
    # print(results)
    return results

def calculate_bleu(prediction, reference):
    """Calculate BLEU score
    Args:
        prediction (str): The generated response to evaluate.
        reference (str): The reference answer for comparison.
    Returns:
        dict: A dictionary containing BLEU scores for different n-grams.
    """
    prediction = str(prediction).strip()
    reference = str(reference).strip()

    # Use jieba for Chinese word segmentation
    prediction_tokens = list(jieba.cut(prediction))
    reference_tokens = [list(jieba.cut(reference))]

    # print("\nCalculating BLEU score:")
    # print(f"Predicted tokens: {' '.join(prediction_tokens[:50])}...")
    # print(f"Reference tokens: {' '.join(reference_tokens[0][:50])}...")

    try:
        # Calculate BLEU scores for 1-4gram
        weights_list = [
            (1.0, 0.0, 0.0, 0.0),  # 1-gram
            (0.5, 0.5, 0.0, 0.0),  # 2-gram
            (0.33, 0.33, 0.34, 0.0),  # 3-gram
            (0.25, 0.25, 0.25, 0.25)  # 4-gram
        ]

        bleu_scores = {}
        for n, weights in enumerate(weights_list, start=1):
            score = sentence_bleu(reference_tokens, prediction_tokens, weights)
            bleu_scores[f'bleu-{n}'] = score

        # print(f"BLEU scores: {bleu_scores}")
        return bleu_scores
    except Exception as e:
        print(f"Error calculating BLEU score: {str(e)}")
        return {f'bleu-{n}': 0.0 for n in range(1, 5)}

def evaluate_results(results, eval_type):
    """Evaluate generated results
    Args:
        results (list): The list of generated results to evaluate.
        eval_type (str): The type of evaluation to perform (e.g., 'qa').
    Returns:
        dict: A dictionary containing average BLEU scores and total score for each model.
    """
    metrics = defaultdict(lambda: defaultdict(float))
    valid_count = 0

    for item in results:
        if item['type'] != eval_type:
            continue

        valid_count += 1
        reference = item['reference']

        for model, response in item['responses'].items():
            # Only take the response part, remove question repetition
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
                # Add to total score (25% weight for each BLEU score)
                total_score += metrics[model][metric] * 0.25
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
        device_map="auto"
    )
    return base_model

def main():
    """Main function to execute the evaluation process."""
    # Initialize model
    model_path = "/content/MiniCPM-2B-sft-bf16"
    # model_path = "/content/MiniCPM3-4B"
    tokenizer = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True)

    # Set padding token
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token

    base_model = load_model(model_path)

    # Load LoRA model
    peft_model = PeftModel.from_pretrained(base_model, "/content/drive/MyDrive/knullcc/MiniCPM-2b-10000-A100/checkpoint-1000")

    models = {
        "MiniCPM-2b": base_model,
        "MiniCPM-2b-10000-A100": peft_model
    }

    # Configure test set path
    test_path = "/content/MobileLMAPP_tools/standard_multi_dataset.json"
    # Load test set
    test_data = load_testset(test_path)

    # Batch generate responses
    results = batch_generate(models, tokenizer, test_data)

    # Evaluate results
    qa_metrics = evaluate_results(results, 'qa')

    print("\n=== Evaluation Results ===")
    for model, scores in qa_metrics.items():
        print(f"\n{model} model:")
        for metric, value in scores.items():
            print(f"  {metric}: {value:.4f}")
        print(f"  总得分: {scores['total_score']:.4f}")

    # Save detailed results
    with open('eval_results.json', 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

if __name__ == "__main__":
    main()
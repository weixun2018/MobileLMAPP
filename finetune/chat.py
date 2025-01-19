from transformers import AutoTokenizer, AutoModelForCausalLM
from peft import PeftModel
import torch

def load_model(model_path, device='cuda', force_auto_device_map=False):
    # Set a more detailed device mapping strategy
    if force_auto_device_map:
        device_map = "auto"
    else:
        device_map = {"": device}

    # Load the base model
    base_model = AutoModelForCausalLM.from_pretrained(
        model_path,
        torch_dtype=torch.float16,
        device_map=device_map,
        trust_remote_code=True,
        offload_folder="offload"  # Set an explicit offload directory
    )

    return base_model

# Initialize the model and tokenizer
model_path = "/content/MiniCPM-2B-sft-bf16"
base_model = load_model(model_path)
tokenizer = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True)

# Load the LoRA model
peft_model = PeftModel.from_pretrained(
    base_model,
    "/content/MiniCPM-MobileLMAPP/finetune/output/OCNLILoRA/20241224023157/checkpoint-1000",
    is_trainable=False
)

def format_dialogue(user_input):
    """Format the dialogue input"""
    return f"Human: {user_input}\n\nAssistant:"

def chat(text, 
         max_length=512,
         temperature=0.7,
         top_p=0.9,
         top_k=50,
         num_beams=4,
         repetition_penalty=1.2):
    
    # Format input as dialogue
    formatted_input = format_dialogue(text)
    inputs = tokenizer(formatted_input, return_tensors="pt").to("cuda")

    # Generation parameters
    gen_kwargs = {
        "max_length": max_length,
        "temperature": temperature,
        "top_p": top_p,
        "top_k": top_k,
        "num_beams": num_beams,
        "repetition_penalty": repetition_penalty,
        "pad_token_id": tokenizer.eos_token_id,
        "do_sample": True,
        "no_repeat_ngram_size": 3,
        "early_stopping": True  # Add early stopping for efficiency
    }

    outputs = peft_model.generate(**inputs, **gen_kwargs)
    response = tokenizer.decode(outputs[0], skip_special_tokens=True)

    # Return only the Assistant's response part
    try:
        return response.split("Assistant:")[-1].strip()
    except:
        return response.strip()

# Test example
query = "上大学后就感觉一切变得很不熟悉，连自己都变得不像自己了,我应该怎么办？"
response = chat(query)
print(f"Human: {query}\n\nAssistant: {response}")

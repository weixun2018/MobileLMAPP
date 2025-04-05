#!/bin/bash
# ===============================================================================
# MiniCPM Fine-tuning Script
# ===============================================================================
# This fine-tuning script is adapted from the official MiniCPM repository.
# It implements DeepSpeed Zero-2 Offload optimization for efficient training
# on consumer-grade GPUs with limited VRAM.
# 
# The script is configured for LoRA fine-tuning on the OCNLI (Original Chinese Natural
# Language Inference) dataset, a natural language inference task in Chinese.
# 
# Reference: https://github.com/OpenBMB/MiniCPM
# ===============================================================================

# Generate timestamp for unique output directory
formatted_time=$(date +"%Y%m%d%H%M%S")
echo $formatted_time

# CUDA configuration for optimal performance
export CUDA_VISIBLE_DEVICES=0                                      # Original: Used NCCL_P2P_DISABLE=1 and NCCL_IB_DISABLE=1 for RTX 4090
export PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True

# DeepSpeed launch command with fine-tuning parameters
deepspeed --include localhost:0 --master_port 19888 finetune.py \  # Original: --include localhost:1
    --model_name_or_path /content/MiniCPM3-4B \
    --output_dir output/OCNLILoRA/$formatted_time/ \
    --train_data_path data/data_4975/train.json \                  # Original: data/ocnli_public_chatml/train.json
    --eval_data_path data/data_4975/dev.json \                     # Original: data/ocnli_public_chatml/dev.json
    --learning_rate 5e-6 \                                         # Original: 5e-5
    --per_device_train_batch_size 4 \                              # Original: 16
    --per_device_eval_batch_size 4 \                               # Original: 128
    --model_max_length 1024 \
    --fp16 \                                                       # Original: bf16
    --use_lora \
    --gradient_accumulation_steps 4 \                              # Original: 1
    --warmup_steps 300 \                                           # Original: 100
    --max_steps 1000 \
    --weight_decay 0.01 \
    --evaluation_strategy steps \
    --eval_steps 500 \
    --save_strategy steps \
    --save_steps 500 \
    --seed 42 \
    --log_level info \
    --logging_strategy steps \
    --logging_steps 10 \
    --deepspeed configs/ds_config_zero2_offload.json               
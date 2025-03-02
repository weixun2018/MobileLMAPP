# Mobile APP for Mental Health Based on Language Model
- Finetune MiniCPM 2B model using mental health data.
- Only support android device.
## Data
### data source
### data describe
### ...

## Finetune
use what kind of finetune skill

- **LoRA**: Fine-tuning using Low-Rank Adaptation technology to reduce the number of parameter updates.
- **FP16**: Using half-precision floating-point training to reduce memory usage and speed up training.
- **Gradient Accumulation**: Adjusting the gradient accumulation steps as needed to accommodate larger batch sizes.
- **Weight Decay**: Applying weight decay to prevent overfitting.
- **Learning Rate Scheduling**: Setting up warm-up steps and maximum training steps for the learning rate.
- **Evaluation and Saving Strategy**: Regularly evaluating and saving the model to monitor the training process.

record everytime finetune performance

|data|GPU|VRAM|time|loss|
|:---:|:---:|:---:|:---:|:---:|
|4700|T4|15GB|78mins|9.05|
|4700|L4|24GB|33mins|8.65|
|5000|A100|40GB|15:42|2.26|
|5000|A100|40GB|11:16|2.00|
|5000|A100|40GB|6:02|2.26|
|10000|A100|40GB|13:27|2.14|
|10000|A100|40GB|31:24|2.12|
|10000|A100|40GB|19:18|1.78|


## Memory
use what kind of skill to retain memory of lanuage model

## Quantize
use what kind of quantize skill

## Inference
use what kind of speed up inference skill

## Deploy
use what kind of deploy tool
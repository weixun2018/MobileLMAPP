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

|training_times|model|data|GPU|VRAM|time|loss|
|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
|1|MiniCPM-2B|4700|T4|15GB|1:18:00|9.05|
|2|MiniCPM-2B|4700|L4|24GB|33:00|8.65|
|3|MiniCPM-2B|5000|A100|40GB|15:42|2.26|
|4|Deepseek-1.5B|5000|A100|40GB|11:16|2.00|
|5|Deepseek-1.5B|5000|A100|40GB|6:02|2.26|
|6|MiniCPM-2B|10000|A100|40GB|13:27|2.14|
|7|MiniCPM-4B|10000|A100|40GB|31:24|2.12|
|8|Deepseek-8B|10000|A100|40GB|19:18|1.78|
|9|MiniCPM-2B|7000|A100|40GB|12:41|1.65|
|10|MiniCPM-2B|17000|A100|40GB|13:45|1.58|
|11|Deepseek-8b-17000-A100|7000|A100|40GB|11:03|1.47|
|12|MiniCPM-4B|7000|A100|40GB|15:58|1.55|
|13|MiniCPM-4B|6558|A100|40GB|15:14|2.06|
|14|MiniCPM-4B|6300|A100|40GB|15:29|2.06|
|15|MiniCPM-4B|3765|A100|40GB|14:25|1.55|
|16|MiniCPM-4B|19k|A100|40GB|16:49|1.82|
|16|MiniCPM-4B|19k|A100|40GB|1:40:39|1.65|

## Memory
use what kind of skill to retain memory of lanuage model

## Quantize
use what kind of quantize skill

## Inference
use what kind of speed up inference skill

## Deploy
use what kind of deploy tool
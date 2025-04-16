## First Fine-Tuning Test
- After merging multi-round data, the dataset was split into train.json and dev.json with a 9:1 ratio.
- Successfully completed the first fine-tuning of the mental health model using the MiniCPM 2.0 official fine-tuning documentation.
- Utilized Google Colab computational resources for training.
- Saved the model to Google Drive after training.
- Compared the performance of the original model with the fine-tuned model.

## Sensitive Word Detection
- Utilized Tencent's sensitive word database (completed).
- Applied the FlashText method to detect sensitive words (completed).
- Planned to implement detection for both user queries and model responses (pending).
- Planned to update the sensitive word database by adding or removing words as needed (pending).

## [Model Evaluation](./evaluation/README.md)

In the early version, due to the lack of output cleaning and ineffective context handling, the model’s BLEU-1 score was only around 8% in multi-turn dialogue scenarios.

By introducing system tags and preset prompts, and optimizing the model’s output structure, the base model’s BLEU-1 score improved to 20%. We also created a custom evaluation set tailored to psychological counseling scenarios, with concise and targeted responses maintained throughout the training data. Based on this setup, the fine-tuned model achieved up to a 140% improvement in BLEU performance compared to the initial version.

## [Model Chat](./model_chat/README.md)
This test is designed for manually comparing the performance of the original model (MiniCPM-4B) and the fine-tuned version. Based on the observed results, the fine-tuned model demonstrates better suitability for human-like conversation, showing improved contextual understanding and response relevance.
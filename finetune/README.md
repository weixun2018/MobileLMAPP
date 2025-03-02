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


## Automatic Evaluation
- Automatically evaluate generated responses using BLEU scores.
- Load the test dataset, generate responses using specified models, and evaluate the results.
- The evaluation results include scores between the original model and the fine-tuned model.
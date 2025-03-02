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

|Model|Training Data|Time|Average Score|BLEU-1|BLEU-2|BLEU-3|BLEU-4|Best Performer|
|---|---|---|---|---|---|---|---|---|
|**MiniCPM-2b**|Base|5'48"|0.052025|0.1473|0.0477|0.0139|0.0032||
|**MiniCPM-2b-5000**|5,000 samples|5'48"|0.0555|0.1556|0.0483|0.0141|0.0040|✅|
||||||||||
|**MiniCPM-2b**|Base|5'48"|0.0573|0.1607|0.0490|0.0152|0.0043|✅|
|**MiniCPM-2b-10000**|10,000 samples|5'48"|0.0564|0.1554|0.0486|0.0163|0.0053||
||||||||||
|**MiniCPM-4b**|Base|18'27"|0.05715|0.1421|0.0585|0.0216|0.0064|✅|
|**MiniCPM-4b-5000**|5,000 samples|18'27"|0.05405|0.1338|0.0548|0.0207|0.0069||
||||||||||
|**MiniCPM3-4B**|Base|12'54"|0.0628|0.1591|0.0622|0.0232|0.0067|✅|
|**MiniCPM-4b-10000**|10,000 samples|12'54"|0.0614|0.1524|0.0622|0.0234|0.0076||
||||||||||
|**Deepseek-8B**|Base|1'58"|0.055375|0.1582|0.0460|0.0125|0.0048||
|**Deepseek-8b-5000**|5,000 samples|1'58"|0.05715|0.1635|0.0484|0.0129|0.0038|✅|
||||||||||
|**Deepseek-8B**|Base|1'53"|0.062325|0.1765|0.0529|0.0147|0.0052|✅|
|**Deepseek-8b-10000**|10,000 samples|1'53"|0.05925|0.1677|0.0511|0.0134|0.0048||


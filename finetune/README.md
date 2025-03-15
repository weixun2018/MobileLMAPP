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


Using Multi-turn Dialogue Standard Test Set:

|Model|Training Data|Time|Average Score|BLEU-1|BLEU-2|BLEU-3|BLEU-4|Best Performer|
|---|---|---|---|---|---|---|---|---|
|**MiniCPM-2b**|Base|9'26"|0.0484|0.1021|0.0536|0.0265|0.0113||
|**MiniCPM-2b-5000**|5,000 samples|9'26"|0.0490|0.1047|0.0538|0.0263|0.0113|✅|
|**MiniCPM-2b-10000**|10,000 samples|9'26"|0.0487|0.1040|0.0537|0.0262|0.0109||
||||||||||
|**MiniCPM-4b**|Base|22'22"|0.0378|0.0768|0.0424|0.0220|0.0098||
|**MiniCPM-4b-5000**|5,000 samples|22'22"|0.0382|0.0763|0.0430|0.0227|0.0108|✅|
|**MiniCPM-4b-10000**|10,000 samples|22'22"|0.0377|0.0761|0.0426|0.0223|0.0099||
||||||||||
|**Deepseek-8B**|Base|4'28"|0.0582|0.1294|0.0630|0.0290|0.0115||
|**Deepseek-8b-5000**|5,000 samples|4'28"|0.0582|0.1296|0.0629|0.0288|0.0115||
|**Deepseek-8b-10000**|10,000 samples|4'28"|0.0584|0.1298|0.0632|0.0290|0.0116|✅|


Using Single-turn Dialogue Standard Test Set:

|Model|Training Data|Time|Average Score|BLEU-1|BLEU-2|BLEU-3|BLEU-4|Best Performer|
|---|---|---|---|---|---|---|---|---|
|**MiniCPM-2b**|Base|5'02"|0.0606|0.1629|0.0551|0.0167|0.0077||
|**MiniCPM-2b-5000**|5,000 samples|5'02"|0.0631|0.1687|0.0575|0.0181|0.0079|✅|
|**MiniCPM-2b-10000**|10,000 samples|5'02"|0.0618|0.1660|0.0558|0.0176|0.0078||
||||||||||
|**MiniCPM-4b**|Base|15'48"|0.0620|0.1613|0.0601|0.0187|0.0081|✅|
|**MiniCPM-4b-5000**|5,000 samples|15'48"|0.0590|0.1579|0.0561|0.0158|0.0063||
|**MiniCPM-4b-10000**|10,000 samples|15'48"|0.0617|0.1623|0.0589|0.0181|0.0073||
||||||||||
|**Deepseek-8b**|Base|4'35"|0.0631|0.1686|0.0592|0.0174|0.0074||
|**Deepseek-8b-5000**|5,000 samples|4'35"|0.0661|0.1782|0.0606|0.0179|0.0078|✅|
|**Deepseek-8b-10000**|10,000 samples|4'35"|0.0659|0.1759|0.0611|0.0185|0.0081||
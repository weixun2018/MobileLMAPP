# Evaluate basic general indicators.
### early_auto_eval
Initially, when evaluating the fine-tuning effects, the BLEU-1 score was low because the generated results were not cleaned (for example, removing user replies and system tags). Based on this, the BLEU score was still used to evaluate each model. The evaluation process includes: loading the test set, generating responses using the specified model, and automatically calculating the BLEU score to compare the performance differences between the original model and the fine-tuned model.

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

In the end, we chose MiniCPM-4B as the base model for this project and conducted multiple rounds of fine-tuning training on it to further improve the overall performance and response quality of the model.


### general_evaluation_emollm

#### Details of Improvement
The early way of carrying contextual information was:
```
历史记录：
求助者：xxx
支持者：xxx
求助者：xxx
支持者：
```
Change to:
```
<system> 你是一位专业的心理咨询师，我有一些心理问题，请你用专业的知识帮我解决。请只生成一轮回复，不要继续对话。
<user> xxx
<AI> xxx
<user> xxx
<AI> 
```
Due to the model sometimes containing users' question information, the initial BLEU-1 score of the model was only 8%. However, after introducing system labels, user labels, and prompt words, the BLEU-1 score increased to 14%. After further segmenting and cleaning the model output, the BLEU score improved from 14% to 20%.

| Model             | bleu-1  | bleu-2  | bleu-3  | bleu-4  | total_score | 总得分  |
|-------------------|---------|---------|---------|---------|-------------|---------|
| Early Version     | 0.0808  | 0.0273  | 0.0066  | 0.0024  | 0.0293      | 0.0293  |
| With Labels & Prompt Words | 0.1393  | 0.0457  | 0.0137  | 0.0035  | 0.0505      | 0.0505  |
| After Result Cleaning | 0.2068  | 0.0848  | 0.0340  | 0.0089  | 0.0836      | 0.0836  |

1. Early Version: 
- The initial version of the model without added labels and prompt words.
2. With Labels & Prompt Words: 
- Introduction of labels and prompt words improved the BLEU scores significantly.
3. After Result Cleaning: 
- Further cleaning of the model's output led to an additional improvement in BLEU scores.

#### Evaluate base and fine-tuned models using the EmoLLM method
The data sources for the fine-tuned versions are:
[Chinese-Psychological-QA-DataSet](https://github.com/flyrae/Chinese-Psychological-QA-DataSet) 

Use EmoLLM to evaluate the test set for automatic assessment:
| Model | BLEU-1 | BLEU-2 | BLEU-3 | BLEU-4 | ROUGE-1 | ROUGE-2 | ROUGE-L |
|---------------------|---------|---------|---------|---------|----------|----------|----------|
| Base Model | 30.85 | 14.10 | 6.85 | 3.46 | 28.85 | 8.01 | 16.14 |
| MiniCPM-4b-4975-A100| 32.42 | 14.65 | 7.12 | 3.59 | 29.27 | 8.03 | 16.57 |
| MiniCPM-4b-7000-A100| 31.24 | 14.24 | 7.00 | 3.55 | 28.55 | 8.04 | 16.43 |


### general_evaluation
We built our own evaluation set and support dynamic model loading, which is very efficient when testing multiple fine-tuned versions of the same base model and helps save GPU resources. Considering the need to simulate real psychological counseling dialogue scenarios, we adjusted the model to generate replies that are as concise as possible to reduce the reading burden on the help-seeker.

In this case, the evaluation results of the original model and the fine-tuned model are as follows:
The data sources for the fine-tuned versions are:
[Chinese-Psychological-QA-DataSet](https://github.com/flyrae/Chinese-Psychological-QA-DataSet) 

[EmoLLM_Datasets](https://github.com/SmartFlowAI/EmoLLM/blob/main/datasets/README.md)  
- single_turn_dataset_1.json
- single_turn_dataset_2.json

| Model                | BLEU-1 | BLEU-2 | BLEU-3 | BLEU-4 | ROUGE-1 | ROUGE-2 | ROUGE-L | Total Score |
| -------------------- | ------ | ------ | ------ | ------ | ------- | ------- | ------- | ----------- |
| Base Model           | 21.56  | 9.65   | 4.98   | 2.91   | 26.70   | 6.30    | 22.06   | 14.08       |
| MiniCPM-4b-3765-A100 | 30.08  | 14.85  | 8.28   | 5.27   | 33.40   | 9.53    | 27.68   | 19.10       |
| MiniCPM-4b-19k-A100  | 34.91  | 19.11  | 12.27  | 8.62   | 39.31   | 13.32   | 33.30   | 23.72       |
|                      |        |        |        |        |         |         |         |             |


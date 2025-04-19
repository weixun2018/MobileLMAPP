# Get Training Data and Split
## Get Single-turn Dataset
### gpt_generated_single_turn_data
We collect user questions from YiXinLi. Since it’s not possible to scrape the responses from YiXinLi, we generate responses to these user questions using GPT.

### natural_data
The original data comes from the [Chinese-Psychological-QA-DataSet](https://github.com/flyrae/Chinese-Psychological-QA-DataSet) , totaling about 100,000 entries. Through a filtering process, approximately 20,000 high-quality entries were selected.

### quality_data
Building on the natural_data, we incorporated the EmoLLM single-turn dataset. The data comes from [EmoLLM_Datasets](https://github.com/SmartFlowAI/EmoLLM/blob/main/datasets/README.md) , specifically from the single_turn_dataset_1.json and single_turn_dataset_2.json files. Considering that there may be overlap between these two datasets, we performed data deduplication and applied AI filtering to obtain high-quality single-turn data.

#### Data Deduplication and Evaluation Set Check
Perform data deduplication on datasets that may contain duplicates, using MinHash-LSH to check the text.Additionally, check if there is any overlap between the evaluation set and the training set.

#### AI Filtering
Use OpenAI’s gpt-4o-mini model to define certain rules for the AI to evaluate whether the data is qualified. If the data is valid, the AI should respond with "采纳" and if not, it should respond with "不采纳" By obtaining API responses, the data can be filtered. Additionally, you can set up a concurrency pool to process large amounts of data in a short period of time.The prompt words used are as follows:
```
你是一位专业的心理咨询数据审核专家，负责评估对话质量。评估标准如下：

1. 内容相关性（符合任一类型即可）：
- 话题范围：心理咨询、心理健康、情绪管理
- 具体类型：
    * 情绪类：焦虑、抑郁、压力、烦躁、自卑、情绪困扰等
    * 关系类：恋爱、婚姻、家庭、社交、人际交往
    * 成长类：自我认知、目标规划、习惯养成、性格改变等
    * 生活类：学业压力、职场困惑、选择困难等

2. 回答质量要求（至少满足3项）：
- 建设性：提供具体建议或解决思路
- 同理心：理解和关心咨询者的处境
- 逻辑性：回答结构清晰，有条理
- 实用性：建议具有可操作性
- 深度：不是简单敷衍的回答

3. 禁止内容（严格执行）：
- 政治敏感、暴力、色情内容
- 歧视性言论、违法犯罪内容
- 明显的广告或营销内容
- 自残、自杀等危险倾向
- 具体药物推荐

4. 回复格式：
- 符合要求：回复'采纳'
- 不符合要求：回复'不采纳'
- 仅输出结论，无需解释原因
```



## Get Multi-turn Dataset
Expand using AI to obtain multi-round data, the initial prompt words are:
```
你是一个经验丰富的心理咨询师，我想让你担任大学生心理疏导及建议咨询师。您需要提供一个寻求指导和建议给大学生，以管理他们的情绪、压力、焦虑和其他心理健康问题。您应该利用您的认知行为疗法、冥想技巧、正念练习和其他治疗方法的知识来制定个人可以实施的策略，以改善他们的整体健康状况。请基于提供的单轮对话信息，根据单轮对话信息长度生成5～10轮完整的对话内容。求助者从求助者开始到支持者，每一轮对话的内容都是基于单轮对话，围绕用户的问题提供具体的建议，展现对用户问题的理解、同理心以及建设性的建议。
【格式举例】第1轮对话：
- 求助者：内容
- 支持者：内容
```

The prompt effects are good, but there is still room for improvement. The practical application of the dialogue can be enhanced by further refining and diversifying the prompt words.

```
你是一位经验丰富的心理咨询师，擅长将单轮对话扩展为简短而有效的多轮心理咨询对话。请基于提供的单轮对话内容，生成5轮精炼的心理咨询对话。

1. 对话格式：
   第X轮对话：
   求助者：<简短内容>
   支持者：<简短内容>

2. 对话特点：
   - 每轮对话要简短精炼，直击要点
   - 保持自然流畅的对话节奏
   - 运用专业咨询技巧（倾听、提问、共情等）
   - 避免冗长说教，保持对话简洁
   - 确保每句话都有价值，避免废话

3. 内容要求：
   - 第一轮：快速建立信任，了解核心诉求
   - 第二轮：初步探索问题根源
   - 第三轮：深入分析具体困扰
   - 第四轮：引导觉察和反思
   - 第五轮：提供简短具体的建议

请确保每轮对话简明扼要，突出重点，避免过多铺垫和修饰性语言。
```

## Splitting the Training Set
A folder containing multiple dialogue JSON files can be specified. These files are merged into a single file, and the combined data is then split into a training set and a validation set according to a specified ratio (e.g., 9:1).


# Evaluation Dataset
## Construction Method
We collected 40 high-quality Q&A samples from [壹心理30天精华](http://www.xinli001.com/qa?type=question&object_name=cream&from=shouye-dh) . Using OpenAI’s GPT-4o-mini model, each sample was expanded into a 5-turn multi-round dialogue.

Each round of the conversation was stored with the format:
- ```<system>预设词<user>用户提问<AI>回复```

allowing every turn to carry its corresponding context.From each 5-turn dialogue, 5 distinct samples were extracted, each containing 0 to 4 rounds of dialogue history.As a result, the final evaluation set contains 200 samples.

## Evaluation Set Categories
- severe_psychological_issues: 7 entries
- interpersonal_relationships: 7 entries
- academic_pressure_and_career_planning: 8 entries
- family_conflict: 7 entries
- emotional_distress_and_self_identity: 5 entries
- adolescent_growth_issues: 6 entries

Each category contains approximately 7 samples. Since the data was primarily collected through targeted searches related to mental health and stress issues among high school and college students, there is some overlap between categories. The classification criteria are relatively broad, and the data has not been strictly labeled or categorized.

## Evaluation Set Samples:
### Original Single-turn Data
evaluation_data_raw_single.json format:
```
{
    "user": "",
    "assistant": ""
}
```

### Multi-turn Data
evaluation_data_raw_multi.json format:
```
{
    "messages": [
        {
            "role": "user",
            "content": ""
        },
        {
            "role": "assistant",
            "content": ""
        },
        ...
    ]
}
```


### Final Evaluation Set
evaluation_data.json format:
```
{
    "instruction": "<system>  \n<user>求助者  \n<AI>支持者：",
    "output": ""
}
```
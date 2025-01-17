## Get Dataset 
### First Method
Crawl question data, use GPT-4o-mini to generate single-turn dialogue data, then use GPT to expand dialogue turns

Data Sources:
- Links containing questions
- Question data
- Single-turn dialogue data  
- Multi-turn dialogue data (over 4000+ entries)

### Second Method
Filter single-turn dialogue data, then use GPT-4o-mini to expand dialogue turns

Data Sources:
- Complete single-turn dialogue data
- Filter required single-turn dialogue data through keyword search
- Multi-turn dialogue data (over 5000+ entries)


## Processing Method:
Multi-turn dialogues are generated using OpenAI's gpt-4o-mini model, with each prompt token around 400-600, response token around 900-1200, generation time around 10-20 seconds, generated through the following prompt:

"你是一个经验丰富的心理咨询师，我想让你担任大学生心理疏导及建议咨询师。您需要提供一个寻求指导和建议给大学生，以管理他们的情绪、压力、焦虑和其他心理健康问题。您应该利用您的认知行为疗法、冥想技巧、正念练习和其他治疗方法的知识来制定个人可以实施的策略，以改善他们的整体健康状况。请基于提供的单轮对话信息，根据单轮对话信息长度生成5～10轮完整的对话内容。求助者从求助者开始到支持者，每一轮对话的内容都是基于单轮对话，围绕用户的问题提供具体的建议，展现对用户问题的理解、同理心以及建设性的建议。【格式举例】第1轮对话：\n求助者：内容\n支持者：内容\n\n"

Generate a stable format, display it in txt format, then convert it to json format, and finally save it locally, where each piece of data is a json file, and the filename can be used to index the corresponding single-round dialogue data.

from transformers import AutoTokenizer, AutoModelForCausalLM
from peft import PeftModel
import torch
import os

def load_model(model_path, device='cuda', force_auto_device_map=False):
    # Set a more detailed device mapping strategy
    if force_auto_device_map:
        device_map = "auto"
    else:
        device_map = {"": device}

    # Load the base model
    base_model = AutoModelForCausalLM.from_pretrained(
        model_path,
        torch_dtype=torch.float16,
        device_map=device_map,
        trust_remote_code=True,
        offload_folder="offload"
    )

    return base_model

def format_dialogue(user_input):
    """Format the dialogue input"""
    return f"Human: {user_input}\n\nAssistant:"

def chat(text, 
         models={},
         tokenizer=None,
         max_length=512,
         temperature=0.7,
         top_p=0.9,
         top_k=50,
         num_beams=4,
         repetition_penalty=1.2):
    
    if tokenizer is None:
        raise ValueError("Tokenizer must be provided")
        
    # Format input as dialogue
    formatted_input = format_dialogue(text)
    inputs = tokenizer(formatted_input, return_tensors="pt").to("cuda")

    # Generation parameters
    gen_kwargs = {
        "max_length": max_length,
        "temperature": temperature,
        "top_p": top_p,
        "top_k": top_k,
        "num_beams": num_beams,
        "repetition_penalty": repetition_penalty,
        "pad_token_id": tokenizer.eos_token_id,
        "do_sample": True,
        "no_repeat_ngram_size": 3,
        "early_stopping": True
    }

    responses = []
    # Generate response for each model
    for model_name, model in models.items():
        outputs = model.generate(**inputs, **gen_kwargs)
        response = tokenizer.decode(outputs[0], skip_special_tokens=True)
        try:
            response = response.split("Assistant:")[-1].strip()
        except:
            response = response.strip()
        responses.append(f"<{model_name}回复>：{response}")

    return "\n".join(responses)

def main():
    # Initialize models and tokenizer
    model_path = "/content/MiniCPM-2B-sft-bf16"
    tokenizer = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True)
    
    base_model = load_model(model_path)

    # Load the LoRA model
    peft_model = PeftModel.from_pretrained(
        base_model,
        "/content/drive/MyDrive/TestModel/checkpoint-1000",
        is_trainable=False
    )

    models = {
        "原始模型": base_model,
        "微调模型": peft_model
    }

    # Test single query
    query = "上大学后就感觉一切变得很不熟悉，连自己都变得不像自己了,我应该怎么办？"
    print(f"问题: {query}")
    print(chat(query, models={"原始模型": base_model}, tokenizer=tokenizer))
    print(chat(query, models={"微调模型": peft_model}, tokenizer=tokenizer))

    # print(chat(query, models=models))
    print("-----------------------\n")

    # Test multiple queries
    queries = [
        "我担心毕业后找不到理想的工作，该如何应对这种不确定性？",
        "面对激烈的就业竞争，我总是感到自己不够优秀，怎么才能调整心态？",
        "总是这样会不自觉的紧张进而影响到学习生活，该怎么办",
        "不想看书怎么办？又要到期末考试了，明明知道成绩很重要，可以让自己毕业找到更好的工作，有更高的薪资待遇，可是还是不想看怎么办？",
        "我是双非二本的大二师范生，最近期末考试压力太大，背的东西太多，鬼使神差的打了小抄被老师发现，大概率有处分了，听说处分解除了对考编考公考研都有影响，心一下凉了，后悔莫及，真的认识到错误了，感觉对不起父母，焦虑一整天了，后面还要考试，真的很怕会被处分，辅导员说要等学院的通知，如果被处分了我真不知道该怎么办了，我是师范生，当初报这个学校就是想选师范，感觉现在一切都毁了，感觉未来无望了，也没敢和父母说，感觉很对不起他们……",
        "其实我也知道没有什么大不了的，但是我就很害怕别人失望，害怕自己不够优秀，特别害怕自己是没有别人想象中那么优秀。",
        "我是一个大学生，家庭情况也不好，就在昨天乘火车上学时被人偷走身上仅有的300元，昨天一个人提着行李箱游走在大街上越走越麻烦，我无助地给妈妈打了个电话，我并没有告诉她我丢钱的事，就是说特别无聊就想家了，当时自己真的万分地想家，特别急切得想要回到家里，爸爸和妈妈都开导了我一番，当我听到家人的声音竟忍不住哭了起来，真的，这么多年，我从来不哭的，但就是在最无助的时候才知道家人的重要，我实在太想回家了，现在心里特别堵得慌，希望能有人帮我疏导疏导。",
        "我在大学是学生会心理健康服务部的，简称心理部，我们系在每年的5月和10月都有活动，要求表演关于大学生心理健康的话剧，因此要求每个成员都查找资料，写剧本，但因为我性格比较外向，平时也没有什么烦心事，所以特别想来了解一下大家在大学或者是上学的时候有什么事情让你难过过，更甚至出现过心理问题，希望大家帮忙想一下。",
        "我在一个地方当学徒，老板看我不会就老说还大学生，我听后就感觉老是讽刺我，他说有什么不会的就问他，结果我问了后，他又说怎么这么简单的问题都不会之类的，这里不是问谁对谁错。我想问为什么听到那些话后心里很愤怒，有时自己都控制不了生气，知道这是正常的，可心里就觉得他看我不顺眼，很愤怒。",
        "不知道如何表达情绪怎么办？想过去咨询心理医生，但是不知道要说什么。所有情绪堵在胸口，并且大脑也很乱，无数件事搅在一起理不出头绪。特别想向人倾诉，但一旦有人问起却只会说“不知道”“没什么”这一类的话。而且我只是即将毕业的大学生，没有钱去看高级的医生。请问这样的我还有救么？",
        "本人大学生，6岁那年父母离异，我随母亲生活，离异后父亲患精神分裂症，现在已经基本痊愈。这么多年来，由于地域关系，我们见面机会甚少。每次见面他总是提过去的事。说实话，多少有点让我难受。已经和他分开生活15年，难免生疏。只是想问问该如何和他很好的交流，希望得到好的建议。",
        "我是一个正在考研的三流二本大学生，平时都会给同学一种比较成熟的形象，每次给父母打电话和女朋友在一起都看的很开，但是一到我一个人的时候总觉得特别压抑，耐性也变得非常差，集中精神复习不超过30分钟，一遇到不会的东西就变得很暴躁，想撕卷子喘粗气，这两天天天头疼，有什么办法可以改变现状吗？",
        "我明明才刚刚进入这个大学，我不想回去，即使我家离学校很近，我好像没有恋家情绪，我是不是不正常啊？",
        "刚进入大学，现在属于躺不平，卷不起来的状态，感觉周边同学非常努力，尝试和他们的状态相似会感到非常烦躁和厌倦，而按照自己的生活方式，又会因为周围人而感到不安和焦虑，不知道应该怎么办。",
        "作为家长，对于在外地读大一的孩子，怎样提示她不要过度玩手机，孩子才不会厌烦？高中时，虽然学习紧张，但是只要放假我并没有完全限制孩子玩手机，高考后，三个月，手机每时每刻都不离手，我有时想，会不会当初她要是不玩手机，会不会考得更好！现在，我有点担心，手机天天在身上，自律什么的怎么办？有点焦虑，怕孩子对于学习不自觉！",
        "本人大一，在一个普通的一本学校，想问一问各位你们会不会没有学习的时候很想学习，可是到了真正学习的时候又静不下心来学习了，我好纠结，而且我这里我一提到学习就被孤立，根本不敢和室友提学习，一个人去图书馆就会被一群人孤立的那种感觉，我该怎么办？？？请您帮我想想办法吧。",
        "我是一个复读的美术生，在复读这一年我仍然没考上国内公认的顶尖美院，但我身边的人很多都考上了，客观来说我平时成绩比他们好，但考试的那道题由于客观因素和一点心理问题做的不好，只有全国三百名，只要前200名，录取不到我，因此我也得不到其他人的认可。别人只认可清华央美，证明不了我比他们强，我该怎么办？",
        "我总是很自卑。从小到大总是名列前茅，但总觉得自己不够完美，即使年级第一也不满足，自责、仰视别人，觉得自己靠天赋，而他人靠天赋超过我。考上清华也觉得是遮羞布，很自卑，怎么办？",
        "大学生，室友之前有矛盾，导致我现在一想到回寝室就害怕。心里很恐惧，在宿舍不敢大声笑大声说话。生怕一个很嚣张跋扈的女生找我茬，我觉得我在寝室一点一点都不自如。怎么办？",
        "将要步入大学，心里却莫名发慌，总是发脾气，担心大学生活，担心与同学的交往，总想考虑好所有问题，晚上经常失眠，心平静不下来，每天不知道要干什么，总是担心一些事。",
        "我是个准大学生，暑假变得非常懒，有几次想看几本书，可半天就放弃了，该怎么办啊？",
        "今年9月，我即将成为一名大学生。但面对新的同学、老师、生活环境，心中既憧憬又焦虑不安。",
        "目前我是一个即将毕业的大学生。而目前我和室友以及班上同学关系不太好。虽然认识人比较多，但不知道维持这种关系，渐渐的，好像我把他们也孤立了，目前始终一个人生活。我该怎么办？",
        "18岁大一男生，名校，高三努力一年考上好大学，取得回报，可却没有了目标和动力，很迷茫。请问我该如何改变这种状态？",
        "本人刚毕业，前几天去做一个项目。由于没有经验，做错了很多事情，带我们做项目的老师对我发脾气，我害怕自己一错再错。现在看到文档就逃避，该怎么办？",
        "目前是一个北方某财经院校的大二学生，大二闲下来后感觉空荡荡的，考了证书也没有动力，感情生活也不顺。总是紧张焦虑，又不知道自己要奋斗什么。怎么办？",
        "做一件自己在乎的事情前，异常焦虑，会拖延。害怕失败，总想着推迟开始。焦虑严重影响生活，习惯性逃避。该怎么办？",
        "我大一新生，来大学已经差不多一个学期了，来的时候认识了两个人，刚开始很好很好，但到后来，我觉得她们两个很好了，我被忽略了，我就有点难过，我就远离她们了不和她们一起走了，但是不走了就不走了啊，她们却来伤害我还发说说说我什么什么的，我觉得这让我很难过，也严重的影响到了我的生活，我不知道她们为什么要这么做，因为没有理由啊，我并没有做什么啊，我只是不和她们一起走而已为什么会这样，我该怎么办？帮帮我，我很痛苦",
        "性格内向，不善于表达，想交往又害怕和别人交往，话很少，和熟人也说不了几句话更不用说陌生人，有时候和别人单独相处只是默默的在一起不说话，因为不知道说什么，很是尴尬",
        "才上大学没两天，就觉得学习很枯燥了，我是学法学的，本来是怀着满腔热情来学校，却觉得很多事情背离了初衷。比如，大家说加社团好，可是我很不喜欢社团里的很多另类过激的玩法，我想学习，却不知道怎么开始。老师上课几乎是照书读，而且课程很少，每天一大把时间我都不知道干什么！自学吧，不懂。考四级吧，没效率，每天都很迷茫，即使是在学，也很难静下心来，怎么办才好？",
        "我是今年的考生，我在班上是个中等偏上成绩的学生，可是高考后在我后面的，从未考过我的人，几乎都考过我了，我觉得我没有发挥失误，可是分就是那么低，我不甘心，凭什么，都甩我二十多分，读着我报不了的学校，真心好难受？"
    ]
    
    results = []
    for i in range(len(queries)):
        print(f"问题{i+1}: {queries[i]}")
        print(chat(queries[i], models=models, tokenizer=tokenizer))
        print("-----------------------\n")
        result = f"问题{i+1}: {queries[i]}\n" + chat(queries[i], models=models, tokenizer=tokenizer)
        results.append(result)
    
    with open('model_comparison.txt', 'w', encoding='utf-8') as f:
        f.writelines(results)


if __name__ =="__main__":
    main()
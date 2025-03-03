memory_test_cases = [
    {
        "name": "基础信息记忆测试",
        "context_chain": [
            # 设置基础信息
            {
                "input": "我叫张三，今年28岁，是一名数据科学家，在字节跳动工作，月薪5万，住在上海浦东",
                "expected_response": "好的，已了解你的基本信息",
            },
            {
                "input": "我的爱好是打篮球和弹吉他，有一只叫小白的猫，毕业于清华大学",
                "expected_response": "好的，已了解你的个人信息",
            },
            # 10轮基础信息测试
            {
                "input": "我叫什么名字？",
                "expected_answer": "张三",
                "keywords": ["张三"],
                "memory_turns": 2,
            },
            {
                "input": "我多大年纪？",
                "expected_answer": "28岁",
                "keywords": ["28"],
                "memory_turns": 2,
            },
            {
                "input": "我的职业是什么？",
                "expected_answer": "数据科学家",
                "keywords": ["数据科学家"],
                "memory_turns": 2,
            },
            {
                "input": "我在哪里工作？",
                "expected_answer": "字节跳动",
                "keywords": ["字节跳动"],
                "memory_turns": 2,
            },
            {
                "input": "我的月薪是多少？",
                "expected_answer": "5万",
                "keywords": ["5万"],
                "memory_turns": 2,
            },
            {
                "input": "我住在哪里？",
                "expected_answer": "上海浦东",
                "keywords": ["上海", "浦东"],
                "memory_turns": 2,
            },
            {
                "input": "我有什么爱好？",
                "expected_answer": "打篮球和弹吉他",
                "keywords": ["篮球", "吉他"],
                "memory_turns": 2,
            },
            {
                "input": "我养了什么宠物？",
                "expected_answer": "一只叫小白的猫",
                "keywords": ["小白", "猫"],
                "memory_turns": 2,
            },
            {
                "input": "我是哪个学校毕业的？",
                "expected_answer": "清华大学",
                "keywords": ["清华"],
                "memory_turns": 2,
            },
            {
                "input": "我的基本情况是什么？",
                "expected_answer": "28岁的张三，是一名数据科学家，在字节跳动工作",
                "keywords": ["张三", "28", "数据科学家", "字节跳动"],
                "memory_turns": 2,
            },
        ],
    },
    {
        "name": "时间序列记忆测试",
        "context_chain": [
            # 设置时间安排
            {
                "input": "周一的安排：上午9点产品会议，下午2点见客户，晚上7点团建",
                "expected_response": "好的，已记录周一的安排",
            },
            {
                "input": "周二的安排：上午10点述职，下午3点培训，晚上6点部门会",
                "expected_response": "好的，已记录周二的安排",
            },
            {
                "input": "周三的安排：上午11点面试，下午4点评审会，晚上8点加班",
                "expected_response": "好的，已记录周三的安排",
            },
            # 10轮时间安排测试
            {
                "input": "周一上午几点开会？是什么会议？",
                "expected_answer": "周一上午9点产品会议",
                "keywords": ["9点", "产品"],
                "memory_turns": 3,
            },
            {
                "input": "周一下午什么安排？",
                "expected_answer": "下午2点见客户",
                "keywords": ["2点", "客户"],
                "memory_turns": 3,
            },
            {
                "input": "周一晚上什么安排？具体时间？",
                "expected_answer": "晚上7点团建",
                "keywords": ["7点", "团建"],
                "memory_turns": 3,
            },
            {
                "input": "周二上午是什么安排？具体时间？",
                "expected_answer": "上午10点述职",
                "keywords": ["10点", "述职"],
                "memory_turns": 3,
            },
            {
                "input": "周二下午具体什么安排？具体时间？",
                "expected_answer": "下午3点培训",
                "keywords": ["3点", "培训"],
                "memory_turns": 3,
            },
            {
                "input": "周二晚上具体什么安排？具体时间？",
                "expected_answer": "晚上6点部门会",
                "keywords": ["6点", "部门会"],
                "memory_turns": 3,
            },
            {
                "input": "周三上午具体什么安排？具体时间？",
                "expected_answer": "上午11点面试",
                "keywords": ["11点", "面试"],
                "memory_turns": 3,
            },
            {
                "input": "周三下午具体什么安排？具体时间？",
                "expected_answer": "下午4点评审会",
                "keywords": ["4点", "评审"],
                "memory_turns": 3,
            },
            {
                "input": "周三晚上具体什么安排？具体时间？",
                "expected_answer": "晚上8点加班",
                "keywords": ["8点", "加班"],
                "memory_turns": 3,
            },
            {
                "input": "周一全天的安排是什么？具体时间？",
                "expected_answer": "上午9点产品会议，下午2点见客户，晚上7点团建",
                "keywords": ["9点", "2点", "7点", "产品", "客户", "团建"],
                "memory_turns": 3,
            },
        ],
    },
    {
        "name": "项目信息记忆测试",
        "context_chain": [
            # 设置项目信息
            {
                "input": "项目基本信息：AI助手项目，预算500万，15人团队，使用Python和TensorFlow开发，预计6个月完成",
                "expected_response": "好的，已了解项目基本信息",
            },
            {
                "input": "团队构成：3个前端(React)，5个后端(Python)，4个算法(TensorFlow)，2个测试，1个产品",
                "expected_response": "好的，已了解团队构成",
            },
            {
                "input": "项目进度：需求分析完成90%，架构设计完成80%，前端完成60%，后端完成45%，算法完成30%，整体完成50%",
                "expected_response": "好的，已了解项目进度",
            },
            # 10轮项目信息测试
            {
                "input": "这是什么项目？",
                "expected_answer": "AI助手项目",
                "keywords": ["AI", "助手"],
                "memory_turns": 3,
            },
            {
                "input": "项目预算多少？",
                "expected_answer": "500万",
                "keywords": ["500万"],
                "memory_turns": 3,
            },
            {
                "input": "团队有多少人？",
                "expected_answer": "15人团队",
                "keywords": ["15"],
                "memory_turns": 3,
            },
            {
                "input": "使用什么技术栈？",
                "expected_answer": "使用Python和TensorFlow开发",
                "keywords": ["Python", "TensorFlow"],
                "memory_turns": 3,
            },
            {
                "input": "项目预计多久完成？",
                "expected_answer": "预计6个月完成",
                "keywords": ["6个月"],
                "memory_turns": 3,
            },
            {
                "input": "前端团队情况如何？",
                "expected_answer": "3个前端，使用React",
                "keywords": ["3", "前端", "React"],
                "memory_turns": 3,
            },
            {
                "input": "后端和算法团队各有多少人？",
                "expected_answer": "5个后端，4个算法",
                "keywords": ["5", "后端", "4", "算法"],
                "memory_turns": 3,
            },
            {
                "input": "需求分析和架构设计的进度？",
                "expected_answer": "需求分析完成90%，架构设计完成80%",
                "keywords": ["90%", "80%"],
                "memory_turns": 3,
            },
            {
                "input": "目前开发进度如何？",
                "expected_answer": "前端完成60%，后端完成45%，算法完成30%",
                "keywords": ["60%", "45%", "30%"],
                "memory_turns": 3,
            },
            {
                "input": "整体项目完成度是多少？",
                "expected_answer": "整体完成50%",
                "keywords": ["50%"],
                "memory_turns": 3,
            },
        ],
    },
    {
        "name": "人物关系记忆测试",
        "context_chain": [
            # 设置人物关系
            {
                "input": "团队成员：张三是项目负责人，李四负责前端，王五负责后端，赵六负责算法",
                "expected_response": "好的，已了解团队分工",
            },
            {
                "input": "工作职责：张三负责项目管理，李四负责用户界面，王五负责接口开发，赵六负责模型训练",
                "expected_response": "好的，已了解工作职责",
            },
            {
                "input": "项目进展：李四完成了登录界面，王五完成了用户接口，赵六优化了模型性能",
                "expected_response": "好的，已了解项目进展",
            },
            # 10轮人物关系测试
            {
                "input": "谁是项目负责人？",
                "expected_answer": "张三是项目负责人",
                "keywords": ["张三", "负责人"],
                "memory_turns": 3,
            },
            {
                "input": "李四负责什么工作？",
                "expected_answer": "李四负责前端和用户界面",
                "keywords": ["李四", "前端", "界面"],
                "memory_turns": 3,
            },
            {
                "input": "后端开发是谁负责的？",
                "expected_answer": "王五负责后端",
                "keywords": ["王五", "后端"],
                "memory_turns": 3,
            },
            {
                "input": "赵六在团队中是什么角色？",
                "expected_answer": "赵六负责算法和模型训练",
                "keywords": ["赵六", "算法", "模型"],
                "memory_turns": 3,
            },
            {
                "input": "张三具体负责什么？",
                "expected_answer": "张三负责项目管理",
                "keywords": ["张三", "项目管理"],
                "memory_turns": 3,
            },
            {
                "input": "前端开发的进展如何？",
                "expected_answer": "李四完成了登录界面",
                "keywords": ["李四", "登录界面"],
                "memory_turns": 3,
            },
            {
                "input": "王五完成了什么工作？",
                "expected_answer": "王五完成了用户接口",
                "keywords": ["王五", "用户接口"],
                "memory_turns": 3,
            },
            {
                "input": "赵六在项目中做了什么？",
                "expected_answer": "赵六优化了模型性能",
                "keywords": ["赵六", "模型", "性能"],
                "memory_turns": 3,
            },
            {
                "input": "谁负责用户界面开发？",
                "expected_answer": "李四负责用户界面",
                "keywords": ["李四", "界面"],
                "memory_turns": 3,
            },
            {
                "input": "接口开发是谁负责的？",
                "expected_answer": "王五负责接口开发",
                "keywords": ["王五", "接口"],
                "memory_turns": 3,
            },
        ],
    },
    {
        "name": "复杂推理记忆测试",
        "context_chain": [
            {
                "input": "我每月收入5万，房租8000，日常开销1.5万，理财支出1万，每周打车上下班花费500元",
                "expected_response": "好的，已了解您的收支情况",
            },
            {
                "input": "我每天早上8点上班，晚上7点下班，单程通勤时间1小时",
                "expected_response": "好的，已了解您的作息情况",
            },
            # 综合推理测试
            {
                "input": "根据我的收入和支出情况，我每月能存多少钱？",
                "expected_answer": "每月收入5万，固定支出3.3万（房租8000+日常开销1.5万+理财1万），加上打车费约2000（500元×4周），每月可存约1.5万",
                "keywords": ["5万", "3.3万", "1.5万"],
                "memory_turns": 2,
                "reasoning_required": True
            },
            {
                "input": "我每天花在通勤上的总时间是多少？",
                "expected_answer": "每天往返通勤共需2小时（单程1小时×2）",
                "keywords": ["2小时", "通勤"],
                "memory_turns": 2,
                "reasoning_required": True
            },
            # 补充复杂推理记忆测试
            {
                "input": "如果我每天少打车一次，改坐地铁，每次能节省40元，一年能节省多少钱？",
                "expected_answer": "每天节省40元，一年可节省约14600元（40元×365天）",
                "keywords": ["40", "14600"],
                "memory_turns": 2,
                "reasoning_required": True
            },
            {
                "input": "如果我的工资涨20%，每月额外支出增加5000，对我的月储蓄有什么影响？",
                "expected_answer": "原工资5万，涨20%后为6万，月储蓄从1.5万增加到2万（增加工资1万-增加支出5000）",
                "keywords": ["20%", "6万", "2万"],
                "memory_turns": 2,
                "reasoning_required": True
            },
            {
                "input": "按目前的开销，我的年收入中有多少比例用于房租？",
                "expected_answer": "月收入5万，年收入60万，年房租9.6万（8000×12），占年收入的16%",
                "keywords": ["60万", "9.6万", "16%"],
                "memory_turns": 2,
                "reasoning_required": True,
                "percentage_calculation": True
            }
        ]
    },
    {
        "name": "长期记忆衰减测试",
        "context_chain": [
            {
                "input": "去年我在A公司工作，年薪30万，今年跳槽到B公司，年薪50万",
                "expected_response": "好的，已了解您的职业变动情况",
            },
            {
                "input": "去年我开一辆大众车，今年换了一辆特斯拉",
                "expected_response": "好的，已了解您的用车变化",
            },
            # 插入10轮其他对话
            {
                "input": "讨论天气",
                "expected_response": "今天天气不错",
                "is_distraction": True
            },
            # 重复10次不相关对话
            {
                "input": "一年前我开什么车？现在呢？",
                "expected_answer": "一年前开大众，现在开特斯拉",
                "keywords": ["大众", "特斯拉"],
                "memory_turns": 12,
                "long_term_memory": True
            },
            # 补充长期记忆衰减测试
            {
                "input": "三年前我的月薪是多少？两年前呢？去年呢？",
                "expected_answer": "三年前2.5万，两年前3.5万，去年年薪30万（月薪2.5万）",
                "keywords": ["2.5万", "3.5万", "30万"],
                "memory_turns": 15,
                "long_term_memory": True
            },
            {
                "input": "我的职业发展经历是怎样的？",
                "expected_answer": "从程序员做起，两年后升为高级工程师，现在是数据科学家",
                "keywords": ["程序员", "高级工程师", "数据科学家"],
                "memory_turns": 15,
                "long_term_memory": True
            }
        ]
    },
    {
        "name": "情感记忆测试",
        "context_chain": [
            {
                "input": "我特别喜欢吃川菜，但是不能吃太辣",
                "expected_response": "好的，已了解您的饮食偏好",
            },
            {
                "input": "我害怕坐飞机，但是经常需要出差",
                "expected_response": "好的，已了解您的出行情况",
            },
            {
                "input": "我最讨厌开长会，特别是下午的会议",
                "expected_response": "好的，已了解您的工作偏好",
            },
            # 情感相关测试
            {
                "input": "我对什么食物有特别的偏好？",
                "expected_answer": "您特别喜欢川菜，但不能吃太辣",
                "keywords": ["川菜", "辣"],
                "memory_turns": 3,
                "emotion_related": True
            },
            # 补充情感记忆测试
            {
                "input": "我最喜欢的运动是什么？最讨厌的运动是什么？",
                "expected_answer": "最喜欢打篮球，最讨厌跑步",
                "keywords": ["篮球", "跑步"],
                "memory_turns": 3,
                "emotion_related": True
            },
            {
                "input": "我对加班的态度是怎样的？",
                "expected_answer": "不喜欢加班，但如果项目需要会接受",
                "keywords": ["加班", "接受"],
                "memory_turns": 3,
                "emotion_related": True
            }
        ]
    },
    {
        "name": "矛盾信息处理测试",
        "context_chain": [
            {
                "input": "我的预算是100万",
                "expected_response": "好的，已了解您的预算情况",
            },
            {
                "input": "我觉得预算可能不够，应该是150万",
                "expected_response": "好的，已更新预算信息",
            },
            {
                "input": "经过仔细计算，预算定为120万",
                "expected_response": "好的，已确认最终预算",
            },
            # 矛盾信息测试
            {
                "input": "我的预算经历了哪些变化？",
                "expected_answer": "预算从最初的100万，调整到150万，最终确定为120万",
                "keywords": ["100万", "150万", "120万"],
                "memory_turns": 3,
                "contradiction_handling": True
            }
        ]
    },
    {
        "name": "多语言混合测试",
        "context_chain": [
            {
                "input": "我的English level是advanced，日本語水平是N2",
                "expected_response": "好的，已了解您的语言水平",
            },
            {
                "input": "I can write Python code很好，also懂得日本語programming",
                "expected_response": "好的，已了解您的编程语言能力",
            },
            # 多语言测试
            {
                "input": "请描述我的语言能力",
                "expected_answer": "您的英语水平是advanced，日语水平是N2，能用Python编程",
                "keywords": ["advanced", "N2", "Python"],
                "memory_turns": 2,
                "multilingual": True
            }
        ]
    },
    {
        "name": "上下文切换测试",
        "context_chain": [
            {
                "input": "工作上下文：正在开发AI项目",
                "expected_response": "好的，已切换到工作上下文",
            },
            {
                "input": "生活上下文：正在规划周末旅行",
                "expected_response": "好的，已切换到生活上下文",
            },
            {
                "input": "工作上下文：继续讨论项目进度",
                "expected_response": "好的，已切换回工作上下文",
            },
            # 上下文切换测试
            {
                "input": "我现在处于什么上下文？之前在做什么？",
                "expected_answer": "现在处于工作上下文，之前在规划周末旅行",
                "keywords": ["工作", "旅行"],
                "memory_turns": 3,
                "context_switching": True
            }
        ]
    },
    # 新增百分比计算测试
    {
        "name": "百分比计算测试",
        "context_chain": [
            {
                "input": "项目总投资1000万，其中技术投入500万，市场投入300万，运营投入200万",
                "expected_response": "好的，已了解投资分配情况",
            },
            {
                "input": "技术投入占总投资的比例是多少？",
                "expected_answer": "技术投入500万占总投资1000万的50%",
                "keywords": ["50%"],
                "memory_turns": 2,
                "percentage_calculation": True
            },
            {
                "input": "市场和运营投入分别占多少比例？",
                "expected_answer": "市场投入占30%，运营投入占20%",
                "keywords": ["30%", "20%"],
                "memory_turns": 2,
                "percentage_calculation": True
            }
        ]
    },
    # 新增时间计算测试
    {
        "name": "时间计算测试",
        "context_chain": [
            {
                "input": "项目开始时间2023年1月1日，预计持续18个月，中间延期2个月",
                "expected_response": "好的，已了解项目时间安排",
            },
            {
                "input": "项目最终预计什么时候完成？",
                "expected_answer": "2024年9月1日（原计划20个月：2024年7月1日，加上延期2个月）",
                "keywords": ["2024年9月"],
                "memory_turns": 2,
                "time_calculation": True
            }
        ]
    }
]
